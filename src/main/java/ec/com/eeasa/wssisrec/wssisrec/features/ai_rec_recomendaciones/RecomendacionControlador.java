package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarFechaMaxDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarRiesgoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.EliminarResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.AsignarResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.DetallePlantillaCorreoRecordatorioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.InformeFiltroDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.NuevaRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.RecomendacionFiltroDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.RecomendacionFiltroRDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.UsuarioRecordatorioNotificacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ValidarRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;


@RestController
@RequestMapping("/api/recomendaciones")
public class RecomendacionControlador {
  private static final Logger logger = LoggerFactory.getLogger(RecomendacionControlador.class);
  @Autowired
  private RecomendacionServicio recomendacionServicio;

  @GetMapping("/conclusion/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorConclusion(@PathVariable Long id) {
    try {
      return EstadoRespuestas.respuestaObtencion("Conclusión y recomendaciones obtenidos exitosamente",
          recomendacionServicio.obtenerPorConclusion(id));
    } catch (Exception e) {
      logger.error("Error al obtener conclusiones y recomendaciones: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener conclusiones y recomendaciones");
    }
  }

  @GetMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorId(@PathVariable Long id) {
    return EstadoRespuestas.respuestaObtencion("Recomendacion obtenida exitosamente",
        recomendacionServicio.obtenerPorId(id));
  }

  @GetMapping("/auditor")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDinamicoAuditor(
      @ModelAttribute RecomendacionFiltroDTO filtroDTO) {
    return recomendacionServicio.obtenerDinamicoAuditor(filtroDTO);
  }

  @GetMapping("/max-num-rec/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerNumRecMax(@PathVariable Long id) {
    return recomendacionServicio.obtenerMaxNumRec(id);
  }

  @GetMapping("/responsable")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDinamicoResponsable(
      @ModelAttribute RecomendacionFiltroRDTO filtroRDTO) {
    return recomendacionServicio.obtenerDinamicoResponsable(filtroRDTO);
  }

  @PostMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> registrarRecomendacion(@RequestBody NuevaRecomendacionDTO dto) {
    recomendacionServicio.registrarRecomendacion(dto, dto.getAreco_codigo());
    return EstadoRespuestas.respuestaCreacion("Recomendacion registrada exitosamente");
  }

  @PutMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarRecomendacion(
      @RequestBody ActualizarRecomendacionDTO dto, @PathVariable Long id) {
    return recomendacionServicio.actualizarRecomendacion(dto, id);
  }

  @PutMapping("validar/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> validarRecomendacion(@PathVariable Long id,
      @RequestBody ValidarRecomendacionDTO validarRecomendacionDTO) {
    return recomendacionServicio.validarRecomendacion(id, validarRecomendacionDTO);
  }

  @PutMapping("{id}/riesgo")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarRiesgo(@PathVariable Long id,
      @RequestBody ActualizarRiesgoDTO dto) {
    return recomendacionServicio.actualizarRiesgo(dto, id);
  }

  @PutMapping("/asignar/responsables/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarResponsables(@PathVariable Long id,
      @RequestBody AsignarResponsableDTO dto) {
    return recomendacionServicio.asignarResponsable(dto, id);
  }

  @DeleteMapping("{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(@RequestBody EliminarDTO auditoria,
      @PathVariable Long id) {
    return recomendacionServicio.eliminarLogico(auditoria, id);
  }

  @DeleteMapping("/eliminar/{id}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(@RequestBody EliminarDTO auditoria,
      @PathVariable Long id) {
    return recomendacionServicio.eliminarFisico(auditoria, id);
  }

  @GetMapping("informe")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorInforme(
      @ModelAttribute InformeFiltroDTO filtroDTO) {
    try {
      return EstadoRespuestas.respuestaObtencion("Recomendaciones obtenidas exitosamente",
          recomendacionServicio.obtenerRecomendacionesByInformes(filtroDTO));
    } catch (Exception e) {
      logger.error("Error al obtener recomendaciones por informe: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener recomendaciones por informe: " + e.getMessage());
    }
  }

  @PutMapping("/fecha-maxima/{arere_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarFechaMaxima(@PathVariable Long arere_codigo,
      @RequestBody ActualizarFechaMaxDTO actualizarFechaMaxDTO) {
    return recomendacionServicio.actualizarFechaMax(arere_codigo, actualizarFechaMaxDTO);
  }

  @GetMapping("/detalle/{arere_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDetalleByRecomendacion(@PathVariable Long arere_codigo) {
    return recomendacionServicio.obtenerDetalle(arere_codigo);
  }

  @PutMapping("/actualizar/responsable/detalle")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarResponsable(
      @RequestBody ActualizarResponsableDTO actualizarResponsableDTO) {
    return recomendacionServicio.actualizarResponsable(actualizarResponsableDTO);
  }

  @DeleteMapping("/eliminar/responsable/detalle/{arere_codigo}/{arers_codigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarResponsable(
      @RequestBody EliminarResponsableDTO eliminarResponsableDTO, @PathVariable Long arere_codigo, @PathVariable Long arers_codigo) {
    return recomendacionServicio.eliminarResponsable(eliminarResponsableDTO, arere_codigo, arers_codigo);
  }

  @GetMapping("/responsables-rec-vencidas")
  public ResponseEntity<?> obtenerCorreoUsuarioResponsables() {
      try {
          List<UsuarioRecordatorioNotificacionDTO> data = this.recomendacionServicio.obtenerCorreoUsuarioResponsablesRecomendacionesVencidas();
          
          // Mapeamos cada responsable con su detalle de recomendaciones vencidas
          List<Map<String, Object>> resultado = new ArrayList<>();
          
          for (UsuarioRecordatorioNotificacionDTO responsable : data) {
              List<DetallePlantillaCorreoRecordatorioDTO> detalle = this.recomendacionServicio.obtenerDetalleRecomendacionesVencidas(responsable.getUsuario());
  
              // Construimos un objeto con el responsable y sus detalles
              Map<String, Object> responsableConDetalles = new HashMap<>();
              responsableConDetalles.put("responsable", responsable);
              responsableConDetalles.put("detalle", detalle);
  
              resultado.add(responsableConDetalles);
          }
  
          return ResponseEntity.ok(resultado);
      } catch (Exception e) {
          logger.error("Error al obtener los datos de los responsables: {}", e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener los datos de los responsables" + e);
      }
  }
}
