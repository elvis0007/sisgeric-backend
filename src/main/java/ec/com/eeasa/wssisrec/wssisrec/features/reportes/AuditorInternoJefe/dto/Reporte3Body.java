package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import lombok.Data;

@Data
public class Reporte3Body {
    private String fecha_inicial;
    private String fecha_fin;
    private String dpto;
    private int org_control;
    private int estado_seguimiento;
    private String auditor;
}
