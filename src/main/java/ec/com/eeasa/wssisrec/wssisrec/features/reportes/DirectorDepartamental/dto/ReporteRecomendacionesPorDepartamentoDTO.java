package ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental.dto;

import lombok.Data;

@Data
public class ReporteRecomendacionesPorDepartamentoDTO {
    private String organismoControl;
    private String auditorInforme;
    private String memorandoAuditoria;
    private String memoAprobacion;
    private String fechaEmision;
    private String tituloInforme;
    private String conclusion;
    private String recomendacion;
    private String fechaACumplir;
    private String estadoSeguimiento;
}
