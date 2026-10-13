package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteGeneralAIJDTO {

    private Long codigoInforme;
    private String memoAuditoria;
    private String fechaMemorando;
    private String memoPresidencia;
    private String fechaAprobacion;
    private String auditor;
    private String fechaCreacionInforme;
    private String tituloInforme;
    private String organismoControl;
    private String hallazgo;
    private String conclusion;
    private Long codigoRecomendacion;
    private Integer numeroRecomendacion;
    private String recomendacion;
    private String fechaVencimiento;
    private String estadoRecomendacion;
    private String estadoPlazo;
    private Integer diasVencidos;
    private Double progresoRecomendacion;
    private String fechaCargaVerificable;
    private String responsable;
    private String estadoVerificable;
    private String departamento;
    private String descripcionVerificableResponsable;
    private String observacionAuditor;
    private Double progresoInforme;
    private String fechaConsulta;
    private String riesgo;
    private String evidenciaRequerida;
    private String tienePlanAccion;
    private Integer cantidadActividadesPlan;
    private String fechaValidacionVerificable;
    private Long aredrCodigo;
    private Long arersCodigo;
    private String arersCodigoPer;
}

