package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.dtos.RecomendacionDetalleDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@RequestMapping("/api/detalle-recomendacion")
@Controller
public class DetalleControlador {
  private static final Logger logger = LoggerFactory.getLogger(DetalleControlador.class);
  @Autowired
  DetalleServicio detalleServicio;

  @GetMapping("{arere_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDetalle(@PathVariable Long arere_codigo) {
    try {
      RecomendacionDetalleDTO recomendacionDetalleDTO = detalleServicio.obtenerRecomendacionDetalle(arere_codigo);
      return EstadoRespuestas.respuestaObtencion("Detalle obtenido exitosamente", recomendacionDetalleDTO);
    } catch (Exception e) {
      logger.error("Error al obtener el detalle: {}", e.getMessage());
      return null;
    }
  }

  // Este metodo es para actualizar el estado de seguimiento del detalle de la
  // recomendacion por cada responsable.
  @PutMapping("{aredr_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarEstadoSeguimiento() {
    return null;
  }
}
