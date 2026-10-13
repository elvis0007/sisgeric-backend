package ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental.dto;

import lombok.Data;

@Data
public class ReporteRecomendacionesPorDptoBody {
    private String fecha_inicial;
    private String fecha_fin;
    private int organismo_control;
    private int estado_seguimiento;
    private String dpto;
    private String auditor;
}
