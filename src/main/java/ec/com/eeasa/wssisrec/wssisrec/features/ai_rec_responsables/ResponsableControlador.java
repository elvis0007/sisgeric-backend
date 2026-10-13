package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@Controller
@RequestMapping("/api/responsables")
public class ResponsableControlador {

  @Autowired
  private ResponsableServicio responsableServicio;

  @DeleteMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(@PathVariable Long id, EliminarDTO dto) {
    return responsableServicio.eliminarLogico(dto, id);
  }

  @GetMapping("/recomendacion/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorRecomendacion(@PathVariable Long id,
      @RequestHeader("X-SISGERH-TOKEN") String token_sisgerh) {
    return EstadoRespuestas.respuestaObtencion("Responsables obtenidos",
        responsableServicio.obtenerPorRecomendacionResp(id, token_sisgerh));
  }

  @GetMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerRecomendaciones() {
    return null;
  }

  @GetMapping("/departamentos")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDepartamentos() {
    return responsableServicio.obtenerDepartamentos();
  }

}
