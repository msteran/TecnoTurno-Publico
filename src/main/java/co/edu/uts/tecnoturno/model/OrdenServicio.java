package co.edu.uts.tecnoturno.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes")
public class OrdenServicio {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "equipo_id", nullable = false)
  @jakarta.validation.constraints.NotNull
  private Equipo equipo;

  @ManyToOne(optional = false)
  @JoinColumn(name = "tecnico_id", nullable = false)
  @jakarta.validation.constraints.NotNull
  private Usuario tecnico;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 25)
  @jakarta.validation.constraints.NotNull
  private Estado estado;

  @Column(nullable = false, length = 1000)
  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Size(max = 1000)
  private String falla;

  @Column(nullable = false, length = 2000)
  @jakarta.validation.constraints.NotNull
  @jakarta.validation.constraints.Size(max = 2000)
  private String diagnostico;

  @Column(nullable = false, precision = 12, scale = 2)
  @jakarta.validation.constraints.NotNull
  @jakarta.validation.constraints.DecimalMin("0.00")
  @jakarta.validation.constraints.Digits(integer = 10, fraction = 2)
  private BigDecimal costo;

  @Column(nullable = false)
  @jakarta.validation.constraints.NotNull
  private LocalDateTime creada;

  @Column(nullable = false)
  @jakarta.validation.constraints.NotNull
  private LocalDateTime actualizada;

  @Version private Long version;

  public Long getId() {
    return id;
  }

  public void setId(Long v) {
    id = v;
  }

  public Equipo getEquipo() {
    return equipo;
  }

  public void setEquipo(Equipo v) {
    equipo = v;
  }

  public Usuario getTecnico() {
    return tecnico;
  }

  public void setTecnico(Usuario v) {
    tecnico = v;
  }

  public Estado getEstado() {
    return estado;
  }

  public void setEstado(Estado v) {
    estado = v;
  }

  public String getFalla() {
    return falla;
  }

  public void setFalla(String v) {
    falla = v;
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

  public LocalDateTime getCreada() {
    return creada;
  }

  public void setCreada(LocalDateTime v) {
    creada = v;
  }

  public LocalDateTime getActualizada() {
    return actualizada;
  }

  public void setActualizada(LocalDateTime v) {
    actualizada = v;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long v) {
    version = v;
  }
}
