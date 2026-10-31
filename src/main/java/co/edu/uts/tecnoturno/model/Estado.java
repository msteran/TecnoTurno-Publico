package co.edu.uts.tecnoturno.model;

public enum Estado {
  RECIBIDO,
  EN_DIAGNOSTICO,
  EN_REPARACION,
  LISTO,
  ENTREGADO,
  CANCELADO;

  public boolean permite(Estado siguiente) {
    return switch (this) {
      case RECIBIDO -> siguiente == EN_DIAGNOSTICO || siguiente == CANCELADO;
      case EN_DIAGNOSTICO -> siguiente == EN_REPARACION || siguiente == CANCELADO;
      case EN_REPARACION -> siguiente == LISTO || siguiente == CANCELADO;
      case LISTO -> siguiente == ENTREGADO;
      default -> false;
    };
  }
}
