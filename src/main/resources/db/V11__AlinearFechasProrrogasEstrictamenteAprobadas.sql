-- =============================================================================
-- MIGRACIÓN V11: Alinear cálculo de fechas de prórrogas estrictamente a solicitudes APROBADAS (ARESO_ESTADO_APROB = '3')
-- =============================================================================

CREATE OR REPLACE FUNCTION ROOTSISREC.FN_RECOMENDACIONES_INFORME(
    P_AININ_CODIGO IN NUMBER,
    P_DEPARTAMENTO IN VARCHAR2   
) RETURN SYS_REFCURSOR
IS
    RESULTADO SYS_REFCURSOR;
    v_clean_dpto VARCHAR2(250);
BEGIN
    IF P_DEPARTAMENTO IS NOT NULL AND P_DEPARTAMENTO <> '0' THEN
        v_clean_dpto := TRIM(REPLACE(REPLACE(REPLACE(UPPER(P_DEPARTAMENTO), 'DPTO.', ''), 'DEPARTAMENTO', ''), 'DIRECCION', ''));
    END IF;

    IF v_clean_dpto IS NOT NULL THEN
        -- Vista contextualizada al departamento del Director: fecha máxima y estado según su departamento
        OPEN RESULTADO FOR
            SELECT 
                R.ARERE_CODIGO,
                R.ARERE_DESCRIPCION,
                R.ARERE_NUM_REC,
                R.ARERE_OBS_EN_PROCESO,
                R.ARERE_OBS_EN_VALIDACION,
                R.ARERE_OBS_NO_APLICA,
                R.ARERE_OBS_NO_CUMPLIDA,
                R.ARERE_USUARIO,
                R.ARERE_FECHA_INICIO_CONTROL,
                R.ARERE_FECHA,
                MAX(COALESCE(
                    (SELECT MAX(S.ARESO_FECHA_MAX_NUE)
                     FROM ROOTSISREC.AI_REC_SOLICITUDES S
                     WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO
                       AND S.ARERS_CODIGO = DR.ARERS_CODIGO
                       AND S.ARESO_ESTADO = '1'
                       AND S.ARESO_ESTADO_APROB = '3'
                       AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),
                    DR.AREDR_FECHA_MAXIMA,
                    R.ARERE_FECHA_MAXIMA
                )) AS ARERE_FECHA_MAXIMA,
                R.ARERE_FECHA_CORTE,
                R.ARERE_ESTADO,
                R.ARERE_NUM_PRORROGA,
                CASE
                    WHEN R.AREES_CODIGO = 4 THEN 4
                    WHEN COUNT(DISTINCT RS.ARERS_CODIGO) > 0 
                     AND COUNT(DISTINCT CASE WHEN V.AREEV_CODIGO = 2 THEN RS.ARERS_CODIGO END) = COUNT(DISTINCT RS.ARERS_CODIGO) THEN 2
                    WHEN COUNT(DISTINCT CASE 
                            WHEN NVL(V.AREEV_CODIGO, 0) <> 2 
                             AND TRUNC(COALESCE(
                                 (SELECT MAX(S.ARESO_FECHA_MAX_NUE)
                                  FROM ROOTSISREC.AI_REC_SOLICITUDES S
                                  WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO
                                    AND S.ARERS_CODIGO = DR.ARERS_CODIGO
                                    AND S.ARESO_ESTADO = '1'
                                    AND S.ARESO_ESTADO_APROB = '3'
                                    AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),
                                 DR.AREDR_FECHA_MAXIMA,
                                 R.ARERE_FECHA_MAXIMA
                             )) < TRUNC(SYSDATE) THEN RS.ARERS_CODIGO END) > 0 THEN 3
                    ELSE 1
                END AS AREES_CODIGO,
                (
                 CASE
                     WHEN COUNT(DISTINCT RS.ARERS_CODIGO) = 0 THEN 0
                     ELSE
                     ROUND(COUNT(DISTINCT CASE WHEN V.AREEV_CODIGO = 2 THEN RS.ARERS_CODIGO END) / COUNT(DISTINCT RS.ARERS_CODIGO), 4)
                 END
                ) AS PORCENTAJE,
                COUNT(DISTINCT RS.ARERS_CODIGO) AS TOTAL_RESPONSABLES,
                COUNT(DISTINCT CASE WHEN V.AREEV_CODIGO = 2 THEN RS.ARERS_CODIGO END) AS RESPONSABLES_CUMPLIDOS,
                COUNT(DISTINCT CASE 
                    WHEN NVL(V.AREEV_CODIGO, 0) <> 2 AND TRUNC(COALESCE(
                        (SELECT MAX(S.ARESO_FECHA_MAX_NUE)
                         FROM ROOTSISREC.AI_REC_SOLICITUDES S
                         WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO
                           AND S.ARERS_CODIGO = DR.ARERS_CODIGO
                           AND S.ARESO_ESTADO = '1'
                           AND S.ARESO_ESTADO_APROB = '3'
                           AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),
                        DR.AREDR_FECHA_MAXIMA,
                        R.ARERE_FECHA_MAXIMA
                    )) < TRUNC(SYSDATE) THEN RS.ARERS_CODIGO END) AS RESPONSABLES_VENCIDOS
            FROM ROOTSISREC.AI_INF_INFORMES I
            JOIN ROOTSISREC.AI_REC_HALLAZGOS H ON I.AININ_CODIGO = H.AININ_CODIGO
            JOIN ROOTSISREC.AI_REC_CONCLUSIONES C ON H.AREHA_CODIGO = C.AREHA_CODIGO
            JOIN ROOTSISREC.AI_REC_RECOMENDACIONES R ON C.ARECO_CODIGO = R.ARECO_CODIGO
            JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON R.ARERE_CODIGO = DR.ARERE_CODIGO AND DR.AREDR_ESTADO = 1
            JOIN ROOTSISREC.AI_REC_RESPONSABLES RS ON DR.ARERS_CODIGO = RS.ARERS_CODIGO AND RS.ARERS_ESTADO = 1
            LEFT JOIN ROOTSISREC.AI_REC_VERIFICABLES V ON DR.AREDR_CODIGO = V.AREDR_CODIGO AND V.AREVE_ESTADO = 1
            WHERE I.AININ_CODIGO = P_AININ_CODIGO
              AND I.AININ_ESTADO = 1
              AND H.AREHA_ESTADO = 1
              AND C.ARECO_ESTADO = 1
              AND R.ARERE_ESTADO = 1
              AND (
                  RS.ARERS_DEPARTAMENTO = P_DEPARTAMENTO
                  OR UPPER(RS.ARERS_DEPARTAMENTO) LIKE '%' || v_clean_dpto || '%'
                  OR UPPER(RS.ARERS_DEPARTAMENTO_ABREV) LIKE '%' || v_clean_dpto || '%'
              )
            GROUP BY
                R.ARERE_CODIGO,
                R.ARERE_DESCRIPCION,
                R.ARERE_OBS_EN_PROCESO,
                R.ARERE_OBS_EN_VALIDACION,
                R.ARERE_OBS_NO_APLICA,
                R.ARERE_OBS_NO_CUMPLIDA,
                R.ARERE_USUARIO,
                R.ARERE_ESTADO,
                R.ARERE_NUM_REC,
                R.ARERE_FECHA,
                R.ARERE_FECHA_INICIO_CONTROL,
                R.ARERE_NUM_PRORROGA,
                R.ARERE_FECHA_CORTE,
                R.AREES_CODIGO;
    ELSE
        -- Vista global (Auditor Interno / General): fecha máxima mayor y estado global
        OPEN RESULTADO FOR
            SELECT 
                R.ARERE_CODIGO,
                R.ARERE_DESCRIPCION,
                R.ARERE_NUM_REC,
                R.ARERE_OBS_EN_PROCESO,
                R.ARERE_OBS_EN_VALIDACION,
                R.ARERE_OBS_NO_APLICA,
                R.ARERE_OBS_NO_CUMPLIDA,
                R.ARERE_USUARIO,
                R.ARERE_FECHA_INICIO_CONTROL,
                R.ARERE_FECHA,
                MAX(COALESCE(
                    (SELECT MAX(S.ARESO_FECHA_MAX_NUE)
                     FROM ROOTSISREC.AI_REC_SOLICITUDES S
                     WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO
                       AND S.ARERS_CODIGO = DR.ARERS_CODIGO
                       AND S.ARESO_ESTADO = '1'
                       AND S.ARESO_ESTADO_APROB = '3'
                       AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),
                    DR.AREDR_FECHA_MAXIMA,
                    R.ARERE_FECHA_MAXIMA
                )) AS ARERE_FECHA_MAXIMA,
                R.ARERE_FECHA_CORTE,
                R.ARERE_ESTADO,
                R.ARERE_NUM_PRORROGA,
                R.AREES_CODIGO,
                (
                 CASE
                     WHEN COUNT(DISTINCT RS.ARERS_CODIGO) = 0 THEN 0
                     ELSE
                     ROUND(COUNT(DISTINCT CASE WHEN V.AREEV_CODIGO = 2 THEN RS.ARERS_CODIGO END) / COUNT(DISTINCT RS.ARERS_CODIGO), 4)
                 END
                ) AS PORCENTAJE,
                COUNT(DISTINCT RS.ARERS_CODIGO) AS TOTAL_RESPONSABLES,
                COUNT(DISTINCT CASE WHEN V.AREEV_CODIGO = 2 THEN RS.ARERS_CODIGO END) AS RESPONSABLES_CUMPLIDOS,
                COUNT(DISTINCT CASE 
                    WHEN NVL(V.AREEV_CODIGO, 0) <> 2 AND TRUNC(COALESCE(
                        (SELECT MAX(S.ARESO_FECHA_MAX_NUE)
                         FROM ROOTSISREC.AI_REC_SOLICITUDES S
                         WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO
                           AND S.ARERS_CODIGO = DR.ARERS_CODIGO
                           AND S.ARESO_ESTADO = '1'
                           AND S.ARESO_ESTADO_APROB = '3'
                           AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),
                        DR.AREDR_FECHA_MAXIMA,
                        R.ARERE_FECHA_MAXIMA
                    )) < TRUNC(SYSDATE) THEN RS.ARERS_CODIGO END) AS RESPONSABLES_VENCIDOS
            FROM ROOTSISREC.AI_INF_INFORMES I
            JOIN ROOTSISREC.AI_REC_HALLAZGOS H ON I.AININ_CODIGO = H.AININ_CODIGO
            JOIN ROOTSISREC.AI_REC_CONCLUSIONES C ON H.AREHA_CODIGO = C.AREHA_CODIGO
            JOIN ROOTSISREC.AI_REC_RECOMENDACIONES R ON C.ARECO_CODIGO = R.ARECO_CODIGO
            JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON R.ARERE_CODIGO = DR.ARERE_CODIGO AND DR.AREDR_ESTADO = 1
            JOIN ROOTSISREC.AI_REC_RESPONSABLES RS ON DR.ARERS_CODIGO = RS.ARERS_CODIGO AND RS.ARERS_ESTADO = 1
            LEFT JOIN ROOTSISREC.AI_REC_VERIFICABLES V ON DR.AREDR_CODIGO = V.AREDR_CODIGO AND V.AREVE_ESTADO = 1
            WHERE I.AININ_CODIGO = P_AININ_CODIGO
              AND I.AININ_ESTADO = 1
              AND H.AREHA_ESTADO = 1
              AND C.ARECO_ESTADO = 1
              AND R.ARERE_ESTADO = 1
            GROUP BY
                R.ARERE_CODIGO,
                R.ARERE_DESCRIPCION,
                R.ARERE_OBS_EN_PROCESO,
                R.ARERE_OBS_EN_VALIDACION,
                R.ARERE_OBS_NO_APLICA,
                R.ARERE_OBS_NO_CUMPLIDA,
                R.ARERE_USUARIO,
                R.ARERE_ESTADO,
                R.ARERE_NUM_REC,
                R.ARERE_FECHA,
                R.ARERE_FECHA_INICIO_CONTROL,
                R.ARERE_NUM_PRORROGA,
                R.ARERE_FECHA_CORTE,
                R.AREES_CODIGO;
    END IF;

    RETURN RESULTADO;
EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20001, 'Error en FN_RECOMENDACIONES_INFORME: ' || SQLERRM);
END FN_RECOMENDACIONES_INFORME;
/

-- =============================================================================
-- PROCEDIMIENTO OBTENERMETRICASRESPONSABLES1
-- Conteo de solicitudes en espera considerando estados '1' (Pendiente) y '2' (Preaprobada Director)
-- y cálculo de fechas de prórrogas estrictamente aprobadas (ARESO_ESTADO_APROB = '3')
-- =============================================================================

CREATE OR REPLACE PROCEDURE ROOTSISREC.OBTENERMETRICASRESPONSABLES1 (
    p_json            OUT CLOB,
    arers_usuario_per IN VARCHAR2
) IS
    rec_pend_cumplimiento          NUMBER;
    solicitudes_reprog_pend        NUMBER;
    rec_vencidas_sin_justificar    NUMBER;
    rec_vencidas_justificadas      NUMBER;
    verificables_pend              NUMBER;
    usu_per                        VARCHAR2(100) := arers_usuario_per;
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
             AND S.ARESO_ESTADO_APROB = '3'
             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),
          dr.aredr_fecha_maxima,
          rec.arere_fecha_maxima
      )) >= TRUNC(SYSDATE);

    -- Métrica 2: solicitudes de prórroga/reprogramación en espera ('1' PENDIENTE o '2' PREAPROBADA DIRECTOR)
    SELECT COUNT(*)
    INTO solicitudes_reprog_pend
    FROM ai_rec_solicitudes solic
    JOIN ai_rec_responsables rec ON solic.arers_codigo = rec.arers_codigo
    WHERE solic.areso_estado_aprob IN ('1', '2')
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
             AND S.ARESO_ESTADO_APROB = '3'
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
             AND S.ARESO_ESTADO_APROB = '3'
             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL),
          dr.aredr_fecha_maxima,
          rec.arere_fecha_maxima
      )) < TRUNC(SYSDATE);

    -- Métrica 4: Verificables pendientes
    verificables_pend := rec_pend_cumplimiento;

    -- Construcción del JSON de retorno
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
        p_json := JSON_OBJECT('error' VALUE SQLERRM);
END OBTENERMETRICASRESPONSABLES1;
/

-- =============================================================================
-- CORRECCIÓN DE ESTADO: Recomendación 5 (ARERE_CODIGO = 841) del Informe AI-099-2026
-- Se corrige de CUMPLIDA (2) a EN PROCESO (1) ya que sólo 1 de los 2 responsables tiene verificable JUSTIFICA
-- =============================================================================
UPDATE ROOTSISREC.AI_REC_RECOMENDACIONES
SET AREES_CODIGO = 1
WHERE ARERE_CODIGO = 841
  AND ARERE_ESTADO = 1;

COMMIT;
