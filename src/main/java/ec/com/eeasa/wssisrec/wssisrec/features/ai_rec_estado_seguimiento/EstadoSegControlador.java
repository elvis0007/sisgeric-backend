package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento;

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
import org.springframework.web.bind.annotation.RestController;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.ActualizarEstadoSegDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.NuevoEstadoSegDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;

@RestController
@RequestMapping("api/estado-seguimiento")
public class EstadoSegControlador {
  @Autowired
  private EstadoSegServicio estadoSegServicio;

  @GetMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerEstadoSeguimiento() {
    return estadoSegServicio.obtener();
  }

  @PostMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> registrarEstadoSeguimiento(@RequestBody NuevoEstadoSegDTO dto) {
    return estadoSegServicio.registrar(dto);
  }

  @PutMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarEstadoSeguimiento(
      @RequestBody ActualizarEstadoSegDTO dto, @PathVariable Long id) {
    return estadoSegServicio.actualiza(dto, id);
  }

  @DeleteMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(@RequestBody EliminarDTO eliminar,
      @PathVariable("id") Long id) {
    return estadoSegServicio.eliminarLogico(eliminar, id);
  }

  @DeleteMapping("/eliminar/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(@RequestBody EliminarDTO eliminar,
      @PathVariable Long id) {
    return estadoSegServicio.eliminarFisico(eliminar, id);
  }

}
