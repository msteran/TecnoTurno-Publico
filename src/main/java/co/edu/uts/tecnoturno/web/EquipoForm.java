package co.edu.uts.tecnoturno.web;

import co.edu.uts.tecnoturno.model.*;
import jakarta.validation.constraints.*;

public class EquipoForm {
  @NotNull private Long clienteId;

  @NotBlank
  @Size(max = 50)
  private String tipo;

  @NotBlank
  @Size(max = 50)
  private String marca;

  @NotBlank
  @Size(max = 80)
  private String modelo;

  @NotBlank
  @Size(max = 80)
  private String serial;

  @Size(max = 300)
  private String accesorios;

  public Long getClienteId() {
    return clienteId;
  }

  public void setClienteId(Long v) {
    clienteId = v;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String v) {
    tipo = v;
  }

  public String getMarca() {
    return marca;
  }

  public void setMarca(String v) {
    marca = v;
  }

  public String getModelo() {
    return modelo;
  }

  public void setModelo(String v) {
    modelo = v;
  }

  public String getSerial() {
    return serial;
  }

  public void setSerial(String v) {
    serial = v;
  }

  public String getAccesorios() {
    return accesorios;
  }

  public void setAccesorios(String v) {
    accesorios = v;
  }
}
