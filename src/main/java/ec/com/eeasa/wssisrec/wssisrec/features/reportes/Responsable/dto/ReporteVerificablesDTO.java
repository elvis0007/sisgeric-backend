package ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteVerificablesDTO {
    private String tituloInforme;
    private String memorando;
    private String memoAprobacion;
    private String recomendacion;
    private String fechaInicioControl;
    private String fechaMaxima;
    private List<VerificableDTO> verificables;


    @Data
    @NoArgsConstructor
    public static class VerificableDTO {
        private String descripcion;
        private String fechaCarga;
        private String fechaValidacion;
        private String estado;
    }
}
