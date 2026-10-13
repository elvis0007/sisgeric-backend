package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ValidarRecomendacionDTO {
  private String arere_motivo_estado;
  private Long arees_codigo;
  private AuditoriaDTO auditoria;
}
