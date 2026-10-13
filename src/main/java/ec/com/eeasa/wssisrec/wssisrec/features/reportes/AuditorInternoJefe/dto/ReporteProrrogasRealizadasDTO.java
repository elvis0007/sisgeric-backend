package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteProrrogasRealizadasDTO {
    
    private List<DetalleReporteProrrogaRealizadaDTO> datos;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetalleReporteProrrogaRealizadaDTO {
        private String tituloInforme;
        private Integer numeroRecomendacion;
        private String descripcionRecomendacion;
        private String estadoRecomendacion;
        private String estadoVerificable;
        private String responsable;
        private String realizadoPor;
        private String fechaCambio;
        private String fechaCambiada;
        private String fechaAnterior;
    }
}
