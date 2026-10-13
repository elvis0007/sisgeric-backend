package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.enums;

public enum EstadoVerEnum {
  PENDIENTE(1L, ""),
  JUSTIFICA(2L, ""),
  NO_JUSTIFICA(3L, ""),
  ADICIONAL(4L, ""),
  PENDIENTE_REVISION(5L, "Pendiente de revisión");

  private final Long codigo;
  private String mensaje;

  EstadoVerEnum(Long codigo, String mensaje) {
    this.codigo = codigo;
    this.mensaje = mensaje;
  }

  public Long getCodigo() {
    return codigo;
  }

  public String getMensaje() {
    return mensaje;
  }
}
