package ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteProgresoRecomendacionesResponsableDTO {
    private int codigoInforme;
    private String tituloInforme;
    private String memorando;
    private String memoAprobacion;
    private String organismoControl;
    private String codigoAuditor;
    private int conteoRecomendaciones;
    private int recomendacionesEnProceso;
    private int recomendacionesCumplidas;
    private int recomendacionesNoCumplidas;
    private int recomendacionesNoAplica;
    private double progreso;
    private String departamentosResponsablesPendientesEnProceso;
    private String departamentosResponsablesNoCumplida;
}
