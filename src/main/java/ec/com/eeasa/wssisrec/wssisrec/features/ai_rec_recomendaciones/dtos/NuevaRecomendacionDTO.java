package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import java.util.List;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.NuevoResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class NuevaRecomendacionDTO {
  private Long areco_codigo;
  private String arere_descripcion;
  private String arere_fecha_maxima;
  private String arere_fecha_inicio_control;
  private int arere_num_rec;
  private String arere_evidencia_requerida;
  private java.util.List<EvidenciaRequeridaDTO> arere_evidencias_requeridas;
  private String arere_riesgo_verificado;
  private List<NuevoResponsableDTO> responsables;
  private AuditoriaDTO auditoria;
}
