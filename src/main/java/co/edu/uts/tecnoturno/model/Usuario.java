package co.edu.uts.tecnoturno.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Size(max = 100)
  private String nombre;

  @Column(nullable = false, unique = true, length = 20)
  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Pattern(regexp = "[0-9]{5,20}")
  private String documento;

  @Column(nullable = false, unique = true, length = 120)
  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Email
  @jakarta.validation.constraints.Size(max = 120)
  private String email;

  @Column(nullable = false, length = 20)
  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Size(max = 20)
  private String telefono;

  @Column(nullable = false, length = 100)
  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Size(max = 100)
  private String password;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 15)
  @jakarta.validation.constraints.NotNull
  private Rol rol;

  @Column(nullable = false)
  private boolean activo;

  public Long getId() {
    return id;
  }

  public void setId(Long v) {
    id = v;
  }

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

  public String getPassword() {
    return password;
  }

  public void setPassword(String v) {
    password = v;
  }

  public Rol getRol() {
    return rol;
  }

  public void setRol(Rol v) {
    rol = v;
  }

  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean v) {
    activo = v;
  }
}
