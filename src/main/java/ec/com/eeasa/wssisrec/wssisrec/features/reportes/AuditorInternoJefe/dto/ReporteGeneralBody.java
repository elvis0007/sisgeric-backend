package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteGeneralBody {

    private String fecha_inicial;
    private String fecha_fin;
    /**
     * 0 = todos los organismos
     */
    private int organismo_control;
    /**
     * 0 = todos los estados
     */
    private int estado_seguimiento;
    /**
     * "0" = todos los auditores
     */
    private String auditor;
    /**
     * "0" = todos los departamentos
     */
    private String dpto;
}

