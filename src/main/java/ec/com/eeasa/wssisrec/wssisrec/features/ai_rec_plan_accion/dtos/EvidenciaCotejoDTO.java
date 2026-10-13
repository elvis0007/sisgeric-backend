package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaCotejoDTO {
  private Long arec_codigo;
  private Long arer_codigo;
  private Long aredr_codigo;
  private String arer_descripcion;
  private boolean cumple;
  private String fecha_cotejo;
}
