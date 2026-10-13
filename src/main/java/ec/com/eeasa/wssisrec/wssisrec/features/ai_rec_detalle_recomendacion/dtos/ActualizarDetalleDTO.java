package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarDetalleDTO {
  private Long arees_codigo;
  private String aredr_estado_motivo;
  private AuditoriaDTO auditoria;
}
