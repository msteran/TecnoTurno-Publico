package co.edu.uts.tecnoturno.web;

import co.edu.uts.tecnoturno.model.*;
import co.edu.uts.tecnoturno.repository.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
  private final UsuarioRepository users;
  private final EquipoRepository equipos;
  private final OrdenServicioRepository ordenes;
  private final PasswordEncoder encoder;

  public AdminController(
      UsuarioRepository u, EquipoRepository e, OrdenServicioRepository o, PasswordEncoder p) {
    users = u;
    equipos = e;
    ordenes = o;
    encoder = p;
  }

  @ModelAttribute("roles")
  Rol[] roles() {
    return Rol.values();
  }

  @GetMapping("/usuarios")
  String usuarios(Model m) {
    m.addAttribute("usuarios", users.findAll());
    return "usuarios";
  }

  @GetMapping({"/usuarios/nuevo", "/usuarios/{id}/editar"})
  String usuarioForm(@PathVariable(required = false) Long id, Model m) {
    var f = new UsuarioForm();
    if (id != null) {
      var u = users.findById(id).orElseThrow();
      f.setNombre(u.getNombre());
      f.setDocumento(u.getDocumento());
      f.setEmail(u.getEmail());
      f.setTelefono(u.getTelefono());
      f.setRol(u.getRol());
      f.setActivo(u.isActivo());
    }
    m.addAttribute("form", f);
    m.addAttribute("id", id);
    return "usuario-form";
  }

  @PostMapping({"/usuarios/nuevo", "/usuarios/{id}/editar"})
  String usuarioGuardar(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") UsuarioForm f,
      BindingResult b,
      Model m,
      RedirectAttributes ra,
      Authentication a) {
    var old = id == null ? new Usuario() : users.findById(id).orElseThrow();
    String email = f.getEmail() == null ? "" : f.getEmail().trim().toLowerCase();
    String pass = f.getPassword() == null ? "" : f.getPassword();
    if ((id == null || !email.equals(old.getEmail())) && users.existsByEmail(email))
      b.rejectValue("email", "duplicado", "Ya existe este correo");
    if ((id == null || !java.util.Objects.equals(f.getDocumento(), old.getDocumento()))
        && users.existsByDocumento(f.getDocumento()))
      b.rejectValue("documento", "duplicado", "Ya existe este documento");
    if ((id == null || !pass.isBlank())
        && (pass.length() < 8
            || pass.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72))
      b.rejectValue("password", "longitud", "Use entre 8 caracteres y 72 bytes");
    if (id != null && f.getRol() != old.getRol())
      b.rejectValue("rol", "fijo", "El perfil no se puede cambiar después de crear la cuenta");
    if (id != null && old.getEmail().equals(a.getName()) && !f.isActivo())
      b.rejectValue("activo", "sesion", "No puede desactivar su propia cuenta");
    if (b.hasErrors()) {
      m.addAttribute("id", id);
      return "usuario-form";
    }
    old.setNombre(f.getNombre().trim());
    old.setDocumento(f.getDocumento());
    old.setEmail(email);
    old.setTelefono(f.getTelefono().trim());
    old.setRol(f.getRol());
    old.setActivo(f.isActivo());
    if (!pass.isBlank()) old.setPassword(encoder.encode(pass));
    users.save(old);
    ra.addFlashAttribute("success", "Usuario guardado con éxito");
    return "redirect:/usuarios";
  }

  private void clientes(Model m) {
    m.addAttribute("clientes", users.findByRolAndActivoTrue(Rol.CLIENTE));
  }

  @GetMapping("/equipos")
  String equipos(Model m) {
    m.addAttribute("equipos", equipos.findAll());
    return "equipos";
  }

  @GetMapping({"/equipos/nuevo", "/equipos/{id}/editar"})
  String equipoForm(@PathVariable(required = false) Long id, Model m) {
    var f = new EquipoForm();
    if (id != null) {
      var e = equipos.findById(id).orElseThrow();
      f.setClienteId(e.getCliente().getId());
      f.setTipo(e.getTipo());
      f.setMarca(e.getMarca());
      f.setModelo(e.getModelo());
      f.setSerial(e.getSerial());
      f.setAccesorios(e.getAccesorios());
    }
    m.addAttribute("form", f);
    m.addAttribute("id", id);
    clientes(m);
    return "equipo-form";
  }

  @PostMapping({"/equipos/nuevo", "/equipos/{id}/editar"})
  String equipoGuardar(
      @PathVariable(required = false) Long id,
      @Valid @ModelAttribute("form") EquipoForm f,
      BindingResult b,
      Model m,
      RedirectAttributes ra) {
    var e = id == null ? new Equipo() : equipos.findById(id).orElseThrow();
    String serial = f.getSerial() == null ? "" : f.getSerial().trim().toUpperCase();
    if ((id == null || !serial.equals(e.getSerial())) && equipos.existsBySerial(serial))
      b.rejectValue("serial", "duplicado", "Este serial ya está registrado");
    var c = f.getClienteId() == null ? null : users.findById(f.getClienteId()).orElse(null);
    if (c == null || c.getRol() != Rol.CLIENTE || !c.isActivo())
      b.rejectValue("clienteId", "cliente", "Seleccione un cliente activo");
    if (id != null
        && ordenes.existsByEquipoId(id)
        && !e.getCliente().getId().equals(f.getClienteId()))
      b.rejectValue(
          "clienteId", "historial", "No puede cambiar el propietario de un equipo con órdenes");
    if (b.hasErrors()) {
      m.addAttribute("id", id);
      clientes(m);
      return "equipo-form";
    }
    e.setCliente(c);
    e.setTipo(f.getTipo().trim());
    e.setMarca(f.getMarca().trim());
    e.setModelo(f.getModelo().trim());
    e.setSerial(serial);
    e.setAccesorios(f.getAccesorios());
    equipos.save(e);
    ra.addFlashAttribute("success", "Equipo guardado con éxito");
    return "redirect:/equipos";
  }

  @PostMapping("/equipos/{id}/eliminar")
  String eliminar(@PathVariable Long id, RedirectAttributes ra) {
    if (ordenes.existsByEquipoId(id))
      ra.addFlashAttribute(
          "warning", "No se puede eliminar un equipo con órdenes; se conserva su historial");
    else {
      equipos.deleteById(id);
      ra.addFlashAttribute("warning", "Equipo eliminado con éxito");
    }
    return "redirect:/equipos";
  }
}
