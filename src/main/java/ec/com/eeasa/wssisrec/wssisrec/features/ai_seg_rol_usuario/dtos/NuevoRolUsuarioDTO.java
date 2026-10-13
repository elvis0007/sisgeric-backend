package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class NuevoRolUsuarioDTO {
  private String codigo_per;
  private Long rol;
  private String email;
  private String aseru_cuenta;
  private AuditoriaDTO auditoria;
}
