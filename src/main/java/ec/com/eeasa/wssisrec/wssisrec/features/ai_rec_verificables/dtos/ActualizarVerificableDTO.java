package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos;

import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ActualizarVerificableDTO {
  private String areve_descripcion;
  @Nullable
  private MultipartFile areve_adjunto;
  private Long arere_codigo;
  private String arers_codigo_per;// Codigo del responsable
  private String urlArchivo;

  // auditor
  private Long areev_codigo;
  private String areve_motivo_estado;

  private String auditoria;
}
