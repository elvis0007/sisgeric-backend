package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuardarCotejoDTO {
  private Long arere_codigo;
  private Long aredr_codigo;
  private Map<String, Boolean> checklist;
}
