package ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.dtos.ActualizarInformesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.dtos.ActualizarRecomendacionesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.dtos.ActualizarResponsablesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.dtos.CrearPeriodoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.models.CorteEvaluacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.models.CorteEvaluacionInforme;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@Service
public class CorteEvaluacionServicio {

    private static final Logger logger = LoggerFactory.getLogger(CorteEvaluacionServicio.class);

    @Autowired
    private CorteEvaluacionRepositorio corteEvaluacionRepositorio;

    @Autowired
    private CorteEvaluacionInformeRepositorio corteEvaluacionInformeRepositorio;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void sincronizarPeriodosActivos(Integer anio) {
        try {
            List<CorteEvaluacion> periodos;
            if (anio != null && anio > 0) {
                periodos = corteEvaluacionRepositorio.findByAnio(anio);
            } else {
                periodos = corteEvaluacionRepositorio.findAllActivos();
            }
            if (periodos != null) {
                for (CorteEvaluacion p : periodos) {
                    if (p.getArcevEstado() != null && p.getArcevEstado() == 1) {
                        corteEvaluacionInformeRepositorio.autoAsignarInformesPorFechaCorte(p.getArcevCodigo(), p.getArcevFechaCorte());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error al auto-sincronizar periodos activos para el año {}: {}", anio, e.getMessage());
        }
    }

    @Transactional
    public void sincronizarPeriodosActivos() {
        sincronizarPeriodosActivos(null);
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerTodos(Integer anio) {
        try {
            if (anio != null && anio > 0) {
                asegurarPeriodosPorDefecto(anio);
            }

            List<CorteEvaluacion> periodos;
            if (anio != null && anio > 0) {
                periodos = corteEvaluacionRepositorio.findByAnio(anio);
            } else {
                periodos = corteEvaluacionRepositorio.findAllActivos();
            }

            for (CorteEvaluacion p : periodos) {
                if (p.getArcevEstado() != null && p.getArcevEstado() == 1) {
                    try {
                        corteEvaluacionInformeRepositorio.autoAsignarInformesPorFechaCorte(p.getArcevCodigo(), p.getArcevFechaCorte());
                    } catch (Exception e) {
                        logger.warn("Error al auto-asignar informes en obtenerTodos para periodo {}: {}", p.getArcevCodigo(), e.getMessage());
                    }
                }
                Integer totalInf = corteEvaluacionInformeRepositorio.countByArcevCodigo(p.getArcevCodigo());
                Integer totalRec = corteEvaluacionInformeRepositorio.countRecomendacionesPorPeriodo(p.getArcevCodigo());
                p.setTotalInformes(totalInf != null ? totalInf : 0);
                p.setTotalRecomendaciones(totalRec != null ? totalRec : 0);
            }

            return EstadoRespuestas.respuestaObtencion("Periodos de evaluación obtenidos exitosamente", periodos);
        } catch (Exception e) {
            logger.error("Error al obtener periodos de evaluación: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al obtener periodos de evaluación: " + e.getMessage());
        }
    }

    private void asegurarPeriodosPorDefecto(Integer anio) {
        if (anio == null || anio < 2000) return;
        List<CorteEvaluacion> existentes = corteEvaluacionRepositorio.findByAnio(anio);
        if (existentes != null && !existentes.isEmpty()) {
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // I Trimestre
            crearPeriodoDefecto("I Trimestre " + anio, "TRIMESTRAL", sdf.parse(anio + "-01-01"), sdf.parse(anio + "-03-31"), sdf.parse(anio + "-03-31"), anio);
            // II Trimestre
            crearPeriodoDefecto("II Trimestre " + anio, "TRIMESTRAL", sdf.parse(anio + "-04-01"), sdf.parse(anio + "-06-30"), sdf.parse(anio + "-06-30"), anio);
            // III Trimestre
            crearPeriodoDefecto("III Trimestre " + anio, "TRIMESTRAL", sdf.parse(anio + "-07-01"), sdf.parse(anio + "-09-30"), sdf.parse(anio + "-09-30"), anio);
            // IV Trimestre
            crearPeriodoDefecto("IV Trimestre " + anio, "TRIMESTRAL", sdf.parse(anio + "-10-01"), sdf.parse(anio + "-12-31"), sdf.parse(anio + "-12-31"), anio);
            // Cierre de Año
            crearPeriodoDefecto("Cierre de Año " + anio, "CIERRE_ANUAL", sdf.parse(anio + "-01-01"), sdf.parse(anio + "-12-31"), sdf.parse(anio + "-12-31"), anio);
        } catch (Exception e) {
            logger.error("Error al generar periodos por defecto para el año {}: {}", anio, e.getMessage());
        }
    }

    private void crearPeriodoDefecto(String nombre, String tipo, Date fIni, Date fFin, Date fCorte, Integer anio) {
        CorteEvaluacion nuevo = new CorteEvaluacion();
        nuevo.setArcevNombre(nombre);
        nuevo.setArcevTipo(tipo);
        nuevo.setArcevFechaInicio(fIni);
        nuevo.setArcevFechaFin(fFin);
        nuevo.setArcevFechaCorte(fCorte);
        nuevo.setArcevAnio(anio);
        nuevo.setArcevEstado(1);
        CorteEvaluacion guardado = corteEvaluacionRepositorio.save(nuevo);
        corteEvaluacionInformeRepositorio.autoAsignarInformesPorFechaCorte(guardado.getArcevCodigo(), guardado.getArcevFechaCorte());
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorId(Long id) {
        try {
            Optional<CorteEvaluacion> periodoOpt = corteEvaluacionRepositorio.findById(id);
            if (!periodoOpt.isPresent()) {
                return EstadoRespuestas.noEncontrado("Periodo de evaluación no encontrado con ID: " + id);
            }
            CorteEvaluacion p = periodoOpt.get();
            if (p.getArcevEstado() != null && p.getArcevEstado() == 1) {
                try {
                    corteEvaluacionInformeRepositorio.autoAsignarInformesPorFechaCorte(p.getArcevCodigo(), p.getArcevFechaCorte());
                } catch (Exception e) {
                    logger.warn("Error al auto-asignar en obtenerPorId para periodo {}: {}", p.getArcevCodigo(), e.getMessage());
                }
            }
            Integer totalInf = corteEvaluacionInformeRepositorio.countByArcevCodigo(p.getArcevCodigo());
            Integer totalRec = corteEvaluacionInformeRepositorio.countRecomendacionesPorPeriodo(p.getArcevCodigo());
            p.setTotalInformes(totalInf != null ? totalInf : 0);
            p.setTotalRecomendaciones(totalRec != null ? totalRec : 0);
            return EstadoRespuestas.respuestaObtencion("Periodo obtenido exitosamente", p);
        } catch (Exception e) {
            logger.error("Error al obtener periodo por ID: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al obtener periodo: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerInformesPorPeriodo(Long periodoId) {
        try {
            Optional<CorteEvaluacion> periodoOpt = corteEvaluacionRepositorio.findById(periodoId);
            if (periodoOpt.isPresent()) {
                CorteEvaluacion p = periodoOpt.get();
                if (p.getArcevEstado() != null && p.getArcevEstado() == 1) {
                    try {
                        corteEvaluacionInformeRepositorio.autoAsignarInformesPorFechaCorte(p.getArcevCodigo(), p.getArcevFechaCorte());
                    } catch (Exception e) {
                        logger.warn("Error al auto-asignar en obtenerInformesPorPeriodo para periodo {}: {}", p.getArcevCodigo(), e.getMessage());
                    }
                }
            }
            List<Map<String, Object>> detalles = corteEvaluacionInformeRepositorio.findDetallesConInformePorPeriodo(periodoId);
            return EstadoRespuestas.respuestaObtencion("Informes del periodo obtenidos exitosamente", detalles);
        } catch (Exception e) {
            logger.error("Error al obtener informes del periodo: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al obtener informes del periodo: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerRecomendacionesPorPeriodo(Long periodoId) {
        try {
            String sql = "SELECT " +
                    "  r.ARERE_CODIGO AS \"arere_codigo\", " +
                    "  i.AININ_CODIGO AS \"ainin_codigo\", " +
                    "  r.ARERE_NUM_REC AS \"numero_recomendacion\", " +
                    "  r.ARERE_DESCRIPCION AS \"recomendacion_descripcion\", " +
                    "  i.AININ_MEMORANDO AS \"ainin_memorando\", " +
                    "  i.AININ_TITULO AS \"ainin_titulo\", " +
                    "  NVL(rec_ex.ARCER_INCLUIDO, 1) AS \"incluido\" " +
                    "FROM AI_CORTE_EVALUACION_INFORME ci " +
                    "JOIN AI_CORTE_EVALUACION ev ON ci.ARCEV_CODIGO = ev.ARCEV_CODIGO " +
                    "JOIN AI_INF_INFORMES i ON ci.AININ_CODIGO = i.AININ_CODIGO " +
                    "JOIN AI_REC_HALLAZGOS h ON i.AININ_CODIGO = h.AININ_CODIGO " +
                    "JOIN AI_REC_CONCLUSIONES c ON h.AREHA_CODIGO = c.AREHA_CODIGO " +
                    "JOIN AI_REC_RECOMENDACIONES r ON c.ARECO_CODIGO = r.ARECO_CODIGO " +
                    "JOIN AI_REC_ESTADO_SEGUIMIENTO es ON r.AREES_CODIGO = es.AREES_CODIGO " +
                    "LEFT JOIN AI_CORTE_EVALUACION_REC rec_ex ON rec_ex.ARCEV_CODIGO = ci.ARCEV_CODIGO AND rec_ex.ARERE_CODIGO = r.ARERE_CODIGO " +
                    "WHERE ci.ARCEV_CODIGO = ? AND (TO_CHAR(r.ARERE_ESTADO) = '1' OR r.ARERE_ESTADO = 1) " +
                    "AND ( " +
                    "    ci.ARCEI_ES_MANUAL = 1 " +
                    "    OR UPPER(TRIM(ev.ARCEV_TIPO)) != 'CIERRE_ANUAL' " +
                    "    OR ( " +
                    "        TRUNC(NVL(i.AININ_FECHA_APROBACION, i.AININ_FECHA_MEMO)) <= ev.ARCEV_FECHA_CORTE " +
                    "        AND ( " +
                    "            r.AREES_CODIGO NOT IN (2, 4) " +
                    "            OR (r.AREES_CODIGO = 2 AND r.ARERE_FECHA_CUMPLIDA IS NOT NULL AND TRUNC(r.ARERE_FECHA_CUMPLIDA) > ev.ARCEV_FECHA_CORTE) " +
                    "        ) " +
                    "        AND UPPER(TRIM(es.AREES_DESCRIPCION)) NOT IN ('NO APLICA') " +
                    "        AND EXISTS ( " +
                    "            SELECT 1 FROM AI_REC_DETALLE_RECOMENDACION dr_sub " +
                    "            JOIN AI_REC_RESPONSABLES res_sub ON dr_sub.arers_codigo = res_sub.arers_codigo AND res_sub.arers_estado = 1 " +
                    "            LEFT JOIN ( " +
                    "                SELECT v_sub.*, ROW_NUMBER() OVER (PARTITION BY v_sub.aredr_codigo ORDER BY CASE WHEN v_sub.areev_codigo = 2 THEN 1 WHEN v_sub.areev_codigo = 1 THEN 2 WHEN v_sub.areev_codigo = 3 THEN 3 ELSE 4 END ASC, NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha) DESC NULLS LAST, v_sub.areve_codigo DESC) AS rn_sub " +
                    "                FROM AI_REC_VERIFICABLES v_sub " +
                    "                WHERE TO_CHAR(v_sub.areve_estado) = '1' " +
                    "                  AND (TRUNC(NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha)) <= ev.arcev_fecha_corte OR v_sub.areve_fecha_carga IS NULL) " +
                    "            ) vr_sub ON dr_sub.aredr_codigo = vr_sub.aredr_codigo AND vr_sub.rn_sub = 1 " +
                    "            LEFT JOIN AI_REC_ESTADO_VERIFICABLE ev_sub ON vr_sub.areev_codigo = ev_sub.areev_codigo " +
                    "            WHERE dr_sub.arere_codigo = r.arere_codigo AND dr_sub.aredr_estado = 1 " +
                    "              AND (vr_sub.areev_codigo IS NULL OR (vr_sub.areev_codigo != 2 AND UPPER(TRIM(NVL(ev_sub.areev_descripcion, ''))) NOT IN ('JUSTIFICA', 'JUSTIFICADO', 'JUSTIFICADA'))) " +
                    "        ) " +
                    "    ) " +
                    ")";
            List<Map<String, Object>> recs = jdbcTemplate.queryForList(sql, periodoId);
            return EstadoRespuestas.respuestaObtencion("Recomendaciones del periodo obtenidas exitosamente", recs);
        } catch (Exception e) {
            logger.error("Error al obtener recomendaciones del periodo: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al obtener recomendaciones del periodo: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerResponsablesPorPeriodo(Long periodoId) {
        try {
            String sql = "SELECT " +
                    "  dr.ARERE_CODIGO AS \"arere_codigo\", " +
                    "  r.ARERE_NUM_REC AS \"numero_recomendacion\", " +
                    "  ci.AININ_CODIGO AS \"ainin_codigo\", " +
                    "  res.ARERS_CODIGO AS \"arers_codigo\", " +
                    "  res.ARERS_NOMBRE || ' ' || res.ARERS_APELLIDO AS \"nombre_completo\", " +
                    "  res.ARERS_DEPARTAMENTO AS \"departamento\", " +
                    "  NVL(resp_ex.ARCES_INCLUIDO, 1) AS \"incluido\" " +
                    "FROM AI_CORTE_EVALUACION_INFORME ci " +
                    "JOIN AI_CORTE_EVALUACION ev ON ci.ARCEV_CODIGO = ev.ARCEV_CODIGO " +
                    "JOIN AI_INF_INFORMES i ON ci.AININ_CODIGO = i.AININ_CODIGO " +
                    "JOIN AI_REC_HALLAZGOS h ON ci.AININ_CODIGO = h.AININ_CODIGO " +
                    "JOIN AI_REC_CONCLUSIONES c ON h.AREHA_CODIGO = c.AREHA_CODIGO " +
                    "JOIN AI_REC_RECOMENDACIONES r ON c.ARECO_CODIGO = r.ARECO_CODIGO " +
                    "JOIN AI_REC_ESTADO_SEGUIMIENTO es ON r.AREES_CODIGO = es.AREES_CODIGO " +
                    "JOIN AI_REC_DETALLE_RECOMENDACION dr ON r.ARERE_CODIGO = dr.ARERE_CODIGO " +
                    "JOIN AI_REC_RESPONSABLES res ON dr.ARERS_CODIGO = res.ARERS_CODIGO " +
                    "LEFT JOIN AI_CORTE_EVALUACION_RESP resp_ex ON resp_ex.ARCEV_CODIGO = ci.ARCEV_CODIGO AND resp_ex.ARERS_CODIGO = res.ARERS_CODIGO " +
                    "WHERE ci.ARCEV_CODIGO = ? AND (TO_CHAR(res.ARERS_ESTADO) = '1' OR res.ARERS_ESTADO = 1) " +
                    "AND ( " +
                    "    ci.ARCEI_ES_MANUAL = 1 " +
                    "    OR UPPER(TRIM(ev.ARCEV_TIPO)) != 'CIERRE_ANUAL' " +
                    "    OR ( " +
                    "        TRUNC(NVL(i.AININ_FECHA_APROBACION, i.AININ_FECHA_MEMO)) <= ev.ARCEV_FECHA_CORTE " +
                    "        AND ( " +
                    "            r.AREES_CODIGO NOT IN (2, 4) " +
                    "            OR (r.AREES_CODIGO = 2 AND r.ARERE_FECHA_CUMPLIDA IS NOT NULL AND TRUNC(r.ARERE_FECHA_CUMPLIDA) > ev.ARCEV_FECHA_CORTE) " +
                    "        ) " +
                    "        AND UPPER(TRIM(es.AREES_DESCRIPCION)) NOT IN ('NO APLICA') " +
                    "        AND (TO_CHAR(dr.AREDR_ESTADO) = '1' OR dr.AREDR_ESTADO = 1) " +
                    "        AND NOT EXISTS ( " +
                    "            SELECT 1 FROM ( " +
                    "                SELECT v_sub.*, ROW_NUMBER() OVER (PARTITION BY v_sub.aredr_codigo ORDER BY CASE WHEN v_sub.areev_codigo = 2 THEN 1 WHEN v_sub.areev_codigo = 1 THEN 2 WHEN v_sub.areev_codigo = 3 THEN 3 ELSE 4 END ASC, NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha) DESC NULLS LAST, v_sub.areve_codigo DESC) AS rn_sub " +
                    "                FROM AI_REC_VERIFICABLES v_sub " +
                    "                WHERE TO_CHAR(v_sub.areve_estado) = '1' " +
                    "                  AND (TRUNC(NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha)) <= ev.arcev_fecha_corte OR v_sub.areve_fecha_carga IS NULL) " +
                    "            ) vr_sub " +
                    "            LEFT JOIN AI_REC_ESTADO_VERIFICABLE ev_sub ON vr_sub.areev_codigo = ev_sub.areev_codigo " +
                    "            WHERE vr_sub.aredr_codigo = dr.aredr_codigo AND vr_sub.rn_sub = 1 " +
                    "              AND (vr_sub.areev_codigo = 2 OR UPPER(TRIM(NVL(ev_sub.areev_descripcion, ''))) IN ('JUSTIFICA', 'JUSTIFICADO', 'JUSTIFICADA')) " +
                    "        ) " +
                    "    ) " +
                    ") " +
                    "GROUP BY dr.ARERE_CODIGO, r.ARERE_NUM_REC, ci.AININ_CODIGO, res.ARERS_CODIGO, res.ARERS_NOMBRE, res.ARERS_APELLIDO, res.ARERS_DEPARTAMENTO, resp_ex.ARCES_INCLUIDO";
            List<Map<String, Object>> resps = jdbcTemplate.queryForList(sql, periodoId);
            return EstadoRespuestas.respuestaObtencion("Responsables del periodo obtenidos exitosamente", resps);
        } catch (Exception e) {
            logger.error("Error al obtener responsables del periodo: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al obtener responsables del periodo: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> crear(CrearPeriodoDTO dto) {
        try {
            CorteEvaluacion nuevo = new CorteEvaluacion();
            nuevo.setArcevNombre(dto.getArcev_nombre());
            nuevo.setArcevTipo(dto.getArcev_tipo());
            nuevo.setArcevFechaInicio(dto.getArcev_fecha_inicio());
            nuevo.setArcevFechaFin(dto.getArcev_fecha_fin());
            nuevo.setArcevFechaCorte(dto.getArcev_fecha_corte());
            nuevo.setArcevAnio(dto.getArcev_anio());
            nuevo.setArcevEstado(1);

            CorteEvaluacion guardado = corteEvaluacionRepositorio.save(nuevo);

            if (dto.getInformes_ids() != null && !dto.getInformes_ids().isEmpty()) {
                for (Long informeId : dto.getInformes_ids()) {
                    CorteEvaluacionInforme detalle = new CorteEvaluacionInforme();
                    detalle.setArcevCodigo(guardado.getArcevCodigo());
                    detalle.setAininCodigo(informeId);
                    detalle.setArceiEsManual(1);
                    detalle.setArceiFechaRegistro(new Date());
                    corteEvaluacionInformeRepositorio.save(detalle);
                }
            } else {
                corteEvaluacionInformeRepositorio.purgeInformesSinPendientesParaCierreAnual(guardado.getArcevCodigo());
                corteEvaluacionInformeRepositorio.autoAsignarInformesPorFechaCorte(guardado.getArcevCodigo(), guardado.getArcevFechaCorte());
            }

            return EstadoRespuestas.respuestaCreacion("Periodo de evaluación registrado exitosamente");
        } catch (Exception e) {
            logger.error("Error al registrar periodo de evaluación: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al registrar periodo de evaluación: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> actualizarInformes(Long periodoId, ActualizarInformesDTO dto) {
        try {
            Optional<CorteEvaluacion> periodoOpt = corteEvaluacionRepositorio.findById(periodoId);
            if (!periodoOpt.isPresent()) {
                return EstadoRespuestas.noEncontrado("Periodo de evaluación no encontrado con ID: " + periodoId);
            }

            corteEvaluacionInformeRepositorio.deleteByArcevCodigo(periodoId);

            if (dto.getInformes_ids() != null && !dto.getInformes_ids().isEmpty()) {
                java.util.Set<Long> uniqueIds = new java.util.LinkedHashSet<>(dto.getInformes_ids());
                for (Long informeId : uniqueIds) {
                    CorteEvaluacionInforme detalle = new CorteEvaluacionInforme();
                    detalle.setArcevCodigo(periodoId);
                    detalle.setAininCodigo(informeId);
                    detalle.setArceiEsManual(1);
                    detalle.setArceiFechaRegistro(new Date());
                    corteEvaluacionInformeRepositorio.save(detalle);
                }
            }

            return EstadoRespuestas.respuestaObtencion("Informes asignados al periodo actualizados exitosamente", null);
        } catch (Exception e) {
            logger.error("Error al actualizar informes del periodo: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al actualizar informes del periodo: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> generarSnapshot(Long periodoId) {
        try {
            Optional<CorteEvaluacion> periodoOpt = corteEvaluacionRepositorio.findById(periodoId);
            if (!periodoOpt.isPresent()) {
                return EstadoRespuestas.noEncontrado("Periodo de evaluación no encontrado con ID: " + periodoId);
            }

            jdbcTemplate.update("CALL SP_GENERAR_SNAPSHOT_CORTE(?)", periodoId);
            logger.info("Snapshot inmutable generado exitosamente para el periodo ID: {}", periodoId);

            return EstadoRespuestas.respuestaObtencion("Snapshot inmutable del periodo congelado exitosamente", null);
        } catch (Exception e) {
            logger.error("Error al generar snapshot del periodo: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al generar snapshot del periodo: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> actualizarRecomendaciones(Long periodoId, ActualizarRecomendacionesDTO dto) {
        try {
            jdbcTemplate.update("DELETE FROM AI_CORTE_EVALUACION_REC WHERE arcev_codigo = ?", periodoId);

            if (dto.getRecomendaciones_excluidas_ids() != null) {
                for (Long recId : dto.getRecomendaciones_excluidas_ids()) {
                    jdbcTemplate.update(
                        "INSERT INTO AI_CORTE_EVALUACION_REC (arcev_codigo, arere_codigo, arcer_incluido, arcer_fecha_reg) VALUES (?, ?, 0, SYSTIMESTAMP)",
                        periodoId, recId
                    );
                }
            }

            if (dto.getRecomendaciones_incluidas_ids() != null) {
                for (Long recId : dto.getRecomendaciones_incluidas_ids()) {
                    jdbcTemplate.update(
                        "INSERT INTO AI_CORTE_EVALUACION_REC (arcev_codigo, arere_codigo, arcer_incluido, arcer_fecha_reg) VALUES (?, ?, 1, SYSTIMESTAMP)",
                        periodoId, recId
                    );
                }
            }

            return EstadoRespuestas.respuestaObtencion("Inclusión granular de recomendaciones actualizada", null);
        } catch (Exception e) {
            logger.error("Error al actualizar recomendaciones del periodo: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al actualizar recomendaciones del periodo: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> actualizarResponsables(Long periodoId, ActualizarResponsablesDTO dto) {
        try {
            jdbcTemplate.update("DELETE FROM AI_CORTE_EVALUACION_RESP WHERE arcev_codigo = ?", periodoId);

            if (dto.getResponsables_excluidos_ids() != null) {
                for (Long respId : dto.getResponsables_excluidos_ids()) {
                    jdbcTemplate.update(
                        "INSERT INTO AI_CORTE_EVALUACION_RESP (arcev_codigo, arers_codigo, arces_incluido, arces_fecha_reg) VALUES (?, ?, 0, SYSTIMESTAMP)",
                        periodoId, respId
                    );
                }
            }

            if (dto.getResponsables_incluidos_ids() != null) {
                for (Long respId : dto.getResponsables_incluidos_ids()) {
                    jdbcTemplate.update(
                        "INSERT INTO AI_CORTE_EVALUACION_RESP (arcev_codigo, arers_codigo, arces_incluido, arces_fecha_reg) VALUES (?, ?, 1, SYSTIMESTAMP)",
                        periodoId, respId
                    );
                }
            }

            return EstadoRespuestas.respuestaObtencion("Inclusión granular de responsables actualizada", null);
        } catch (Exception e) {
            logger.error("Error al actualizar responsables del periodo: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al actualizar responsables del periodo: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> eliminar(Long id) {
        try {
            try {
                jdbcTemplate.update("DELETE FROM AI_CORTE_EVALUACION_SNAPSHOT WHERE arcev_codigo = ?", id);
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.update("DELETE FROM AI_CORTE_EVALUACION_RESP WHERE arcev_codigo = ?", id);
            } catch (Exception ignored) {}
            try {
                jdbcTemplate.update("DELETE FROM AI_CORTE_EVALUACION_REC WHERE arcev_codigo = ?", id);
            } catch (Exception ignored) {}
            corteEvaluacionInformeRepositorio.deleteByArcevCodigo(id);
            corteEvaluacionRepositorio.deleteById(id);
            return EstadoRespuestas.respuestaEliminacion("Periodo de evaluación eliminado exitosamente");
        } catch (Exception e) {
            logger.error("Error al eliminar periodo de evaluación: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al eliminar periodo de evaluación: " + e.getMessage());
        }
    }
}
