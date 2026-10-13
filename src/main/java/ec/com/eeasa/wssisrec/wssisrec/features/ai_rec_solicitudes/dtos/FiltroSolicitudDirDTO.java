package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos;

import lombok.Data;

@Data
public class FiltroSolicitudDirDTO {
  private String departamento;
  private String arers_codigo_per; // NO para auditor
  private Integer anio;
  private String areso_estado_aprob;
}
