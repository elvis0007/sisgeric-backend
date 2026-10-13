package ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental.dto;

import lombok.Data;

@Data
public class ReporteDepartamentoFechasVencidasDTO {
    private String tituloInforme;
    private String recomendacion;
    private String enteControl;
    private String memorando;
    private String memoAprobacion;
    private String observacion;
    private String fechaEmision;
    private String fechaMaxima;
    private Integer plazo;
    private String auditor;
    private String responsables;
    private Integer arereCodigo;
}

