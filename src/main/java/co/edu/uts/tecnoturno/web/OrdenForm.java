package co.edu.uts.tecnoturno.web;

import co.edu.uts.tecnoturno.model.*;
import jakarta.validation.constraints.*;

public class OrdenForm {
  @NotNull private Long equipoId;
  @NotNull private Long tecnicoId;

  @NotBlank
  @Size(max = 1000)
  private String falla;

  public Long getEquipoId() {
    return equipoId;
  }

  public void setEquipoId(Long v) {
    equipoId = v;
  }

  public Long getTecnicoId() {
    return tecnicoId;
  }

  public void setTecnicoId(Long v) {
    tecnicoId = v;
  }

  public String getFalla() {
    return falla;
  }

  public void setFalla(String v) {
    falla = v;
  }
}
