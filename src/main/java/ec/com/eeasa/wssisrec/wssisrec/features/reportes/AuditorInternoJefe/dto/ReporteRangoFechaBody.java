package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import lombok.Data;

@Data
public class ReporteRangoFechaBody {
    private String fecha_inicial;
    private String fecha_fin;
    private String dpto;
    private int org_control;
}
