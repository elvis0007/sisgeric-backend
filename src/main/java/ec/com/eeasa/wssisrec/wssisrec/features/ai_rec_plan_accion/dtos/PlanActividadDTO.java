package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanActividadDTO {
  private Long arpac_codigo;
  private Long arpa_codigo;
  private Long arer_codigo;
  private Integer num_actividad;
  private String actividad;
  private String fecha_ejecucion;
  private String entregable_esperado;
  private Character arpac_estado;
}
