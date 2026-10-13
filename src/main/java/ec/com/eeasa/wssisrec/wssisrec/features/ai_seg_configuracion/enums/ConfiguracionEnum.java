package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.enums;

public enum ConfiguracionEnum {
  NAS(6L);

  private final Long codigo;

  ConfiguracionEnum(Long codigo) {
    this.codigo = codigo;
  }

  public Long getCodigo() {
    return codigo;
  }
}
