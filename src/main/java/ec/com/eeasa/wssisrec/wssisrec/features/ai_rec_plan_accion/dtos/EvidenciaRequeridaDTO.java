package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaRequeridaDTO {
  private Long arer_codigo;
  private Long arere_codigo;
  private String arer_descripcion;
  private Character arer_estado;
}
