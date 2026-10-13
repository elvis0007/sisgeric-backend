package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RolesUsuariosDTO {
  private Long asero_codigo;
  private String asero_descripcion;
  private List<UsuarioRolDTO> usuarios;
}
