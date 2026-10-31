package co.edu.uts.tecnoturno;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.edu.uts.tecnoturno.model.*;
import co.edu.uts.tecnoturno.repository.*;
import co.edu.uts.tecnoturno.service.*;
import co.edu.uts.tecnoturno.web.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:tests;MODE=MySQL;DB_CLOSE_DELAY=-1",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "tecnoturno.demo.datos-ampliados=false"
    })
@ActiveProfiles("demo")
@AutoConfigureMockMvc
@Transactional
class TecnoTurnoIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired TallerService service;
  @Autowired UsuarioRepository users;
  @Autowired EquipoRepository equipos;
  @Autowired OrdenServicioRepository ordenes;
  @Autowired HistorialEstadoRepository historial;

  Usuario admin() {
    return users.findByEmail("admin@tecnoturno.test").orElseThrow();
  }

  @Test
  void loginAndPublicAssets() throws Exception {
    mvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("TecnoTurno")));
    mvc.perform(get("/webjars/bootstrap/5.3.3/css/bootstrap.min.css")).andExpect(status().isOk());
    mvc.perform(get("/ordenes")).andExpect(status().is3xxRedirection());
  }

  @Test
  void realLogin() throws Exception {
    mvc.perform(
            post("/login")
                .with(csrf())
                .param("username", "admin@tecnoturno.test")
                .param("password", "Tecno2026!"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }

  @Test
  @WithMockUser(username = "admin@tecnoturno.test", roles = "ADMIN")
  void allTemplatesRender() throws Exception {
    for (String path :
        new String[] {
          "/",
          "/ordenes",
          "/ordenes?estado=RECIBIDO&q=DEMO",
          "/ordenes/nueva",
          "/ordenes/1",
          "/usuarios",
          "/usuarios/nuevo",
          "/usuarios/1/editar",
          "/equipos",
          "/equipos/nuevo",
          "/equipos/1/editar"
        }) mvc.perform(get(path)).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "cliente@tecnoturno.test", roles = "CLIENTE")
  void clientCannotAdminOrWrite() throws Exception {
    mvc.perform(get("/usuarios")).andExpect(status().isForbidden());
    mvc.perform(get("/ordenes/1")).andExpect(status().isOk());
    mvc.perform(post("/ordenes/1").with(csrf())).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "tecnico@tecnoturno.test", roles = "TECNICO")
  void unassignedOrderHidden() throws Exception {
    Usuario other = new Usuario();
    other.setNombre("Otro técnico");
    other.setDocumento("99999999");
    other.setEmail("otro@test.test");
    other.setTelefono("3000000000");
    other.setPassword("unused");
    other.setActivo(true);
    other.setRol(Rol.TECNICO);
    users.save(other);
    ordenes.findById(1L).orElseThrow().setTecnico(other);
    mvc.perform(get("/ordenes/1")).andExpect(status().isForbidden());
    assertThat(
            service.listar(users.findByEmail("tecnico@tecnoturno.test").orElseThrow(), null, null))
        .isEmpty();
  }

  @Test
  @WithMockUser(username = "admin@tecnoturno.test", roles = "ADMIN")
  void formValidationAndCsrf() throws Exception {
    mvc.perform(post("/ordenes/nueva")).andExpect(status().isForbidden());
    mvc.perform(post("/ordenes/nueva").with(csrf()).param("falla", ""))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("form"));
    mvc.perform(
            post("/usuarios/nuevo")
                .with(csrf())
                .param("nombre", "A")
                .param("documento", "12345678")
                .param("email", "no-valido")
                .param("telefono", "3001234567")
                .param("rol", "CLIENTE")
                .param("password", "123"))
        .andExpect(model().attributeHasFieldErrors("form", "email", "password"));
  }

  @Test
  void lifecycleAndHistory() {
    var o = service.orden(1L);
    int before = historial.findByOrdenIdOrderByFechaAscIdAsc(1L).size();
    for (Estado e :
        new Estado[] {
          Estado.EN_DIAGNOSTICO, Estado.EN_REPARACION, Estado.LISTO, Estado.ENTREGADO
        }) {
      var f = new ActualizarForm();
      f.setEstado(e);
      f.setCosto(new BigDecimal("85000.00"));
      f.setDiagnostico("Conector de carga reemplazado");
      f.setNota("Avance registrado");
      f.setVersion(o.getVersion());
      service.actualizar(o.getId(), f, admin());
    }
    assertThat(o.getEstado()).isEqualTo(Estado.ENTREGADO);
    assertThat(historial.findByOrdenIdOrderByFechaAscIdAsc(1L)).hasSize(before + 4);
    var closed = new ActualizarForm();
    closed.setVersion(o.getVersion());
    closed.setEstado(Estado.RECIBIDO);
    assertThatThrownBy(() -> service.actualizar(1L, closed, admin()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void invalidTransitionAndConcurrentEdit() {
    var o = service.orden(1L);
    var f = new ActualizarForm();
    f.setEstado(Estado.ENTREGADO);
    f.setVersion(o.getVersion());
    assertThatThrownBy(() -> service.actualizar(1L, f, admin()))
        .isInstanceOf(IllegalArgumentException.class);
    f.setEstado(Estado.EN_DIAGNOSTICO);
    f.setVersion(-1L);
    assertThatThrownBy(() -> service.actualizar(1L, f, admin()))
        .hasMessageContaining("Otro usuario");
  }

  @Test
  void duplicateOpenOrder() {
    var f = new OrdenForm();
    f.setEquipoId(1L);
    f.setTecnicoId(users.findByEmail("tecnico@tecnoturno.test").orElseThrow().getId());
    f.setFalla("Duplicado");
    assertThatThrownBy(() -> service.crear(f, admin())).hasMessageContaining("orden abierta");
  }

  @Test
  @WithMockUser(username = "admin@tecnoturno.test", roles = "ADMIN")
  void preserveEquipmentWithOrders() throws Exception {
    mvc.perform(post("/equipos/1/eliminar").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attributeExists("warning"));
    assertThat(equipos.existsById(1L)).isTrue();
  }

  @Test
  @WithMockUser(username = "admin@tecnoturno.test", roles = "ADMIN")
  void duplicateSerialAndEmail() throws Exception {
    mvc.perform(
            post("/equipos/nuevo")
                .with(csrf())
                .param("clienteId", "3")
                .param("tipo", "PC")
                .param("marca", "Marca")
                .param("modelo", "Modelo")
                .param("serial", " demo-001 "))
        .andExpect(model().attributeHasFieldErrors("form", "serial"));
    mvc.perform(
            post("/usuarios/nuevo")
                .with(csrf())
                .param("nombre", "Otro")
                .param("documento", "123456789")
                .param("email", "ADMIN@tecnoturno.test")
                .param("telefono", "3001234567")
                .param("rol", "ADMIN")
                .param("password", "Segura2026!"))
        .andExpect(model().attributeHasFieldErrors("form", "email"));
  }

  @Test
  @WithMockUser(username = "admin@tecnoturno.test", roles = "ADMIN")
  void createEditAndRemoveWorkflow() throws Exception {
    mvc.perform(
            post("/usuarios/nuevo")
                .with(csrf())
                .param("nombre", "Cliente Nuevo")
                .param("documento", "76543210")
                .param("email", "nuevo@demo.test")
                .param("telefono", "3001234567")
                .param("rol", "CLIENTE")
                .param("password", "Segura2026!")
                .param("activo", "true"))
        .andExpect(redirectedUrl("/usuarios"));
    var cliente = users.findByEmail("nuevo@demo.test").orElseThrow();
    mvc.perform(
            post("/equipos/nuevo")
                .with(csrf())
                .param("clienteId", cliente.getId().toString())
                .param("tipo", "Portátil")
                .param("marca", "Dell")
                .param("modelo", "Inspiron")
                .param("serial", "nuevo-01")
                .param("accesorios", "Cargador"))
        .andExpect(redirectedUrl("/equipos"));
    var equipo =
        equipos.findAll().stream()
            .filter(e -> e.getSerial().equals("NUEVO-01"))
            .findFirst()
            .orElseThrow();
    mvc.perform(
            post("/equipos/" + equipo.getId() + "/editar")
                .with(csrf())
                .param("clienteId", cliente.getId().toString())
                .param("tipo", "Portátil")
                .param("marca", "Dell")
                .param("modelo", "Inspiron actualizado")
                .param("serial", "NUEVO-01")
                .param("accesorios", ""))
        .andExpect(redirectedUrl("/equipos"));
    mvc.perform(
            post("/ordenes/nueva")
                .with(csrf())
                .param("equipoId", equipo.getId().toString())
                .param("tecnicoId", "2")
                .param("falla", "Pantalla rota"))
        .andExpect(status().is3xxRedirection());
    assertThat(service.listar(cliente, null, null)).hasSize(1);
    assertThat(
            service.listar(
                users.findByEmail("cliente@tecnoturno.test").orElseThrow(), "NUEVO-01", null))
        .isEmpty();
    var nueva = service.listar(cliente, null, null).get(0);
    mvc.perform(
            post("/ordenes/" + nueva.getId())
                .with(csrf())
                .param("estado", "EN_DIAGNOSTICO")
                .param("diagnostico", "Pantalla dañada")
                .param("costo", "120000.00")
                .param("nota", "Revisión iniciada")
                .param("version", nueva.getVersion().toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attributeExists("success"));
    assertThat(historial.findByOrdenIdOrderByFechaAscIdAsc(nueva.getId())).hasSize(2);
  }

  @Test
  @WithMockUser(username = "admin@tecnoturno.test", roles = "ADMIN")
  void negativeCostIsRejected() throws Exception {
    mvc.perform(
            post("/ordenes/1")
                .with(csrf())
                .param("estado", "EN_DIAGNOSTICO")
                .param("diagnostico", "Prueba")
                .param("costo", "-1")
                .param("nota", "Prueba")
                .param("version", service.orden(1L).getVersion().toString()))
        .andExpect(model().attributeHasFieldErrors("form", "costo"));
    assertThat(service.orden(1L).getEstado()).isEqualTo(Estado.RECIBIDO);
  }

  @Test
  @WithMockUser(username = "admin@tecnoturno.test", roles = "ADMIN")
  void removeUnusedEquipmentAndDisableClient() throws Exception {
    var e = new Equipo();
    e.setCliente(users.findByEmail("cliente@tecnoturno.test").orElseThrow());
    e.setTipo("PC");
    e.setMarca("HP");
    e.setModelo("Demo");
    e.setSerial("BORRAR");
    equipos.save(e);
    mvc.perform(post("/equipos/" + e.getId() + "/eliminar").with(csrf()))
        .andExpect(redirectedUrl("/equipos"));
    assertThat(equipos.existsById(e.getId())).isFalse();
    var c = users.findByEmail("cliente@tecnoturno.test").orElseThrow();
    mvc.perform(
            post("/usuarios/" + c.getId() + "/editar")
                .with(csrf())
                .param("nombre", c.getNombre())
                .param("documento", c.getDocumento())
                .param("email", c.getEmail())
                .param("telefono", c.getTelefono())
                .param("rol", "CLIENTE")
                .param("password", "")
                .param("_activo", "on"))
        .andExpect(redirectedUrl("/usuarios"));
    assertThat(c.isActivo()).isFalse();
    mvc.perform(
            post("/login")
                .with(csrf())
                .param("username", c.getEmail())
                .param("password", "Tecno2026!"))
        .andExpect(redirectedUrl("/login?error"));
  }
}
