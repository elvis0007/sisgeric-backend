package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos;

import java.util.List;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarAllConclusionDTO {
  private Long areco_codigo;
  private String areco_conclusion;
  private AuditoriaDTO auditoria;
  private List<ActualizarRecomendacionDTO> recomendaciones;
}
