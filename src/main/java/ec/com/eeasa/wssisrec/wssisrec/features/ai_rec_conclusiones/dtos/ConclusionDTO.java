package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos;

import lombok.Data;

@Data
public class ConclusionDTO {
  private Long areco_codigo;
  private String areco_conclusion;
  private Long totalRecomendaciones;
}
