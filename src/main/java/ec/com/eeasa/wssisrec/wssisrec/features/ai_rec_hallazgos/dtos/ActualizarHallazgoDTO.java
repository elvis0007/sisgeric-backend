package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarHallazgoDTO {
  private Long areha_codigo;
  private String areha_titulo;
  private AuditoriaDTO auditoria;
}
