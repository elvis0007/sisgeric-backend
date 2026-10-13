package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import lombok.Data;

@Data
public class Reporte6RecomendacionesVencidasDTO {
    private String tituloInforme;
    private String recomendacion;
    private String organismoControl;
    private String memorando;
    private String memoAprobacion;
    private String observacion;
    private String fechaEmision;
    private String fechaMaxima;
    private String auditor;
    private Integer plazo;
    private String responsables;
    private Long arereCodigo;
}
