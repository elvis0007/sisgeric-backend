package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import lombok.Data;

@Data
public class Reporte4ConteoRecomendacionesEstadoDTO {
    private String organismoControl;
    private int totalRecomendaciones;
    private int recomendacionesEnProceso;
    private int recomendacionesCumplidas;
    private int recomendacionesNoCumplidas;
    private int recomendacionesNoAplica;
}
