package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont;

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

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.dtos.ActualizarOrganismoControlDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.dtos.NuevoOrganismoControlDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;

@RestController
@RequestMapping("/api/organismos")
public class OrganismosControlControlador {
  @Autowired
  private OrganismosControlServicio organismosControlServicio;

  @GetMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerOrganismos() {
    return organismosControlServicio.obtener();
  }

  @PostMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> registrarOrganismo(
      @RequestBody NuevoOrganismoControlDTO nuevoOrganismoControlDTO) {
    return organismosControlServicio.registrar(nuevoOrganismoControlDTO);
  }

  @PutMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarOrganismo(
      @RequestBody ActualizarOrganismoControlDTO actualizarOrganismoControlDTO, @PathVariable Long id) {
    return organismosControlServicio.actualizar(actualizarOrganismoControlDTO, id);
  }

  @DeleteMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarOrganismo(
      @RequestBody AuditoriaDTO auditoriaDTO, @PathVariable Long id) {
    return organismosControlServicio.eliminar(auditoriaDTO, id);
  }

  @DeleteMapping("/eliminar/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(
      @RequestBody AuditoriaDTO auditoriaDTO, @PathVariable Long id) {
    return organismosControlServicio.eliminarFisico(auditoriaDTO, id);
  }
}
