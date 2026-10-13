package ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos;

import lombok.Data;

@Data
public class UsuarioDTO {
  private String dsgus_cuenta;
  private String nombre;
  private String apellido;
  private String numero_rol;
  private String cargo;
  private String departamento;
  private String fotografia;
  private String codigo;
  private String email;
}
