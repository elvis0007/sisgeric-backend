package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarRolUsuarioDTO {
  private String codigo_per;  // Código de personal del nuevo usuario
  private Long rol;
  private AuditoriaDTO auditoria;
}
