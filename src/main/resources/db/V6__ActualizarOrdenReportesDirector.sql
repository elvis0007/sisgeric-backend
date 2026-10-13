-- V6__ActualizarOrdenReportesDirector.sql
-- Actualización de los procedimientos de reportes de Directores Departamentales:
-- 1. reporteDepartamentoFechasVencidasDirector (/report-departamento-fechas-vencidas)
-- 2. reporteRecomendacionesPorDirectorDepartamental (/inicio/reporte-director-rec)
--
-- Características de ordenamiento y agrupación:
-- - Agrupados por informe: todas las recomendaciones de un mismo informe aparecen contiguas.
-- - Orden de informes: del más antiguo al más actual (informeFechaOrden ASC NULLS LAST).
-- - Orden de recomendaciones: dentro de cada informe, ordenadas ascendentemente del 1 en adelante (numRec ASC NULLS LAST).
-- - Desempate determinista por código de recomendación (arereCodigo ASC).

-- ============================================================================
-- 1. reporteDepartamentoFechasVencidasDirector
-- ============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.reporteDepartamentoFechasVencidasDirector (
    p_json              OUT     CLOB,
    fecha_inicial       IN      VARCHAR2,
    fecha_fin           IN      VARCHAR2,
    organismo_control   IN      INTEGER,
    estado_seguimiento  IN      INTEGER,
    auditor             IN      VARCHAR2,
    dpto                IN      VARCHAR2
) IS
    v_fecha_ini   TIMESTAMP;
    v_fecha_fin   TIMESTAMP;
    v_clean_dpto  VARCHAR2(250);
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

    v_clean_dpto := TRIM(REPLACE(REPLACE(REPLACE(UPPER(dpto), 'DPTO.', ''), 'DEPARTAMENTO', ''), 'DIRECCION', ''));

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
    asignaciones_dpto AS (
        SELECT 
            INF.AININ_CODIGO AS aininCodigo,
            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) AS informeFechaOrden,
            ORGCON.AINOC_DESCRIPCION AS enteControl,
            INF.AININ_MEMORANDO AS memorando,
            INF.AININ_MEMO_APROBACION AS memoAprobacion,
            TO_CHAR(NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA_APROBACION), 'yyyy-MM-dd') AS fechaEmision,
            INF.AININ_TITULO AS tituloInforme,
            REC.ARERE_DESCRIPCION AS recomendacion,
            NVL(UV.AREVE_OBSERVACION, '') AS observacion,
            COALESCE(PR.FECHA_PRORROGA, DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) AS fechaMaximaDate,
            TO_CHAR(COALESCE(PR.FECHA_PRORROGA, DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'yyyy-MM-dd') AS fechaMaxima,
            FN_CALCULAR_DIAS_LABORABLES(COALESCE(PR.FECHA_PRORROGA, DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)) AS plazo,
            REC.ARERE_CODIGO AS arereCodigo,
            COALESCE(
                TO_NUMBER(REGEXP_SUBSTR(TO_CHAR(REC.ARERE_NUM_REC), '^\d+')),
                TO_NUMBER(REGEXP_SUBSTR(SUBSTR(REC.ARERE_DESCRIPCION, 1, 120), '([0-9]{1,3})\.', 1, 1, 'i', 1)),
                TO_NUMBER(REGEXP_SUBSTR(REC.ARERE_DESCRIPCION, '^\d+')),
                TO_NUMBER(REGEXP_SUBSTR(REC.ARERE_DESCRIPCION, '\d+')),
                999
            ) AS numRec,
            INF.AININ_USUARIO AS auditor,
            TRIM(RESP.ARERS_NOMBRE || ' ' || RESP.ARERS_APELLIDO) ||
            CASE 
                WHEN UV.AREVE_CODIGO IS NULL THEN ' (VERIFICABLE NO CREADO)'
                WHEN UV.AREEV_CODIGO = 1 THEN ' (PENDIENTE)'
                WHEN UV.AREEV_CODIGO = 2 THEN ' (JUSTIFICA)'
                WHEN UV.AREEV_CODIGO = 3 THEN ' (NO JUSTIFICA)'
                ELSE ''
            END ||
            CASE 
                WHEN UV.AREVE_OBSERVACION IS NOT NULL AND LENGTH(TRIM(UV.AREVE_OBSERVACION)) > 0 THEN 
                    ' - ' || TRIM(UV.AREVE_OBSERVACION)
                ELSE ''
            END AS responsables,
            REC.AREES_CODIGO,
            UV.AREEV_CODIGO
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
          AND (organismo_control = 0 OR INF.AINOC_CODIGO = organismo_control)
          AND (auditor = '0' OR INF.AININ_USUARIO = auditor)
          AND INF.AININ_ESTADO = 1
          AND HALL.AREHA_ESTADO = 1
          AND CON.ARECO_ESTADO = 1
          AND REC.ARERE_ESTADO = 1
          AND ORGCON.AINOC_ESTADO = 1
          AND (
                dpto = '0'
             OR RESP.ARERS_DEPARTAMENTO = dpto
             OR UPPER(RESP.ARERS_DEPARTAMENTO) LIKE '%' || v_clean_dpto || '%'
             OR UPPER(RESP.ARERS_DEPARTAMENTO_ABREV) LIKE '%' || v_clean_dpto || '%'
          )
    ),
    clasificadas AS (
        SELECT 
            aininCodigo,
            informeFechaOrden,
            enteControl,
            memorando,
            memoAprobacion,
            fechaEmision,
            tituloInforme,
            recomendacion,
            observacion,
            fechaMaxima,
            plazo,
            arereCodigo,
            numRec,
            auditor,
            responsables,
            CASE 
                WHEN AREES_CODIGO = 4 THEN 4
                WHEN AREES_CODIGO = 2 OR AREEV_CODIGO = 2 THEN 2
                WHEN AREEV_CODIGO = 3 OR plazo < 0 THEN 3
                ELSE 1
            END AS numEstado
        FROM asignaciones_dpto
    )
    SELECT JSON_ARRAYAGG(
        JSON_OBJECT(
            'tituloInforme'     VALUE datos.tituloInforme,
            'recomendacion'     VALUE datos.recomendacion,
            'enteControl'       VALUE datos.enteControl,
            'memorando'         VALUE datos.memorando,
            'memoAprobacion'    VALUE datos.memoAprobacion,
            'observacion'       VALUE CASE WHEN datos.observacion IS NULL OR TRIM(datos.observacion) = '' THEN 'no existe observacion' ELSE datos.observacion END,
            'fechaEmision'      VALUE datos.fechaEmision,
            'fechaMaxima'       VALUE datos.fechaMaxima,
            'plazo'             VALUE TO_NUMBER(NVL(datos.plazo, 0)),
            'auditor'           VALUE datos.auditor,
            'responsables'      VALUE datos.responsables,
            'arereCodigo'       VALUE TO_NUMBER(NVL(datos.arereCodigo, 0)),
            'numRec'            VALUE TO_NUMBER(NVL(datos.numRec, 0))
            RETURNING CLOB
        )
        ORDER BY datos.informeFechaOrden ASC NULLS LAST, datos.aininCodigo ASC, datos.numRec ASC NULLS LAST, datos.arereCodigo ASC
        RETURNING CLOB
    )
    INTO p_json
    FROM clasificadas datos
    WHERE (estado_seguimiento = 0 AND datos.numEstado IN (1, 3))
       OR (estado_seguimiento = 1 AND datos.numEstado = 1)
       OR (estado_seguimiento = 2 AND datos.numEstado = 3)
       OR (estado_seguimiento = 3 AND datos.numEstado = 3);

    IF p_json IS NULL THEN
        p_json := TO_CLOB(JSON_OBJECT('mensaje' VALUE 'No se encontraron recomendaciones con los criterios especificados'));
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END reporteDepartamentoFechasVencidasDirector;
/

-- ============================================================================
-- 2. reporteRecomendacionesPorDirectorDepartamental
-- ============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.reporteRecomendacionesPorDirectorDepartamental (
    p_json OUT CLOB,
    fecha_inicial IN VARCHAR2,
    fecha_fin IN VARCHAR2,
    organismo_control IN NUMBER,
    estado_seguimiento IN NUMBER,
    auditor IN VARCHAR2,
    dpto IN VARCHAR2
) AS
    v_fecha_ini TIMESTAMP;
    v_fecha_fin TIMESTAMP;
    v_clean_dpto VARCHAR2(250);
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

    v_clean_dpto := TRIM(REPLACE(REPLACE(REPLACE(UPPER(dpto), 'DPTO.', ''), 'DEPARTAMENTO', ''), 'DIRECCION', ''));

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
    rec_estatus_dpto AS (
        SELECT 
            INF.AININ_CODIGO AS codigoInforme,
            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) AS informeFechaOrden,
            ORGCON.AINOC_DESCRIPCION AS organismoControl,
            INF.AININ_USUARIO AS auditorInforme,
            INF.AININ_MEMORANDO AS memorandoAuditoria,
            INF.AININ_MEMO_APROBACION AS memoAprobacion,
            TO_CHAR(NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA_APROBACION), 'yyyy-MM-dd') AS fechaEmision,
            INF.AININ_TITULO AS tituloInforme,
            REC.ARERE_DESCRIPCION AS recomendacion,
            CON.ARECO_CODIGO AS codigoConclusion,
            TO_CHAR(MAX(COALESCE(PR.FECHA_PRORROGA, DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)), 'yyyy-MM-dd') AS fechaACumplir,
            FN_CALCULAR_DIAS_LABORABLES(MAX(COALESCE(PR.FECHA_PRORROGA, DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA))) AS PLAZO_DIAS,
            REC.ARERE_CODIGO AS arereCodigo,
            COALESCE(
                TO_NUMBER(REGEXP_SUBSTR(TO_CHAR(REC.ARERE_NUM_REC), '^\d+')),
                TO_NUMBER(REGEXP_SUBSTR(SUBSTR(REC.ARERE_DESCRIPCION, 1, 120), '([0-9]{1,3})\.', 1, 1, 'i', 1)),
                TO_NUMBER(REGEXP_SUBSTR(REC.ARERE_DESCRIPCION, '^\d+')),
                TO_NUMBER(REGEXP_SUBSTR(REC.ARERE_DESCRIPCION, '\d+')),
                999
            ) AS numRec,
            MAX(REC.AREES_CODIGO) AS AREES_CODIGO,
            COUNT(CASE WHEN NVL(ULT_VER.AREEV_CODIGO, 0) = 2 THEN 1 END) AS RESPS_CUMPLIDOS_DPTO,
            COUNT(CASE WHEN NVL(ULT_VER.AREEV_CODIGO, 0) <> 2 THEN 1 END) AS RESPS_PENDIENTES_DPTO,
            MAX(CASE WHEN ULT_VER.AREEV_CODIGO = 3 THEN 1 ELSE 0 END) AS TIENE_EVALUACION_NO_JUSTIFICA
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORGCON ON INF.AINOC_CODIGO = ORGCON.AINOC_CODIGO
        JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DET ON REC.ARERE_CODIGO = DET.ARERE_CODIGO AND DET.AREDR_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RESP ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO AND RESP.ARERS_ESTADO = 1
        LEFT JOIN ultimas_prorrogas PR ON PR.ARERE_CODIGO = DET.ARERE_CODIGO AND PR.ARERS_CODIGO = DET.ARERS_CODIGO
        LEFT JOIN ultimos_verificables ULT_VER ON DET.AREDR_CODIGO = ULT_VER.AREDR_CODIGO AND ULT_VER.RN = 1
        WHERE (INF.AININ_FECHA_APROBACION IS NULL OR INF.AININ_FECHA_APROBACION BETWEEN v_fecha_ini AND v_fecha_fin)
          AND (organismo_control = 0 OR INF.AINOC_CODIGO = organismo_control)
          AND (auditor = '0' OR INF.AININ_USUARIO = auditor)
          AND INF.AININ_ESTADO = 1
          AND HALL.AREHA_ESTADO = 1
          AND CON.ARECO_ESTADO = 1
          AND REC.ARERE_ESTADO = 1
          AND ORGCON.AINOC_ESTADO = 1
          AND (
                dpto = '0'
             OR RESP.ARERS_DEPARTAMENTO = dpto
             OR UPPER(RESP.ARERS_DEPARTAMENTO) LIKE '%' || v_clean_dpto || '%'
             OR UPPER(RESP.ARERS_DEPARTAMENTO_ABREV) LIKE '%' || v_clean_dpto || '%'
          )
        GROUP BY 
            INF.AININ_CODIGO,
            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO),
            ORGCON.AINOC_DESCRIPCION,
            INF.AININ_USUARIO,
            INF.AININ_MEMORANDO,
            INF.AININ_MEMO_APROBACION,
            TO_CHAR(NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA_APROBACION), 'yyyy-MM-dd'),
            INF.AININ_TITULO,
            REC.ARERE_DESCRIPCION,
            CON.ARECO_CODIGO,
            REC.ARERE_CODIGO,
            REC.ARERE_NUM_REC
    ),
    rec_clasificadas AS (
        SELECT 
            codigoInforme,
            informeFechaOrden,
            organismoControl,
            auditorInforme,
            memorandoAuditoria,
            memoAprobacion,
            fechaEmision,
            tituloInforme,
            recomendacion,
            codigoConclusion,
            fechaACumplir,
            arereCodigo,
            numRec,
            CASE 
                WHEN AREES_CODIGO = 4 THEN 'NO APLICA'
                WHEN AREES_CODIGO = 2 OR RESPS_PENDIENTES_DPTO = 0 THEN 'CUMPLIDA'
                WHEN TIENE_EVALUACION_NO_JUSTIFICA = 1 OR PLAZO_DIAS < 0 THEN 'NO CUMPLIDA'
                ELSE 'EN PROCESO'
            END AS estadoSeguimiento,
            CASE 
                WHEN AREES_CODIGO = 4 THEN 4
                WHEN AREES_CODIGO = 2 OR RESPS_PENDIENTES_DPTO = 0 THEN 2
                WHEN TIENE_EVALUACION_NO_JUSTIFICA = 1 OR PLAZO_DIAS < 0 THEN 3
                ELSE 1
            END AS numEstado
        FROM rec_estatus_dpto
    )
    SELECT
        JSON_ARRAYAGG(
            JSON_OBJECT(
                'codigoInforme' VALUE rec_clasificadas.codigoInforme,
                'organismoControl' VALUE rec_clasificadas.organismoControl,
                'auditorInforme' VALUE rec_clasificadas.auditorInforme,
                'memorandoAuditoria' VALUE rec_clasificadas.memorandoAuditoria,
                'memoAprobacion' VALUE rec_clasificadas.memoAprobacion,
                'fechaEmision' VALUE rec_clasificadas.fechaEmision,
                'tituloInforme' VALUE rec_clasificadas.tituloInforme,
                'conclusion' VALUE NVL(CON.ARECO_CONCLUSION, ''),
                'recomendacion' VALUE rec_clasificadas.recomendacion,
                'fechaACumplir' VALUE rec_clasificadas.fechaACumplir,
                'estadoSeguimiento' VALUE rec_clasificadas.estadoSeguimiento,
                'arereCodigo' VALUE TO_NUMBER(NVL(rec_clasificadas.arereCodigo, 0)),
                'numRec' VALUE TO_NUMBER(NVL(rec_clasificadas.numRec, 0))
                RETURNING CLOB
            )
            ORDER BY rec_clasificadas.informeFechaOrden ASC NULLS LAST, rec_clasificadas.codigoInforme ASC, rec_clasificadas.numRec ASC NULLS LAST, rec_clasificadas.arereCodigo ASC
            RETURNING CLOB
        )
        INTO p_json
        FROM rec_clasificadas
        LEFT JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON rec_clasificadas.codigoConclusion = CON.ARECO_CODIGO
        WHERE estado_seguimiento = 0 OR rec_clasificadas.numEstado = estado_seguimiento;

    IF p_json IS NULL THEN
        p_json := TO_CLOB(JSON_OBJECT('mensaje' VALUE 'No se encontraron registros'));
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END reporteRecomendacionesPorDirectorDepartamental;
/
