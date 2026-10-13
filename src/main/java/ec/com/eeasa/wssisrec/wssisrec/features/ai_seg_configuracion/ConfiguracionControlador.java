package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.dtos.ActualizarConfiguracionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.dtos.NuevaConfiguracionDTO;

@RestController
@RequestMapping("api/configuracion")
public class ConfiguracionControlador {
  @Autowired
  private ConfiguracionServicio configuracionServicio;

  @GetMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerConfiguracion() {
    return configuracionServicio.obtener();
  }

  @GetMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerConfiguracion(@PathVariable Long id) {
    return configuracionServicio.obtenerPorId(id);
  }

  @PostMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> guardar(
      @RequestBody NuevaConfiguracionDTO nuevaConfiguracionDTO) {
    return configuracionServicio.registrar(nuevaConfiguracionDTO);
  }

  @PutMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizar(@RequestBody ActualizarConfiguracionDTO actualizarDto,
      @PathVariable Long id) {
    return configuracionServicio.actualizar(actualizarDto, id);
  }
}
