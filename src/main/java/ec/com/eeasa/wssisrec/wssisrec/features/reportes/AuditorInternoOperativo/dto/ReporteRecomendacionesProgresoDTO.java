package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteRecomendacionesProgresoDTO {
    private String codigoInforme;
    private String tituloInforme;
    private String memorandoAI;
    private String memorandoPresidencia;
    private String organismoControl;
    private String departamentos;
    private String auditorInforme;
    private int totalAsignaciones;
    private int totalRecomendaciones;
    private int recomendacionesEnProceso;
    private int recomendacionesCumplidas;
    private int recomendacionesNoCumplidas;
    private int recomendacionesNoAplica;
    private float progreso;
}
