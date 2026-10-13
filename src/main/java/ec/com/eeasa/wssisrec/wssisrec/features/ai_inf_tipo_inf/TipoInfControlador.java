package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf;

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

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.dto.ActualizarTipoInfDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.dto.NuevoTipoInfDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;

@RestController
@RequestMapping("api/tipoInf")
public class TipoInfControlador {
  @Autowired
  private TipoInfServicio tipoInfServicio;

  @GetMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> obtener() {
    return tipoInfServicio.obtener();
  }

  @PostMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> registrar(
      @RequestBody NuevoTipoInfDTO nuevoTipoInfDTO) {
    return tipoInfServicio.registrar(nuevoTipoInfDTO);
  }

  @PutMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizar(
      @RequestBody ActualizarTipoInfDTO actualizarTipoInfDTO, @PathVariable Long id) {
    return tipoInfServicio.actualizar(actualizarTipoInfDTO, id);
  }

  @DeleteMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(@RequestBody EliminarDTO eliminarDTO,
      @PathVariable Long id) {
    return tipoInfServicio.eliminarLogico(eliminarDTO, id);
  }

  @DeleteMapping("/eliminar/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(@RequestBody EliminarDTO eliminarDTO,
      @PathVariable Long id) {
    return tipoInfServicio.eliminarFisica(eliminarDTO, id);

  }
}
