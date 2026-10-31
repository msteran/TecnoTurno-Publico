package co.edu.uts.tecnoturno.service;

import co.edu.uts.tecnoturno.model.*;
import co.edu.uts.tecnoturno.repository.*;
import co.edu.uts.tecnoturno.web.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TallerService {
  private final UsuarioRepository usuarios;
  private final EquipoRepository equipos;
  private final OrdenServicioRepository ordenes;
  private final HistorialEstadoRepository historial;

  public TallerService(
      UsuarioRepository u,
      EquipoRepository e,
      OrdenServicioRepository o,
      HistorialEstadoRepository h) {
    usuarios = u;
    equipos = e;
    ordenes = o;
    historial = h;
  }

  public Usuario actual(Authentication a) {
    var u = usuarios.findByEmail(a.getName()).orElseThrow();
    if (!u.isActivo()) throw new AccessDeniedException("Cuenta inactiva");
    return u;
  }

  public OrdenServicio orden(Long id) {
    return ordenes
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("La orden no existe"));
  }

  public boolean visible(OrdenServicio o, Usuario u) {
    return u.getRol() == Rol.ADMIN
        || (u.getRol() == Rol.TECNICO && o.getTecnico().getId().equals(u.getId()))
        || (u.getRol() == Rol.CLIENTE && o.getEquipo().getCliente().getId().equals(u.getId()));
  }

  public List<OrdenServicio> listar(Usuario u, String q, Estado estado) {
    String term = q == null ? "" : q.trim().toLowerCase();
    return ordenes.findAllByOrderByIdDesc().stream()
        .filter(o -> visible(o, u))
        .filter(o -> estado == null || o.getEstado() == estado)
        .filter(
            o ->
                term.isEmpty()
                    || ("OT-"
                            + o.getId()
                            + " "
                            + o.getEquipo().getSerial()
                            + " "
                            + o.getEquipo().getCliente().getNombre())
                        .toLowerCase()
                        .contains(term))
        .toList();
  }

  public OrdenServicio crear(OrdenForm f, Usuario actor) {
    if (actor.getRol() != Rol.ADMIN)
      throw new AccessDeniedException("Solo administración puede recibir equipos");
    var e =
        equipos
            .buscarParaRecepcion(f.getEquipoId())
            .orElseThrow(() -> new IllegalArgumentException("Seleccione un equipo válido"));
    var t =
        usuarios
            .findById(f.getTecnicoId())
            .orElseThrow(() -> new IllegalArgumentException("Seleccione un técnico válido"));
    if (t.getRol() != Rol.TECNICO || !t.isActivo() || !e.getCliente().isActivo())
      throw new IllegalArgumentException("El técnico y el cliente deben estar activos");
    boolean abierta =
        ordenes.findAll().stream()
            .anyMatch(
                o ->
                    o.getEquipo().getId().equals(e.getId())
                        && o.getEstado() != Estado.ENTREGADO
                        && o.getEstado() != Estado.CANCELADO);
    if (abierta) throw new IllegalArgumentException("Este equipo ya tiene una orden abierta");
    var o = new OrdenServicio();
    o.setEquipo(e);
    o.setTecnico(t);
    o.setEstado(Estado.RECIBIDO);
    o.setFalla(f.getFalla().trim());
    o.setDiagnostico("");
    o.setCosto(BigDecimal.ZERO);
    o.setCreada(LocalDateTime.now());
    o.setActualizada(o.getCreada());
    ordenes.save(o);
    registrar(o, actor, "Recepción del equipo");
    return o;
  }

  public void actualizar(Long id, ActualizarForm f, Usuario actor) {
    var o = orden(id);
    if (!visible(o, actor) || actor.getRol() == Rol.CLIENTE)
      throw new AccessDeniedException("No tiene permiso para actualizar esta orden");
    if (!Objects.equals(o.getVersion(), f.getVersion()))
      throw new IllegalArgumentException(
          "Otro usuario modificó la orden. Recargue antes de guardar");
    if (o.getEstado() == Estado.ENTREGADO || o.getEstado() == Estado.CANCELADO)
      throw new IllegalArgumentException("La orden ya está cerrada");
    if (o.getEstado() != f.getEstado() && !o.getEstado().permite(f.getEstado()))
      throw new IllegalArgumentException("La transición de estado no está permitida");
    o.setEstado(f.getEstado());
    o.setDiagnostico(f.getDiagnostico().trim());
    o.setCosto(f.getCosto());
    o.setActualizada(LocalDateTime.now());
    registrar(o, actor, f.getNota().trim());
    ordenes.saveAndFlush(o);
  }

  private void registrar(OrdenServicio o, Usuario actor, String nota) {
    var h = new HistorialEstado();
    h.setOrden(o);
    h.setResponsable(actor);
    h.setEstado(o.getEstado());
    h.setNota(nota);
    h.setFecha(LocalDateTime.now());
    historial.save(h);
  }
}
