package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos;

import java.util.Date;

import lombok.Data;

@Data
public class UsuarioRolDTO {
  private long aseru_codigo;
  private String dsgus_cuenta;
  private String codigo_per;
  private String nombre_completo;
  private String departamento;
  private Date fecha;
  private String email;
}
