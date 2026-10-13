package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import lombok.Data;

@Data
public class Reporte3RecomendacionesDirectorioDTO {
    private String codigoInforme;
    private String organismoControl;
    private String auditor;
    private String nroMemorando;
    private String fechaEmision;
    private String tituloInforme;
    private String conclusion;
    private String recomendacion;
    private String departamentos;
    private String estadoSeguimiento;
    private String fechaACumplir;
    private float progresoInforme;
}
