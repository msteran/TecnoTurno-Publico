package co.edu.uts.tecnoturno.repository;

import co.edu.uts.tecnoturno.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenServicioRepository extends JpaRepository<OrdenServicio, Long> {
  boolean existsByEquipoId(Long id);

  java.util.List<OrdenServicio> findAllByOrderByIdDesc();
}
