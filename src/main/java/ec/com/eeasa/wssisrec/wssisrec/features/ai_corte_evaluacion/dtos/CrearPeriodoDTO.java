package ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.dtos;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class CrearPeriodoDTO {
    private String arcev_nombre;
    private String arcev_tipo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date arcev_fecha_inicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date arcev_fecha_fin;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date arcev_fecha_corte;

    private Integer arcev_anio;
    private List<Long> informes_ids;
}
