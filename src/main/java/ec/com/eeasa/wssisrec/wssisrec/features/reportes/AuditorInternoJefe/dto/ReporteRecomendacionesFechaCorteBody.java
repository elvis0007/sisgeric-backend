package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;

import lombok.Data;

@Data
public class ReporteRecomendacionesFechaCorteBody {
    private String fecha_corte;
    private String fecha_corte_emision;
    private String fecha_corte_estado;
    private String dpto;
    private int org_control;

    public String getFecha_corte_emision() {
        if (fecha_corte_emision != null && !fecha_corte_emision.trim().isEmpty()) {
            return fecha_corte_emision;
        }
        return fecha_corte;
    }

    public String getFecha_corte_estado() {
        if (fecha_corte_estado != null && !fecha_corte_estado.trim().isEmpty()) {
            return fecha_corte_estado;
        }
        return fecha_corte;
    }
}
