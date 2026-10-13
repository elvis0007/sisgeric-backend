package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConclusionRecomendacionDTO {
  private Long areco_codigo;
  private String areco_conclusion;
  private String ainin_orden_trabajo;
  private String arere_fecha_inicio_control;
  private String ainin_titulo;
  private List<RecomendacionDTO> recomendaciones;
}
