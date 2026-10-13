package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables;

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

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.dtos.ActualizarEstadoVerDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.dtos.NuevoEstadoVerDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;

@RestController
@RequestMapping("api/estado-verificable")
public class EstadoVerControlador {

  @Autowired
  private EstadoVerServicios estadoVerServicios;

  @GetMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerEstados() {
    return estadoVerServicios.obtenerEstados();
  }

  @PostMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> registrarEstado(
      @RequestBody NuevoEstadoVerDTO nuevoEstadoVerDTO) {
    return estadoVerServicios.registrarEstado(nuevoEstadoVerDTO);
  }

  @PutMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarEstado(
      @RequestBody ActualizarEstadoVerDTO actualizarEstadoVerDTO, @PathVariable Long id) {
    return estadoVerServicios.actualizarEstado(actualizarEstadoVerDTO, id);
  }

  @DeleteMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarEstado(
      @RequestBody EliminarDTO auditoriaDTO, @PathVariable Long id) {
    return estadoVerServicios.eliminarFisico(auditoriaDTO, id);
  }
}
