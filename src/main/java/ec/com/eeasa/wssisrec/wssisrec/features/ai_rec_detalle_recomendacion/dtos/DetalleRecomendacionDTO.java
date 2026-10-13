package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.EstadoSegDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.ResponsableDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleRecomendacionDTO {
  private Long aredr_codigo;
  private String arere_estado_motivo;
  private EstadoSegDTO estado_seguimiento;
  private ResponsableDTO responsable;
}
