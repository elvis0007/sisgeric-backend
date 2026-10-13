package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import lombok.Data;

@Data
public class ReporteHallazgosBody {
    private String fecha_inicial;
    private String fecha_fin;
    private int organismo_control;
    private String auditor;
}
