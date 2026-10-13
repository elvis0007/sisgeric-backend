package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.NuevoResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarResponsableDTO {
  private Long arere_codigo; // CODIGO DE LA RECOMENDACION
  private Long arers_codigo; // CODIGO DEL RESPONSABLE A REEMPLAZAR
  private NuevoResponsableDTO responsable; // OBJETO DEL NUEVO RESPONSABLE
  private AuditoriaDTO auditoria; // AUDITORIA
}
