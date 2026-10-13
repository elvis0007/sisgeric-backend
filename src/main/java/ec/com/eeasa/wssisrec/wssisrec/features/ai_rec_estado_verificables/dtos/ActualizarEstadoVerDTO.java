package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarEstadoVerDTO {
  private String areev_descripcion;
  private AuditoriaDTO auditoria;
}
