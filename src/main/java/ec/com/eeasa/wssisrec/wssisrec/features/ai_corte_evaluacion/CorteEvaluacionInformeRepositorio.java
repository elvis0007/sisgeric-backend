package ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.models.CorteEvaluacionInforme;

@Repository
public interface CorteEvaluacionInformeRepositorio extends JpaRepository<CorteEvaluacionInforme, Long> {

    List<CorteEvaluacionInforme> findByArcevCodigo(Long arcevCodigo);

    @Query(value = "SELECT " +
            "  ci.ARCEI_CODIGO AS \"arcei_codigo\", " +
            "  ci.ARCEV_CODIGO AS \"arcev_codigo\", " +
            "  ci.AININ_CODIGO AS \"ainin_codigo\", " +
            "  i.AININ_TITULO AS \"ainin_titulo\", " +
            "  i.AININ_MEMORANDO AS \"ainin_memorando\", " +
            "  TO_CHAR(NVL(i.AININ_FECHA_APROBACION, i.AININ_FECHA_MEMO), 'YYYY-MM-DD') AS \"ainin_fecha_emision\", " +
            "  TO_CHAR(i.AININ_FECHA_APROBACION, 'YYYY-MM-DD') AS \"ainin_fecha_aprobacion\", " +
            "  o.AINOC_DESCRIPCION AS \"organismo_control\", " +
            "  ci.ARCEI_ES_MANUAL AS \"arcei_es_manual\", " +
            "  TO_CHAR(ci.ARCEI_FECHA_REGISTRO, 'YYYY-MM-DD HH24:MI:SS') AS \"arcei_fecha_registro\" " +
            "FROM AI_CORTE_EVALUACION_INFORME ci " +
            "JOIN AI_CORTE_EVALUACION ev ON ci.ARCEV_CODIGO = ev.ARCEV_CODIGO " +
            "JOIN AI_INF_INFORMES i ON ci.AININ_CODIGO = i.AININ_CODIGO " +
            "LEFT JOIN AI_INF_ORG_CONT o ON i.AINOC_CODIGO = o.AINOC_CODIGO " +
            "WHERE ci.ARCEV_CODIGO = :arcevCodigo " +
            "AND ( " +
            "    ci.ARCEI_ES_MANUAL = 1 " +
            "    OR UPPER(TRIM(ev.ARCEV_TIPO)) != 'CIERRE_ANUAL' " +
            "    OR ( " +
            "        TRUNC(NVL(i.AININ_FECHA_APROBACION, i.AININ_FECHA_MEMO)) <= ev.ARCEV_FECHA_CORTE " +
            "        AND EXISTS ( " +
            "            SELECT 1 FROM AI_REC_HALLAZGOS h " +
            "            JOIN AI_REC_CONCLUSIONES c ON h.areha_codigo = c.areha_codigo " +
            "            JOIN AI_REC_RECOMENDACIONES r ON c.areco_codigo = r.areco_codigo " +
            "            JOIN AI_REC_ESTADO_SEGUIMIENTO es ON r.arees_codigo = es.arees_codigo " +
            "            WHERE h.ainin_codigo = i.ainin_codigo " +
            "              AND (TO_CHAR(r.arere_estado) = '1' OR r.arere_estado = 1) " +
            "              AND ( " +
            "                  r.arees_codigo NOT IN (2, 4) " +
            "                  OR (r.arees_codigo = 2 AND r.arere_fecha_cumplida IS NOT NULL AND TRUNC(r.arere_fecha_cumplida) > ev.arcev_fecha_corte) " +
            "              ) " +
            "              AND UPPER(TRIM(es.arees_descripcion)) NOT IN ('NO APLICA') " +
            "              AND EXISTS ( " +
            "                  SELECT 1 FROM AI_REC_DETALLE_RECOMENDACION dr_sub " +
            "                  JOIN AI_REC_RESPONSABLES res_sub ON dr_sub.arers_codigo = res_sub.arers_codigo AND res_sub.arers_estado = 1 " +
            "                  LEFT JOIN ( " +
            "                      SELECT v_sub.*, ROW_NUMBER() OVER (PARTITION BY v_sub.aredr_codigo ORDER BY CASE WHEN v_sub.areev_codigo = 2 THEN 1 WHEN v_sub.areev_codigo = 1 THEN 2 WHEN v_sub.areev_codigo = 3 THEN 3 ELSE 4 END ASC, NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha) DESC NULLS LAST, v_sub.areve_codigo DESC) AS rn_sub " +
            "                      FROM AI_REC_VERIFICABLES v_sub " +
            "                      WHERE TO_CHAR(v_sub.areve_estado) = '1' " +
            "                        AND (TRUNC(NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha)) <= ev.arcev_fecha_corte OR v_sub.areve_fecha_carga IS NULL) " +
            "                  ) vr_sub ON dr_sub.aredr_codigo = vr_sub.aredr_codigo AND vr_sub.rn_sub = 1 " +
            "                  LEFT JOIN AI_REC_ESTADO_VERIFICABLE ev_sub ON vr_sub.areev_codigo = ev_sub.areev_codigo " +
            "                  WHERE dr_sub.arere_codigo = r.arere_codigo AND dr_sub.aredr_estado = 1 " +
            "                    AND (vr_sub.areev_codigo IS NULL OR (vr_sub.areev_codigo != 2 AND UPPER(TRIM(NVL(ev_sub.areev_descripcion, ''))) NOT IN ('JUSTIFICA', 'JUSTIFICADO', 'JUSTIFICADA'))) " +
            "              ) " +
            "        ) " +
            "    ) " +
            ")", nativeQuery = true)
    List<Map<String, Object>> findDetallesConInformePorPeriodo(@Param("arcevCodigo") Long arcevCodigo);

    @Modifying
    @Query(value = "DELETE FROM AI_CORTE_EVALUACION_INFORME WHERE ARCEV_CODIGO = :arcevCodigo", nativeQuery = true)
    void deleteByArcevCodigo(@Param("arcevCodigo") Long arcevCodigo);

    @Query(value = "SELECT COUNT(DISTINCT ci.AININ_CODIGO) " +
            "FROM AI_CORTE_EVALUACION_INFORME ci " +
            "JOIN AI_CORTE_EVALUACION ev ON ci.ARCEV_CODIGO = ev.ARCEV_CODIGO " +
            "JOIN AI_INF_INFORMES i ON ci.AININ_CODIGO = i.AININ_CODIGO " +
            "WHERE ci.ARCEV_CODIGO = :arcevCodigo " +
            "AND ( " +
            "    ci.ARCEI_ES_MANUAL = 1 " +
            "    OR UPPER(TRIM(ev.ARCEV_TIPO)) != 'CIERRE_ANUAL' " +
            "    OR ( " +
            "        TRUNC(NVL(i.AININ_FECHA_APROBACION, i.AININ_FECHA_MEMO)) <= ev.ARCEV_FECHA_CORTE " +
            "        AND EXISTS ( " +
            "            SELECT 1 FROM AI_REC_HALLAZGOS h " +
            "            JOIN AI_REC_CONCLUSIONES c ON h.areha_codigo = c.areha_codigo " +
            "            JOIN AI_REC_RECOMENDACIONES r ON c.areco_codigo = r.areco_codigo " +
            "            JOIN AI_REC_ESTADO_SEGUIMIENTO es ON r.arees_codigo = es.arees_codigo " +
            "            WHERE h.ainin_codigo = i.ainin_codigo " +
            "              AND (TO_CHAR(r.arere_estado) = '1' OR r.arere_estado = 1) " +
            "              AND ( " +
            "                  r.arees_codigo NOT IN (2, 4) " +
            "                  OR (r.arees_codigo = 2 AND r.arere_fecha_cumplida IS NOT NULL AND TRUNC(r.arere_fecha_cumplida) > ev.arcev_fecha_corte) " +
            "              ) " +
            "              AND UPPER(TRIM(es.arees_descripcion)) NOT IN ('NO APLICA') " +
            "              AND EXISTS ( " +
            "                  SELECT 1 FROM AI_REC_DETALLE_RECOMENDACION dr_sub " +
            "                  JOIN AI_REC_RESPONSABLES res_sub ON dr_sub.arers_codigo = res_sub.arers_codigo AND res_sub.arers_estado = 1 " +
            "                  LEFT JOIN ( " +
            "                      SELECT v_sub.*, ROW_NUMBER() OVER (PARTITION BY v_sub.aredr_codigo ORDER BY CASE WHEN v_sub.areev_codigo = 2 THEN 1 WHEN v_sub.areev_codigo = 1 THEN 2 WHEN v_sub.areev_codigo = 3 THEN 3 ELSE 4 END ASC, NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha) DESC NULLS LAST, v_sub.areve_codigo DESC) AS rn_sub " +
            "                      FROM AI_REC_VERIFICABLES v_sub " +
            "                      WHERE TO_CHAR(v_sub.areve_estado) = '1' " +
            "                        AND (TRUNC(NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha)) <= ev.arcev_fecha_corte OR v_sub.areve_fecha_carga IS NULL) " +
            "                  ) vr_sub ON dr_sub.aredr_codigo = vr_sub.aredr_codigo AND vr_sub.rn_sub = 1 " +
            "                  LEFT JOIN AI_REC_ESTADO_VERIFICABLE ev_sub ON vr_sub.areev_codigo = ev_sub.areev_codigo " +
            "                  WHERE dr_sub.arere_codigo = r.arere_codigo AND dr_sub.aredr_estado = 1 " +
            "                    AND (vr_sub.areev_codigo IS NULL OR (vr_sub.areev_codigo != 2 AND UPPER(TRIM(NVL(ev_sub.areev_descripcion, ''))) NOT IN ('JUSTIFICA', 'JUSTIFICADO', 'JUSTIFICADA'))) " +
            "              ) " +
            "        ) " +
            "    ) " +
            ")", nativeQuery = true)
    Integer countByArcevCodigo(@Param("arcevCodigo") Long arcevCodigo);

    @Query(value = "SELECT COUNT(DISTINCT r.ARERE_CODIGO) " +
            "FROM AI_CORTE_EVALUACION_INFORME ci " +
            "JOIN AI_CORTE_EVALUACION ev ON ci.ARCEV_CODIGO = ev.ARCEV_CODIGO " +
            "JOIN AI_INF_INFORMES i ON ci.AININ_CODIGO = i.AININ_CODIGO " +
            "JOIN AI_REC_HALLAZGOS h ON ci.AININ_CODIGO = h.AININ_CODIGO " +
            "JOIN AI_REC_CONCLUSIONES c ON h.AREHA_CODIGO = c.AREHA_CODIGO " +
            "JOIN AI_REC_RECOMENDACIONES r ON c.ARECO_CODIGO = r.ARECO_CODIGO " +
            "JOIN AI_REC_ESTADO_SEGUIMIENTO es ON r.AREES_CODIGO = es.AREES_CODIGO " +
            "WHERE ci.ARCEV_CODIGO = :arcevCodigo " +
            "AND (TO_CHAR(r.ARERE_ESTADO) = '1' OR r.ARERE_ESTADO = 1) " +
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
            ")", nativeQuery = true)
    Integer countRecomendacionesPorPeriodo(@Param("arcevCodigo") Long arcevCodigo);

    @Modifying
    @Query(value = "DELETE FROM AI_CORTE_EVALUACION_INFORME ci " +
            "WHERE ci.arcev_codigo = :arcevCodigo " +
            "  AND EXISTS (SELECT 1 FROM AI_CORTE_EVALUACION p WHERE p.arcev_codigo = :arcevCodigo AND UPPER(TRIM(p.arcev_tipo)) = 'CIERRE_ANUAL') " +
            "  AND ( " +
            "      EXISTS ( " +
            "          SELECT 1 FROM AI_INF_INFORMES i " +
            "          JOIN AI_CORTE_EVALUACION p ON p.arcev_codigo = :arcevCodigo " +
            "          WHERE i.ainin_codigo = ci.ainin_codigo " +
            "            AND TRUNC(NVL(i.ainin_fecha_aprobacion, i.ainin_fecha_memo)) > p.arcev_fecha_corte " +
            "      ) " +
            "      OR NOT EXISTS ( " +
            "          SELECT 1 FROM AI_REC_HALLAZGOS h " +
            "          JOIN AI_REC_CONCLUSIONES c ON h.areha_codigo = c.areha_codigo " +
            "          JOIN AI_REC_RECOMENDACIONES r ON c.areco_codigo = r.areco_codigo " +
            "          JOIN AI_REC_ESTADO_SEGUIMIENTO es ON r.arees_codigo = es.arees_codigo " +
            "          JOIN AI_CORTE_EVALUACION p ON p.arcev_codigo = :arcevCodigo " +
            "          WHERE h.ainin_codigo = ci.ainin_codigo " +
            "            AND (TO_CHAR(r.arere_estado) = '1' OR r.arere_estado = 1) " +
            "            AND ( " +
            "                r.arees_codigo NOT IN (2, 4) " +
            "                OR (r.arees_codigo = 2 AND r.arere_fecha_cumplida IS NOT NULL AND TRUNC(r.arere_fecha_cumplida) > p.arcev_fecha_corte) " +
            "            ) " +
            "            AND UPPER(TRIM(es.arees_descripcion)) NOT IN ('NO APLICA') " +
            "            AND EXISTS ( " +
            "                SELECT 1 FROM AI_REC_DETALLE_RECOMENDACION dr_sub " +
            "                JOIN AI_REC_RESPONSABLES res_sub ON dr_sub.arers_codigo = res_sub.arers_codigo AND res_sub.arers_estado = 1 " +
            "                LEFT JOIN ( " +
            "                    SELECT v_sub.*, ROW_NUMBER() OVER (PARTITION BY v_sub.aredr_codigo ORDER BY CASE WHEN v_sub.areev_codigo = 2 THEN 1 WHEN v_sub.areev_codigo = 1 THEN 2 WHEN v_sub.areev_codigo = 3 THEN 3 ELSE 4 END ASC, NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha) DESC NULLS LAST, v_sub.areve_codigo DESC) AS rn_sub " +
            "                    FROM AI_REC_VERIFICABLES v_sub " +
            "                    WHERE TO_CHAR(v_sub.areve_estado) = '1' " +
            "                      AND (TRUNC(NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha)) <= p.arcev_fecha_corte OR v_sub.areve_fecha_carga IS NULL) " +
            "                ) vr_sub ON dr_sub.aredr_codigo = vr_sub.aredr_codigo AND vr_sub.rn_sub = 1 " +
            "                LEFT JOIN AI_REC_ESTADO_VERIFICABLE ev_sub ON vr_sub.areev_codigo = ev_sub.areev_codigo " +
            "                WHERE dr_sub.arere_codigo = r.arere_codigo AND dr_sub.aredr_estado = 1 " +
            "                  AND (vr_sub.areev_codigo IS NULL OR (vr_sub.areev_codigo != 2 AND UPPER(TRIM(NVL(ev_sub.areev_descripcion, ''))) NOT IN ('JUSTIFICA', 'JUSTIFICADO', 'JUSTIFICADA'))) " +
            "            ) " +
            "      ) " +
            "  )", nativeQuery = true)
    void purgeInformesSinPendientesParaCierreAnual(@Param("arcevCodigo") Long arcevCodigo);

    @Modifying
    @Query(value = "INSERT INTO AI_CORTE_EVALUACION_INFORME (arcev_codigo, ainin_codigo, arcei_es_manual, arcei_fecha_registro) " +
            "SELECT :arcevCodigo, i.ainin_codigo, 0, SYSTIMESTAMP " +
            "FROM AI_INF_INFORMES i " +
            "WHERE TRUNC(COALESCE(i.ainin_fecha_aprobacion, i.ainin_fecha_memo, i.ainin_fecha)) <= :fechaCorte " +
            "  AND (TO_CHAR(i.ainin_estado) = '1' OR i.ainin_estado = 1) " +
            "  AND ( " +
            "      EXISTS ( " +
            "          SELECT 1 FROM AI_CORTE_EVALUACION p " +
            "          WHERE p.arcev_codigo = :arcevCodigo AND UPPER(TRIM(p.arcev_tipo)) != 'CIERRE_ANUAL' " +
            "            AND TRUNC(COALESCE(i.ainin_fecha_aprobacion, i.ainin_fecha_memo, i.ainin_fecha)) BETWEEN p.arcev_fecha_inicio AND p.arcev_fecha_fin " +
            "      ) " +
            "      OR EXISTS ( " +
            "          SELECT 1 FROM AI_CORTE_EVALUACION p " +
            "          WHERE p.arcev_codigo = :arcevCodigo AND UPPER(TRIM(p.arcev_tipo)) = 'CIERRE_ANUAL' " +
            "            AND EXISTS ( " +
            "                SELECT 1 FROM AI_REC_HALLAZGOS h " +
            "                JOIN AI_REC_CONCLUSIONES c ON h.areha_codigo = c.areha_codigo " +
            "                JOIN AI_REC_RECOMENDACIONES r ON c.areco_codigo = r.areco_codigo " +
            "                JOIN AI_REC_ESTADO_SEGUIMIENTO es ON r.arees_codigo = es.arees_codigo " +
            "                WHERE h.ainin_codigo = i.ainin_codigo " +
            "                  AND (TO_CHAR(r.arere_estado) = '1' OR r.arere_estado = 1) " +
            "                  AND ( " +
            "                      r.arees_codigo NOT IN (2, 4) " +
            "                      OR (r.arees_codigo = 2 AND r.arere_fecha_cumplida IS NOT NULL AND TRUNC(r.arere_fecha_cumplida) > :fechaCorte) " +
            "                  ) " +
            "                  AND UPPER(TRIM(es.arees_descripcion)) NOT IN ('NO APLICA') " +
            "                  AND EXISTS ( " +
            "                      SELECT 1 FROM AI_REC_DETALLE_RECOMENDACION dr_sub " +
            "                      JOIN AI_REC_RESPONSABLES res_sub ON dr_sub.arers_codigo = res_sub.arers_codigo AND res_sub.arers_estado = 1 " +
            "                      LEFT JOIN ( " +
            "                          SELECT v_sub.*, ROW_NUMBER() OVER (PARTITION BY v_sub.aredr_codigo ORDER BY CASE WHEN v_sub.areev_codigo = 2 THEN 1 WHEN v_sub.areev_codigo = 1 THEN 2 WHEN v_sub.areev_codigo = 3 THEN 3 ELSE 4 END ASC, NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha) DESC NULLS LAST, v_sub.areve_codigo DESC) AS rn_sub " +
            "                          FROM AI_REC_VERIFICABLES v_sub " +
            "                          WHERE TO_CHAR(v_sub.areve_estado) = '1' " +
            "                            AND (TRUNC(NVL(v_sub.areve_fecha_carga, v_sub.areve_fecha)) <= :fechaCorte OR v_sub.areve_fecha_carga IS NULL) " +
            "                      ) vr_sub ON dr_sub.aredr_codigo = vr_sub.aredr_codigo AND vr_sub.rn_sub = 1 " +
            "                      LEFT JOIN AI_REC_ESTADO_VERIFICABLE ev_sub ON vr_sub.areev_codigo = ev_sub.areev_codigo " +
            "                      WHERE dr_sub.arere_codigo = r.arere_codigo AND dr_sub.aredr_estado = 1 " +
            "                        AND (vr_sub.areev_codigo IS NULL OR (vr_sub.areev_codigo != 2 AND UPPER(TRIM(NVL(ev_sub.areev_descripcion, ''))) NOT IN ('JUSTIFICA', 'JUSTIFICADO', 'JUSTIFICADA'))) " +
            "                  ) " +
            "            ) " +
            "      ) " +
            "  ) " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM AI_CORTE_EVALUACION_INFORME ci " +
            "      WHERE ci.arcev_codigo = :arcevCodigo AND ci.ainin_codigo = i.ainin_codigo " +
            "  )", nativeQuery = true)
    void autoAsignarInformesPorFechaCorte(@Param("arcevCodigo") Long arcevCodigo, @Param("fechaCorte") Date fechaCorte);
}
