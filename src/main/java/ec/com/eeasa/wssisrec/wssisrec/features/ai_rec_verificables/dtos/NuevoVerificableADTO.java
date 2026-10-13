package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class NuevoVerificableADTO {
  private String areve_descripcion;
  private MultipartFile areve_adjunto;
  private Long arere_codigo;
  private String arers_codigo_per;// Codigo del responsable
  private Long areev_codigo;// Codigo estado seguimiento
  private String areve_motivo_estado; // Descripcion del estado de seguimiento.
  private String auditoria;
  private Boolean omitirCorreo;
  
  @Override
  public String toString() {
    return "NuevoVerificableADTO [" +
           "areve_descripcion=" + areve_descripcion + ", " +
           "areve_adjunto=" + (areve_adjunto != null ? areve_adjunto.getOriginalFilename() : "null") + ", " +
           "arere_codigo=" + arere_codigo + ", " +
           "arers_codigo_per=" + arers_codigo_per + ", " +
           "areev_codigo=" + areev_codigo + ", " +
           "areve_motivo_estado=" + areve_motivo_estado + ", " +
           "auditoria=" + auditoria + ", " +
           "omitirCorreo=" + omitirCorreo + "]";
  }
}
