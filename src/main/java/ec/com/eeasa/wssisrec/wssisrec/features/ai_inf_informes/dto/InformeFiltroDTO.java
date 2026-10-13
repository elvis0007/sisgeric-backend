package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class InformeFiltroDTO {
  private Long ainoc_organismo_control;
  private Long ainti_tipo_inf;
  private String ainin_fecha_inicio;
  private String ainin_fecha_fin;
  private String ainin_usuario;
  private String departamento;
}
