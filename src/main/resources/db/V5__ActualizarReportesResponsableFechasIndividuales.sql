-- V5__ActualizarReportesResponsableFechasIndividuales.sql
-- Actualización de los 3 procedimientos de reportes de responsables:
-- 1. REPORTERECOMENDACIONESVENCIDASRESPONSABLE
-- 2. reporteRecomendacionesEstadoFechaCumplimientoResponsable
-- 3. reporteProgresoRecomendacionesResponsable
--
-- Características:
-- - Fechas individuales por responsable: COALESCE(prórroga, fecha_individual, fecha_global)
-- - Cálculo de días laborables en plazo (FN_CALCULAR_DIAS_LABORABLES)
-- - Parseo flexible de fechas (DD/MM/YYYY, YYYY-MM-DD, vacíos)
-- - Filtro estricto por responsable (UPPER(TRIM(usuario)))
-- - Agrupados y ordenados por informe (de fecha más antigua a más actual) y dentro de cada informe
--   ordenados ascendentemente por el número de recomendación (1, 2, 3...)
-- - Estructura JSON plana compatible al 100% con los DTOs y tablas del frontend

-- ============================================================================
-- 1. REPORTERECOMENDACIONESVENCIDASRESPONSABLE
-- ============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.REPORTERECOMENDACIONESVENCIDASRESPONSABLE(
    p_json              OUT CLOB,
    fecha_inicial       IN  VARCHAR2,
    fecha_fin           IN  VARCHAR2,
    estado_seguimiento  IN  INTEGER,
    usuario             IN  VARCHAR2
)
IS
    v_fecha_ini    TIMESTAMP;
    v_fecha_fin    TIMESTAMP;
BEGIN
    -- 1. Parseo flexible de fechas
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
    asignaciones AS (
        SELECT 
            INF.AININ_CODIGO AS aininCodigo,
            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) AS informeFechaOrden,
            ORGCON.AINOC_DESCRIPCION AS organismoControl,
            INF.AININ_MEMORANDO AS memorando,
            INF.AININ_MEMO_APROBACION AS memoAprobacion,
            TO_CHAR(NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA_APROBACION), 'DD/MM/YYYY') AS fechaEmision,
            INF.AININ_TITULO AS tituloInforme,
            REC.ARERE_DESCRIPCION AS recomendacion,
            NVL(UV.AREVE_OBSERVACION, 'no existe observacion') AS observacion,
            COALESCE(PR.FECHA_PRORROGA, DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) AS fechaMaximaDate,
            TO_CHAR(COALESCE(PR.FECHA_PRORROGA, DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'DD/MM/YYYY') AS fechaMaxima,
            FN_CALCULAR_DIAS_LABORABLES(COALESCE(PR.FECHA_PRORROGA, DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)) AS plazo,
            REC.ARERE_CODIGO AS arereCodigo,
            TO_NUMBER(REGEXP_SUBSTR(REC.ARERE_NUM_REC, '^\d+')) AS numRec,
            TRIM(RESP.ARERS_NOMBRE || ' ' || RESP.ARERS_APELLIDO) ||
            CASE 
                WHEN UV.AREVE_CODIGO IS NULL THEN ' (VERIFICABLE NO CREADO)'
                WHEN UV.AREEV_CODIGO = 1 THEN ' (PENDIENTE)'
                WHEN UV.AREEV_CODIGO = 3 THEN ' (NO JUSTIFICA)'
                ELSE ''
            END ||
            CASE 
                WHEN UV.AREVE_CODIGO IS NOT NULL 
                     AND UV.AREVE_OBSERVACION IS NOT NULL 
                     AND LENGTH(TRIM(UV.AREVE_OBSERVACION)) > 0 THEN 
                    ' - ' || TRIM(UV.AREVE_OBSERVACION)
                ELSE ''
            END AS responsable_info
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORGCON ON INF.AINOC_CODIGO = ORGCON.AINOC_CODIGO
        JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DET ON REC.ARERE_CODIGO = DET.ARERE_CODIGO AND DET.AREDR_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RESP ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO AND RESP.ARERS_ESTADO = 1
        LEFT JOIN ultimas_prorrogas PR ON PR.ARERE_CODIGO = DET.ARERE_CODIGO AND PR.ARERS_CODIGO = DET.ARERS_CODIGO
        LEFT JOIN ultimos_verificables UV ON DET.AREDR_CODIGO = UV.AREDR_CODIGO AND UV.RN = 1
        WHERE (INF.AININ_FECHA_APROBACION IS NULL OR INF.AININ_FECHA_APROBACION BETWEEN v_fecha_ini AND v_fecha_fin)
          AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)
          AND (usuario = '0' OR UPPER(TRIM(RESP.ARERS_USUARIO_PER)) = UPPER(TRIM(usuario)))
          AND INF.AININ_ESTADO = 1
          AND HALL.AREHA_ESTADO = 1
          AND CON.ARECO_ESTADO = 1
          AND REC.ARERE_ESTADO = 1
          AND ORGCON.AINOC_ESTADO = 1
          AND REC.AREES_CODIGO NOT IN (2, 4)
          AND (UV.AREVE_CODIGO IS NULL OR UV.AREEV_CODIGO IN (1, 3))
    ),
    asignaciones_filtradas AS (
        SELECT 
            aininCodigo,
            informeFechaOrden,
            organismoControl,
            memorando,
            memoAprobacion,
            fechaEmision,
            tituloInforme,
            recomendacion,
            MAX(observacion) AS observacion,
            fechaMaxima,
            plazo,
            arereCodigo,
            numRec,
            LISTAGG(responsable_info, ', ' ON OVERFLOW TRUNCATE) WITHIN GROUP (ORDER BY responsable_info) AS responsables
        FROM asignaciones
        WHERE TRUNC(fechaMaximaDate) < TRUNC(SYSDATE)
        GROUP BY 
            aininCodigo,
            informeFechaOrden,
            organismoControl,
            memorando,
            memoAprobacion,
            fechaEmision,
            tituloInforme,
            recomendacion,
            fechaMaxima,
            plazo,
            arereCodigo,
            numRec
    )
    SELECT JSON_ARRAYAGG(
        JSON_OBJECT(
            'tituloInforme'     VALUE NVL(tituloInforme, ''),
            'recomendacion'     VALUE NVL(recomendacion, ''),
            'organismoControl'  VALUE NVL(organismoControl, ''),
            'memorando'         VALUE NVL(memorando, ''),
            'memoAprobacion'    VALUE NVL(memoAprobacion, ''),
            'observacion'       VALUE NVL(observacion, 'no existe observacion'),
            'fechaEmision'      VALUE NVL(fechaEmision, ''),
            'fechaMaxima'       VALUE NVL(fechaMaxima, ''),
            'plazo'             VALUE TO_NUMBER(NVL(plazo, 0)),
            'responsables'      VALUE NVL(responsables, ''),
            'arereCodigo'       VALUE TO_NUMBER(NVL(arereCodigo, 0))
            RETURNING CLOB
        )
        ORDER BY informeFechaOrden ASC NULLS LAST, aininCodigo ASC, numRec ASC NULLS LAST, arereCodigo ASC
        RETURNING CLOB
    )
    INTO p_json
    FROM asignaciones_filtradas;

    IF p_json IS NULL THEN
        p_json := TO_CLOB(JSON_OBJECT('mensaje' VALUE 'No se encontraron recomendaciones vencidas con los criterios especificados'));
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END REPORTERECOMENDACIONESVENCIDASRESPONSABLE;
/

-- ============================================================================
-- 2. reporteRecomendacionesEstadoFechaCumplimientoResponsable
-- ============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.reporteRecomendacionesEstadoFechaCumplimientoResponsable (
    p_json             OUT CLOB,
    fecha_inicial      IN  VARCHAR,
    fecha_fin          IN  VARCHAR,
    estado_seguimiento IN  INTEGER,
    usuario            IN  VARCHAR
) IS
    v_fecha_ini TIMESTAMP;
    v_fecha_fin TIMESTAMP;
BEGIN
    -- 1. Parseo flexible de fechas
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

    WITH
    ultimas_prorrogas AS (
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
    UltimosVerificables AS (
        SELECT
            v.*,
            ROW_NUMBER() OVER (
                PARTITION BY v.AREDR_CODIGO
                ORDER BY v.AREVE_FECHA_CARGA DESC NULLS LAST, v.AREVE_CODIGO DESC
            ) AS rn
        FROM ROOTSISREC.AI_REC_VERIFICABLES v
        WHERE v.AREVE_ESTADO = 1
    ),
    responsables_pendientes AS (
        SELECT DISTINCT
            dr.ARERE_CODIGO,
            res.ARERS_NOMBRE || ' ' || res.ARERS_APELLIDO AS nombre_completo,
            res.ARERS_DEPARTAMENTO,
            COALESCE(ev.AREEV_DESCRIPCION, 'VERIFICABLE NO CREADO') AS estado_verificable
        FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION dr
        JOIN ROOTSISREC.AI_REC_RESPONSABLES res ON dr.ARERS_CODIGO = res.ARERS_CODIGO
        LEFT JOIN UltimosVerificables uv ON uv.AREDR_CODIGO = dr.AREDR_CODIGO AND uv.rn = 1
        LEFT JOIN ROOTSISREC.AI_REC_ESTADO_VERIFICABLE ev ON ev.AREEV_CODIGO = uv.AREEV_CODIGO AND ev.AREEV_ESTADO = 1
        WHERE dr.AREDR_ESTADO = 1 AND res.ARERS_ESTADO = 1
    ),
    datos AS (
        SELECT DISTINCT
            INF.AININ_CODIGO                                       AS aininCodigo,
            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO)  AS informeFechaOrden,
            INF.AININ_TITULO || ' (' || INF.AININ_USUARIO || ') ' AS tituloInforme,
            DBMS_LOB.SUBSTR(CON.ARECO_CONCLUSION, 2000, 1)        AS conclusion,
            INF.AININ_MEMORANDO                                    AS memorando,
            INF.AININ_MEMO_APROBACION                             AS memoAprobacion,
            TO_CHAR(COALESCE(PR.FECHA_PRORROGA, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'yyyy-MM-dd') AS fechaMaxima,
            TO_CHAR(NVL(REC.ARERE_FECHA_INICIO_CONTROL, INF.AININ_FECHA_APROBACION), 'yyyy-MM-dd') AS fechaInicioControl,
            REC.ARERE_CODIGO                                       AS codigoRecomendacion,
            TO_NUMBER(REGEXP_SUBSTR(REC.ARERE_NUM_REC, '^\d+'))    AS numRec,
            REC.ARERE_DESCRIPCION                                  AS recomendacion,
            ORG.AINOC_DESCRIPCION                                  AS organismoControl,
            ES.AREES_DESCRIPCION                                   AS estadoSeguimiento,
            (
                SELECT LISTAGG(DISTINCT rp.arers_departamento, ', ' ON OVERFLOW TRUNCATE)
                       WITHIN GROUP (ORDER BY rp.arers_departamento)
                FROM responsables_pendientes rp
                WHERE rp.ARERE_CODIGO = REC.ARERE_CODIGO
                AND UPPER(rp.estado_verificable) != 'JUSTIFICA'
            ) AS pendienteCumplimientoDepartamentos,
            (
                SELECT LISTAGG(DISTINCT rp.nombre_completo, ', ' ON OVERFLOW TRUNCATE)
                       WITHIN GROUP (ORDER BY rp.nombre_completo)
                FROM responsables_pendientes rp
                WHERE rp.ARERE_CODIGO = REC.ARERE_CODIGO
                AND UPPER(rp.estado_verificable) != 'JUSTIFICA'
            ) AS pendienteCumplimientoResponsables
        FROM ROOTSISREC.AI_REC_RECOMENDACIONES       REC
        JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR  ON REC.ARERE_CODIGO = DR.ARERE_CODIGO AND DR.AREDR_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RESPONSABLES          RES ON DR.ARERS_CODIGO  = RES.ARERS_CODIGO AND RES.ARERS_ESTADO = 1
        LEFT JOIN ultimas_prorrogas PR ON PR.ARERE_CODIGO = DR.ARERE_CODIGO AND PR.ARERS_CODIGO = DR.ARERS_CODIGO
        JOIN ROOTSISREC.AI_REC_ESTADO_SEGUIMIENTO    ES  ON REC.AREES_CODIGO  = ES.AREES_CODIGO AND ES.AREES_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES          CON ON REC.ARECO_CODIGO  = CON.ARECO_CODIGO AND CON.ARECO_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_HALLAZGOS             HALL ON CON.AREHA_CODIGO  = HALL.AREHA_CODIGO AND HALL.AREHA_ESTADO = 1
        JOIN ROOTSISREC.AI_INF_INFORMES              INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO AND INF.AININ_ESTADO = 1
        JOIN ROOTSISREC.AI_INF_ORG_CONT              ORG ON INF.AINOC_CODIGO  = ORG.AINOC_CODIGO AND ORG.AINOC_ESTADO = 1
        WHERE (COALESCE(REC.ARERE_FECHA_INICIO_CONTROL, INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) BETWEEN v_fecha_ini AND v_fecha_fin)
          AND REC.ARERE_ESTADO  = 1
          AND (usuario = '0' OR UPPER(TRIM(RES.ARERS_USUARIO_PER)) = UPPER(TRIM(usuario)))
          AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)
    )
    SELECT JSON_ARRAYAGG(
        JSON_OBJECT(
            'tituloInforme'      VALUE datos.tituloInforme,
            'conclusion'         VALUE datos.conclusion,
            'memorando'          VALUE datos.memorando,
            'memoAprobacion'     VALUE datos.memoAprobacion,
            'fechaInicioControl' VALUE datos.fechaInicioControl,
            'fechaMaxima'        VALUE datos.fechaMaxima,
            'codigoRecomendacion' VALUE datos.codigoRecomendacion,
            'recomendacion'      VALUE datos.recomendacion,
            'organismoControl'   VALUE datos.organismoControl,
            'estadoSeguimiento'  VALUE datos.estadoSeguimiento,
            'pendienteCumplimientoDepartamentos' VALUE datos.pendienteCumplimientoDepartamentos,
            'pendienteCumplimientoResponsables' VALUE datos.pendienteCumplimientoResponsables
            RETURNING CLOB
        )
        ORDER BY datos.informeFechaOrden ASC NULLS LAST, datos.aininCodigo ASC, datos.numRec ASC NULLS LAST, datos.codigoRecomendacion ASC
        RETURNING CLOB
    )
    INTO p_json
    FROM datos;

    IF p_json IS NULL THEN
        p_json := JSON_OBJECT('mensaje' VALUE 'No se encontraron registros');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END reporteRecomendacionesEstadoFechaCumplimientoResponsable;
/

-- ============================================================================
-- 3. reporteProgresoRecomendacionesResponsable
-- ============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.reporteProgresoRecomendacionesResponsable (
    p_json        OUT CLOB,
    fecha_inicial IN VARCHAR,
    fecha_fin     IN VARCHAR,
    usuario       IN VARCHAR
) IS
    v_fecha_ini TIMESTAMP;
    v_fecha_fin TIMESTAMP;
BEGIN
    -- 1. Parseo flexible de fechas
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

    WITH responsables_en_proceso_pendientes AS (
        SELECT DISTINCT
            dr.arere_codigo,
            res.arers_departamento
        FROM ROOTSISREC.ai_rec_detalle_recomendacion dr
        JOIN ROOTSISREC.ai_rec_responsables res ON dr.arers_codigo = res.arers_codigo
        JOIN ROOTSISREC.ai_rec_recomendaciones rec ON dr.arere_codigo = rec.arere_codigo
        WHERE dr.aredr_estado = 1
          AND res.arers_estado = 1
          AND rec.arere_estado = 1
          AND rec.arees_codigo = 1
          AND ( NOT EXISTS (
              SELECT 1 FROM ROOTSISREC.ai_rec_verificables v WHERE v.aredr_codigo = dr.aredr_codigo AND v.areve_estado = 1
          ) OR EXISTS (
              SELECT 1 FROM ROOTSISREC.ai_rec_verificables v
              JOIN ROOTSISREC.ai_rec_estado_verificable ev ON v.areev_codigo = ev.areev_codigo
              WHERE v.aredr_codigo = dr.aredr_codigo AND v.areve_estado = 1 AND ev.areev_estado = 1 AND UPPER(ev.areev_descripcion) IN ('PENDIENTE', 'NO_JUSTIFICA')
          ))
    ),
    responsables_no_cumplida_pendientes AS (
        SELECT DISTINCT
            dr.arere_codigo,
            res.arers_departamento
        FROM ROOTSISREC.ai_rec_detalle_recomendacion dr
        JOIN ROOTSISREC.ai_rec_responsables res ON dr.arers_codigo = res.arers_codigo
        JOIN ROOTSISREC.ai_rec_recomendaciones rec ON dr.arere_codigo = rec.arere_codigo
        WHERE dr.aredr_estado = 1
          AND res.arers_estado = 1
          AND rec.arere_estado = 1
          AND rec.arees_codigo = 3
          AND ( NOT EXISTS (
              SELECT 1 FROM ROOTSISREC.ai_rec_verificables v WHERE v.aredr_codigo = dr.aredr_codigo AND v.areve_estado = 1
          ) OR EXISTS (
              SELECT 1 FROM ROOTSISREC.ai_rec_verificables v
              JOIN ROOTSISREC.ai_rec_estado_verificable ev ON v.areev_codigo = ev.areev_codigo
              WHERE v.aredr_codigo = dr.aredr_codigo AND v.areve_estado = 1 AND ev.areev_estado = 1 AND UPPER(ev.areev_descripcion) IN ('PENDIENTE', 'NO_JUSTIFICA')
          ))
    ),
    datos AS (
        SELECT
            inf.ainin_codigo                 AS codigo_informe,
            inf.ainin_titulo                 AS titulo_informe,
            org.ainoc_descripcion            AS organismo_control,
            inf.ainin_usuario                AS auditor,
            inf.ainin_memorando              AS memorando,
            inf.ainin_memo_aprobacion        AS memo_aprobacion,
            NVL(inf.ainin_fecha_aprobacion, inf.ainin_fecha_memo) AS fecha_orden,
            ROUND((COUNT(DISTINCT CASE WHEN rec.arees_codigo = 2 THEN rec.arere_codigo END) * 100.0) / NULLIF(COUNT(DISTINCT rec.arere_codigo), 0), 2) AS progreso,
            COUNT(DISTINCT rec.arere_codigo) AS conteo_recomendaciones,
            COUNT(DISTINCT CASE WHEN rec.arees_codigo = 1 THEN rec.arere_codigo END) AS total_recomendaciones_en_proceso,
            COUNT(DISTINCT CASE WHEN rec.arees_codigo = 2 THEN rec.arere_codigo END) AS total_recomendaciones_cumplidas,
            COUNT(DISTINCT CASE WHEN rec.arees_codigo = 3 THEN rec.arere_codigo END) AS total_recomendaciones_no_cumplidas,
            COUNT(DISTINCT CASE WHEN rec.arees_codigo = 4 THEN rec.arere_codigo END) AS total_recomendaciones_no_aplica,
            (
                SELECT LISTAGG(DISTINCT rep.arers_departamento, ', ' ON OVERFLOW TRUNCATE) WITHIN GROUP (ORDER BY rep.arers_departamento)
                FROM responsables_en_proceso_pendientes rep
                WHERE rep.arere_codigo IN (
                    SELECT DISTINCT rec2.arere_codigo
                    FROM ROOTSISREC.ai_rec_recomendaciones rec2
                    JOIN ROOTSISREC.ai_rec_detalle_recomendacion dr2 ON rec2.arere_codigo = dr2.arere_codigo AND dr2.aredr_estado = 1
                    JOIN ROOTSISREC.ai_rec_responsables res2 ON dr2.arers_codigo = res2.arers_codigo AND res2.arers_estado = 1
                    JOIN ROOTSISREC.ai_rec_conclusiones con2 ON rec2.areco_codigo = con2.areco_codigo AND con2.areco_estado = 1
                    JOIN ROOTSISREC.ai_rec_hallazgos hall2 ON con2.areha_codigo = hall2.areha_codigo AND hall2.areha_estado = 1
                    WHERE hall2.ainin_codigo = inf.ainin_codigo
                      AND rec2.arees_codigo = 1
                      AND rec2.arere_estado = 1
                      AND (usuario = '0' OR UPPER(TRIM(res2.arers_usuario_per)) = UPPER(TRIM(usuario)))
                )
            ) AS departamentosresponsablespendientesenproceso,
            (
                SELECT LISTAGG(DISTINCT rnp.arers_departamento, ', ' ON OVERFLOW TRUNCATE) WITHIN GROUP (ORDER BY rnp.arers_departamento)
                FROM responsables_no_cumplida_pendientes rnp
                WHERE rnp.arere_codigo IN (
                    SELECT DISTINCT rec3.arere_codigo
                    FROM ROOTSISREC.ai_rec_recomendaciones rec3
                    JOIN ROOTSISREC.ai_rec_detalle_recomendacion dr3 ON rec3.arere_codigo = dr3.arere_codigo AND dr3.aredr_estado = 1
                    JOIN ROOTSISREC.ai_rec_responsables res3 ON dr3.arers_codigo = res3.arers_codigo AND res3.arers_estado = 1
                    JOIN ROOTSISREC.ai_rec_conclusiones con3 ON rec3.areco_codigo = con3.areco_codigo AND con3.areco_estado = 1
                    JOIN ROOTSISREC.ai_rec_hallazgos hall3 ON con3.areha_codigo = hall3.areha_codigo AND hall3.areha_estado = 1
                    WHERE hall3.ainin_codigo = inf.ainin_codigo
                      AND rec3.arees_codigo = 3
                      AND rec3.arere_estado = 1
                      AND (usuario = '0' OR UPPER(TRIM(res3.arers_usuario_per)) = UPPER(TRIM(usuario)))
                )
            ) AS departamentosresponsablesnocumplida
        FROM ROOTSISREC.ai_rec_recomendaciones rec
        JOIN ROOTSISREC.ai_rec_detalle_recomendacion dr ON rec.arere_codigo = dr.arere_codigo AND dr.aredr_estado = 1
        JOIN ROOTSISREC.ai_rec_responsables res ON dr.arers_codigo = res.arers_codigo AND res.arers_estado = 1
        JOIN ROOTSISREC.ai_rec_conclusiones con ON rec.areco_codigo = con.areco_codigo AND con.areco_estado = 1
        JOIN ROOTSISREC.ai_rec_hallazgos hall ON con.areha_codigo = hall.areha_codigo AND hall.areha_estado = 1
        JOIN ROOTSISREC.ai_inf_informes inf ON hall.ainin_codigo = inf.ainin_codigo AND inf.ainin_estado = 1
        JOIN ROOTSISREC.ai_inf_org_cont org ON inf.ainoc_codigo = org.ainoc_codigo AND org.ainoc_estado = 1
        WHERE (NVL(inf.ainin_fecha_aprobacion, inf.ainin_fecha_memo) BETWEEN v_fecha_ini AND v_fecha_fin)
          AND (usuario = '0' OR UPPER(TRIM(res.arers_usuario_per)) = UPPER(TRIM(usuario)))
          AND rec.arere_estado = 1
        GROUP BY
            inf.ainin_codigo,
            inf.ainin_titulo,
            org.ainoc_descripcion,
            inf.ainin_usuario,
            inf.ainin_memorando,
            inf.ainin_memo_aprobacion,
            NVL(inf.ainin_fecha_aprobacion, inf.ainin_fecha_memo)
    )
    SELECT
        JSON_ARRAYAGG(
            JSON_OBJECT(
                'codigoInforme' VALUE datos.codigo_informe,
                'tituloInforme' VALUE datos.titulo_informe,
                'organismoControl' VALUE datos.organismo_control,
                'memorando' VALUE datos.memorando,
                'memoAprobacion' VALUE datos.memo_aprobacion,
                'codigoAuditor' VALUE datos.auditor,
                'conteoRecomendaciones' VALUE datos.conteo_recomendaciones,
                'recomendacionesEnProceso' VALUE datos.total_recomendaciones_en_proceso,
                'recomendacionesCumplidas' VALUE datos.total_recomendaciones_cumplidas,
                'recomendacionesNoCumplidas' VALUE datos.total_recomendaciones_no_cumplidas,
                'recomendacionesNoAplica' VALUE datos.total_recomendaciones_no_aplica,
                'progreso' VALUE datos.progreso,
                'departamentosResponsablesPendientesEnProceso' VALUE datos.departamentosresponsablespendientesenproceso,
                'departamentosResponsablesNoCumplida' VALUE datos.departamentosresponsablesnocumplida
            RETURNING CLOB)
            ORDER BY datos.fecha_orden ASC NULLS LAST, datos.codigo_informe ASC
        RETURNING CLOB)
    INTO p_json
    FROM datos;

    IF p_json IS NULL THEN
        p_json := JSON_OBJECT('mensaje' VALUE 'No se encontraron registros');
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END reporteProgresoRecomendacionesResponsable;
/
