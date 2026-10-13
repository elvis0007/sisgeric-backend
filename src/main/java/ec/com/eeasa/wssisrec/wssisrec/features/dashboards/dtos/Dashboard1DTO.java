package ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Autor: Mateo Díaz
 */

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class Dashboard1DTO {

    //VAR
    @JsonProperty("departamentos")
    private List<DepartamentosDTO> departamento;
    private int cantRecIngreUltimoMes;
    private int cantRecPendientesAprobacion;
    @JsonProperty("general")
    private GeneralDTO general;
    @JsonProperty("totalesGlobales")
    private TotalesGlobalesDTO totalesGlobales;

    //OTHER DTO'S
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DepartamentosDTO{
        private String departamento;
        private int totalRecomendaciones;
        private int enProceso;
        private int cumplidas;
        private int noCumplidas;
        private int noAplica;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GeneralDTO {
        private int enProceso;
        private int cumplidas;
        private int noCumplidas;
        private int noAplica;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TotalesGlobalesDTO {
        private int totalRecomendaciones;
        private int enProceso;
        private int cumplidas;
        private int noCumplidas;
        private int noAplica;
    }
}
