package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.RecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.ResponsableDTO;
import lombok.Data;

@Data
public class SolicitudDTO {
  private Long areso_codigo;
  private Long numeroSolicitud;
  private Character asero_estado_aprob;
  private String areso_tipo_cambio;
  private String areso_descripcion;
  private String areso_fecha_max_ant;
  private String areso_fecha_max_nue;
  private String areso_memorando;
  private String areso_adjunto;
  private String areso_fecha_carga;
  private ResponsableDTO responsable;
  private RecomendacionDTO recomendacion;

}
