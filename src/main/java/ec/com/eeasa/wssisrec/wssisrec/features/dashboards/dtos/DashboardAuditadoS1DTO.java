package ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardAuditadoS1DTO {
    private int recomendacionesPendientes;
    private int solicitudesPendientesAprobacion;
    private int recomendacionesVencidasSinJustificar;
    private int recomendacionesVencidasJustificadas;
    private int verificablesPendientes;
}
