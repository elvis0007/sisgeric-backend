package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos;

import java.util.List;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.NuevaRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class NuevoConclusionDTO {
  private String areco_conclusion;
  private Long areha_codigo;
  private AuditoriaDTO auditoria;
  private List<NuevaRecomendacionDTO> recomendaciones;
}
