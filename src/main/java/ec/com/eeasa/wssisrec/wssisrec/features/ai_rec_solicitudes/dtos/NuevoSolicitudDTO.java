package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class NuevoSolicitudDTO {
  private Long arere_codigo;
  private String arers_codigo_per;
  private String areso_tipo_cambio;
  private String areso_descripcion;
  private String areso_fecha_max_ant;
  private String areso_fecha_max_nue;
  private String areso_memorando;
  private MultipartFile areso_adjunto;
  private String auditoria;
}
