package ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto;

import lombok.Data;

@Data
public class RecomendacionesResponsableBody {
    private String fecha_inicial;
    private String fecha_fin;
    private int estado_seguimiento;
    private String usuario;
}
