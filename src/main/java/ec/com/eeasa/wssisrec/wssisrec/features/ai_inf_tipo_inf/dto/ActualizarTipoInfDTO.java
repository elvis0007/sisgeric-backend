package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.dto;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarTipoInfDTO {
  private Long ainti_codigo;
  private String ainti_descripcion;
  private AuditoriaDTO auditoria;
}
