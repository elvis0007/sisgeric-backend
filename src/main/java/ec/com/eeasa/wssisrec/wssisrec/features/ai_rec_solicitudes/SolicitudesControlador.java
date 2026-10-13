package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes;

import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.AprobarSolicitudDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.FiltroSolicitudAudDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.FiltroSolicitudDirDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.NuevoSolicitudDTO;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudesControlador {

  private static final Logger logger = LoggerFactory.getLogger(SolicitudesControlador.class);

  @Autowired
  SolicitudesServicio solicitudesServicio;

  @PostMapping(consumes = "multipart/form-data")
  public ResponseEntity<LinkedHashMap<String, Object>> registrarSolicitud(
      @ModelAttribute NuevoSolicitudDTO nuevoSolicitudDTO) {
    logger.info("[POST /api/solicitudes] Recibiendo solicitud: arere_codigo={}, arers_codigo_per={}",
        nuevoSolicitudDTO.getArere_codigo(), nuevoSolicitudDTO.getArers_codigo_per());
    ResponseEntity<LinkedHashMap<String, Object>> response = solicitudesServicio.registrarSolicitud(nuevoSolicitudDTO);
    logger.info("[POST /api/solicitudes] Respuesta: codigo={}, status={}", 
        response.getBody().get("codigo"), response.getStatusCode());
    return response;
  }

  @GetMapping("director")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerSolicitudes(
      @ModelAttribute FiltroSolicitudDirDTO filtroSolicitudDTO) {
    logger.info("[GET /api/solicitudes/director] Recibiendo filtro: departamento={}, anio={}, estado={}",
        filtroSolicitudDTO.getDepartamento(), filtroSolicitudDTO.getAnio(), filtroSolicitudDTO.getAreso_estado_aprob());
    ResponseEntity<LinkedHashMap<String, Object>> response = solicitudesServicio.obtenerDinamicoDir(filtroSolicitudDTO);
    logger.info("[GET /api/solicitudes/director] Respuesta: codigo={}, registros={}", 
        response.getBody().get("codigo"), 
        response.getBody().get("datos") != null ? ((List<?>) response.getBody().get("datos")).size() : 0);
    return response;
  }

  @GetMapping("auditor")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorAuditor(
      @ModelAttribute FiltroSolicitudAudDTO filtroSolicitudAudDTO) {
    return solicitudesServicio.obtenerDinamicoAud(filtroSolicitudAudDTO);
  }

  @GetMapping("/responsable/{arere_codigo}/{codigo_per}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorRecomendacionResposablePer(
      @PathVariable Long arere_codigo, @PathVariable String codigo_per) {
    return solicitudesServicio.obtenerPorRecomendacionResponsablePer(arere_codigo, codigo_per);
  }

  @GetMapping("/detalle/{areso_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDetalleSolicitud(
      @PathVariable Long areso_codigo) {
    return solicitudesServicio.obtenerDetalleSolicitud(areso_codigo);
  }

  @PutMapping("/aprobar/{areso_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> aprobarSolicitud(@PathVariable Long areso_codigo,
      @RequestBody AprobarSolicitudDTO aprobarSolicitudDTO) {
    return solicitudesServicio.aprobarSolicitud(aprobarSolicitudDTO, areso_codigo);
  }

  @DeleteMapping("/{areso_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarSolicitud(@PathVariable Long areso_codigo) {
    return solicitudesServicio.eliminarSolicitud(areso_codigo);
  }

  @GetMapping("/obtener-director/{codigo_per}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDirectorDepartamental(
      @PathVariable String codigo_per) {
    logger.info("[GET /api/solicitudes/obtener-director] Recibiendo: codigo_per={}", codigo_per);
    ResponseEntity<LinkedHashMap<String, Object>> response = solicitudesServicio.obtenerDirectorDepartamental(codigo_per);
    logger.info("[GET /api/solicitudes/obtener-director] Respuesta: codigo={}, status={}, datos={}", 
        response.getBody().get("codigo"), response.getStatusCode(), response.getBody().get("datos"));
    return response;
  }

  @GetMapping("/obtener-auditor/{arere_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerAuditorAsignado(
      @PathVariable Long arere_codigo) {
    logger.info("[GET /api/solicitudes/obtener-auditor] Recibiendo: arere_codigo={}", arere_codigo);
    ResponseEntity<LinkedHashMap<String, Object>> response = solicitudesServicio.obtenerAuditorAsignado(arere_codigo);
    logger.info("[GET /api/solicitudes/obtener-auditor] Respuesta: codigo={}, status={}, datos={}", 
        response.getBody().get("codigo"), response.getStatusCode(), response.getBody().get("datos"));
    return response;
  }

  @GetMapping("/obtener-director-actual")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDirectorActual() {
    logger.info("[GET /api/solicitudes/obtener-director-actual] Llamada desde usuario loguado");
    ResponseEntity<LinkedHashMap<String, Object>> response = solicitudesServicio.obtenerDirectorDelUsuarioActual();
    logger.info("[GET /api/solicitudes/obtener-director-actual] Respuesta: codigo={}, status={}, datos={}", 
        response.getBody().get("codigo"), response.getStatusCode(), response.getBody().get("datos"));
    return response;
  }
}
