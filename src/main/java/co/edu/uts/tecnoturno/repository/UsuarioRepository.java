package co.edu.uts.tecnoturno.repository;

import co.edu.uts.tecnoturno.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  java.util.Optional<Usuario> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByDocumento(String documento);

  java.util.List<Usuario> findByRolAndActivoTrue(Rol rol);
}
