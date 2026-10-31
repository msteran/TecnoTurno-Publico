package co.edu.uts.tecnoturno;

import static org.assertj.core.api.Assertions.*;

import co.edu.uts.tecnoturno.model.*;
import co.edu.uts.tecnoturno.repository.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:vivida;MODE=MySQL;DB_CLOSE_DELAY=-1",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "tecnoturno.demo.datos-ampliados=true"
    })
@ActiveProfiles("demo")
@Transactional
class DatosDemoTest {
  @Autowired UsuarioRepository usuarios;
  @Autowired EquipoRepository equipos;
  @Autowired OrdenServicioRepository ordenes;
  @Autowired HistorialEstadoRepository historial;

  @Autowired
  @Qualifier("seed")
  CommandLineRunner cargar;

  @Test
  void ejemplosCompletosYRelacionados() {
    assertThat(usuarios.count()).isEqualTo(14);
    assertThat(equipos.count()).isEqualTo(25);
    assertThat(ordenes.count()).isEqualTo(25);
    assertThat(historial.count()).isEqualTo(73);
    var cliente1 = usuarios.findByEmail("cliente1@tecnoturno.test").orElseThrow();
    assertThat(ordenes.findAll()).anyMatch(o ->
        o.getEquipo().getCliente().getId().equals(cliente1.getId()));
    assertThat(ordenes.findAll())
        .extracting(OrdenServicio::getEstado)
        .containsAll(Arrays.asList(Estado.values()));
    for (var o : ordenes.findAll()) {
      var eventos = historial.findByOrdenIdOrderByFechaAscIdAsc(o.getId());
      assertThat(eventos).isNotEmpty();
      assertThat(eventos.get(0).getEstado()).isEqualTo(Estado.RECIBIDO);
      assertThat(eventos.get(eventos.size() - 1).getEstado()).isEqualTo(o.getEstado());
      if (o.getEquipo().getSerial().startsWith("VIVA-"))
        assertThat(eventos.get(eventos.size() - 1).getFecha()).isEqualTo(o.getActualizada());
      assertThat(o.getCosto()).isNotNegative();
      assertThat(o.getTecnico().getRol()).isEqualTo(Rol.TECNICO);
      assertThat(o.getEquipo().getCliente().getRol()).isEqualTo(Rol.CLIENTE);
    }
  }

  @Test
  void reiniciarNoDuplicaNiSobrescribeCambios() throws Exception {
    var orden =
        ordenes.findAll().stream()
            .filter(o -> o.getEquipo().getSerial().equals("VIVA-0002"))
            .findFirst()
            .orElseThrow();
    orden.setDiagnostico("Diagnóstico editado por el usuario");
    ordenes.saveAndFlush(orden);
    cargar.run();
    assertThat(ordenes.count()).isEqualTo(25);
    assertThat(historial.count()).isEqualTo(73);
    assertThat(usuarios.count()).isEqualTo(14);
    assertThat(equipos.count()).isEqualTo(25);
    assertThat(ordenes.findById(orden.getId()).orElseThrow().getDiagnostico())
        .isEqualTo("Diagnóstico editado por el usuario");
  }
}
