package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import java.util.List;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.NuevoResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class AsignarResponsableDTO {
  private AuditoriaDTO auditoria;
  private List<NuevoResponsableDTO> responsables;
}
