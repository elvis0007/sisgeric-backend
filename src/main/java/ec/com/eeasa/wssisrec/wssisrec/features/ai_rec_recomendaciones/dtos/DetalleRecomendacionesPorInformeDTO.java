package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetalleRecomendacionesPorInformeDTO {
    //VAR
    private ResponsableDTO responsableDTO;
    private InformeDTO informeDTO;
    private List<RecomendacionesDTO> recomendacionesDTO;

    //OTHERS DTO's
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponsableDTO {
        private String nombres;
        private String apellidos;
        private String correo;
        private String usuarioPer;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InformeDTO {
        private String titulo;
        private String memoAprobacion;
        private String memorando;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecomendacionesDTO {
        private String descripcion;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate fechaMaxima;
    }
}
