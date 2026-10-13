package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HallazgoConclusionDTO {
  private Long areha_codigo;
  private String areha_titulo;
  private String ainin_orden_trabajo;
  private String ainin_titulo;
  private List<ConclusionDTO> conclusiones;

}
