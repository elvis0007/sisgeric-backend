package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecomendacionDetalleDTO {
  private Long arere_codigo;
  private String arere_descripcion;
  private List<DetalleRecomendacionDTO> detalle_recomendacion;
}
