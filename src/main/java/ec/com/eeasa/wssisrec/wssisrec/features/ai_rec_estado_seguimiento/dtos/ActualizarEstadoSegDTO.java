package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarEstadoSegDTO {
  private String arees_codigo;
  private String arees_descripcion;
  private AuditoriaDTO auditoria;

}
