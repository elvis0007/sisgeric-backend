-- Migration script to replace the ObtenerMetricasResponsableS1 procedure with updated logic
-- Evaluates individual deadline (COALESCE(dr.aredr_fecha_maxima, rec.arere_fecha_maxima)) per responsible

CREATE OR REPLACE PROCEDURE obtenermetricasresponsables1 (
    p_json            OUT CLOB,
    arers_usuario_per IN VARCHAR2
) IS
    rec_pend_cumplimiento          NUMBER;
    solicitudes_reprog_pend        NUMBER;
    rec_vencidas_sin_justificar    NUMBER;
    rec_vencidas_justificadas      NUMBER;
    verificables_pend              NUMBER;
    usu_per                        VARCHAR2(100) := arers_usuario_per;
    v_es_director                  NUMBER := 0;
    v_estado_aprob                 NUMBER := 1;
BEGIN
    -- Métrica 1: recomendaciones pendientes de cumplimiento (vigentes) para este responsable
    SELECT COUNT(*)
    INTO rec_pend_cumplimiento
    FROM ai_rec_detalle_recomendacion dr
    JOIN ai_rec_responsables    res ON dr.arers_codigo = res.arers_codigo
    JOIN ai_rec_recomendaciones rec ON dr.arere_codigo = rec.arere_codigo
    WHERE res.arers_usuario_per = usu_per
      AND rec.arere_estado = 1
      AND dr.aredr_estado = 1
      AND res.arers_estado = 1
      AND rec.arees_codigo NOT IN (2, 4)
      AND NOT EXISTS (
          SELECT 1 FROM ai_rec_verificables v
          WHERE v.aredr_codigo = dr.aredr_codigo
            AND v.areve_estado = 1
            AND v.areev_codigo = 2
      )
      AND TRUNC(COALESCE(
          (SELECT MAX(S.ARESO_FECHA_MAX_NUE) 
           FROM AI_REC_SOLICITUDES S 
           WHERE S.ARERE_CODIGO = dr.ARERE_CODIGO 
             AND S.ARERS_CODIGO = dr.ARERS_CODIGO 
             AND S.ARESO_ESTADO = '1' 
             AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(SYSDATE)))
             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),
          dr.aredr_fecha_maxima,
          rec.arere_fecha_maxima
      )) >= TRUNC(SYSDATE);

    -- Métrica 2: solicitudes de reprogramación pendientes
    SELECT COUNT(*)
    INTO v_es_director
    FROM AI_SEG_ROL_USU
    WHERE UPPER(TRIM(ASERU_CUENTA)) = UPPER(TRIM(usu_per))
      AND ASERO_CODIGO = 5
      AND (ASERU_ESTADO = '1' OR ASERU_ESTADO = 1);

    IF v_es_director > 0 THEN
        v_estado_aprob := 2;
    ELSE
        v_estado_aprob := 1;
    END IF;

    SELECT COUNT(*)
    INTO solicitudes_reprog_pend
    FROM ai_rec_solicitudes solic
    JOIN ai_rec_responsables rec ON solic.arers_codigo = rec.arers_codigo
    WHERE solic.areso_estado_aprob = v_estado_aprob
      AND rec.arers_usuario_per = usu_per
      AND solic.areso_estado = 1
      AND rec.arers_estado = 1;

    -- Métrica 3a: recomendaciones vencidas sin justificar para este responsable
    SELECT COUNT(*)
    INTO rec_vencidas_sin_justificar
    FROM ai_rec_detalle_recomendacion dr
    JOIN ai_rec_responsables    res ON dr.arers_codigo = res.arers_codigo
    JOIN ai_rec_recomendaciones rec ON dr.arere_codigo = rec.arere_codigo
    WHERE res.arers_usuario_per = usu_per
      AND rec.arere_estado = 1
      AND dr.aredr_estado = 1
      AND res.arers_estado = 1
      AND rec.arees_codigo NOT IN (2, 4)
      AND NOT EXISTS (
          SELECT 1 FROM ai_rec_verificables v
          WHERE v.aredr_codigo = dr.aredr_codigo
            AND v.areve_estado = 1
            AND v.areev_codigo = 2
      )
      AND TRUNC(COALESCE(
          (SELECT MAX(S.ARESO_FECHA_MAX_NUE) 
           FROM AI_REC_SOLICITUDES S 
           WHERE S.ARERE_CODIGO = dr.ARERE_CODIGO 
             AND S.ARERS_CODIGO = dr.ARERS_CODIGO 
             AND S.ARESO_ESTADO = '1' 
             AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(SYSDATE)))
             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),
          dr.aredr_fecha_maxima,
          rec.arere_fecha_maxima
      )) < TRUNC(SYSDATE);

    -- Métrica 3b: recomendaciones vencidas justificadas para este responsable
    SELECT COUNT(*)
    INTO rec_vencidas_justificadas
    FROM ai_rec_detalle_recomendacion dr
    JOIN ai_rec_responsables    res ON dr.arers_codigo = res.arers_codigo
    JOIN ai_rec_recomendaciones rec ON dr.arere_codigo = rec.arere_codigo
    WHERE res.arers_usuario_per = usu_per
      AND rec.arere_estado = 1
      AND dr.aredr_estado = 1
      AND res.arers_estado = 1
      AND rec.arees_codigo NOT IN (2, 4)
      AND EXISTS (
          SELECT 1 FROM ai_rec_verificables v
          WHERE v.aredr_codigo = dr.aredr_codigo
            AND v.areve_estado = 1
            AND v.areev_codigo = 2
      )
      AND TRUNC(COALESCE(
          (SELECT MAX(S.ARESO_FECHA_MAX_NUE) 
           FROM AI_REC_SOLICITUDES S 
           WHERE S.ARERE_CODIGO = dr.ARERE_CODIGO 
             AND S.ARERS_CODIGO = dr.ARERS_CODIGO 
             AND S.ARESO_ESTADO = '1' 
             AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(SYSDATE)))
             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),
          dr.aredr_fecha_maxima,
          rec.arere_fecha_maxima
      )) < TRUNC(SYSDATE);

    -- Métrica 4: Verificables pendientes
    verificables_pend := rec_pend_cumplimiento;

    -- Build JSON response
    SELECT JSON_OBJECT(
            'recomendacionesPendientes'             VALUE rec_pend_cumplimiento,
            'solicitudesPendientesAprobacion'      VALUE solicitudes_reprog_pend,
            'recomendacionesVencidasSinJustificar' VALUE rec_vencidas_sin_justificar,
            'recomendacionesVencidasJustificadas'  VALUE rec_vencidas_justificadas,
            'verificablesPendientes'               VALUE verificables_pend
        RETURNING CLOB)
    INTO p_json
    FROM dual;

EXCEPTION
    WHEN OTHERS THEN
        p_json := JSON_OBJECT('error' VALUE sqlerrm);
END obtenermetricasresponsables1;
/
