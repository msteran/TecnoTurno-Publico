package co.edu.uts.tecnoturno.web;

import co.edu.uts.tecnoturno.model.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ActualizarForm {
  @NotNull private Estado estado;

  @NotBlank
  @Size(max = 2000)
  private String diagnostico;

  @NotNull
  @DecimalMin("0.00")
  @DecimalMax("9999999999.99")
  @Digits(integer = 10, fraction = 2)
  private BigDecimal costo;

  @NotBlank
  @Size(max = 1000)
  private String nota;

  @NotNull private Long version;

  public Estado getEstado() {
    return estado;
  }

  public void setEstado(Estado v) {
    estado = v;
  }

  public String getDiagnostico() {
    return diagnostico;
  }

  public void setDiagnostico(String v) {
    diagnostico = v;
  }

  public BigDecimal getCosto() {
    return costo;
  }

  public void setCosto(BigDecimal v) {
    costo = v;
  }

  public String getNota() {
    return nota;
  }

  public void setNota(String v) {
    nota = v;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long v) {
    version = v;
  }
}
