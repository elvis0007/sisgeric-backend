package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarConfiguracionDTO {
  private Long aseco_codigo;
  private String aseco_host;
  private String aseco_port;
  private String aseco_user;
  private String aseco_contrasenia;
  private String aseco_nombre_host;
  private String aseco_directorio;
  private Long aseco_size_file_mb;
  private AuditoriaDTO auditoria;
}
