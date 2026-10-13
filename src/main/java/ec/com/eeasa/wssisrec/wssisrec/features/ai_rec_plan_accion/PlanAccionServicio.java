package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.DetalleRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos.EvidenciaCotejoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos.EvidenciaRequeridaDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos.GuardarCotejoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos.GuardarPlanAccionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos.PlanAccionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.dtos.PlanActividadDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.models.EvidenciaCotejo;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.models.EvidenciaRequerida;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.models.PlanAccion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.models.PlanActividad;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.repositorios.EvidenciaCotejoRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.repositorios.EvidenciaRequeridaRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.repositorios.PlanAccionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.repositorios.PlanActividadRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.RecomendacionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@Service
public class PlanAccionServicio {

  private static final Logger logger = LoggerFactory.getLogger(PlanAccionServicio.class);
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  @Autowired
  private PlanAccionRepositorio planAccionRepositorio;

  @Autowired
  private PlanActividadRepositorio planActividadRepositorio;

  @Autowired
  private EvidenciaRequeridaRepositorio evidenciaRequeridaRepositorio;

  @Autowired
  private EvidenciaCotejoRepositorio evidenciaCotejoRepositorio;

  @Autowired
  private RecomendacionRepositorio recomendacionRepositorio;

  @Autowired
  private DetalleRepositorio detalleRepositorio;

  // =========================================================================
  // 1. EVIDENCIAS REQUERIDAS
  // =========================================================================

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerEvidenciasPorRecomendacion(Long arereCodigo) {
    try {
      Optional<Recomendacion> optRec = recomendacionRepositorio.findById(arereCodigo);
      if (!optRec.isPresent()) {
        return EstadoRespuestas.noEncontrado("Recomendación no encontrada con código: " + arereCodigo);
      }
      Recomendacion recomendacion = optRec.get();

      List<EvidenciaRequerida> lista = evidenciaRequeridaRepositorio.findByRecomendacion(arereCodigo);

      // Si no existen filas, o si solo hay 1 fila pero contiene un texto largo concatenado con ';' o múltiples oraciones:
      if (lista.size() == 1) {
        String desc = lista.get(0).getArer_descripcion();
        if (desc != null && (desc.contains(";") || desc.length() > 80)) {
          try {
            evidenciaRequeridaRepositorio.delete(lista.get(0));
            lista = new ArrayList<>();
          } catch (Exception ex) {
            logger.warn("No se pudo eliminar fila única de evidencia requerida para reparseo: {}", ex.getMessage());
          }
        }
      }

      // Si no existen filas pero la recomendación tiene texto en arere_evidencia_requerida,
      // sincronizamos/migramos automáticamente las evidencias separadas a la tabla de forma segura sin duplicar
      if (lista.isEmpty() && recomendacion.getArere_evidencia_requerida() != null
          && !recomendacion.getArere_evidencia_requerida().trim().isEmpty()) {
        List<String> parsed = parseEvidenciasTexto(recomendacion.getArere_evidencia_requerida());
        for (String desc : parsed) {
          if (!desc.trim().isEmpty()) {
            String safeDesc = desc.trim();
            if (safeDesc.length() > 500) {
              safeDesc = safeDesc.substring(0, 500);
            }
            List<EvidenciaRequerida> yaExiste = evidenciaRequeridaRepositorio.findByRecomendacionAndDescripcion(arereCodigo, safeDesc);
            if (yaExiste == null || yaExiste.isEmpty()) {
              EvidenciaRequerida er = new EvidenciaRequerida();
              er.setRecomendacion(recomendacion);
              er.setArer_descripcion(safeDesc);
              er.setArer_estado('A');
              evidenciaRequeridaRepositorio.save(er);
            }
          }
        }
        evidenciaRequeridaRepositorio.flush();
        lista = evidenciaRequeridaRepositorio.findByRecomendacion(arereCodigo);
      }

      // Asegurar que la respuesta nunca contenga duplicados (por descripción)
      Map<String, EvidenciaRequerida> deduplicadas = new LinkedHashMap<>();
      for (EvidenciaRequerida er : lista) {
        if (er.getArer_descripcion() != null) {
          String key = er.getArer_descripcion().trim().toLowerCase();
          deduplicadas.putIfAbsent(key, er);
        }
      }

      List<EvidenciaRequeridaDTO> dtos = deduplicadas.values().stream()
          .map(e -> new EvidenciaRequeridaDTO(e.getArer_codigo(), arereCodigo, e.getArer_descripcion(), e.getArer_estado()))
          .collect(Collectors.toList());

      return EstadoRespuestas.respuestaObtencion("Evidencias requeridas obtenidas exitosamente", dtos);
    } catch (Exception e) {
      logger.error("Error al obtener evidencias requeridas: {}", e.getMessage(), e);
      return EstadoRespuestas.respuestaError("Error al obtener evidencias requeridas: " + e.getMessage());
    }
  }

  private DetalleRecomendacion resolverDetalleRecomendacion(Long arereCodigo, Long idRespODet) {
    return resolverDetalleRecomendacion(arereCodigo, idRespODet, null, null);
  }

  private DetalleRecomendacion resolverDetalleRecomendacion(Long arereCodigo, Long idAredr, Long idArers, String arersCodigoPer) {
    if (idAredr != null) {
      Optional<DetalleRecomendacion> optDet = detalleRepositorio.findById(idAredr);
      if (optDet.isPresent()) {
        return optDet.get();
      }
      if (arereCodigo != null) {
        DetalleRecomendacion det = detalleRepositorio.findByRecomendacionResponsable(arereCodigo, idAredr);
        if (det != null) return det;
      }
    }
    if (idArers != null && arereCodigo != null) {
      DetalleRecomendacion det = detalleRepositorio.findByRecomendacionResponsable(arereCodigo, idArers);
      if (det != null) return det;
    }
    if (arersCodigoPer != null && !arersCodigoPer.trim().isEmpty() && arereCodigo != null) {
      DetalleRecomendacion det = detalleRepositorio.findByRecomendacionResponsablePer(arereCodigo, arersCodigoPer.trim());
      if (det != null) return det;
    }
    if (arereCodigo != null) {
      List<DetalleRecomendacion> detalles = detalleRepositorio.findByRecomendacion(arereCodigo);
      if (detalles != null && !detalles.isEmpty()) {
        if (idAredr != null) {
          for (DetalleRecomendacion d : detalles) {
            if (idAredr.equals(d.getAredr_codigo())) return d;
            if (d.getResponsable() != null && idAredr.equals(d.getResponsable().getArers_codigo())) return d;
          }
        }
        if (detalles.size() == 1) {
          return detalles.get(0);
        }
      }
    }
    return null;
  }

  // =========================================================================
  // 2. PLAN DE ACCIÓN Y ACTIVIDADES (POR RESPONSABLE)
  // =========================================================================

  @Transactional(readOnly = true)
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPlanAccion(Long arereCodigo, Long aredrCodigo) {
    try {
      DetalleRecomendacion detalle = resolverDetalleRecomendacion(arereCodigo, aredrCodigo);
      Long aredrReal = detalle != null ? detalle.getAredr_codigo() : aredrCodigo;

      Optional<PlanAccion> optPlan = planAccionRepositorio.findByRecomendacionAndDetalle(arereCodigo, aredrReal);

      if (!optPlan.isPresent()) {
        // Retornar estructura inicial vacía sin error
        PlanAccionDTO dtoVacio = new PlanAccionDTO();
        dtoVacio.setArere_codigo(arereCodigo);
        dtoVacio.setAredr_codigo(aredrReal);
        dtoVacio.setActividades(new ArrayList<>());
        return EstadoRespuestas.respuestaObtencion("No existe plan de acción registrado aún", dtoVacio);
      }

      PlanAccion plan = optPlan.get();
      List<PlanActividad> actividades = planActividadRepositorio.findByPlanAccion(plan.getArpa_codigo());

      List<PlanActividadDTO> actDTOs = actividades.stream().map(a -> {
        PlanActividadDTO dto = new PlanActividadDTO();
        dto.setArpac_codigo(a.getArpac_codigo());
        dto.setArpa_codigo(plan.getArpa_codigo());
        dto.setArer_codigo(a.getEvidenciaRequerida() != null ? a.getEvidenciaRequerida().getArer_codigo() : null);
        dto.setNum_actividad(a.getArpac_num_actividad());
        dto.setActividad(a.getArpac_actividad());
        dto.setFecha_ejecucion(a.getArpac_fecha_ejecucion() != null ? dateFormat.format(a.getArpac_fecha_ejecucion()) : "");
        dto.setEntregable_esperado(a.getArpac_entregable_esperado());
        dto.setArpac_estado(a.getArpac_estado());
        return dto;
      }).collect(Collectors.toList());

      PlanAccionDTO planDTO = new PlanAccionDTO();
      planDTO.setArpa_codigo(plan.getArpa_codigo());
      planDTO.setArere_codigo(arereCodigo);
      planDTO.setAredr_codigo(aredrReal);
      planDTO.setFecha_maxima_ref(plan.getArpa_fecha_maxima_ref() != null ? dateFormat.format(plan.getArpa_fecha_maxima_ref()) : "");
      planDTO.setFecha_actualizacion(plan.getArpa_fecha_actualizacion() != null ? dateFormat.format(plan.getArpa_fecha_actualizacion()) : "");
      planDTO.setArpa_estado(plan.getArpa_estado());
      planDTO.setActividades(actDTOs);

      return EstadoRespuestas.respuestaObtencion("Plan de acción obtenido exitosamente", planDTO);
    } catch (Exception e) {
      logger.error("Error al obtener plan de acción: {}", e.getMessage(), e);
      return EstadoRespuestas.respuestaError("Error al obtener plan de acción: " + e.getMessage());
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> guardarPlanAccion(GuardarPlanAccionDTO dto) {
    try {
      if (dto.getArere_codigo() == null) {
        return EstadoRespuestas.respuestaError("El código de recomendación es obligatorio");
      }

      Optional<Recomendacion> optRec = recomendacionRepositorio.findById(dto.getArere_codigo());
      if (!optRec.isPresent()) {
        return EstadoRespuestas.noEncontrado("Recomendación no encontrada: " + dto.getArere_codigo());
      }

      DetalleRecomendacion detalle = resolverDetalleRecomendacion(dto.getArere_codigo(), dto.getAredr_codigo(), dto.getArers_codigo(), dto.getArers_codigo_per());
      if (detalle == null) {
        return EstadoRespuestas.noEncontrado("Responsable/Detalle de recomendación no encontrado para la recomendación: " + dto.getArere_codigo());
      }

      Recomendacion recomendacion = optRec.get();

      // Buscar si ya existe cabecera de plan de acción o crear una nueva
      Optional<PlanAccion> optPlan = planAccionRepositorio.findByRecomendacionAndDetalle(dto.getArere_codigo(), detalle.getAredr_codigo());
      PlanAccion plan = optPlan.orElseGet(PlanAccion::new);

      plan.setRecomendacion(recomendacion);
      plan.setDetalleRecomendacion(detalle);
      plan.setArpa_fecha_actualizacion(new Date());
      plan.setArpa_estado('A');

      Date fechaMaxRef = detalle.getAredr_fecha_maxima() != null ? detalle.getAredr_fecha_maxima() : recomendacion.getArere_fecha_maxima();
      if (dto.getFecha_maxima_ref() != null && !dto.getFecha_maxima_ref().trim().isEmpty()) {
        try {
          fechaMaxRef = dateFormat.parse(dto.getFecha_maxima_ref().trim());
        } catch (Exception ex) {
          logger.warn("No se pudo parsear fecha_maxima_ref: {}", dto.getFecha_maxima_ref());
        }
      }
      plan.setArpa_fecha_maxima_ref(fechaMaxRef != null ? fechaMaxRef : new Date());

      PlanAccion planGuardado = planAccionRepositorio.save(plan);

      // Desactivar actividades anteriores si existían
      if (optPlan.isPresent()) {
        planActividadRepositorio.desactivarActividadesPorPlan(planGuardado.getArpa_codigo());
        planActividadRepositorio.flush();
      }

      // Pre-cargar evidencias requeridas para asociar si el entregable coincide
      List<EvidenciaRequerida> evidencias = new ArrayList<>();
      try {
        evidencias = evidenciaRequeridaRepositorio.findByRecomendacion(dto.getArere_codigo());
      } catch (Exception ex) {
        logger.warn("No se pudieron consultar evidencias requeridas: {}", ex.getMessage());
      }

      // Insertar las nuevas actividades
      if (dto.getActividades() != null && !dto.getActividades().isEmpty()) {
        for (PlanActividadDTO actDto : dto.getActividades()) {
          PlanActividad act = new PlanActividad();
          act.setPlanAccion(planGuardado);
          act.setArpac_num_actividad(actDto.getNum_actividad() != null ? actDto.getNum_actividad() : 1);
          act.setArpac_actividad(actDto.getActividad() != null ? actDto.getActividad() : "");
          act.setArpac_entregable_esperado(actDto.getEntregable_esperado() != null ? actDto.getEntregable_esperado() : "");
          act.setArpac_estado('A');

          Date fechaEjecucion = new Date();
          if (actDto.getFecha_ejecucion() != null && !actDto.getFecha_ejecucion().trim().isEmpty()) {
            try {
              fechaEjecucion = dateFormat.parse(actDto.getFecha_ejecucion().trim());
            } catch (Exception ex) {
              logger.warn("No se pudo parsear fecha_ejecucion: {}", actDto.getFecha_ejecucion());
            }
          }
          act.setArpac_fecha_ejecucion(fechaEjecucion);

          // Asociar evidencia requerida si viene el código o coincide el texto
          if (actDto.getArer_codigo() != null) {
            evidencias.stream()
                .filter(e -> e.getArer_codigo().equals(actDto.getArer_codigo()))
                .findFirst()
                .ifPresent(act::setEvidenciaRequerida);
          } else if (act.getArpac_entregable_esperado() != null && !act.getArpac_entregable_esperado().isEmpty()) {
            evidencias.stream()
                .filter(e -> e.getArer_descripcion() != null &&
                    e.getArer_descripcion().trim().equalsIgnoreCase(act.getArpac_entregable_esperado().trim()))
                .findFirst()
                .ifPresent(act::setEvidenciaRequerida);
          }

          planActividadRepositorio.save(act);
        }
        planActividadRepositorio.flush();
      }

      return EstadoRespuestas.respuestaCreacion("Plan de acción guardado exitosamente");
    } catch (Exception e) {
      logger.error("Error al guardar plan de acción: {}", e.getMessage(), e);
      return EstadoRespuestas.respuestaError("Error al guardar plan de acción: " + e.getMessage());
    }
  }

  // =========================================================================
  // 3. COTEJO DE EVIDENCIAS REQUERIDAS (CHECKLIST DEL AUDITOR)
  // =========================================================================

  @Transactional(readOnly = true)
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerCotejoEvidencias(Long arereCodigo, Long aredrCodigo) {
    try {
      DetalleRecomendacion detalle = resolverDetalleRecomendacion(arereCodigo, aredrCodigo);
      Long aredrReal = detalle != null ? detalle.getAredr_codigo() : aredrCodigo;

      List<EvidenciaRequerida> evidencias = evidenciaRequeridaRepositorio.findByRecomendacion(arereCodigo);
      List<EvidenciaCotejo> cotejosExistentes = evidenciaCotejoRepositorio.findByDetalleResponsable(aredrReal);

      Map<Long, EvidenciaCotejo> mapCotejos = cotejosExistentes.stream()
          .filter(c -> c.getEvidenciaRequerida() != null)
          .collect(Collectors.toMap(c -> c.getEvidenciaRequerida().getArer_codigo(), c -> c, (a, b) -> a));

      List<EvidenciaCotejoDTO> resultado = evidencias.stream().map(e -> {
        EvidenciaCotejoDTO dto = new EvidenciaCotejoDTO();
        dto.setArer_codigo(e.getArer_codigo());
        dto.setAredr_codigo(aredrReal);
        dto.setArer_descripcion(e.getArer_descripcion());

        EvidenciaCotejo cotejo = mapCotejos.get(e.getArer_codigo());
        if (cotejo != null) {
          dto.setArec_codigo(cotejo.getArec_codigo());
          dto.setCumple(Character.valueOf('S').equals(cotejo.getArec_cumple()));
          dto.setFecha_cotejo(cotejo.getArec_fecha_cotejo() != null ? dateFormat.format(cotejo.getArec_fecha_cotejo()) : "");
        } else {
          dto.setArec_codigo(null);
          dto.setCumple(false);
          dto.setFecha_cotejo("");
        }
        return dto;
      }).collect(Collectors.toList());

      return EstadoRespuestas.respuestaObtencion("Cotejo de evidencias obtenido exitosamente", resultado);
    } catch (Exception e) {
      logger.error("Error al obtener cotejo de evidencias: {}", e.getMessage(), e);
      return EstadoRespuestas.respuestaError("Error al obtener cotejo de evidencias: " + e.getMessage());
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> guardarCotejoEvidencias(GuardarCotejoDTO dto) {
    try {
      if (dto.getAredr_codigo() == null || dto.getChecklist() == null) {
        return EstadoRespuestas.respuestaError("Código de responsable y checklist son requeridos");
      }

      DetalleRecomendacion detalle = resolverDetalleRecomendacion(dto.getArere_codigo(), dto.getAredr_codigo());
      if (detalle == null) {
        return EstadoRespuestas.noEncontrado("Responsable no encontrado con código: " + dto.getAredr_codigo());
      }

      List<EvidenciaRequerida> evidencias = dto.getArere_codigo() != null
          ? evidenciaRequeridaRepositorio.findByRecomendacion(dto.getArere_codigo())
          : evidenciaRequeridaRepositorio.findByRecomendacion(detalle.getRecomendacion().getArere_codigo());

      for (Map.Entry<String, Boolean> entry : dto.getChecklist().entrySet()) {
        String key = entry.getKey();
        Boolean cumple = Boolean.TRUE.equals(entry.getValue());

        // La llave puede ser el arer_codigo (número) o el texto de la descripción
        EvidenciaRequerida er = null;
        try {
          Long idNum = Long.parseLong(key);
          er = evidencias.stream().filter(e -> e.getArer_codigo().equals(idNum)).findFirst().orElse(null);
        } catch (NumberFormatException ignored) {
          er = evidencias.stream().filter(e -> e.getArer_descripcion() != null &&
              e.getArer_descripcion().trim().equalsIgnoreCase(key.trim())).findFirst().orElse(null);
        }

        if (er != null) {
          Optional<EvidenciaCotejo> optCotejo = evidenciaCotejoRepositorio.findByEvidenciaAndResponsable(er.getArer_codigo(), detalle.getAredr_codigo());
          EvidenciaCotejo cotejo = optCotejo.orElseGet(EvidenciaCotejo::new);
          cotejo.setEvidenciaRequerida(er);
          cotejo.setDetalleRecomendacion(detalle);
          cotejo.setArec_cumple(cumple ? 'S' : 'N');
          cotejo.setArec_fecha_cotejo(new Date());
          cotejo.setArec_estado('A');
          evidenciaCotejoRepositorio.save(cotejo);
        }
      }

      return EstadoRespuestas.respuestaCreacion("Cotejo de evidencias guardado exitosamente");
    } catch (Exception e) {
      logger.error("Error al guardar cotejo de evidencias: {}", e.getMessage(), e);
      return EstadoRespuestas.respuestaError("Error al guardar cotejo de evidencias: " + e.getMessage());
    }
  }

  // =========================================================================
  // HELPER: PARSEAR EVIDENCIAS DESDE TEXTO MULTILÍNEA
  // =========================================================================

  private List<String> parseEvidenciasTexto(String raw) {
    Set<String> set = new LinkedHashSet<>();
    if (raw == null || raw.trim().isEmpty()) {
      return new ArrayList<>();
    }
    // Separar por saltos de línea, punto y coma, viñetas, o punto seguido antes de mayúscula / número
    String[] parts = raw.split("[\\r\\n;•]+|(?<=[\\.\\?!])\\s+(?=[A-ZÁÉÍÓÚÑ0-9])|(?<=\\s|^)(?:[0-9]+[\\.\\)]|[a-zA-Z][\\.\\)])\\s+");
    for (String part : parts) {
      String clean = part.replaceAll("^[0-9]+\\s*[\\.\\-\\)]\\s*", "")
                         .replaceAll("^[•\\-*\\s]+", "")
                         .replaceAll("[;\\.]+\\s*$", "")
                         .trim();
      if (!clean.isEmpty()) {
        set.add(clean);
      }
    }
    return new ArrayList<>(set);
  }
}
