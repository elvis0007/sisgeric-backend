package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import lombok.Data;

@Data
public class ReporteHallazgos {

    private String tituloInforme;
    private String organismoControl;
    private String memorando;
    private String memoAprobacion;
    private String hallazgo;
    private String auditor;
}
