package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.dtos.EstadoVerDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.ResponsableDTO;
import lombok.Data;

@Data
public class VerificableDTO {
  private Long areve_codigo;
  private Long aredr_codigo;
  private String aredr_fecha_maxima;
  private String areve_descripcion;
  private String areve_fecha_carga;
  private String areve_fecha_validacion;
  private String areve_adjunto;
  private String areve_motivo_estado;
  private byte[] areve_archivo;
  private EstadoVerDTO estado_seguimiento;
  private ResponsableDTO responsable;
  private String advertencia;
  private String areve_usuario;
  private String auditor_recomendacion;
  private Boolean subido_por_responsable;
  private Boolean subido_por_auditor;
  private Boolean puede_editar;
}
