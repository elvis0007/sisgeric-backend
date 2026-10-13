package ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos;

import java.util.List;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.dtos.RolDTO;
import lombok.Data;

@Data
public class UsuarioInfoDTO {
  private String dsgus_cuenta;
  private String nombre_completo;
  private String fotografia;
  private String numerol_rol;
  private String codigo_per;
  private String departamento;
  private List<RolDTO> roles;
  private String token;
  private String token_sisgerh;
}
