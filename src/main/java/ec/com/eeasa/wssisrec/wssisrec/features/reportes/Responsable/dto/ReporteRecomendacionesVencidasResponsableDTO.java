package ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteRecomendacionesVencidasResponsableDTO {
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

