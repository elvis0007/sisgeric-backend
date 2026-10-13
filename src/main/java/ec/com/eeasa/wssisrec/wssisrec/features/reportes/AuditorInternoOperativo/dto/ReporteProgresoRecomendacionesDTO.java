package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto;

import lombok.Data;

@Data
public class ReporteProgresoRecomendacionesDTO {
    private String fecha_inicial;
    private String fecha_fin;
    private String auditor_responsable;
    private int org_control;
}
