package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.ActualizarVerificableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.FiltroVerificableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.NotificarEnvioVerificablesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.NuevoVerificableADTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.ValidarVerificableDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@RequestMapping("/api/verificables")
@Controller
public class VerificableControlador {

  @Autowired
  private VerificableServicio verificableServicio;

  @PostMapping(consumes = "multipart/form-data")
  public ResponseEntity<LinkedHashMap<String, Object>> registrarVerificable(
      @ModelAttribute NuevoVerificableADTO nuevoVerificableDTO) {
    
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    List<String> roles = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
    if (roles.contains("2") || roles.contains("3")) {
      return verificableServicio.registarVerificableAuditor(nuevoVerificableDTO);
    }
    if (roles.contains("4")) {
      return verificableServicio.registrarVerificableResponsable(nuevoVerificableDTO);
    }
    return EstadoRespuestas.respuestaError("Error al registrar verificable"); 
  }

  @PostMapping("/notificar-envio")
  public ResponseEntity<LinkedHashMap<String, Object>> notificarEnvio(
      @RequestBody NotificarEnvioVerificablesDTO notificarDTO) {
    return verificableServicio.notificarEnvioVerificables(notificarDTO);
  }

  @PostMapping(value = "{id}/actualizar", consumes = "multipart/form-data")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarVerificables(
      @ModelAttribute ActualizarVerificableDTO actualizarVerificableDTO,
      @PathVariable Long id) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    List<String> roles = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
    if (roles.contains("2") || roles.contains("3")) {
      return verificableServicio.actualizarVerificableAuditor(id, actualizarVerificableDTO);
    }
    if (roles.contains("4")) {
      return verificableServicio.actualizarVerificableResponsable(id, actualizarVerificableDTO);
    }
    return EstadoRespuestas.respuestaError("Error al registrar verificable");
  }

  @DeleteMapping("/eliminar/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(@RequestBody EliminarDTO eliminarDTO,
      @PathVariable Long id) {
    return verificableServicio.eliminarFisico(eliminarDTO, id);
  }

  @GetMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerVerificables(
      @ModelAttribute FiltroVerificableDTO filtroVerificableDTO) {
    try {
      return EstadoRespuestas.respuestaObtencion("Verificables obtenidos exitosamente",
          verificableServicio.obtenerVerificables(filtroVerificableDTO));
    } catch (Exception e) {
      return EstadoRespuestas.respuestaError("Error al obtener los verificables");
    }
  }

  @PutMapping("/validar/{areve_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> validarVerificable(@PathVariable Long areve_codigo,
      @RequestBody ValidarVerificableDTO validarVerificableDTO) {
    return verificableServicio.validarVerificable(areve_codigo, validarVerificableDTO);
  }

  @GetMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerVerificable(@PathVariable Long id) {
    return verificableServicio.obtenerVerificable(id);
  }
}