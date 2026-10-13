package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import lombok.Data;

@Data
public class ReportePorTipoInformeDTO {
    private String tipoInforme;
    private int cantidadRecomendacionesEnProceso;
    private int cantidadRecomendacionesCumplidas;
}
