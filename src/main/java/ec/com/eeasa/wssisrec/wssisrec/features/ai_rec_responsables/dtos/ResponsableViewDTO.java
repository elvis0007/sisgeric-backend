package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponsableViewDTO {
  private Long aredr_codigo;
  private Long arers_codigo;
  private String nombre_completo;
  private String arers_correo;
  private String arers_seccion;
  private String arers_cargo;
  private String arers_departamento;
  private String arers_departamento_abrev;
  private String arers_codigo_per;
  private String arers_num_rol;
  private String arers_usuario;
  private String fotografia;
  private Character arers_estado;
  private String arers_fecha;
  private String arers_fecha_reempl;
  private String arers_reemp_por;
  private String areev_descripcion;
  private String observacion;
  private String aredr_fecha_maxima;
  private Integer plazo;
  private String estado_plazo;
}
