package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto.ActualizarInformeDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto.ActualizarUsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto.InformeFiltroDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto.NuevoInformeDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;


@Controller
@RequestMapping("api/informes")
public class InformeControlador {
  @Autowired
  InformeServicio informeServicio;

  @GetMapping("/filtro-auditor-operativo")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerInformesDinamico(
      @ModelAttribute InformeFiltroDTO informeFiltroDTO) {
    return informeServicio.getInformesAuditorOperativo(informeFiltroDTO);
  }

  @GetMapping("/filtro-auditor-general")
  public ResponseEntity<LinkedHashMap<String, Object>> getInfomerAuditorGeneral(
    @ModelAttribute InformeFiltroDTO informeFiltroDTO) {
      return informeServicio.getInformesAuditorGeneral(informeFiltroDTO);
  }

  @GetMapping("/filtro-director")
  public ResponseEntity<LinkedHashMap<String, Object>> getInfomerDirector(
    @ModelAttribute InformeFiltroDTO informeFiltroDTO) {
      // Validación básica para asegurar que el departamento no sea nulo
      if (informeFiltroDTO.getDepartamento() == null || informeFiltroDTO.getDepartamento().isEmpty()) {
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("mensaje", "El departamento es requerido para filtrar");
        response.put("estado", false);
        return ResponseEntity.badRequest().body(response);
      }
      return informeServicio.getInformesDirector(informeFiltroDTO);
  }

  @GetMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerInforme(@PathVariable Long id) {
    return informeServicio.obtenerPorId(id);
  }

  @PostMapping(consumes = "multipart/form-data")
  public ResponseEntity<LinkedHashMap<String, Object>> registrarInforme(
      @ModelAttribute NuevoInformeDTO nuevoInformeDTO) {
    String auditoriaJson = nuevoInformeDTO.getAuditoria();
    return informeServicio.registrar(nuevoInformeDTO, auditoriaJson);
  }

  @PostMapping(value = "{id}/actualizar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarInforme(
      @ModelAttribute ActualizarInformeDTO actualizarInformeDTO,
      @PathVariable Long id) {
    String auditoriaJson = actualizarInformeDTO.getAuditoria();
    return informeServicio.actualizar(actualizarInformeDTO, auditoriaJson, id);
  }

  @DeleteMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarInforme(@PathVariable Long id,
      @RequestBody EliminarDTO eliminarDTO) {
    return informeServicio.eliminarLogica(eliminarDTO, id);
  }

  @DeleteMapping("eliminar/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisicoInforme(@PathVariable Long id,
      @RequestBody EliminarDTO eliminarDTO) {
    return informeServicio.eliminarFisico(eliminarDTO, id);
  }

  @PutMapping("actualizar/usuario")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarUsuario(
      @RequestBody ActualizarUsuarioDTO actualizarUsuarioDTO) {
    return informeServicio.actualizarUsuarios(actualizarUsuarioDTO);
  }
}
