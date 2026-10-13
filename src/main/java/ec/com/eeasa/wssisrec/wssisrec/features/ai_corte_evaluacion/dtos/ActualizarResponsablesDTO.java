package ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.dtos;

import java.util.List;
import lombok.Data;

@Data
public class ActualizarResponsablesDTO {
    private List<Long> responsables_incluidos_ids;
    private List<Long> responsables_excluidos_ids;
}
