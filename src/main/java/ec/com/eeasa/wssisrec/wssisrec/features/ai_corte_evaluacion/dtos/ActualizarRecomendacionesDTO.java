package ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.dtos;

import java.util.List;
import lombok.Data;

@Data
public class ActualizarRecomendacionesDTO {
    private List<Long> recomendaciones_incluidas_ids;
    private List<Long> recomendaciones_excluidas_ids;
}
