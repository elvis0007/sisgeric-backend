package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformeRecomendacionesDTO {
  private Long ainin_codigo;
  private String ainin_orden_trabajo;
  private String ainin_titulo;
  private List<RecomendacionDTO> recomendaciones;
}
