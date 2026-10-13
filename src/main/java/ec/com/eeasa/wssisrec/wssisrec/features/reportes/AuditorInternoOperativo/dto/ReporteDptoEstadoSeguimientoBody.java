package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto;

import lombok.Data;

@Data
public class ReporteDptoEstadoSeguimientoBody {
    private String fecha_inicial;
    private String fecha_fin;
    private String dpto;
    private int estado_seguimiento;
    private String fechaMaximaCumpl;
    private String auditor_responsable;
}
