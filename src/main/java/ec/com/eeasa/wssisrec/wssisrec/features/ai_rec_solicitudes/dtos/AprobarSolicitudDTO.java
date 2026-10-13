package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class AprobarSolicitudDTO {
  private String aseru_codigo_per;
  private Character areso_estado_aprob;
  private AuditoriaDTO auditoria;
}
