package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos;

import lombok.Data;

@Data
public class FiltroVerificableDTO {
  private Long arere_codigo;
  private String arers_codigo_per; // Si el codigo_per es 0 entonces se obtienes todos los verificables,
}
