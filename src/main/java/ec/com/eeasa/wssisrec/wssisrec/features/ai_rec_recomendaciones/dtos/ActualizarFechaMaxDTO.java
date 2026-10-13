package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActualizarFechaMaxDTO {
  private String arere_fecha_maxima;
  private Long aredr_codigo;
  private Long arers_codigo;
  private AuditoriaDTO auditoria;
}
