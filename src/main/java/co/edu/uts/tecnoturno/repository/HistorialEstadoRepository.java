package co.edu.uts.tecnoturno.repository;

import co.edu.uts.tecnoturno.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
  java.util.List<HistorialEstado> findByOrdenIdOrderByFechaAscIdAsc(Long id);
}
