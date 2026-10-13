package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteRecomendacionesFechaCorteDTO {
    private String tituloInforme;
    private String auditorInforme;
    private String organismoControl;
    private String memoAuditoria;
    private String memoAprobacion;
    private String fechaMemo;
    private String fechaAprobacion;
    private String conclusion;
    private String recomendacion;
    private String riesgoVerificado;
    private String evidenciaRequerida;
    private String estadoRecomendacion;
    private String fechaRecomendacionCumplida;
    private String fechaMaximaCumplimiento;
    private String listaResponsables;
    private String listaDepartamentos;
    private Double progresoInforme;
    private Double progresoRecomendacion;
    @com.fasterxml.jackson.annotation.JsonAlias({ "idInforme", "id_informe", "ainin_codigo", "aininCodigo" })
    private Long idInforme;
    @com.fasterxml.jackson.annotation.JsonAlias({ "numeroRecomendacion", "numero_recomendacion", "num_rec", "arere_num_rec", "arereNumRec" })
    private Integer numeroRecomendacion;
    @com.fasterxml.jackson.annotation.JsonAlias({ "arere_codigo", "arereCodigo", "idRecomendacion", "id_recomendacion" })
    private Long arere_codigo;
}
