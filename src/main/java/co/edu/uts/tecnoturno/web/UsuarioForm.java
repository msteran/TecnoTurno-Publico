package co.edu.uts.tecnoturno.web;

import co.edu.uts.tecnoturno.model.*;
import jakarta.validation.constraints.*;

public class UsuarioForm {
  @NotBlank
  @Size(max = 100)
  private String nombre;

  @NotBlank
  @Pattern(regexp = "[0-9]{5,20}", message = "Escriba entre 5 y 20 dígitos")
  private String documento;

  @NotBlank
  @Email
  @Size(max = 120)
  private String email;

  @NotBlank
  @Pattern(regexp = "[0-9+() -]{7,20}", message = "Teléfono inválido")
  private String telefono;

  @NotNull private Rol rol;

  @Size(max = 72)
  private String password;

  private boolean activo = true;

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String v) {
    nombre = v;
  }

  public String getDocumento() {
    return documento;
  }

  public void setDocumento(String v) {
    documento = v;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String v) {
    email = v;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String v) {
    telefono = v;
  }

  public Rol getRol() {
    return rol;
  }

  public void setRol(Rol v) {
    rol = v;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String v) {
    password = v;
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean v) {
    activo = v;
  }
}
