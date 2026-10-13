package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import java.util.List;
import lombok.Data;

@Data
public class Reporte7AuditoresDTO {
    private String tituloInforme;
    private String recomendacion;
    private String enteControl;
    private String memorando;
    private String memoAprobacion;
    private String fechaEmision;
    private String fechaMaxima;
    private String auditor;
    private List<ResponsableReporte7DTO> responsables;
    private Long arereCodigo;
}

