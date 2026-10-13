package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RolEnum {
  ADMINISTRADOR(1L),
  AUDITOR_OPERATIVO(3L),
  AUDITOR_JEFE_INTERNO(2L),
  RESPONSABLE(4L),
  DIRECTOR_DEPARTAMENTAL(5L);

  private static final Logger logger = LoggerFactory.getLogger(RolEnum.class);

  private final Long code;

  RolEnum(Long code) {
    this.code = code;
  }

  public Long getCode() {
    return code;
  }

  public static RolEnum fromCode(Long code) {
    for (RolEnum role : values()) {
      if (role.getCode().equals(code)) {
        return role;
      }
    }
    logger.error("Rol inválido: {}", code);
    return null;
  }
}
