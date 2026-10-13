package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_historial;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recomendaciones")
public class HistorialRecomendacionControlador {

  @Autowired
  private HistorialRecomendacionServicio historialServicio;

  @GetMapping("/{id}/historial")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerHistorialPorRecomendacion(@PathVariable Long id) {
    return historialServicio.obtenerHistorialPorRecomendacion(id);
  }
}
