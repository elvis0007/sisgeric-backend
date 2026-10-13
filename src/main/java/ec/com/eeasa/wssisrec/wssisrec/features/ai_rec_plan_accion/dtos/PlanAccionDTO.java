package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanAccionDTO {
  private Long arpa_codigo;
  private Long arere_codigo;
  private Long aredr_codigo;
  private String fecha_maxima_ref;
  private String fecha_actualizacion;
  private Character arpa_estado;
  private List<PlanActividadDTO> actividades;
}
