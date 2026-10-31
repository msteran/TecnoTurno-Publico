package co.edu.uts.tecnoturno.web;

import co.edu.uts.tecnoturno.model.*;
import co.edu.uts.tecnoturno.repository.*;
import co.edu.uts.tecnoturno.service.*;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TallerController {
  private final TallerService service;
  private final UsuarioRepository users;
  private final EquipoRepository equipos;
  private final HistorialEstadoRepository historial;

  public TallerController(
      TallerService s, UsuarioRepository u, EquipoRepository e, HistorialEstadoRepository h) {
    service = s;
    users = u;
    equipos = e;
    historial = h;
  }

  @GetMapping("/login")
  String login() {
    return "login";
  }

  @GetMapping("/denegado")
  String denied() {
    return "denegado";
  }

  @ModelAttribute("estados")
  Estado[] estados() {
    return Estado.values();
  }

  @GetMapping("/")
  String home(Authentication a, Model m) {
    var list = service.listar(service.actual(a), null, null);
    m.addAttribute("total", list.size());
    m.addAttribute(
        "abiertas",
        list.stream()
            .filter(o -> o.getEstado() != Estado.ENTREGADO && o.getEstado() != Estado.CANCELADO)
            .count());
    m.addAttribute("listas", list.stream().filter(o -> o.getEstado() == Estado.LISTO).count());
    m.addAttribute("recientes", list.stream().limit(5).toList());
    return "home";
  }

  @GetMapping("/ordenes")
  String list(
      Authentication a,
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) Estado estado,
      Model m) {
    m.addAttribute("ordenes", service.listar(service.actual(a), q, estado));
    m.addAttribute("q", q);
    m.addAttribute("estadoSeleccionado", estado);
    return "ordenes";
  }

  private void opciones(Model m) {
    m.addAttribute(
        "equipos", equipos.findAll().stream().filter(e -> e.getCliente().isActivo()).toList());
    m.addAttribute("tecnicos", users.findByRolAndActivoTrue(Rol.TECNICO));
  }

  @GetMapping("/ordenes/nueva")
  String nueva(Model m) {
    m.addAttribute("form", new OrdenForm());
    opciones(m);
    return "orden-form";
  }

  @PostMapping("/ordenes/nueva")
  String crear(
      @Valid @ModelAttribute("form") OrdenForm f,
      BindingResult b,
      Authentication a,
      Model m,
      RedirectAttributes ra) {
    if (!b.hasErrors()) {
      try {
        var o = service.crear(f, service.actual(a));
        ra.addFlashAttribute("success", "Orden OT-" + o.getId() + " registrada con éxito");
        return "redirect:/ordenes/" + o.getId();
      } catch (IllegalArgumentException ex) {
        b.reject("regla", ex.getMessage());
      }
    }
    opciones(m);
    return "orden-form";
  }

  private void detalle(Long id, Authentication a, Model m) {
    var o = service.orden(id);
    var u = service.actual(a);
    if (!service.visible(o, u))
      throw new AccessDeniedException("Esta orden pertenece a otro usuario");
    m.addAttribute("orden", o);
    m.addAttribute("historial", historial.findByOrdenIdOrderByFechaAscIdAsc(id));
    m.addAttribute(
        "puedeEditar",
        u.getRol() != Rol.CLIENTE
            && o.getEstado() != Estado.ENTREGADO
            && o.getEstado() != Estado.CANCELADO);
    m.addAttribute(
        "siguientes",
        java.util.Arrays.stream(Estado.values())
            .filter(e -> e == o.getEstado() || o.getEstado().permite(e))
            .toList());
  }

  @GetMapping("/ordenes/{id}")
  String ver(@PathVariable Long id, Authentication a, Model m) {
    detalle(id, a, m);
    var o = service.orden(id);
    var f = new ActualizarForm();
    f.setEstado(o.getEstado());
    f.setDiagnostico(o.getDiagnostico());
    f.setCosto(o.getCosto());
    f.setVersion(o.getVersion());
    m.addAttribute("form", f);
    return "detalle";
  }

  @PostMapping("/ordenes/{id}")
  String update(
      @PathVariable Long id,
      @Valid @ModelAttribute("form") ActualizarForm f,
      BindingResult b,
      Authentication a,
      Model m,
      RedirectAttributes ra) {
    detalle(id, a, m);
    if (service.actual(a).getRol() == Rol.CLIENTE) throw new AccessDeniedException("Solo lectura");
    if (!b.hasErrors()) {
      try {
        service.actualizar(id, f, service.actual(a));
        ra.addFlashAttribute(
            "success", "Orden actualizada; el cambio quedó registrado en el historial");
        return "redirect:/ordenes/" + id;
      } catch (IllegalArgumentException ex) {
        b.reject("regla", ex.getMessage());
      }
    }
    return "detalle";
  }
}
