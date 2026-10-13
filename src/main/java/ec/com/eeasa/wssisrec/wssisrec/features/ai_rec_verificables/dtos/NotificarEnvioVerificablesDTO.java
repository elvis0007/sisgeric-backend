package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos;

import java.util.List;
import lombok.Data;

@Data
public class NotificarEnvioVerificablesDTO {
  private Long arere_codigo;
  private String arers_codigo_per;
  private Integer cantidadVerificables;
  private List<String> nombresArchivos;
  private String descripcion;
  private String auditoria;
}
