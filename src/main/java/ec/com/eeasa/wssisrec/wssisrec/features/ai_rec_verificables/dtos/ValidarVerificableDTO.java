package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ValidarVerificableDTO {
  private Long areve_codigo;
  private Long areev_codigo;
  private String areve_motivo_estado;
  private AuditoriaDTO auditoria;
}
