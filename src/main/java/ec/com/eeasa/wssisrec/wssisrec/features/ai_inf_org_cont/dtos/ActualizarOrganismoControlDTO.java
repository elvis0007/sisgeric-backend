package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarOrganismoControlDTO {
  private Long ainoc_codigo;
  private String ainoc_descripcion;
  private AuditoriaDTO auditoria;
}
