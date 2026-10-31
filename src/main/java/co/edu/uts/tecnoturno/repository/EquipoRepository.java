package co.edu.uts.tecnoturno.repository;

import co.edu.uts.tecnoturno.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from Equipo e where e.id = :id")
  java.util.Optional<Equipo> buscarParaRecepcion(
      @org.springframework.data.repository.query.Param("id") Long id);

  boolean existsBySerial(String serial);

  boolean existsByClienteId(Long id);
}
