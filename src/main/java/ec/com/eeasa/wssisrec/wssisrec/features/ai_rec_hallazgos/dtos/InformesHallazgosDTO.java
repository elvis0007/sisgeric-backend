package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InformesHallazgosDTO {
  private Long ainin_codigo;
  private String ainin_titulo;
  private String ainin_orden_trabajo;
  private List<HallazgoDTO> hallazgos;
}
