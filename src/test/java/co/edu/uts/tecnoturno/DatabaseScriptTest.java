package co.edu.uts.tecnoturno;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.*;
import java.sql.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class DatabaseScriptTest {
  @Test
  void scriptsCreateAndSeedRelatedRecordsInH2MysqlMode() throws Exception {
    try (var c =
            DriverManager.getConnection(
                "jdbc:h2:mem:sqlscripts;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        var st = c.createStatement()) {
      for (String path :
          new String[] {"database/01_crear_base_datos.sql", "database/02_insertar_datos.sql"}) {
        String sql =
            Files.readString(Path.of(path))
                .replaceAll("(?m)^--.*$", "")
                .replaceAll("(?i)CREATE DATABASE[^;]*;", "")
                .replaceAll("(?i)USE tecnoturno;", "")
                .replace("ENGINE=InnoDB", "")
                .replace("START TRANSACTION", "SET AUTOCOMMIT FALSE");
        for (String query : sql.split(";")) if (!query.isBlank()) st.execute(query);
      }
      try (var rs = st.executeQuery("select count(*) from ordenes")) {
        rs.next();
        assertThat(rs.getInt(1)).isEqualTo(2);
      }
      try (var rs = st.executeQuery("select count(*) from historial")) {
        rs.next();
        assertThat(rs.getInt(1)).isEqualTo(3);
      }
      try (var rs = st.executeQuery("select password from usuarios where id=1")) {
        rs.next();
        assertThat(new BCryptPasswordEncoder().matches("Tecno2026!", rs.getString(1))).isTrue();
      }
      assertThatThrownBy(() -> st.execute("update ordenes set costo=-1 where id=1"))
          .isInstanceOf(SQLException.class);
      assertThatThrownBy(() -> st.execute("delete from equipos where id=1"))
          .isInstanceOf(SQLException.class);
    }
  }
}
