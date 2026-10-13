package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos.ActualizarConclusionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos.NuevoConclusionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.NuevoResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;

@RestController
@RequestMapping("api/conclusiones")
public class ConclusionControlador {
  @Autowired
  private ConclusionServicio conclusionServicio;

  @GetMapping("/hallazgo/{areha_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorHallazgo(@PathVariable Long areha_codigo,
      @RequestParam(required = false) String termino) {
    if (termino != null && !termino.trim().isEmpty()) {
      return conclusionServicio.obtenerPorHallazgoYRecomendacion(areha_codigo, termino);
    }
    return conclusionServicio.obtenerPorHallazgo(areha_codigo);
  }

  @GetMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorId(@PathVariable Long id) {
    return conclusionServicio.obtenerPorId(id);
  }

  @PutMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizar(@RequestBody ActualizarConclusionDTO dto,
      @PathVariable Long id) {
    return conclusionServicio.actualizarConclusion(dto, id);
  }

  @PutMapping("/responsables/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarResponsables(@PathVariable Long id,
      @RequestBody NuevoResponsableDTO dto) {
    return null;
  }


  @DeleteMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(@RequestBody EliminarDTO eliminarDTO,
      @PathVariable Long id) {
    return conclusionServicio.eliminarLogico(eliminarDTO, id);
  }

  @DeleteMapping("/eliminar/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(@RequestBody EliminarDTO auditoria,
      @PathVariable Long id) {
    return conclusionServicio.eliminarFisico(auditoria, id);
  }

  @PostMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> registrar(@RequestBody NuevoConclusionDTO nuevoConclusionDTO) {
    return conclusionServicio.registrarConclusion(nuevoConclusionDTO);
  }

}
