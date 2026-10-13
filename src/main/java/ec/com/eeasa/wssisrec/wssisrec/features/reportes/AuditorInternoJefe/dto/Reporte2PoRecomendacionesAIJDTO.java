package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class Reporte2PoRecomendacionesAIJDTO {
    private String organismoControl;
    private String memorando;
    private String memoAprobacion;
    private String fechaEmision;
    private String fechaAprobacion;
    private String tituloInforme;
    private String recomendacion;
    @JsonAlias({ "arere_riesgo_verificado", "riesgo_verificado" })
    private String riesgoVerificado;
    private String evidenciaRequerida;
    private String estadoSeguimiento;
    private String responsables;
    private String auditor;

    @JsonAlias({ "progresoInforme", "progreso_informe", "progreso_informe_pct", "progreso" })
    private Double progresoInforme;
}

