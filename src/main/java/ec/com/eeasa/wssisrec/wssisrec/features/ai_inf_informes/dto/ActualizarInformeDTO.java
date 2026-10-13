package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto;

import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ActualizarInformeDTO {
  private String ainin_titulo;
  @Nullable
  private MultipartFile ainin_infAdjunto;
  private String ainin_fechaEmision;
  @Nullable
  private String ainin_fechaAprobacion;
  private String ainin_memorando;
  @Nullable
  private String ainin_memoAprobacion;
  private String ainin_fechaMemo;
  private Long ainin_otnum;
  private Long ainin_otanio;
  private Long ainti_tipoInf;
  private Long ainoc_organismoControl;
  private String auditoria;
  private String urlArchivo;
}
