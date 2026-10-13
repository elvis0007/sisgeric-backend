package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.EstadoSegDTO;
import lombok.Data;

@Data
public class RecomendacionDTO {
  private float arere_progreso;
  private Long arere_codigo;
  private int arere_num_rec;
  private String arere_descripcion;
  private String arere_fecha_inicio_control;
  private String arere_fecha_maxima;
  private Integer arere_num_prorroga;
  private int plazo;
  private EstadoSegDTO estado_seguimiento;
  private String motivo_estado;
  private int responsables;
  private String estado_solicitud;
  private String estado_verificable;
  private EstadoSegDTO estado_general;
  private EstadoSegDTO estado_individual;
  private String fecha_maxima_general;
  private String fecha_maxima_individual;
  private int plazo_individual;
  private int total_responsables;
  private int responsables_cumplidos;
  private int responsables_vencidos;
  private String arere_evidencia_requerida;
  private java.util.List<EvidenciaRequeridaDTO> arere_evidencias_requeridas;
  private String arere_riesgo_verificado;
  private String ainin_memorando;
  private String ainin_titulo;
  private Long aredr_codigo;
  private Long arers_codigo;
  private String arers_codigo_per;
}
