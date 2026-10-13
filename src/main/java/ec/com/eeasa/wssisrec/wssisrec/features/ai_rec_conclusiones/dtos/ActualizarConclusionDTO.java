package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarConclusionDTO {
  private Long areco_codigo;
  private String areco_conclusion;
  private AuditoriaDTO auditoria;

}
