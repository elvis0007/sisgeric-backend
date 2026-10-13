package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarRolDTO {
  private String asero_descripcion;
  private AuditoriaDTO auditoria;
}
