package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NuevoDetalleDTO {
  private AuditoriaDTO auditoria;
}
