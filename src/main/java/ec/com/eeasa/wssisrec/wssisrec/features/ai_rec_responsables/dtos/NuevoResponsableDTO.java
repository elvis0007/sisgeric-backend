package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NuevoResponsableDTO {
  private String arers_codigo_per;
  private String arers_nombre;
  private String arers_apellido;
  private String arers_correo;
  private String arers_seccion;
  private String arers_cargo;
  private String arers_departamento;
  private String arers_departamento_abrev;
  private String arers_num_rol;
  private String arers_usuario_per;
  private AuditoriaDTO auditoria;
}
