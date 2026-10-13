package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos;

import java.util.List;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.EstadoSegDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionVerificableDTO {
  private Long arere_codigo;
  private String arere_descripcion;
  private Integer arere_num_rec;
  private String arere_evidencia_requerida;
  private String arere_riesgo_verificado;
  private String ainin_memorando;
  private String ainin_titulo;
  private EstadoSegDTO estado_seguimiento;
  private List<VerificableDTO> verificables;

  public RecomendacionVerificableDTO(Long arere_codigo, String arere_descripcion, List<VerificableDTO> verificables) {
    this.arere_codigo = arere_codigo;
    this.arere_descripcion = arere_descripcion;
    this.verificables = verificables;
  }

  public RecomendacionVerificableDTO(Long arere_codigo, String arere_descripcion, EstadoSegDTO estado_seguimiento, List<VerificableDTO> verificables) {
    this.arere_codigo = arere_codigo;
    this.arere_descripcion = arere_descripcion;
    this.estado_seguimiento = estado_seguimiento;
    this.verificables = verificables;
  }
}
