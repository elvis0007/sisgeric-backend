package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reporte1AIJDTO {
    private String departamento;
    @JsonProperty("memorando_nro")
    private String memorandoNro;
    @JsonProperty("organismo_control")
    private String organismoControl;
    private String informe;
    private List<ResponsableDTO> responsables;
    private List<ConclusionDTO> conclusiones;
    private List<RecomendacionDTO> recomendaciones;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponsableDTO {
        @JsonProperty("nombres_apellidos")
        private String nombresApellidos;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConclusionDTO{
        private String texto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecomendacionDTO{
        private String texto;
        @JsonProperty("fecha_a_cumplir")
        private LocalDate fechaACumplir;
    }

}
