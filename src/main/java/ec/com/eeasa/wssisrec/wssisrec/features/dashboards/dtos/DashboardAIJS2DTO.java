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
public class DashboardAIJS2DTO {
    //VAR
    private List<AnioDTO> anios;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnioDTO{
        private String anio;
        private Map<String, DetalleRecomendacionesAnioDTO> departamentos;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetalleRecomendacionesAnioDTO{
        private int totalRecomendaciones;
        private int enProceso;
        private int cumplidas;
        private int noCumplidas;
        private int noAplica;
        private String departamentoAbrev;
    }
}
