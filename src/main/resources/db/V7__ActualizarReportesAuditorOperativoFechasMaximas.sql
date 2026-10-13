-- V7__ActualizarReportesAuditorOperativoFechasMaximas.sql
-- Actualización de los procedimientos de reportes del Auditor Operativo:
-- 1. reportePorDepartamentoEstadoSeguimiento (/report-est-seg-oper)
-- 2. REPORTERECOMENDACIONESVENCIDASOPERATIVO (/report-recomendaciones-vencidas-operativo)
-- 3. reporteRecomendacionesProgreso (/report-detalle-est-seg)
--
-- Características implementadas:
-- - Regla central: la fecha global de cada recomendación corresponde al responsable con fecha más actual:
--   MAX(COALESCE(prorroga_aprobada, fecha_individual_responsable, fecha_general_rec))
-- - En el reporte de vencidas operativo: se clasifica como vencida si y solo si la fecha más actual entre sus responsables ya expiró (plazo < 0) y no está cumplida.
-- - En el reporte por departamento y seguimiento: se expone la fecha global calculada y el detalle de plazo individual por responsable.
-- - En el reporte de progreso de recomendaciones: el conteo de En Proceso, Cumplidas y Vencidas se evalúa con la fecha más actual del responsable.
-- - Agrupados y ordenados por informe (antiguo a actual) y recomendaciones (del 1 en adelante).

-- ============================================================================
-- 1. reportePorDepartamentoEstadoSeguimiento
-- ============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.reportePorDepartamentoEstadoSeguimiento
(
    p_json                  OUT     CLOB,
    fecha_inicial           IN      VARCHAR2,
    fecha_fin               IN      VARCHAR2,
    dpto                    IN      VARCHAR2,
    estado_seguimiento      IN      INTEGER,
    auditor_responsable     IN      VARCHAR2
)
IS
    v_fecha_ini   TIMESTAMP;
    v_fecha_fin   TIMESTAMP;
BEGIN
    IF fecha_inicial IS NULL OR TRIM(fecha_inicial) IS NULL THEN
        v_fecha_ini := TO_TIMESTAMP('01/01/1900 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\d{2}/\d{2}/\d{4}$') THEN
        v_fecha_ini := TO_TIMESTAMP(TRIM(fecha_inicial) || ' 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\d{4}-\d{2}-\d{2}') THEN
        v_fecha_ini := TO_TIMESTAMP(SUBSTR(TRIM(fecha_inicial), 1, 10) || ' 00:00:00', 'YYYY-MM-DD HH24:MI:SS');
    ELSE
        v_fecha_ini := TO_TIMESTAMP(TRIM(fecha_inicial), 'DD/MM/YYYY HH24:MI:SS');
    END IF;

    IF fecha_fin IS NULL OR TRIM(fecha_fin) IS NULL THEN
        v_fecha_fin := TO_TIMESTAMP('31/12/2999 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\d{2}/\d{2}/\d{4}$') THEN
        v_fecha_fin := TO_TIMESTAMP(TRIM(fecha_fin) || ' 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\d{4}-\d{2}-\d{2}') THEN
        v_fecha_fin := TO_TIMESTAMP(SUBSTR(TRIM(fecha_fin), 1, 10) || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS');
    ELSE
        v_fecha_fin := TO_TIMESTAMP(TRIM(fecha_fin), 'DD/MM/YYYY HH24:MI:SS');
    END IF;

    WITH ultimas_prorrogas AS (
        SELECT 
            S.ARERE_CODIGO,
            S.ARERS_CODIGO,
            MAX(S.ARESO_FECHA_MAX_NUE) AS FECHA_PRORROGA
        FROM ROOTSISREC.AI_REC_SOLICITUDES S
        WHERE S.ARESO_ESTADO = '1'
          AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))
          AND S.ARESO_FECHA_MAX_NUE IS NOT NULL
        GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO
    ),
    ultimos_verificables AS (
        SELECT
            v.AREDR_CODIGO,
            v.AREEV_CODIGO,
            v.AREVE_OBSERVACION,
            ROW_NUMBER() OVER (
                PARTITION BY v.AREDR_CODIGO
                ORDER BY NVL(v.AREVE_FECHA_CARGA, v.AREVE_FECHA) DESC NULLS LAST, v.AREVE_CODIGO DESC
            ) AS rn
        FROM ROOTSISREC.AI_REC_VERIFICABLES v
        WHERE v.AREVE_ESTADO = 1
    ),
    recomendaciones_base AS (
        SELECT
            REC.ARERE_CODIGO,
            REC.ARERE_NUM_REC,
            TO_NUMBER(REGEXP_SUBSTR(REC.ARERE_NUM_REC, '^\d+')) AS numRec,
            INF.AININ_CODIGO,
            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) AS informeFechaOrden,
            INF.AININ_FECHA_APROBACION,
            SUBSTR(INF.AININ_MEMORANDO, 1, 1000)       AS AININ_MEMORANDO,
            SUBSTR(INF.AININ_MEMO_APROBACION, 1, 1000) AS AININ_MEMO_APROBACION,
            SUBSTR(ORG.AINOC_DESCRIPCION, 1, 1000)     AS AINOC_DESCRIPCION,
            SUBSTR(INF.AININ_TITULO, 1, 2000)          AS AININ_TITULO,
            SUBSTR(CON.ARECO_CONCLUSION, 1, 3900)      AS ARECO_CONCLUSION,
            SUBSTR(REC.ARERE_DESCRIPCION, 1, 3900)     AS ARERE_DESCRIPCION,
            REC.ARERE_FECHA_MAXIMA,
            REC.AREES_CODIGO,
            (
                SELECT NVL(SUBSTR(VER.AREVE_DESCRIPCION, 1, 1500), 'NO EXISTEN VERIFICABLES')
                FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR
                LEFT JOIN ROOTSISREC.AI_REC_VERIFICABLES VER ON DR.AREDR_CODIGO = VER.AREDR_CODIGO
                WHERE DR.ARERE_CODIGO = REC.ARERE_CODIGO
                  AND DR.AREDR_ESTADO = 1
                  AND VER.AREVE_ESTADO = 1
                  AND ROWNUM = 1
            ) AS situacion_actual
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO
        WHERE (INF.AININ_FECHA_APROBACION IS NULL OR INF.AININ_FECHA_APROBACION BETWEEN v_fecha_ini AND v_fecha_fin)
          AND (REC.AREES_CODIGO = estado_seguimiento OR estado_seguimiento = 0)
          AND (auditor_responsable = '0' OR INF.AININ_USUARIO = auditor_responsable)
          AND (dpto = '0' OR REC.ARERE_CODIGO IN (
              SELECT DR.ARERE_CODIGO
              FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR
              JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO
              WHERE RES.ARERS_DEPARTAMENTO = dpto
                AND DR.AREDR_ESTADO = 1
                AND RES.ARERS_ESTADO = 1
          ))
          AND REC.ARERE_ESTADO = 1
          AND INF.AININ_ESTADO = 1
          AND HALL.AREHA_ESTADO = 1
          AND CON.ARECO_ESTADO = 1
          AND ORG.AINOC_ESTADO = 1
    ),
    fechas_maximas_rec AS (
        SELECT 
            DR.ARERE_CODIGO,
            MAX(COALESCE(PR.FECHA_PRORROGA, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)) AS FECHA_MAXIMA_GLOBAL
        FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARERE_CODIGO = DR.ARERE_CODIGO
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO
        LEFT JOIN ultimas_prorrogas PR ON PR.ARERE_CODIGO = DR.ARERE_CODIGO AND PR.ARERS_CODIGO = DR.ARERS_CODIGO
        WHERE DR.AREDR_ESTADO = 1 AND RES.ARERS_ESTADO = 1
        GROUP BY DR.ARERE_CODIGO
    ),
    departamentos_agrupados AS (
        SELECT
            t.ARERE_CODIGO,
            RTRIM(
                XMLCAST(
                    XMLAGG(XMLELEMENT(e, t.ARERS_DEPARTAMENTO || ', ') ORDER BY t.ARERS_DEPARTAMENTO) AS CLOB
                ),
                ', '
            ) AS departamento
        FROM (
            SELECT DISTINCT
                DR.ARERE_CODIGO,
                SUBSTR(RES.ARERS_DEPARTAMENTO, 1, 100) AS ARERS_DEPARTAMENTO
            FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR
            JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO
            WHERE (dpto = '0' OR RES.ARERS_DEPARTAMENTO = dpto)
              AND DR.AREDR_ESTADO = 1
              AND RES.ARERS_ESTADO = 1
        ) t
        GROUP BY t.ARERE_CODIGO
    ),
    responsables_asignados AS (
        SELECT 
            DR.ARERE_CODIGO,
            DR.AREDR_CODIGO,
            RES.ARERS_NOMBRE,
            RES.ARERS_APELLIDO,
            COALESCE(PR.FECHA_PRORROGA, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) AS FECHA_INDIVIDUAL,
            FN_CALCULAR_DIAS_LABORABLES(COALESCE(PR.FECHA_PRORROGA, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)) AS PLAZO_INDIVIDUAL,
            ROW_NUMBER() OVER (
                PARTITION BY DR.ARERE_CODIGO, DR.ARERS_CODIGO
                ORDER BY DR.AREDR_CODIGO DESC
            ) AS rn_dedup
        FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARERE_CODIGO = DR.ARERE_CODIGO
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO
        LEFT JOIN ultimas_prorrogas PR ON PR.ARERE_CODIGO = DR.ARERE_CODIGO AND PR.ARERS_CODIGO = DR.ARERS_CODIGO
        WHERE (dpto = '0' OR RES.ARERS_DEPARTAMENTO = dpto)
          AND DR.AREDR_ESTADO = 1
          AND RES.ARERS_ESTADO = 1
    ),
    responsables_detalle AS (
        SELECT
            ra.ARERE_CODIGO,
            ra.ARERS_NOMBRE,
            ra.ARERS_APELLIDO,
            ra.FECHA_INDIVIDUAL,
            ra.PLAZO_INDIVIDUAL,
            NVL(EV.AREEV_DESCRIPCION, 'VERIFICABLE NO CREADO') AS estado_verificable,
            uv.AREVE_OBSERVACION AS observacion_auditora,
            ROW_NUMBER() OVER (
                PARTITION BY ra.ARERE_CODIGO
                ORDER BY ra.ARERS_NOMBRE, ra.ARERS_APELLIDO, ra.AREDR_CODIGO
            ) AS r_num
        FROM responsables_asignados ra
        LEFT JOIN ultimos_verificables uv ON uv.AREDR_CODIGO = ra.AREDR_CODIGO AND uv.rn = 1
        LEFT JOIN ROOTSISREC.AI_REC_ESTADO_VERIFICABLE EV ON EV.AREEV_CODIGO = uv.AREEV_CODIGO
        WHERE ra.rn_dedup = 1
    ),
    responsables_agrupados AS (
        SELECT
            rd.ARERE_CODIGO,
            RTRIM(
                XMLCAST(
                    XMLAGG(
                        XMLELEMENT(
                            e,
                            'R' || rd.r_num || ': ' || rd.ARERS_NOMBRE || ' ' || rd.ARERS_APELLIDO ||
                            ' (' || rd.estado_verificable || '), '
                        ) ORDER BY rd.r_num
                    ) AS CLOB
                ),
                ', '
            ) AS funcionario_responsable
        FROM responsables_detalle rd
        GROUP BY rd.ARERE_CODIGO
    ),
    observaciones_agrupadas AS (
        SELECT
            rd.ARERE_CODIGO,
            RTRIM(
                XMLCAST(
                    XMLAGG(
                        XMLELEMENT(
                            e,
                            'OBS' || rd.r_num || ' - R' || rd.r_num || ': ' ||
                            NVL(TRIM(REGEXP_REPLACE(REPLACE(REPLACE(rd.observacion_auditora, CHR(13), ' '), CHR(10), ' '), '\s+', ' ')), 'SIN OBSERVACION') || '; '
                        ) ORDER BY rd.r_num
                    ) AS CLOB
                ),
                '; '
            ) AS observacion_auditora
        FROM responsables_detalle rd
        GROUP BY rd.ARERE_CODIGO
    ),
    datos_finales AS (
        SELECT
            rb.AININ_CODIGO AS codigo_informe,
            rb.informeFechaOrden,
            rb.numRec,
            rb.AININ_FECHA_APROBACION AS fecha_aprobacion_informe,
            rb.ARERE_CODIGO AS codigo_recomendacion,
            rb.ARERE_NUM_REC AS numero_recomendacion,
            rb.AININ_MEMORANDO AS memorando,
            rb.AININ_MEMO_APROBACION AS memo_aprobacion,
            rb.AINOC_DESCRIPCION AS organismo_control,
            NVL(TO_CHAR(SUBSTR(dptoag.departamento, 1, 3900)), 'SIN DEPARTAMENTO') AS departamento,
            NVL(TO_CHAR(SUBSTR(respag.funcionario_responsable, 1, 3900)), 'SIN RESPONSABLE') AS funcionario_responsable,
            rb.AININ_TITULO AS titulo_informe,
            rb.ARECO_CONCLUSION AS conclusion,
            rb.ARERE_DESCRIPCION AS recomendacion,
            rb.situacion_actual,
            NVL(TO_CHAR(SUBSTR(obsag.observacion_auditora, 1, 3900)), 'SIN OBSERVACION') AS observacion_auditora,
            COALESCE(fmr.FECHA_MAXIMA_GLOBAL, rb.ARERE_FECHA_MAXIMA) AS fecha_maxima
        FROM recomendaciones_base rb
        LEFT JOIN fechas_maximas_rec fmr ON fmr.ARERE_CODIGO = rb.ARERE_CODIGO
        LEFT JOIN departamentos_agrupados dptoag ON dptoag.ARERE_CODIGO = rb.ARERE_CODIGO
        LEFT JOIN responsables_agrupados respag ON respag.ARERE_CODIGO = rb.ARERE_CODIGO
        LEFT JOIN observaciones_agrupadas obsag ON obsag.ARERE_CODIGO = rb.ARERE_CODIGO
    )
    SELECT JSON_ARRAYAGG(
               JSON_OBJECT(
                   'codigoRecomendacion' VALUE codigo_recomendacion,
                   'codigoInforme' VALUE codigo_informe,
                   'memorandoAI' VALUE memorando,
                   'memorandoAprobacion' VALUE memo_aprobacion,
                   'organismoControl' VALUE organismo_control,
                   'departamento' VALUE departamento,
                   'funcionarioResponsable' VALUE funcionario_responsable,
                   'tituloInforme' VALUE titulo_informe,
                   'conclusion' VALUE conclusion,
                   'recomendacion' VALUE recomendacion,
                   'numeroRecomendacion' VALUE numero_recomendacion,
                   'fechaAprobacion' VALUE TO_CHAR(fecha_aprobacion_informe, 'YYYY-MM-DD"T"HH24:MI:SS'),
                   'situacionActual' VALUE situacion_actual,
                   'observacionAuditora' VALUE observacion_auditora,
                   'observacionVerificable' VALUE observacion_auditora,
                   'fechaMaximaRecomendacion' VALUE TO_CHAR(fecha_maxima, 'YYYY-MM-DD')
                   RETURNING CLOB
               )
               ORDER BY informeFechaOrden ASC NULLS LAST,
                        codigo_informe ASC,
                        numRec ASC NULLS LAST,
                        codigo_recomendacion ASC
               RETURNING CLOB
           )
      INTO p_json
      FROM datos_finales;

    IF p_json IS NULL THEN
        p_json := TO_CLOB('{"mensaje":"No se encontraron registros"}');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB('{"error":"' || REPLACE(REPLACE(SUBSTR(SQLERRM, 1, 3000), CHR(10), ' '), '"', '\"') || '"}');
END reportePorDepartamentoEstadoSeguimiento;
/

-- ============================================================================
-- 2. REPORTERECOMENDACIONESVENCIDASOPERATIVO
-- ============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.REPORTERECOMENDACIONESVENCIDASOPERATIVO (
    p_json              OUT CLOB,
    fecha_inicial       IN  VARCHAR2,
    fecha_fin           IN  VARCHAR2,
    estado_seguimiento  IN  INTEGER,
    auditor_responsable IN  VARCHAR2
) IS
    v_fecha_ini    TIMESTAMP;
    v_fecha_fin    TIMESTAMP;
BEGIN
    IF fecha_inicial IS NULL OR TRIM(fecha_inicial) IS NULL THEN
        v_fecha_ini := TO_TIMESTAMP('01/01/1900 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\d{2}/\d{2}/\d{4}$') THEN
        v_fecha_ini := TO_TIMESTAMP(TRIM(fecha_inicial) || ' 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\d{2}/\d{2}/\d{4} \d{2}:\d{2}:\d{2}') THEN
        v_fecha_ini := TO_TIMESTAMP(TRIM(fecha_inicial), 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\d{4}-\d{2}-\d{2}') THEN
        v_fecha_ini := TO_TIMESTAMP(SUBSTR(TRIM(fecha_inicial), 1, 10) || ' 00:00:00', 'YYYY-MM-DD HH24:MI:SS');
    ELSE
        v_fecha_ini := TO_TIMESTAMP('01/01/1900 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
    END IF;

    IF fecha_fin IS NULL OR TRIM(fecha_fin) IS NULL THEN
        v_fecha_fin := TO_TIMESTAMP('31/12/2999 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\d{2}/\d{2}/\d{4}$') THEN
        v_fecha_fin := TO_TIMESTAMP(TRIM(fecha_fin) || ' 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\d{2}/\d{2}/\d{4} \d{2}:\d{2}:\d{2}') THEN
        v_fecha_fin := TO_TIMESTAMP(TRIM(fecha_fin), 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\d{4}-\d{2}-\d{2}') THEN
        v_fecha_fin := TO_TIMESTAMP(SUBSTR(TRIM(fecha_fin), 1, 10) || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS');
    ELSE
        v_fecha_fin := TO_TIMESTAMP('31/12/2999 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    END IF;

    WITH ultimas_prorrogas AS (
        SELECT 
            S.ARERE_CODIGO,
            S.ARERS_CODIGO,
            MAX(S.ARESO_FECHA_MAX_NUE) AS FECHA_PRORROGA
        FROM ROOTSISREC.AI_REC_SOLICITUDES S
        WHERE S.ARESO_ESTADO = '1'
          AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))
          AND S.ARESO_FECHA_MAX_NUE IS NOT NULL
        GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO
    ),
    ultimos_verificables AS (
        SELECT 
            VER.AREDR_CODIGO,
            VER.AREVE_CODIGO,
            VER.AREEV_CODIGO,
            VER.AREVE_OBSERVACION,
            ROW_NUMBER() OVER (PARTITION BY VER.AREDR_CODIGO ORDER BY VER.AREVE_FECHA_CARGA DESC NULLS LAST, VER.AREVE_CODIGO DESC) AS RN
        FROM ROOTSISREC.AI_REC_VERIFICABLES VER
        WHERE VER.AREVE_ESTADO = 1
    ),
    asignaciones_rec AS (
        SELECT 
            DET.ARERE_CODIGO,
            COALESCE(PR.FECHA_PRORROGA, DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) AS FECHA_INDIVIDUAL,
            UV.AREEV_CODIGO,
            TRIM(RESP.ARERS_NOMBRE || ' ' || RESP.ARERS_APELLIDO) ||
            CASE 
                WHEN UV.AREVE_CODIGO IS NULL THEN ' (VERIFICABLE NO CREADO)'
                WHEN UV.AREEV_CODIGO = 1 THEN ' (PENDIENTE)'
                WHEN UV.AREEV_CODIGO = 2 THEN ' (JUSTIFICA)'
                WHEN UV.AREEV_CODIGO = 3 THEN ' (NO JUSTIFICA)'
                ELSE ' (VERIFICABLE NO CREADO)'
            END AS RESPONSABLE_CON_ESTADO
        FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DET
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARERE_CODIGO = DET.ARERE_CODIGO
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RESP ON RESP.ARERS_CODIGO = DET.ARERS_CODIGO
        LEFT JOIN ultimas_prorrogas PR ON PR.ARERE_CODIGO = DET.ARERE_CODIGO AND PR.ARERS_CODIGO = DET.ARERS_CODIGO
        LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DET.AREDR_CODIGO AND UV.RN = 1
        WHERE DET.AREDR_ESTADO = 1 AND RESP.ARERS_ESTADO = 1 AND REC.ARERE_ESTADO = 1
    ),
    resumen_rec AS (
        SELECT 
            ARERE_CODIGO,
            MAX(FECHA_INDIVIDUAL) AS FECHA_MAXIMA_GLOBAL,
            COUNT(1) AS TOTAL_RESPONSABLES,
            COUNT(CASE WHEN AREEV_CODIGO = 2 THEN 1 END) AS TOTAL_CUMPLIDOS,
            RTRIM(XMLCAST(XMLAGG(XMLELEMENT(e, RESPONSABLE_CON_ESTADO || ', ') ORDER BY RESPONSABLE_CON_ESTADO) AS CLOB), ', ') AS LISTA_RESPONSABLES
        FROM asignaciones_rec
        GROUP BY ARERE_CODIGO
    ),
    recomendaciones_evaluadas AS (
        SELECT 
            INF.AININ_CODIGO AS aininCodigo,
            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) AS informeFechaOrden,
            INF.AININ_TITULO AS tituloInforme,
            REC.ARERE_DESCRIPCION AS recomendacion,
            ORG.AINOC_DESCRIPCION AS organismoControl,
            INF.AININ_MEMORANDO AS memorando,
            INF.AININ_MEMO_APROBACION AS memoAprobacion,
            '' AS observacion,
            TO_CHAR(NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA_APROBACION), 'DD/MM/YYYY') AS fechaEmision,
            COALESCE(RES.FECHA_MAXIMA_GLOBAL, REC.ARERE_FECHA_MAXIMA) AS fechaMaximaDate,
            TO_CHAR(COALESCE(RES.FECHA_MAXIMA_GLOBAL, REC.ARERE_FECHA_MAXIMA), 'DD/MM/YYYY') AS fechaMaxima,
            FN_CALCULAR_DIAS_LABORABLES(COALESCE(RES.FECHA_MAXIMA_GLOBAL, REC.ARERE_FECHA_MAXIMA)) AS plazo,
            NVL(TO_CHAR(SUBSTR(RES.LISTA_RESPONSABLES, 1, 3900)), 'SIN RESPONSABLE') AS responsables,
            REC.ARERE_CODIGO AS arereCodigo,
            TO_NUMBER(REGEXP_SUBSTR(REC.ARERE_NUM_REC, '^\d+')) AS numRec,
            REC.AREES_CODIGO AS estadoSegRec,
            NVL(RES.TOTAL_RESPONSABLES, 0) AS totalResp,
            NVL(RES.TOTAL_CUMPLIDOS, 0) AS cumplidosResp
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO
        LEFT JOIN resumen_rec RES ON RES.ARERE_CODIGO = REC.ARERE_CODIGO
        WHERE INF.AININ_ESTADO = 1
          AND HALL.AREHA_ESTADO = 1
          AND CON.ARECO_ESTADO = 1
          AND REC.ARERE_ESTADO = 1
          AND ORG.AINOC_ESTADO = 1
          AND (NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) BETWEEN v_fecha_ini AND v_fecha_fin)
          AND (auditor_responsable = '0' OR INF.AININ_USUARIO = auditor_responsable)
          AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)
          -- Excluir si la recomendación ya está cumplida o no aplica:
          AND REC.AREES_CODIGO NOT IN (2, 4)
          AND NOT (RES.TOTAL_RESPONSABLES > 0 AND RES.TOTAL_CUMPLIDOS = RES.TOTAL_RESPONSABLES)
    )
    SELECT JSON_ARRAYAGG(
               JSON_OBJECT(
                   'tituloInforme'    VALUE tituloInforme,
                   'recomendacion'    VALUE recomendacion,
                   'organismoControl' VALUE organismoControl,
                   'memorando'        VALUE memorando,
                   'memoAprobacion'   VALUE memoAprobacion,
                   'observacion'      VALUE observacion,
                   'fechaEmision'     VALUE fechaEmision,
                   'fechaMaxima'      VALUE fechaMaxima,
                   'plazo'            VALUE plazo,
                   'responsables'     VALUE responsables,
                   'arereCodigo'      VALUE arereCodigo
                   RETURNING CLOB
               )
               ORDER BY informeFechaOrden ASC NULLS LAST,
                        aininCodigo ASC,
                        numRec ASC NULLS LAST,
                        arereCodigo ASC
               RETURNING CLOB
           )
    INTO p_json
    FROM recomendaciones_evaluadas
    WHERE plazo < 0;

    IF p_json IS NULL THEN
        p_json := TO_CLOB(JSON_OBJECT('mensaje' VALUE 'No se encontraron recomendaciones vencidas con los criterios especificados'));
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END REPORTERECOMENDACIONESVENCIDASOPERATIVO;
/

-- ============================================================================
-- 3. reporteRecomendacionesProgreso
-- ============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.reporteRecomendacionesProgreso (
    p_json              OUT CLOB,
    fecha_inicial       IN  VARCHAR2,
    fecha_fin           IN  VARCHAR2,
    auditor_responsable IN  VARCHAR2,
    org_control         IN  INTEGER
) IS
    v_fecha_ini TIMESTAMP;
    v_fecha_fin TIMESTAMP;
BEGIN
    IF fecha_inicial IS NULL OR TRIM(fecha_inicial) IS NULL THEN
        v_fecha_ini := TO_TIMESTAMP('01/01/1900 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\d{2}/\d{2}/\d{4}$') THEN
        v_fecha_ini := TO_TIMESTAMP(TRIM(fecha_inicial) || ' 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\d{4}-\d{2}-\d{2}') THEN
        v_fecha_ini := TO_TIMESTAMP(SUBSTR(TRIM(fecha_inicial), 1, 10) || ' 00:00:00', 'YYYY-MM-DD HH24:MI:SS');
    ELSE
        v_fecha_ini := TO_TIMESTAMP('01/01/1900 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
    END IF;

    IF fecha_fin IS NULL OR TRIM(fecha_fin) IS NULL THEN
        v_fecha_fin := TO_TIMESTAMP('31/12/2999 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\d{2}/\d{2}/\d{4}$') THEN
        v_fecha_fin := TO_TIMESTAMP(TRIM(fecha_fin) || ' 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\d{4}-\d{2}-\d{2}') THEN
        v_fecha_fin := TO_TIMESTAMP(SUBSTR(TRIM(fecha_fin), 1, 10) || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS');
    ELSE
        v_fecha_fin := TO_TIMESTAMP('31/12/2999 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    END IF;

    WITH ultimas_prorrogas AS (
        SELECT 
            S.ARERE_CODIGO,
            S.ARERS_CODIGO,
            MAX(S.ARESO_FECHA_MAX_NUE) AS FECHA_PRORROGA
        FROM ROOTSISREC.AI_REC_SOLICITUDES S
        WHERE S.ARESO_ESTADO = '1'
          AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))
          AND S.ARESO_FECHA_MAX_NUE IS NOT NULL
        GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO
    ),
    ultimos_verificables AS (
        SELECT 
            VER.AREDR_CODIGO,
            VER.AREEV_CODIGO,
            ROW_NUMBER() OVER (PARTITION BY VER.AREDR_CODIGO ORDER BY VER.AREVE_FECHA_CARGA DESC NULLS LAST, VER.AREVE_CODIGO DESC) AS RN
        FROM ROOTSISREC.AI_REC_VERIFICABLES VER
        WHERE VER.AREVE_ESTADO = 1
    ),
    asignaciones_detalle AS (
        SELECT 
            DET.ARERE_CODIGO,
            DET.AREDR_CODIGO,
            RESP.ARERS_DEPARTAMENTO,
            COALESCE(PR.FECHA_PRORROGA, DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) AS FECHA_INDIVIDUAL,
            UV.AREEV_CODIGO
        FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DET
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARERE_CODIGO = DET.ARERE_CODIGO
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RESP ON RESP.ARERS_CODIGO = DET.ARERS_CODIGO
        LEFT JOIN ultimas_prorrogas PR ON PR.ARERE_CODIGO = DET.ARERE_CODIGO AND PR.ARERS_CODIGO = DET.ARERS_CODIGO
        LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DET.AREDR_CODIGO AND UV.RN = 1
        WHERE DET.AREDR_ESTADO = 1 AND RESP.ARERS_ESTADO = 1 AND REC.ARERE_ESTADO = 1
    ),
    resumen_rec AS (
        SELECT 
            ARERE_CODIGO,
            MAX(FECHA_INDIVIDUAL) AS FECHA_MAX_GLOBAL,
            COUNT(1) AS TOTAL_ASIGNACIONES,
            COUNT(CASE WHEN AREEV_CODIGO = 2 THEN 1 END) AS CUMPLIDOS,
            COUNT(CASE WHEN AREEV_CODIGO <> 2 AND (FECHA_INDIVIDUAL IS NOT NULL AND FN_CALCULAR_DIAS_LABORABLES(FECHA_INDIVIDUAL) < 0) THEN 1 END) AS VENCIDOS
        FROM asignaciones_detalle
        GROUP BY ARERE_CODIGO
    ),
    recomendaciones_evaluadas AS (
        SELECT 
            INF.AININ_CODIGO,
            REC.ARERE_CODIGO,
            REC.AREES_CODIGO,
            NVL(RR.TOTAL_ASIGNACIONES, 0) AS totalAsigRec,
            CASE 
                WHEN REC.AREES_CODIGO = 4 THEN 'NO APLICA'
                WHEN REC.AREES_CODIGO = 2 OR (RR.TOTAL_ASIGNACIONES > 0 AND RR.CUMPLIDOS = RR.TOTAL_ASIGNACIONES) THEN 'CUMPLIDA'
                WHEN RR.FECHA_MAX_GLOBAL IS NOT NULL AND FN_CALCULAR_DIAS_LABORABLES(RR.FECHA_MAX_GLOBAL) < 0 THEN 'NO CUMPLIDA'
                WHEN RR.FECHA_MAX_GLOBAL IS NULL AND REC.ARERE_FECHA_MAXIMA IS NOT NULL AND FN_CALCULAR_DIAS_LABORABLES(REC.ARERE_FECHA_MAXIMA) < 0 THEN 'NO CUMPLIDA'
                ELSE 'EN PROCESO'
            END AS estadoFinalRec
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO
        LEFT JOIN resumen_rec RR ON RR.ARERE_CODIGO = REC.ARERE_CODIGO
        WHERE INF.AININ_ESTADO = 1
          AND HALL.AREHA_ESTADO = 1
          AND CON.ARECO_ESTADO = 1
          AND REC.ARERE_ESTADO = 1
          AND ORG.AINOC_ESTADO = 1
          AND (NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) BETWEEN v_fecha_ini AND v_fecha_fin)
          AND (auditor_responsable = '0' OR INF.AININ_USUARIO = auditor_responsable)
          AND (org_control = 0 OR INF.AINOC_CODIGO = org_control)
    ),
    departamentos_por_informe AS (
        SELECT 
            INF.AININ_CODIGO,
            RTRIM(XMLCAST(XMLAGG(XMLELEMENT(e, t.ARERS_DEPARTAMENTO || ', ') ORDER BY t.ARERS_DEPARTAMENTO) AS CLOB), ', ') AS DEPARTAMENTOS
        FROM (
            SELECT DISTINCT HALL.AININ_CODIGO, AD.ARERS_DEPARTAMENTO
            FROM ROOTSISREC.AI_REC_HALLAZGOS HALL
            JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
            JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
            JOIN asignaciones_detalle AD ON AD.ARERE_CODIGO = REC.ARERE_CODIGO
            WHERE HALL.AREHA_ESTADO = 1 AND CON.ARECO_ESTADO = 1 AND REC.ARERE_ESTADO = 1
        ) t
        JOIN ROOTSISREC.AI_INF_INFORMES INF ON INF.AININ_CODIGO = t.AININ_CODIGO
        GROUP BY INF.AININ_CODIGO
    ),
    informes_agrupados AS (
        SELECT 
            INF.AININ_CODIGO AS aininCodigo,
            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) AS informeFechaOrden,
            INF.AININ_TITULO AS tituloInforme,
            INF.AININ_MEMORANDO AS memorandoAI,
            INF.AININ_MEMO_APROBACION AS memorandoPresidencia,
            ORG.AINOC_DESCRIPCION AS organismoControl,
            NVL(TO_CHAR(SUBSTR(DPI.DEPARTAMENTOS, 1, 3900)), 'SIN DEPARTAMENTO') AS departamentos,
            INF.AININ_USUARIO AS auditorInforme,
            NVL(SUM(RE.totalAsigRec), 0) AS totalAsignaciones,
            COUNT(RE.ARERE_CODIGO) AS totalRecomendaciones,
            COUNT(CASE WHEN RE.estadoFinalRec = 'EN PROCESO' THEN 1 END) AS recomendacionesEnProceso,
            COUNT(CASE WHEN RE.estadoFinalRec = 'CUMPLIDA' THEN 1 END) AS recomendacionesCumplidas,
            COUNT(CASE WHEN RE.estadoFinalRec = 'NO CUMPLIDA' THEN 1 END) AS recomendacionesNoCumplidas,
            COUNT(CASE WHEN RE.estadoFinalRec = 'NO APLICA' THEN 1 END) AS recomendacionesNoAplica,
            CASE 
                WHEN COUNT(RE.ARERE_CODIGO) > 0 THEN 
                    ROUND((COUNT(CASE WHEN RE.estadoFinalRec = 'CUMPLIDA' THEN 1 END) * 100.0) / COUNT(RE.ARERE_CODIGO), 2)
                ELSE 0
            END AS progreso
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO
        JOIN recomendaciones_evaluadas RE ON RE.AININ_CODIGO = INF.AININ_CODIGO
        LEFT JOIN departamentos_por_informe DPI ON DPI.AININ_CODIGO = INF.AININ_CODIGO
        GROUP BY 
            INF.AININ_CODIGO,
            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO),
            INF.AININ_TITULO,
            INF.AININ_MEMORANDO,
            INF.AININ_MEMO_APROBACION,
            ORG.AINOC_DESCRIPCION,
            NVL(TO_CHAR(SUBSTR(DPI.DEPARTAMENTOS, 1, 3900)), 'SIN DEPARTAMENTO'),
            INF.AININ_USUARIO
    )
    SELECT JSON_ARRAYAGG(
               JSON_OBJECT(
                   'tituloInforme'              VALUE tituloInforme,
                   'memorandoAI'                VALUE memorandoAI,
                   'memorandoPresidencia'       VALUE memorandoPresidencia,
                   'organismoControl'           VALUE organismoControl,
                   'departamentos'              VALUE departamentos,
                   'auditorInforme'             VALUE auditorInforme,
                   'totalAsignaciones'          VALUE totalAsignaciones,
                   'totalRecomendaciones'       VALUE totalRecomendaciones,
                   'recomendacionesEnProceso'   VALUE recomendacionesEnProceso,
                   'recomendacionesCumplidas'   VALUE recomendacionesCumplidas,
                   'recomendacionesNoCumplidas' VALUE recomendacionesNoCumplidas,
                   'recomendacionesNoAplica'    VALUE recomendacionesNoAplica,
                   'progreso'                   VALUE progreso
                   RETURNING CLOB
               )
               ORDER BY informeFechaOrden ASC NULLS LAST, aininCodigo ASC
               RETURNING CLOB
           )
    INTO p_json
    FROM informes_agrupados;

    IF p_json IS NULL THEN
        p_json := TO_CLOB('{"mensaje":"No se encontraron registros"}');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB('{"error":"' || REPLACE(REPLACE(SUBSTR(SQLERRM, 1, 3000), CHR(10), ' '), '"', '\"') || '"}');
END reporteRecomendacionesProgreso;
/

-- =============================================================================
-- PROCEDIMIENTO: reporteSeguimientoResponsableOperativo
-- DESCRIPCIÓN: Reporte de seguimiento individual por responsable para Auditor Operativo,
--              filtrando por el auditor asignado al informe (o todos si auditor_responsable='0'),
--              calculando fecha efectiva individual con prórrogas aprobadas/vigentes,
--              estado individual de recomendación y estado/días de plazo.
-- =============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.reporteSeguimientoResponsableOperativo (
    p_json              OUT CLOB,
    fecha_inicial       IN  VARCHAR2,
    fecha_fin           IN  VARCHAR2,
    organismo_control   IN  INTEGER,
    estado_seguimiento  IN  INTEGER,
    auditor_responsable IN  VARCHAR2,
    dpto                IN  VARCHAR2
) IS
    v_fecha_ini TIMESTAMP;
    v_fecha_fin TIMESTAMP;
BEGIN
    IF fecha_inicial IS NULL OR TRIM(fecha_inicial) IS NULL THEN
        v_fecha_ini := TO_TIMESTAMP('01/01/1900 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\d{2}/\d{2}/\d{4}$') THEN
        v_fecha_ini := TO_TIMESTAMP(TRIM(fecha_inicial) || ' 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\d{2}/\d{2}/\d{4} \d{2}:\d{2}:\d{2}') THEN
        v_fecha_ini := TO_TIMESTAMP(TRIM(fecha_inicial), 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_inicial), '^\d{4}-\d{2}-\d{2}') THEN
        v_fecha_ini := TO_TIMESTAMP(SUBSTR(TRIM(fecha_inicial), 1, 10) || ' 00:00:00', 'YYYY-MM-DD HH24:MI:SS');
    ELSE
        v_fecha_ini := TO_TIMESTAMP('01/01/1900 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
    END IF;

    IF fecha_fin IS NULL OR TRIM(fecha_fin) IS NULL THEN
        v_fecha_fin := TO_TIMESTAMP('31/12/2999 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\d{2}/\d{2}/\d{4}$') THEN
        v_fecha_fin := TO_TIMESTAMP(TRIM(fecha_fin) || ' 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\d{2}/\d{2}/\d{4} \d{2}:\d{2}:\d{2}') THEN
        v_fecha_fin := TO_TIMESTAMP(TRIM(fecha_fin), 'DD/MM/YYYY HH24:MI:SS');
    ELSIF REGEXP_LIKE(TRIM(fecha_fin), '^\d{4}-\d{2}-\d{2}') THEN
        v_fecha_fin := TO_TIMESTAMP(SUBSTR(TRIM(fecha_fin), 1, 10) || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS');
    ELSE
        v_fecha_fin := TO_TIMESTAMP('31/12/2999 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    END IF;

    WITH ultimas_prorrogas AS (
        SELECT 
            S.ARERE_CODIGO,
            S.ARERS_CODIGO,
            MAX(S.ARESO_FECHA_MAX_NUE) AS fecha_prorroga
        FROM ROOTSISREC.AI_REC_SOLICITUDES S
        WHERE S.ARESO_ESTADO = '1'
          AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))
          AND S.ARESO_FECHA_MAX_NUE IS NOT NULL
        GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO
    ),
    ultimos_verificables AS (
        SELECT 
            V.AREDR_CODIGO,
            V.AREEV_CODIGO,
            V.AREVE_DESCRIPCION,
            V.AREVE_OBSERVACION,
            V.AREVE_FECHA_CARGA,
            ROW_NUMBER() OVER (
                PARTITION BY V.AREDR_CODIGO
                ORDER BY V.AREVE_FECHA_CARGA DESC NULLS LAST, V.AREVE_CODIGO DESC
            ) AS rn
        FROM ROOTSISREC.AI_REC_VERIFICABLES V
        WHERE V.AREVE_ESTADO = 1
    ),
    progreso_recomendacion AS (
        SELECT
            DR.ARERE_CODIGO,
            COUNT(DISTINCT DR.ARERS_CODIGO) AS total_responsables,
            COUNT(DISTINCT CASE WHEN UV.AREEV_CODIGO = 2 THEN DR.ARERS_CODIGO END) AS responsables_cumplidos
        FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO AND RES.ARERS_ESTADO = 1
        LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DR.AREDR_CODIGO AND UV.rn = 1
        WHERE DR.AREDR_ESTADO = 1
        GROUP BY DR.ARERE_CODIGO
    ),
    progreso_informe AS (
        SELECT
            INF.AININ_CODIGO,
            COUNT(DISTINCT REC.ARERE_CODIGO) AS total_recs,
            COUNT(DISTINCT CASE 
                WHEN REC.AREES_CODIGO = 2 OR (PR.total_responsables > 0 AND PR.responsables_cumplidos = PR.total_responsables) 
                THEN REC.ARERE_CODIGO 
            END) AS recs_cumplidas
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO AND HALL.AREHA_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO AND CON.ARECO_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO AND REC.ARERE_ESTADO = 1
        LEFT JOIN progreso_recomendacion PR ON PR.ARERE_CODIGO = REC.ARERE_CODIGO
        WHERE INF.AININ_ESTADO = 1
        GROUP BY INF.AININ_CODIGO
    ),
    datos_base AS (
        SELECT
            INF.AININ_CODIGO                                        AS codigoinforme,
            INF.AININ_MEMORANDO                                     AS memoauditoria,
            TO_CHAR(NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA_APROBACION), 'DD/MM/YYYY') AS fechamemorando,
            INF.AININ_MEMO_APROBACION                               AS memopresidencia,
            TO_CHAR(INF.AININ_FECHA_APROBACION, 'DD/MM/YYYY')       AS fechaaprobacion,
            INF.AININ_USUARIO                                       AS auditor,
            TO_CHAR(INF.AININ_FECHA, 'DD/MM/YYYY')                  AS fechacreacioninforme,
            TO_CHAR(INF.AININ_TITULO)                               AS tituloinforme,
            ORG.AINOC_DESCRIPCION                                   AS organismocontrol,
            TO_CHAR(HALL.AREHA_TITULO)                              AS hallazgo,
            CON.ARECO_CONCLUSION                                    AS conclusion,
            REC.ARERE_CODIGO                                        AS codigorecomendacion,
            TO_CHAR(REC.ARERE_DESCRIPCION)                          AS recomendacion,
            COALESCE(UP.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) AS fecha_efectiva,
            UV.AREEV_CODIGO                                         AS areev_codigo,
            REC.AREES_CODIGO                                        AS arees_codigo,
            TO_CHAR(UV.AREVE_FECHA_CARGA, 'DD/MM/YYYY HH24:MI:SS')  AS fechacargaverificable,
            TRIM(RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO)     AS responsable,
            COALESCE(EV.AREEV_DESCRIPCION, 'VERIFICABLE NO CREADO') AS estadoverificable,
            RES.ARERS_DEPARTAMENTO                                  AS departamento,
            UV.AREVE_DESCRIPCION                                    AS descripcionverificableresponsable,
            UV.AREVE_OBSERVACION                                    AS observacionauditor,
            NVL(PREC.total_responsables, 0)                         AS total_resp_rec,
            NVL(PREC.responsables_cumplidos, 0)                     AS resp_cump_rec,
            NVL(PINF.total_recs, 0)                                  AS total_recs_inf,
            NVL(PINF.recs_cumplidas, 0)                              AS recs_cump_inf,
            TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS')               AS fechaconsulta,
            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO)   AS fecha_orden,
            TO_NUMBER(REGEXP_SUBSTR(REC.ARERE_NUM_REC, '^\d+'))     AS num_rec
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO AND HALL.AREHA_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO AND CON.ARECO_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO AND REC.ARERE_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO AND DR.AREDR_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO AND RES.ARERS_ESTADO = 1
        LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DR.AREDR_CODIGO AND UV.rn = 1
        LEFT JOIN ROOTSISREC.AI_REC_ESTADO_VERIFICABLE EV ON EV.AREEV_CODIGO = UV.AREEV_CODIGO
        LEFT JOIN ultimas_prorrogas UP ON UP.ARERE_CODIGO = DR.ARERE_CODIGO AND UP.ARERS_CODIGO = DR.ARERS_CODIGO
        LEFT JOIN progreso_recomendacion PREC ON PREC.ARERE_CODIGO = REC.ARERE_CODIGO
        LEFT JOIN progreso_informe PINF ON PINF.AININ_CODIGO = INF.AININ_CODIGO
        WHERE INF.AININ_ESTADO = 1
          AND ORG.AINOC_ESTADO = 1
          AND (NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) BETWEEN v_fecha_ini AND v_fecha_fin)
          AND (organismo_control = 0 OR ORG.AINOC_CODIGO = organismo_control)
          AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)
          AND (auditor_responsable IS NULL OR auditor_responsable = '0' OR UPPER(TRIM(INF.AININ_USUARIO)) = UPPER(TRIM(auditor_responsable)))
          AND (dpto IS NULL OR dpto = '0' OR RES.ARERS_DEPARTAMENTO = dpto OR UPPER(TRIM(RES.ARERS_DEPARTAMENTO)) = UPPER(TRIM(dpto)))
    ),
    datos_calculados AS (
        SELECT
            codigoinforme,
            memoauditoria,
            fechamemorando,
            memopresidencia,
            fechaaprobacion,
            auditor,
            fechacreacioninforme,
            tituloinforme,
            organismocontrol,
            hallazgo,
            conclusion,
            codigorecomendacion,
            recomendacion,
            TO_CHAR(fecha_efectiva, 'DD/MM/YYYY') AS fechavencimiento,
            CASE
                WHEN areev_codigo = 2 THEN 'CUMPLIDA'
                WHEN arees_codigo = 4 THEN 'NO APLICA'
                WHEN fecha_efectiva IS NULL OR TRUNC(fecha_efectiva) >= TRUNC(CURRENT_TIMESTAMP) THEN 'EN PROCESO'
                ELSE 'NO CUMPLIDA'
            END AS estadorecomendacion,
            CASE
                WHEN areev_codigo = 2 OR arees_codigo = 4 THEN 'A TIEMPO'
                WHEN fecha_efectiva IS NULL OR TRUNC(fecha_efectiva) >= TRUNC(CURRENT_TIMESTAMP) THEN 'EN PLAZO'
                ELSE 'VENCIDO'
            END AS estadoplazo,
            CASE
                WHEN areev_codigo = 2 OR arees_codigo = 4 THEN 0
                WHEN fecha_efectiva IS NULL THEN 0
                WHEN FN_CALCULAR_DIAS_LABORABLES(fecha_efectiva) < 0 THEN ABS(FN_CALCULAR_DIAS_LABORABLES(fecha_efectiva))
                ELSE FN_CALCULAR_DIAS_LABORABLES(fecha_efectiva)
            END AS diasvencidos,
            CASE
                WHEN arees_codigo = 2 THEN 100.0
                WHEN total_resp_rec > 0 THEN ROUND((resp_cump_rec * 100.0) / total_resp_rec, 2)
                ELSE 0.0
            END AS progresorecomendacion,
            fechacargaverificable,
            responsable,
            estadoverificable,
            departamento,
            descripcionverificableresponsable,
            observacionauditor,
            CASE
                WHEN total_recs_inf > 0 THEN ROUND((recs_cump_inf * 100.0) / total_recs_inf, 2)
                ELSE 0.0
            END AS progresoinforme,
            fechaconsulta,
            fecha_orden,
            num_rec
        FROM datos_base
    )
    SELECT JSON_ARRAYAGG(
               JSON_OBJECT(
                   'codigoInforme'                    VALUE NVL(codigoinforme, 0),
                   'memoAuditoria'                    VALUE NVL(memoauditoria, ''),
                   'fechaMemorando'                   VALUE NVL(fechamemorando, ''),
                   'memoPresidencia'                  VALUE NVL(memopresidencia, ''),
                   'fechaAprobacion'                  VALUE NVL(fechaaprobacion, ''),
                   'auditor'                          VALUE NVL(auditor, ''),
                   'fechaCreacionInforme'             VALUE NVL(fechacreacioninforme, ''),
                   'tituloInforme'                    VALUE NVL(tituloinforme, ''),
                   'organismoControl'                 VALUE NVL(organismocontrol, ''),
                   'hallazgo'                         VALUE NVL(hallazgo, ''),
                   'conclusion'                       VALUE NVL(conclusion, ''),
                   'codigoRecomendacion'              VALUE NVL(codigorecomendacion, 0),
                   'numeroRecomendacion'              VALUE NVL(num_rec, 0),
                   'recomendacion'                    VALUE NVL(recomendacion, ''),
                   'fechaVencimiento'                 VALUE NVL(fechavencimiento, ''),
                   'estadoRecomendacion'              VALUE NVL(estadorecomendacion, ''),
                   'estadoPlazo'                      VALUE NVL(estadoplazo, ''),
                   'diasVencidos'                     VALUE NVL(diasvencidos, 0),
                   'progresoRecomendacion'            VALUE NVL(progresorecomendacion, 0),
                   'fechaCargaVerificable'            VALUE NVL(fechacargaverificable, ''),
                   'responsable'                      VALUE NVL(responsable, ''),
                   'estadoVerificable'                VALUE NVL(estadoverificable, ''),
                   'departamento'                     VALUE NVL(departamento, ''),
                   'descripcionVerificableResponsable' VALUE NVL(descripcionverificableresponsable, ''),
                   'observacionAuditor'               VALUE NVL(observacionauditor, ''),
                   'progresoInforme'                  VALUE NVL(progresoinforme, 0),
                   'fechaConsulta'                    VALUE NVL(fechaconsulta, '')
                   RETURNING CLOB
               )
               ORDER BY fecha_orden DESC, codigoinforme DESC, num_rec ASC, responsable ASC
               RETURNING CLOB
           )
    INTO p_json
    FROM datos_calculados;

    IF p_json IS NULL THEN
        p_json := TO_CLOB('[]');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END reporteSeguimientoResponsableOperativo;
/

