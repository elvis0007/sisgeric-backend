package ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardAIJS3DTO {
    private List<AnioDTO> anios;

    @Data
    @AllArgsConstructor
    public static class AnioDTO{
        private String anio;
        private AnioDataDTO detalles;
    }

    @Data
    @AllArgsConstructor
    public static class AnioDataDTO{
        private int totalRecomendaciones;
        private int enProceso;
        private int cumplidas;
        private int noCumplidas;
        private int noAplica;
    }
}
