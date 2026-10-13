package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto;

import lombok.Data;

@Data
public class ReportePorDepartamentoEstadoSeguimientoDTO {
    private Long codigoRecomendacion;
    private Long codigoInforme;
    private String memorandoAI;
    private String memorandoAprobacion;
    private String organismoControl;
    private String departamento;
    private String funcionarioResponsable;
    private String tituloInforme;
    private String conclusion;
    private String recomendacion;
    private String riesgoVerificado;
    private String evidenciaRequerida;
    private Integer numeroRecomendacion;
    private String fechaAprobacion;
    private String situacionActual;
    private String estadoSeguimiento;
    private String observacionAuditora;
    private String observacionVerificable;
    private String fechaMaximaRecomendacion;
}
