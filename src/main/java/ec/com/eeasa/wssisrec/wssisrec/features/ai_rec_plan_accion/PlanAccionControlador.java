package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos.GuardarCotejoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos.GuardarPlanAccionDTO;

import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PlanAccionControlador {

  @Autowired
  private PlanAccionServicio planAccionServicio;

  // =========================================================================
  // 1. PLAN DE ACCIÓN Y ACTIVIDADES
  // =========================================================================

  @GetMapping(value = {"/plan-accion/recomendacion/{arereCodigo}/responsable/{aredrCodigo}", "/plan-accion/recomendacion/{arereCodigo}"})
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPlanAccion(
      @PathVariable("arereCodigo") Long arereCodigo,
      @PathVariable(value = "aredrCodigo", required = false) Long aredrCodigo) {
    return planAccionServicio.obtenerPlanAccion(arereCodigo, aredrCodigo != null ? aredrCodigo : 0L);
  }

  @PostMapping("/plan-accion")
  public ResponseEntity<LinkedHashMap<String, Object>> guardarPlanAccion(
      @RequestBody GuardarPlanAccionDTO dto) {
    return planAccionServicio.guardarPlanAccion(dto);
  }

  // =========================================================================
  // 2. EVIDENCIAS REQUERIDAS
  // =========================================================================

  @GetMapping("/evidencias-requeridas/recomendacion/{arereCodigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerEvidenciasPorRecomendacion(
      @PathVariable("arereCodigo") Long arereCodigo) {
    return planAccionServicio.obtenerEvidenciasPorRecomendacion(arereCodigo);
  }

  // =========================================================================
  // 3. COTEJO DE EVIDENCIAS REQUERIDAS (CHECKLIST DEL AUDITOR)
  // =========================================================================

  @GetMapping("/evidencias-requeridas/cotejo/recomendacion/{arereCodigo}/responsable/{aredrCodigo}")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerCotejoEvidencias(
      @PathVariable("arereCodigo") Long arereCodigo,
      @PathVariable("aredrCodigo") Long aredrCodigo) {
    return planAccionServicio.obtenerCotejoEvidencias(arereCodigo, aredrCodigo);
  }

  @PostMapping("/evidencias-requeridas/cotejo")
  public ResponseEntity<LinkedHashMap<String, Object>> guardarCotejoEvidencias(
      @RequestBody GuardarCotejoDTO dto) {
    return planAccionServicio.guardarCotejoEvidencias(dto);
  }
}
