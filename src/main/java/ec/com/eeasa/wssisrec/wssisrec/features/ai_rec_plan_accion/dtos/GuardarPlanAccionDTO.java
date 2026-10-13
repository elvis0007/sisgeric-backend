package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuardarPlanAccionDTO {
  private Long arere_codigo;
  private Long aredr_codigo;
  private Long arers_codigo;
  private String arers_codigo_per;
  private String fecha_maxima_ref;
  private List<PlanActividadDTO> actividades;
}
