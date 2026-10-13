package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto;

import lombok.Data;

@Data
public class ReporteRecomendacionesVencidasOperativoDTO {
    private String tituloInforme;
    private String recomendacion;
    private String organismoControl;
    private String memorando;
    private String memoAprobacion;
    private String observacion;
    private String fechaEmision;
    private String fechaMaxima;
    private Integer plazo;
    private String responsables;
    private Long arereCodigo;
}

