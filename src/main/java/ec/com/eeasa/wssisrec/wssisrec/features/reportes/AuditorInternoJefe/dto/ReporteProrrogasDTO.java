package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteProrrogasDTO {
    
    private List<DetalleReporteProrrogaDTO> datos;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetalleReporteProrrogaDTO {
        private String numeroRecomendacion;
        private String descripcionRecomendacion;
        private String fechaMaxima;
        private String estadoRecomendacion;
        private String responsable;
        private String departamento;
        private String estadoVerificable;
        private int totalProrrogas;
        private List<ProrrogaDetalleDTO> prorrogas;
        private String memoPresidencia;
        private String fechaAprobacion;
        private String auditor;
        private String tituloInforme;
        private String organismoControl;
        private String memoAuditoria;
        private String fechaMemorando;
        private Integer diasVencidos;
        private String estadoPlazo;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProrrogaDetalleDTO {
        private String fechaAnterior;
        private String nuevaFecha;
    }
}
