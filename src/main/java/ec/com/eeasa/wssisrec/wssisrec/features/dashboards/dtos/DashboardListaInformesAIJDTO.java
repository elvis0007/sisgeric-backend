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
public class DashboardListaInformesAIJDTO {
    //VAR
    private List<DptoDTO> departamentos;

    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DptoDTO{
        private String departamento;
        private Map<String, detalleRecomendacionesAnioDTO> codigoInformes;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class detalleRecomendacionesAnioDTO{
        private int totalRecomendaciones;
        private int enProceso;
        private int cumplidas;
        private int noCumplidas;
        private int noAplica;
    }
}
