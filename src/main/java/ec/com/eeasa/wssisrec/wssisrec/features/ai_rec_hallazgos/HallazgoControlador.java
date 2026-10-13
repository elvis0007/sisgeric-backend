package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos;

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

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.dtos.ActualizarHallazgoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.dtos.NuevoHallazgoDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;

@RestController
@RequestMapping("api/hallazgos")
public class HallazgoControlador {
  @Autowired
  private HallazgoServicio hallazgoServicio;

  @GetMapping("/informe/{ainin_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorInforme(@PathVariable Long ainin_codigo,
      @RequestParam(required = false) String termino) {
    if (termino != null && !termino.trim().isEmpty()) {
      return hallazgoServicio.obtenerPorInformeYRecomendacion(ainin_codigo, termino);
    }
    return hallazgoServicio.obtenerPorInforme(ainin_codigo);
  }

  @GetMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorId(@PathVariable Long id) {
    return hallazgoServicio.obtenerPorId(id);
  }

  @PostMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> registrar(@RequestBody NuevoHallazgoDTO nuevoHallazgoDTO) {
    return hallazgoServicio.registrar(nuevoHallazgoDTO);
  }

  @PutMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizar(@RequestBody ActualizarHallazgoDTO dto,
      @PathVariable Long id) {
    return hallazgoServicio.actualizar(dto, id);
  }

  @DeleteMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(@RequestBody EliminarDTO eliminar,
      @PathVariable Long id) {
    return hallazgoServicio.eliminarLogico(eliminar, id);
  }

  @DeleteMapping("/eliminar/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(@RequestBody EliminarDTO auditoria,
      @PathVariable Long id) {
    return hallazgoServicio.eliminarFisica(auditoria, id);
  }

  @GetMapping("titulos/{areha_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerTitulos(
      @PathVariable Long areha_codigo) {
    return hallazgoServicio.obtenerTitulos(areha_codigo);
  }
}
