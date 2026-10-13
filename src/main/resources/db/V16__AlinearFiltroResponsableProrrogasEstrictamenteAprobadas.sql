-- =============================================================================
-- MIGRACIÓN V16: Alinear FN_RECOMENDACIONES_FILTROS_R a prórrogas estrictamente aprobadas
-- Las solicitudes de ampliación pendientes de aprobación (ARESO_ESTADO_APROB in ('1', '2'))
-- NO deben extender el plazo ni mover la recomendación vencida al filtro de PENDIENTES.
-- La recomendación debe permanecer en NO CUMPLIDAS hasta que la solicitud sea formalmente APROBADA (ARESO_ESTADO_APROB = '3').
-- =============================================================================

CREATE OR REPLACE FUNCTION ROOTSISREC.FN_RECOMENDACIONES_FILTROS_R (
    p_anio          IN NUMBER,
    p_arees_codigo  IN NUMBER,
    p_arers_codper  IN VARCHAR2
) RETURN SYS_REFCURSOR
IS
    v_cursor   SYS_REFCURSOR;
    v_sql      VARCHAR2(4000);
BEGIN
    v_sql :=
      'WITH solicitudes_prorroga AS (
           SELECT S.ARERE_CODIGO, S.ARERS_CODIGO, MAX(S.ARESO_FECHA_MAX_NUE) AS FECHA_PRORROGA
           FROM ROOTSISREC.AI_REC_SOLICITUDES S
           WHERE S.ARESO_ESTADO = ''1''
             AND S.ARESO_ESTADO_APROB = ''3''
             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL
           GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO
       )
       SELECT DISTINCT D.*, R.ARERE_FECHA_MAXIMA
         FROM ROOTSISREC.AI_INF_INFORMES            I
         JOIN ROOTSISREC.AI_REC_HALLAZGOS           H ON H.AININ_CODIGO = I.AININ_CODIGO
         JOIN ROOTSISREC.AI_REC_CONCLUSIONES        C ON C.AREHA_CODIGO = H.AREHA_CODIGO
         JOIN ROOTSISREC.AI_REC_RECOMENDACIONES     R ON R.ARECO_CODIGO = C.ARECO_CODIGO
         JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION D ON D.ARERE_CODIGO = R.ARERE_CODIGO
         JOIN ROOTSISREC.AI_REC_RESPONSABLES        S ON S.ARERS_CODIGO = D.ARERS_CODIGO
         LEFT JOIN solicitudes_prorroga             SP ON SP.ARERE_CODIGO = D.ARERE_CODIGO AND SP.ARERS_CODIGO = D.ARERS_CODIGO
        WHERE R.ARERE_ESTADO = 1
          AND I.AININ_ESTADO = 1
          AND D.AREDR_ESTADO = 1
          AND S.ARERS_ESTADO = 1
          AND S.ARERS_CODIGO_PER = ''' || p_arers_codper || '''';

    IF p_arees_codigo IS NOT NULL AND p_arees_codigo <> 0 THEN
        IF p_arees_codigo = 1 THEN
            v_sql := v_sql || ' AND (
                R.AREES_CODIGO != 2
                AND (COALESCE(SP.FECHA_PRORROGA, D.AREDR_FECHA_MAXIMA, R.ARERE_FECHA_MAXIMA) IS NULL 
                     OR TRUNC(COALESCE(SP.FECHA_PRORROGA, D.AREDR_FECHA_MAXIMA, R.ARERE_FECHA_MAXIMA)) >= TRUNC(SYSDATE))
                AND NOT EXISTS (
                    SELECT 1 FROM ROOTSISREC.AI_REC_VERIFICABLES V 
                    WHERE V.AREDR_CODIGO = D.AREDR_CODIGO AND V.AREVE_ESTADO = 1 AND V.AREEV_CODIGO = 2
                )
            )';
        ELSIF p_arees_codigo = 2 THEN
            v_sql := v_sql || ' AND (
                R.AREES_CODIGO = 2
                OR EXISTS (
                    SELECT 1 FROM ROOTSISREC.AI_REC_VERIFICABLES V 
                    WHERE V.AREDR_CODIGO = D.AREDR_CODIGO AND V.AREVE_ESTADO = 1 AND V.AREEV_CODIGO = 2
                )
            )';
        ELSIF p_arees_codigo = 3 THEN
            v_sql := v_sql || ' AND (
                R.AREES_CODIGO NOT IN (2, 4)
                AND TRUNC(COALESCE(SP.FECHA_PRORROGA, D.AREDR_FECHA_MAXIMA, R.ARERE_FECHA_MAXIMA)) < TRUNC(SYSDATE)
                AND NOT EXISTS (
                    SELECT 1 FROM ROOTSISREC.AI_REC_VERIFICABLES V 
                    WHERE V.AREDR_CODIGO = D.AREDR_CODIGO AND V.AREVE_ESTADO = 1 AND V.AREEV_CODIGO = 2
                )
            )';
        END IF;
    END IF;

    IF p_anio IS NOT NULL AND p_anio <> 0 THEN
        v_sql := v_sql || ' AND EXTRACT(YEAR FROM NVL(R.ARERE_FECHA_INICIO_CONTROL, NVL(I.AININ_FECHA_APROBACION, I.AININ_FECHA_MEMO))) = ' || p_anio;
    END IF;

    v_sql := v_sql || ' ORDER BY COALESCE(D.AREDR_FECHA_MAXIMA, R.ARERE_FECHA_MAXIMA) DESC';

    OPEN v_cursor FOR v_sql;
    RETURN v_cursor;
EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20001, 'Error en FN_RECOMENDACIONES_FILTROS_R: ' || SQLERRM);
END FN_RECOMENDACIONES_FILTROS_R;
