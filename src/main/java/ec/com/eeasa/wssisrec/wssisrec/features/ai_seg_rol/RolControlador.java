package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol;

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

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.dtos.ActualizarRolDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.dtos.NuevoRolDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;

@RestController
@RequestMapping("/api/roles")
public class RolControlador {
  @Autowired
  private RolServicio rolServicio;

  @GetMapping()
  public ResponseEntity<?> obtenerRoles() {
    return rolServicio.obtener();
  }

  @PostMapping()
  public ResponseEntity<?> registrarRol(@RequestBody NuevoRolDTO nuevoRolDTO) {
    return rolServicio.registrar(nuevoRolDTO);
  }

  @PutMapping("{id}")
  public ResponseEntity<?> actualizarRol(@RequestBody ActualizarRolDTO actualizarRolDTO,
      @PathVariable Long id) {
    return rolServicio.actualizar(actualizarRolDTO, id);
  }

  @PutMapping("eliminar/{id}")
  public ResponseEntity<?> eliminarRol(@RequestBody EliminarDTO auditoria, @PathVariable Long id) {
    return rolServicio.eliminarLogico(auditoria, id);
  }

  @DeleteMapping("eliminar/fisico/{id}")
  public ResponseEntity<?> eliminarRolFisico(@RequestBody EliminarDTO auditoria, @PathVariable Long id) {
    return rolServicio.eliminarFisico(auditoria, id);
  }
}
