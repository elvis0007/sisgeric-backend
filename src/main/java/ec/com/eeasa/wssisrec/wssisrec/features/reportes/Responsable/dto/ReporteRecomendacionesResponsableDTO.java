package ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReporteRecomendacionesResponsableDTO {
    private String tituloInforme;
    private String organismoControl;
    private String memorando;
    private String memoAprobacion;
    private String conclusion;
    private String recomendacion;
    private String fechaInicioControl;
    private String fechaMaxima;
    private String estadoSeguimiento;
    private String pendienteCumplimientoDepartamentos;
    private String pendienteCumplimientoResponsables;
    private Long codigoRecomendacion;
}
