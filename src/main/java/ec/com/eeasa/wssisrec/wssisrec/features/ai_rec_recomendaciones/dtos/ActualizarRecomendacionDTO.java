package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarRecomendacionDTO {
  private Long arere_codigo;
  private String arere_descripcion;
  private String arere_fecha_maxima;
  private int arere_num_rec;
  private Long arers_codigo;
  private String arere_riesgo_verificado;
  private String arere_evidencia_requerida;
  private java.util.List<EvidenciaRequeridaDTO> arere_evidencias_requeridas;
  private AuditoriaDTO auditoria;
}
