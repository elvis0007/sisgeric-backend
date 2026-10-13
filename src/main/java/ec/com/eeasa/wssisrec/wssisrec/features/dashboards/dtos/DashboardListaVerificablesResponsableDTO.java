package ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardListaVerificablesResponsableDTO {
    //VAR
    private List<AniosDTO> anios;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AniosDTO{
        private String anio;
        private Map<String, detalleVerificablesDTO> detalleVerificables;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class detalleVerificablesDTO{
        private int totalVerificables;
        private int justificados;
        private int noJustificados;
        private int pendientes;
    }
}
