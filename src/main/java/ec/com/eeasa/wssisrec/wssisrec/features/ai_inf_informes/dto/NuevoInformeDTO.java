package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NuevoInformeDTO {
  private String ainin_titulo;
  private MultipartFile ainin_infAdjunto;
  private String ainin_fechaEmision;
  private String ainin_fechaAprobacion;
  private String ainin_memoAprobacion;
  private String ainin_memorando;
  private String ainin_fechaMemo;
  private Long ainin_otnum;
  private Long ainin_otanio;
  private Long ainti_tipoInf;
  private Long ainoc_organismoControl;
  private String auditoria;
}
