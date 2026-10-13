package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.ActualizarRolUsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.NuevoRolUsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@RestController
@RequestMapping("/api/roles/usuario")
public class RolUsuarioControlador {
  @Autowired
  private RolUsuarioServicio rolUsuarioServicio;

  // Endpoints específicos primero para evitar conflictos de rutas
  @GetMapping("/usuario/{aseru_codigo:[0-9]+}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorId(@PathVariable Long aseru_codigo) {
    return rolUsuarioServicio.obtenerPorId(aseru_codigo);
  }

  @GetMapping("/responsables/director/{aseru_codigo_per}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerResponsables() {
    return rolUsuarioServicio.obtenerResponsables();
  }

  @GetMapping("/director")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerInfoDirector(@RequestBody String correo) {
      return EstadoRespuestas.respuestaObtencion("Director departamental obtenido correctamente.", this.rolUsuarioServicio.obtenerInfoDirector(correo));
  }

  @GetMapping("/verificar-rol")
  public ResponseEntity<LinkedHashMap<String, Object>> verificarTieneRol(
      @RequestParam String dsgus_cuenta, 
      @RequestParam String aseru_num_rol) {
    return rolUsuarioServicio.verificarTieneRol(dsgus_cuenta, aseru_num_rol);
  }

  // Endpoint genérico al final (solo acepta números para evitar conflictos)
  @GetMapping({"/rol/{id:[0-9]+}", "{id:[0-9]+}"})
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorRol(@PathVariable Long id,
      @RequestHeader("X-SISGERH-TOKEN") String token_sisgerh) {
    return rolUsuarioServicio.obtenerPorRol(id, token_sisgerh);
  }

  @PostMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> registrar(@RequestBody NuevoRolUsuarioDTO nuevoRolUsuarioDTO,
      @RequestHeader("X-SISGERH-TOKEN") String token_sisgerh) {
    return rolUsuarioServicio.registrar(nuevoRolUsuarioDTO, token_sisgerh);
  }

  @PutMapping("{aseru_codigo:[0-9]+}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizar(@PathVariable Long aseru_codigo,
      @RequestBody ActualizarRolUsuarioDTO actualizarRolUsuarioDTO,
      @RequestHeader("X-SISGERH-TOKEN") String token_sisgerh) {
    return rolUsuarioServicio.actualizarUsuario(aseru_codigo, actualizarRolUsuarioDTO, token_sisgerh);
  }

  @DeleteMapping("eliminar/{id:[0-9]+}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminar(@RequestBody EliminarDTO eliminarDTO,
      @PathVariable Long id) {
    return rolUsuarioServicio.eliminar(id, eliminarDTO);
  }
}
