package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos;

import java.util.List;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.RecomendacionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConclusionActDTO {
  private Long aseco_codigo;
  private String aseco_conclusion;
  private List<RecomendacionDTO> recomendaciones;
}
