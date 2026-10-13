package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.enums;

public enum EstadoSegEnum {
  EN_PROCESO(1L, ""),
  CUMPLIDA(2L, ""),
  NO_CUMPLIDA(3L, "NO CUMPLIDA POR FECHA MAXIMA VENCIDA"),
  NO_APLICA(4L, "");

  private final Long codigo;
  private String mensaje;

  EstadoSegEnum(Long codigo, String mensaje) {
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
