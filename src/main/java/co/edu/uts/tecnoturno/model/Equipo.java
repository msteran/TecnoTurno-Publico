package co.edu.uts.tecnoturno.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipos")
public class Equipo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "cliente_id", nullable = false)
  @jakarta.validation.constraints.NotNull
  private Usuario cliente;

  @Column(nullable = false, length = 50)
  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Size(max = 50)
  private String tipo;

  @Column(nullable = false, length = 50)
  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Size(max = 50)
  private String marca;

  @Column(nullable = false, length = 80)
  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Size(max = 80)
  private String modelo;

  @Column(nullable = false, unique = true, length = 80)
  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Size(max = 80)
  private String serial;

  @Column(length = 300)
  @jakarta.validation.constraints.Size(max = 300)
  private String accesorios;

  public Long getId() {
    return id;
  }

  public void setId(Long v) {
    id = v;
  }

  public Usuario getCliente() {
    return cliente;
  }

  public void setCliente(Usuario v) {
    cliente = v;
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
