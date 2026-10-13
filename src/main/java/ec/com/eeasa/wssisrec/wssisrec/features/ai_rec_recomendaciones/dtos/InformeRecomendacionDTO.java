package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformeRecomendacionDTO {
  private Long ainin_codigo;
  private String ainin_orden_trabajo;
  private String ainin_titulo;
  private String ainoc_descripcion;
  private RecomendacionDTO recomendaciones;
  private String ainin_memorando;
  private String ainin_memo_aprobacion;
  private String ainin_usuario;
  private String lista_responsables;
  private Double ainin_progreso;
}
