package co.edu.uts.tecnoturno.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial")
public class HistorialEstado {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "orden_id", nullable = false)
  @jakarta.validation.constraints.NotNull
  private OrdenServicio orden;

  @ManyToOne(optional = false)
  @JoinColumn(name = "responsable_id", nullable = false)
  @jakarta.validation.constraints.NotNull
  private Usuario responsable;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 25)
  @jakarta.validation.constraints.NotNull
  private Estado estado;

  @Column(nullable = false, length = 1000)
  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Size(max = 1000)
  private String nota;

  @Column(nullable = false)
  @jakarta.validation.constraints.NotNull
  private LocalDateTime fecha;

  public Long getId() {
    return id;
  }

  public void setId(Long v) {
    id = v;
  }

  public OrdenServicio getOrden() {
    return orden;
  }

  public void setOrden(OrdenServicio v) {
    orden = v;
  }

  public Usuario getResponsable() {
    return responsable;
  }

  public void setResponsable(Usuario v) {
    responsable = v;
  }

  public Estado getEstado() {
    return estado;
  }

  public void setEstado(Estado v) {
    estado = v;
  }

  public String getNota() {
    return nota;
  }

  public void setNota(String v) {
    nota = v;
  }

  public LocalDateTime getFecha() {
    return fecha;
  }

  public void setFecha(LocalDateTime v) {
    fecha = v;
  }
}
