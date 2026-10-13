-- =============================================================================
-- MIGRACIÓN V12: Priorizar Estado JUSTIFICA en Reportes de Todos los Roles
-- Implementa ordenamiento jerárquico en la CTE de últimos verificables:
--   1° JUSTIFICA   (AREEV_CODIGO = 2) -> Prevalece si al menos uno fue aprobado
--   2° PENDIENTE   (AREEV_CODIGO = 1) -> Prevalece si está en trámite
--   3° NO JUSTIFICA(AREEV_CODIGO = 3) -> Si todos fueron rechazados
--   4° Desempate por fecha más reciente (AREVE_FECHA_CARGA DESC)
--   5° Sin verificables -> VERIFICABLE NO CREADO vía LEFT JOIN
-- =============================================================================

-- =============================================================================
-- GRUPO 1: DIRECTOR DEPARTAMENTAL (ROL 5)
-- =============================================================================

-- 1. reporteDepartamentoFechasVencidasDirector
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
          AND S.ARESO_ESTADO_APROB = '3'
          AND S.ARESO_FECHA_MAX_NUE IS NOT NULL
        GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO
    ),
    ultimos_verificables AS (
        SELECT 
            VER.AREDR_CODIGO,
            VER.AREVE_CODIGO,
            VER.AREEV_CODIGO,
            VER.AREVE_OBSERVACION,
            ROW_NUMBER() OVER (
                PARTITION BY VER.AREDR_CODIGO 
                ORDER BY 
                    CASE 
                        WHEN VER.AREEV_CODIGO = 2 THEN 1
                        WHEN VER.AREEV_CODIGO = 1 THEN 2
                        WHEN VER.AREEV_CODIGO = 3 THEN 3
                        ELSE 4 
                    END ASC,
                    NVL(VER.AREVE_FECHA_CARGA, VER.AREVE_FECHA) DESC NULLS LAST, 
                    VER.AREVE_CODIGO DESC
            ) AS RN
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

-- 2. reporteRecomendacionesPorDirectorDepartamental
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
          AND S.ARESO_ESTADO_APROB = '3'
          AND S.ARESO_FECHA_MAX_NUE IS NOT NULL
        GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO
    ),
    ultimos_verificables AS (
        SELECT 
            VER.AREDR_CODIGO,
            VER.AREEV_CODIGO,
            ROW_NUMBER() OVER (
                PARTITION BY VER.AREDR_CODIGO 
                ORDER BY 
                    CASE 
                        WHEN VER.AREEV_CODIGO = 2 THEN 1
                        WHEN VER.AREEV_CODIGO = 1 THEN 2
                        WHEN VER.AREEV_CODIGO = 3 THEN 3
                        ELSE 4 
                    END ASC,
                    NVL(VER.AREVE_FECHA_CARGA, VER.AREVE_FECHA) DESC NULLS LAST, 
                    VER.AREVE_CODIGO DESC
            ) AS RN
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

-- 3. ObtenerReporteS1Dpto
CREATE OR REPLACE PROCEDURE ROOTSISREC.ObtenerReporteS1Dpto (
    nombre_dpto IN VARCHAR2,
    p_json OUT CLOB
) IS
    v_cumplidas NUMBER := 0;
    v_en_proceso NUMBER := 0;
    v_vencidas NUMBER := 0;
    v_no_aplica NUMBER := 0;
    v_ultimo_mes NUMBER := 0;
    v_solicitudes_pend NUMBER := 0;
    v_clean_dpto VARCHAR2(250);
BEGIN
    v_clean_dpto := TRIM(REPLACE(REPLACE(REPLACE(UPPER(nombre_dpto), 'DPTO.', ''), 'DEPARTAMENTO', ''), 'DIRECCION', ''));

    WITH ultimas_prorrogas AS (
        SELECT 
            S.ARERE_CODIGO,
            S.ARERS_CODIGO,
            MAX(S.ARESO_FECHA_MAX_NUE) AS FECHA_PRORROGA
        FROM ROOTSISREC.AI_REC_SOLICITUDES S
        WHERE S.ARESO_ESTADO = '1'
          AND S.ARESO_ESTADO_APROB = '3'
          AND S.ARESO_FECHA_MAX_NUE IS NOT NULL
        GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO
    ),
    ultimos_verificables AS (
        SELECT 
            VER.AREDR_CODIGO,
            VER.AREEV_CODIGO,
            ROW_NUMBER() OVER (
                PARTITION BY VER.AREDR_CODIGO 
                ORDER BY 
                    CASE 
                        WHEN VER.AREEV_CODIGO = 2 THEN 1
                        WHEN VER.AREEV_CODIGO = 1 THEN 2
                        WHEN VER.AREEV_CODIGO = 3 THEN 3
                        ELSE 4 
                    END ASC,
                    NVL(VER.AREVE_FECHA_CARGA, VER.AREVE_FECHA) DESC NULLS LAST, 
                    VER.AREVE_CODIGO DESC
            ) AS RN
        FROM ROOTSISREC.AI_REC_VERIFICABLES VER
        WHERE VER.AREVE_ESTADO = 1
    ),
    asignaciones_dpto AS (
        SELECT 
            DET.AREDR_CODIGO,
            REC.AREES_CODIGO,
            UV.AREEV_CODIGO,
            FN_CALCULAR_DIAS_LABORABLES(COALESCE(PR.FECHA_PRORROGA, DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)) AS PLAZO_DIAS
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORGCON ON INF.AINOC_CODIGO = ORGCON.AINOC_CODIGO
        JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DET ON REC.ARERE_CODIGO = DET.ARERE_CODIGO AND DET.AREDR_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RESP ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO AND RESP.ARERS_ESTADO = 1
        LEFT JOIN ultimas_prorrogas PR ON PR.ARERE_CODIGO = DET.ARERE_CODIGO AND PR.ARERS_CODIGO = DET.ARERS_CODIGO
        LEFT JOIN ultimos_verificables UV ON DET.AREDR_CODIGO = UV.AREDR_CODIGO AND UV.RN = 1
        WHERE INF.AININ_ESTADO = 1
          AND HALL.AREHA_ESTADO = 1
          AND CON.ARECO_ESTADO = 1
          AND REC.ARERE_ESTADO = 1
          AND ORGCON.AINOC_ESTADO = 1
          AND (
                UPPER(RESP.ARERS_DEPARTAMENTO) LIKE '%' || v_clean_dpto || '%'
             OR UPPER(RESP.ARERS_DEPARTAMENTO_ABREV) LIKE '%' || v_clean_dpto || '%'
          )
    ),
    clasificadas AS (
        SELECT 
            CASE 
                WHEN AREES_CODIGO = 4 THEN 4
                WHEN AREES_CODIGO = 2 OR AREEV_CODIGO = 2 THEN 2
                WHEN AREEV_CODIGO = 3 OR PLAZO_DIAS < 0 THEN 3
                ELSE 1
            END AS ESTADO_FINAL
        FROM asignaciones_dpto
    )
    SELECT 
        COUNT(CASE WHEN ESTADO_FINAL = 1 THEN 1 END),
        COUNT(CASE WHEN ESTADO_FINAL = 2 THEN 1 END),
        COUNT(CASE WHEN ESTADO_FINAL = 3 THEN 1 END),
        COUNT(CASE WHEN ESTADO_FINAL = 4 THEN 1 END)
    INTO v_en_proceso, v_cumplidas, v_vencidas, v_no_aplica
    FROM clasificadas;

    -- Último mes
    SELECT COUNT(DISTINCT REC.ARERE_CODIGO)
    INTO v_ultimo_mes
    FROM ROOTSISREC.AI_INF_INFORMES INF
    JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO
    JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
    JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
    JOIN ROOTSISREC.AI_INF_ORG_CONT ORGCON ON INF.AINOC_CODIGO = ORGCON.AINOC_CODIGO
    JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DET ON REC.ARERE_CODIGO = DET.ARERE_CODIGO AND DET.AREDR_ESTADO = 1
    JOIN ROOTSISREC.AI_REC_RESPONSABLES RESP ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO AND RESP.ARERS_ESTADO = 1
    WHERE INF.AININ_ESTADO = 1
      AND HALL.AREHA_ESTADO = 1
      AND CON.ARECO_ESTADO = 1
      AND REC.ARERE_ESTADO = 1
      AND ORGCON.AINOC_ESTADO = 1
      AND (
            UPPER(RESP.ARERS_DEPARTAMENTO) LIKE '%' || v_clean_dpto || '%'
         OR UPPER(RESP.ARERS_DEPARTAMENTO_ABREV) LIKE '%' || v_clean_dpto || '%'
      )
      AND REC.ARERE_FECHA >= ADD_MONTHS(SYSDATE, -1);

    -- Solicitudes pendientes de aprobación
    SELECT COUNT(DISTINCT s.areso_codigo)
    INTO v_solicitudes_pend
    FROM ROOTSISREC.ai_rec_solicitudes s
    JOIN ROOTSISREC.ai_rec_responsables res ON s.arers_codigo = res.arers_codigo
    WHERE s.areso_estado = 1
      AND res.arers_estado = 1
      AND (
            UPPER(res.arers_departamento) LIKE '%' || v_clean_dpto || '%'
         OR UPPER(res.arers_departamento_abrev) LIKE '%' || v_clean_dpto || '%'
      )
      AND s.areso_estado_aprob IN (1, 2);

    -- JSON Output
    SELECT JSON_OBJECT(
        'cantRecIngreUltimoMes' VALUE v_ultimo_mes,
        'cantRecPendientesAprobacion' VALUE v_solicitudes_pend,
        'general' VALUE JSON_OBJECT(
            'enProceso' VALUE v_en_proceso,
            'cumplidas' VALUE v_cumplidas,
            'noCumplidas' VALUE v_vencidas,
            'noAplica' VALUE v_no_aplica
        ),
        'totalesGlobales' VALUE JSON_OBJECT(
            'totalRecomendaciones' VALUE (v_en_proceso + v_cumplidas + v_vencidas + v_no_aplica),
            'enProceso' VALUE v_en_proceso,
            'cumplidas' VALUE v_cumplidas,
            'noCumplidas' VALUE v_vencidas,
            'noAplica' VALUE v_no_aplica
        ),
        'departamentos' VALUE JSON_ARRAY(
            JSON_OBJECT(
                'departamento' VALUE nombre_dpto,
                'totalRecomendaciones' VALUE (v_en_proceso + v_cumplidas + v_vencidas + v_no_aplica),
                'enProceso' VALUE v_en_proceso,
                'cumplidas' VALUE v_cumplidas,
                'noCumplidas' VALUE v_vencidas,
                'noAplica' VALUE v_no_aplica
            )
        )
    RETURNING CLOB)
    INTO p_json
    FROM dual;

EXCEPTION
    WHEN OTHERS THEN
        p_json := JSON_OBJECT('error' VALUE SQLERRM);
END ObtenerReporteS1Dpto;
/

-- =============================================================================
-- GRUPO 2: AUDITOR INTERNO OPERATIVO (ROL 3)
-- =============================================================================

-- 4. reportePorDepartamentoEstadoSeguimiento
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
    v_clean_dpto  VARCHAR2(250);
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
        v_fecha_ini := TO_TIMESTAMP(TRIM(fecha_inicial), 'DD/MM/YYYY HH24:MI:SS');
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
        v_fecha_fin := TO_TIMESTAMP(TRIM(fecha_fin), 'DD/MM/YYYY HH24:MI:SS');
    END IF;

    v_clean_dpto := TRIM(REPLACE(REPLACE(REPLACE(UPPER(dpto), 'DPTO.', ''), 'DEPARTAMENTO', ''), 'DIRECCION', ''));

    WITH ultimas_prorrogas AS (
        SELECT 
            S.ARERE_CODIGO,
            S.ARERS_CODIGO,
            MAX(S.ARESO_FECHA_MAX_NUE) AS FECHA_PRORROGA
        FROM ROOTSISREC.AI_REC_SOLICITUDES S
        WHERE S.ARESO_ESTADO = '1'
          AND S.ARESO_ESTADO_APROB = '3'
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
                ORDER BY 
                    CASE 
                        WHEN v.AREEV_CODIGO = 2 THEN 1
                        WHEN v.AREEV_CODIGO = 1 THEN 2
                        WHEN v.AREEV_CODIGO = 3 THEN 3
                        ELSE 4 
                    END ASC,
                    NVL(v.AREVE_FECHA_CARGA, v.AREVE_FECHA) DESC NULLS LAST, 
                    v.AREVE_CODIGO DESC
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
            ES.AREES_DESCRIPCION                       AS ESTADO_SEGUIMIENTO_DESC,
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
        JOIN ROOTSISREC.AI_REC_ESTADO_SEGUIMIENTO ES ON ES.AREES_CODIGO = REC.AREES_CODIGO
        WHERE (INF.AININ_FECHA_APROBACION IS NULL OR INF.AININ_FECHA_APROBACION BETWEEN v_fecha_ini AND v_fecha_fin)
          AND (REC.AREES_CODIGO = estado_seguimiento OR estado_seguimiento = 0)
          AND (auditor_responsable = '0' OR INF.AININ_USUARIO = auditor_responsable)
          AND (dpto = '0' OR REC.ARERE_CODIGO IN (
              SELECT DR.ARERE_CODIGO
              FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR
              JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO
              WHERE (dpto = '0' 
                     OR RES.ARERS_DEPARTAMENTO = dpto 
                     OR UPPER(RES.ARERS_DEPARTAMENTO) LIKE '%' || v_clean_dpto || '%' 
                     OR UPPER(RES.ARERS_DEPARTAMENTO_ABREV) LIKE '%' || v_clean_dpto || '%')
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
            WHERE (dpto = '0' 
                   OR RES.ARERS_DEPARTAMENTO = dpto 
                   OR UPPER(RES.ARERS_DEPARTAMENTO) LIKE '%' || v_clean_dpto || '%' 
                   OR UPPER(RES.ARERS_DEPARTAMENTO_ABREV) LIKE '%' || v_clean_dpto || '%')
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
        WHERE (dpto = '0' 
               OR RES.ARERS_DEPARTAMENTO = dpto 
               OR UPPER(RES.ARERS_DEPARTAMENTO) LIKE '%' || v_clean_dpto || '%' 
               OR UPPER(RES.ARERS_DEPARTAMENTO_ABREV) LIKE '%' || v_clean_dpto || '%')
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
            rb.ESTADO_SEGUIMIENTO_DESC AS estado_seguimiento,
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
                   'estadoSeguimiento' VALUE estado_seguimiento,
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

-- 5. REPORTERECOMENDACIONESVENCIDASOPERATIVO
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
            ROW_NUMBER() OVER (
                PARTITION BY VER.AREDR_CODIGO 
                ORDER BY 
                    CASE 
                        WHEN VER.AREEV_CODIGO = 2 THEN 1
                        WHEN VER.AREEV_CODIGO = 1 THEN 2
                        WHEN VER.AREEV_CODIGO = 3 THEN 3
                        ELSE 4
                    END ASC,
                    NVL(VER.AREVE_FECHA_CARGA, VER.AREVE_FECHA) DESC NULLS LAST, 
                    VER.AREVE_CODIGO DESC
            ) AS RN
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

-- 6. reporteRecomendacionesProgreso
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
            ROW_NUMBER() OVER (
                PARTITION BY VER.AREDR_CODIGO 
                ORDER BY 
                    CASE 
                        WHEN VER.AREEV_CODIGO = 2 THEN 1
                        WHEN VER.AREEV_CODIGO = 1 THEN 2
                        WHEN VER.AREEV_CODIGO = 3 THEN 3
                        ELSE 4 
                    END ASC,
                    NVL(VER.AREVE_FECHA_CARGA, VER.AREVE_FECHA) DESC NULLS LAST, 
                    VER.AREVE_CODIGO DESC
            ) AS RN
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
                   'codigoInforme'              VALUE TO_CHAR(aininCodigo),
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

-- 7. reporteSeguimientoResponsableOperativo
CREATE OR REPLACE PROCEDURE ROOTSISREC.reporteSeguimientoResponsableOperativo (
    p_json              OUT CLOB,
    fecha_inicial       IN  VARCHAR2,
    fecha_fin           IN  VARCHAR2,
    organismo_control   IN  INTEGER,
    estado_seguimiento  IN  INTEGER,
    auditor_responsable IN  VARCHAR2,
    dpto                IN  VARCHAR2,
    responsable         IN  VARCHAR2 DEFAULT '0'
) IS
    v_fecha_ini TIMESTAMP;
    v_fecha_fin TIMESTAMP;
    v_clean_dpto VARCHAR2(250);
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

    v_clean_dpto := TRIM(REPLACE(REPLACE(REPLACE(UPPER(dpto), 'DPTO.', ''), 'DEPARTAMENTO', ''), 'DIRECCION', ''));

    WITH ultimas_prorrogas AS (
        SELECT 
            S.ARERE_CODIGO,
            S.ARERS_CODIGO,
            MAX(S.ARESO_FECHA_MAX_NUE) AS fecha_prorroga
        FROM ROOTSISREC.AI_REC_SOLICITUDES S
        WHERE S.ARESO_ESTADO = '1'
          AND S.ARESO_ESTADO_APROB = '3'
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
                ORDER BY 
                    CASE 
                        WHEN V.AREEV_CODIGO = 2 THEN 1
                        WHEN V.AREEV_CODIGO = 1 THEN 2
                        WHEN V.AREEV_CODIGO = 3 THEN 3
                        ELSE 4 
                    END ASC,
                    NVL(V.AREVE_FECHA_CARGA, V.AREVE_FECHA) DESC NULLS LAST, 
                    V.AREVE_CODIGO DESC
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
            COUNT(DISTINCT CASE WHEN EST.AREES_DESCRIPCION = 'CUMPLIDA' THEN REC.ARERE_CODIGO END) AS recs_cumplidas
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO AND HALL.AREHA_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO AND CON.ARECO_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO AND REC.ARERE_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_ESTADO_SEGUIMIENTO EST ON EST.AREES_CODIGO = REC.AREES_CODIGO
        WHERE INF.AININ_ESTADO = 1
        GROUP BY INF.AININ_CODIGO
    ),
    filas AS (
        SELECT
            INF.AININ_CODIGO                                           AS codigoinforme,
            INF.AININ_MEMORANDO                                        AS memoauditoria,
            TO_CHAR(INF.AININ_FECHA_MEMO, 'DD/MM/YYYY')               AS fechamemorando,
            INF.AININ_MEMO_APROBACION                                  AS memopresidencia,
            TO_CHAR(INF.AININ_FECHA_APROBACION, 'DD/MM/YYYY')          AS fechaaprobacion,
            INF.AININ_USUARIO                                          AS auditor,
            TO_CHAR(INF.AININ_FECHA, 'DD/MM/YYYY')                     AS fechacreacioninforme,
            INF.AININ_TITULO                                           AS tituloinforme,
            ORG.AINOC_DESCRIPCION                                      AS organismocontrol,
            HALL.AREHA_TITULO                                          AS hallazgo,
            CON.ARECO_CONCLUSION                                       AS conclusion,
            REC.ARERE_CODIGO                                           AS codigorecomendacion,
            REC.ARERE_DESCRIPCION                                      AS recomendacion,
            TO_NUMBER(REGEXP_SUBSTR(REC.ARERE_NUM_REC, '^\d+'))       AS numrec,
            NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO)     AS informefechaorden,
            COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) AS fechavencimiento_date,
            TO_CHAR(COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'DD/MM/YYYY') AS fechavencimiento,
            EST.AREES_DESCRIPCION                                      AS estadorecomendacion,
            CASE
                WHEN UV.AREEV_CODIGO = 2                                THEN 'CUMPLIDA'
                WHEN COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) < TRUNC(SYSDATE) THEN 'VENCIDO'
                ELSE 'EN PROCESO'
            END AS estadoplazo,
            CASE
                WHEN COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) < TRUNC(SYSDATE)
                THEN TRUNC(SYSDATE) - TRUNC(COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA))
                ELSE 0
            END AS diasvencidos,
            ROUND((PR.responsables_cumplidos * 100.0) / NULLIF(PR.total_responsables, 0), 2) AS progresorecomendacion,
            TRIM(RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO)         AS responsable,
            RES.ARERS_USUARIO_PER                                       AS responsable_usuario,
            RES.ARERS_DEPARTAMENTO                                      AS departamento,
            UV.AREEV_CODIGO                                             AS areev_codigo,
            TO_CHAR(UV.AREVE_FECHA_CARGA, 'DD/MM/YYYY HH24:MI:SS')     AS fechacargaverificable,
            COALESCE(EV.AREEV_DESCRIPCION, 'VERIFICABLE NO CREADO')     AS estadoverificable,
            UV.AREVE_DESCRIPCION                                        AS descripcionverificableresponsable,
            UV.AREVE_OBSERVACION                                        AS observacionauditor,
            ROUND((PI.recs_cumplidas * 100.0) / NULLIF(PI.total_recs, 0), 2) AS progresoinforme,
            TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS')                  AS fechaconsulta
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO AND HALL.AREHA_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO AND CON.ARECO_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO AND REC.ARERE_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_ESTADO_SEGUIMIENTO EST ON EST.AREES_CODIGO = REC.AREES_CODIGO
        JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO AND DR.AREDR_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO AND RES.ARERS_ESTADO = 1
        LEFT JOIN ultimas_prorrogas UPR ON UPR.ARERE_CODIGO = DR.ARERE_CODIGO AND UPR.ARERS_CODIGO = DR.ARERS_CODIGO
        LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DR.AREDR_CODIGO AND UV.rn = 1
        LEFT JOIN ROOTSISREC.AI_REC_ESTADO_VERIFICABLE EV ON EV.AREEV_CODIGO = UV.AREEV_CODIGO
        LEFT JOIN progreso_recomendacion PR ON PR.ARERE_CODIGO = REC.ARERE_CODIGO
        LEFT JOIN progreso_informe PI ON PI.AININ_CODIGO = INF.AININ_CODIGO
        WHERE INF.AININ_ESTADO = 1
          AND (INF.AININ_FECHA_APROBACION IS NULL OR INF.AININ_FECHA_APROBACION BETWEEN v_fecha_ini AND v_fecha_fin)
          AND (organismo_control = 0 OR ORG.AINOC_CODIGO = organismo_control)
          AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)
          AND (auditor_responsable = '0' OR INF.AININ_USUARIO = auditor_responsable)
          AND (
                dpto = '0'
             OR RES.ARERS_DEPARTAMENTO = dpto
             OR UPPER(RES.ARERS_DEPARTAMENTO) LIKE '%' || v_clean_dpto || '%'
             OR UPPER(RES.ARERS_DEPARTAMENTO_ABREV) LIKE '%' || v_clean_dpto || '%'
          )
          AND (
                responsable = '0'
             OR UPPER(TRIM(RES.ARERS_USUARIO_PER)) = UPPER(TRIM(responsable))
             OR UPPER(TRIM(RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO)) LIKE '%' || UPPER(TRIM(responsable)) || '%'
          )
    )
    SELECT
        JSON_ARRAYAGG(
            JSON_OBJECT(
                'codigoInforme'                     VALUE NVL(f.codigoinforme, 0),
                'memoAuditoria'                     VALUE NVL(f.memoauditoria, ''),
                'fechaMemorando'                    VALUE NVL(f.fechamemorando, ''),
                'memoPresidencia'                   VALUE NVL(f.memopresidencia, ''),
                'fechaAprobacion'                   VALUE NVL(f.fechaaprobacion, ''),
                'auditor'                           VALUE NVL(f.auditor, ''),
                'fechaCreacionInforme'              VALUE NVL(f.fechacreacioninforme, ''),
                'tituloInforme'                     VALUE NVL(f.tituloinforme, ''),
                'organismoControl'                  VALUE NVL(f.organismocontrol, ''),
                'hallazgo'                          VALUE NVL(f.hallazgo, ''),
                'conclusion'                        VALUE NVL(f.conclusion, ''),
                'codigoRecomendacion'               VALUE NVL(f.codigorecomendacion, 0),
                'recomendacion'                     VALUE NVL(f.recomendacion, ''),
                'fechaVencimiento'                  VALUE NVL(f.fechavencimiento, ''),
                'estadoRecomendacion'               VALUE NVL(f.estadorecomendacion, ''),
                'estadoPlazo'                       VALUE NVL(f.estadoplazo, ''),
                'diasVencidos'                      VALUE NVL(f.diasvencidos, 0),
                'progresoRecomendacion'             VALUE NVL(f.progresorecomendacion, 0),
                'fechaCargaVerificable'             VALUE NVL(f.fechacargaverificable, ''),
                'responsable'                       VALUE NVL(f.responsable, ''),
                'estadoVerificable'                 VALUE NVL(f.estadoverificable, ''),
                'departamento'                      VALUE NVL(f.departamento, ''),
                'descripcionVerificableResponsable' VALUE NVL(f.descripcionverificableresponsable, ''),
                'observacionAuditor'                VALUE NVL(f.observacionauditor, ''),
                'progresoInforme'                   VALUE NVL(f.progresoinforme, 0),
                'fechaConsulta'                     VALUE NVL(f.fechaconsulta, '')
                RETURNING CLOB
            )
            ORDER BY f.informefechaorden ASC NULLS LAST, f.codigoinforme ASC, f.numrec ASC NULLS LAST, f.codigorecomendacion ASC, f.responsable ASC
            RETURNING CLOB
        )
    INTO p_json
    FROM filas f;

    IF p_json IS NULL THEN
        p_json := TO_CLOB(JSON_OBJECT('mensaje' VALUE 'No se encontraron registros para el reporte de seguimiento con los criterios especificados'));
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END reporteSeguimientoResponsableOperativo;
/

-- =============================================================================
-- GRUPO 3: RESPONSABLE / AUDITADO (ROL 4)
-- =============================================================================

-- 8. REPORTERECOMENDACIONESVENCIDASRESPONSABLE
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
          AND S.ARESO_ESTADO_APROB = '3'
          AND S.ARESO_FECHA_MAX_NUE IS NOT NULL
        GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO
    ),
    ultimos_verificables AS (
        SELECT 
            VER.AREDR_CODIGO,
            VER.AREVE_CODIGO,
            VER.AREEV_CODIGO,
            VER.AREVE_OBSERVACION,
            ROW_NUMBER() OVER (
                PARTITION BY VER.AREDR_CODIGO 
                ORDER BY 
                    CASE 
                        WHEN VER.AREEV_CODIGO = 2 THEN 1
                        WHEN VER.AREEV_CODIGO = 1 THEN 2
                        WHEN VER.AREEV_CODIGO = 3 THEN 3
                        ELSE 4 
                    END ASC,
                    NVL(VER.AREVE_FECHA_CARGA, VER.AREVE_FECHA) DESC NULLS LAST, 
                    VER.AREVE_CODIGO DESC
            ) AS RN
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
          AND (UV.AREVE_CODIGO IS NULL OR UV.AREEV_CODIGO IN (1, 3))
    ),
    filtradas AS (
        SELECT 
            aininCodigo,
            informeFechaOrden,
            organismoControl,
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
            responsable_info
        FROM asignaciones
        WHERE plazo < 0
    )
    SELECT JSON_ARRAYAGG(
        JSON_OBJECT(
            'codigoInforme'     VALUE datos.aininCodigo,
            'organismoControl'  VALUE datos.organismoControl,
            'memorando'         VALUE datos.memorando,
            'memoAprobacion'    VALUE datos.memoAprobacion,
            'fechaEmision'      VALUE datos.fechaEmision,
            'tituloInforme'     VALUE datos.tituloInforme,
            'recomendacion'     VALUE datos.recomendacion,
            'observacion'       VALUE datos.observacion,
            'fechaMaxima'       VALUE datos.fechaMaxima,
            'plazo'             VALUE TO_NUMBER(NVL(datos.plazo, 0)),
            'responsable'       VALUE datos.responsable_info,
            'arereCodigo'       VALUE TO_NUMBER(NVL(datos.arereCodigo, 0)),
            'numRec'            VALUE TO_NUMBER(NVL(datos.numRec, 0))
            RETURNING CLOB
        )
        ORDER BY datos.informeFechaOrden ASC NULLS LAST, datos.aininCodigo ASC, datos.numRec ASC NULLS LAST, datos.arereCodigo ASC
        RETURNING CLOB
    )
    INTO p_json
    FROM filtradas datos;

    IF p_json IS NULL THEN
        p_json := TO_CLOB(JSON_OBJECT('mensaje' VALUE 'No se encontraron recomendaciones vencidas para el usuario'));
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END REPORTERECOMENDACIONESVENCIDASRESPONSABLE;
/

-- 9. reporteRecomendacionesEstadoFechaCumplimientoResponsable
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
          AND S.ARESO_ESTADO_APROB = '3'
          AND S.ARESO_FECHA_MAX_NUE IS NOT NULL
        GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO
    ),
    UltimosVerificables AS (
        SELECT
            v.*,
            ROW_NUMBER() OVER (
                PARTITION BY v.AREDR_CODIGO
                ORDER BY 
                    CASE 
                        WHEN v.AREEV_CODIGO = 2 THEN 1
                        WHEN v.AREEV_CODIGO = 1 THEN 2
                        WHEN v.AREEV_CODIGO = 3 THEN 3
                        ELSE 4 
                    END ASC,
                    NVL(v.AREVE_FECHA_CARGA, v.AREVE_FECHA) DESC NULLS LAST, 
                    v.AREVE_CODIGO DESC
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
            )                                                      AS departamento,
            (
                SELECT LISTAGG(
                           TRIM(rp.nombre_completo) || ' (' || rp.estado_verificable || ')',
                           ', ' ON OVERFLOW TRUNCATE
                       ) WITHIN GROUP (ORDER BY rp.nombre_completo)
                FROM responsables_pendientes rp
                WHERE rp.ARERE_CODIGO = REC.ARERE_CODIGO
                  AND UPPER(rp.estado_verificable) != 'JUSTIFICA'
            )                                                      AS responsable,
            (
                SELECT COUNT(DISTINCT rp.nombre_completo)
                FROM responsables_pendientes rp
                WHERE rp.ARERE_CODIGO = REC.ARERE_CODIGO
                  AND UPPER(rp.estado_verificable) != 'JUSTIFICA'
            )                                                      AS cant_responsables_pendientes
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO
        JOIN ROOTSISREC.AI_REC_ESTADO_SEGUIMIENTO ES ON ES.AREES_CODIGO = REC.AREES_CODIGO
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORG ON ORG.AINOC_CODIGO = INF.AINOC_CODIGO
        JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO
        LEFT JOIN ultimas_prorrogas PR ON PR.ARERE_CODIGO = DR.ARERE_CODIGO AND PR.ARERS_CODIGO = DR.ARERS_CODIGO
        WHERE (INF.AININ_FECHA_APROBACION IS NULL OR INF.AININ_FECHA_APROBACION BETWEEN v_fecha_ini AND v_fecha_fin)
          AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)
          AND (usuario = '0' OR UPPER(TRIM(RES.ARERS_USUARIO_PER)) = UPPER(TRIM(usuario)))
          AND INF.AININ_ESTADO = 1
          AND HALL.AREHA_ESTADO = 1
          AND CON.ARECO_ESTADO = 1
          AND REC.ARERE_ESTADO = 1
          AND ORG.AINOC_ESTADO = 1
          AND DR.AREDR_ESTADO = 1
          AND RES.ARERS_ESTADO = 1
    )
    SELECT
        JSON_ARRAYAGG(
            JSON_OBJECT(
                'codigoInforme'         VALUE TO_NUMBER(datos.aininCodigo),
                'tituloInforme'         VALUE NVL(datos.tituloInforme, ''),
                'conclusion'            VALUE NVL(datos.conclusion, ''),
                'memorando'             VALUE NVL(datos.memorando, ''),
                'memoAprobacion'        VALUE NVL(datos.memoAprobacion, ''),
                'fechaMaxima'           VALUE NVL(datos.fechaMaxima, ''),
                'fechaInicioControl'    VALUE NVL(datos.fechaInicioControl, ''),
                'codigoRecomendacion'   VALUE TO_NUMBER(datos.codigoRecomendacion),
                'recomendacion'         VALUE NVL(datos.recomendacion, ''),
                'organismoControl'      VALUE NVL(datos.organismoControl, ''),
                'estadoSeguimiento'     VALUE NVL(datos.estadoSeguimiento, ''),
                'departamento'          VALUE NVL(datos.departamento, ''),
                'responsable'           VALUE NVL(datos.responsable, '')
                RETURNING CLOB
            )
            ORDER BY datos.informeFechaOrden ASC NULLS LAST, datos.aininCodigo ASC, datos.numRec ASC NULLS LAST, datos.codigoRecomendacion ASC
            RETURNING CLOB
        )
    INTO p_json
    FROM datos;

    IF p_json IS NULL THEN
        p_json := TO_CLOB(JSON_OBJECT('mensaje' VALUE 'No se encontraron recomendaciones con los criterios especificados'));
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END reporteRecomendacionesEstadoFechaCumplimientoResponsable;
/

-- =============================================================================
-- GRUPO 4: AUDITOR INTERNO JEFE (ROL 2)
-- =============================================================================

-- 10. reportegeneralaij
CREATE OR REPLACE PROCEDURE ROOTSISREC.reportegeneralaij (
    p_json             OUT CLOB,
    fecha_inicial      IN VARCHAR2,
    fecha_fin          IN VARCHAR2,
    organismo_control  IN INTEGER,  -- 0 = todos
    estado_seguimiento IN INTEGER,  -- 0 = todos
    auditor            IN VARCHAR2, -- '0' = todos
    dpto               IN VARCHAR2  -- '0' = todos
) IS
    v_fecha_ini TIMESTAMP;
    v_fecha_fin TIMESTAMP;
BEGIN
    v_fecha_ini := TO_TIMESTAMP ( fecha_inicial, 'DD/MM/YYYY HH24:MI:SS' );
    v_fecha_fin := TO_TIMESTAMP ( fecha_fin, 'DD/MM/YYYY HH24:MI:SS' );
    
    WITH ultimosverificables AS (
        SELECT
            v.*,
            ROW_NUMBER() OVER(
                PARTITION BY v.aredr_codigo
                ORDER BY 
                    CASE 
                        WHEN v.areev_codigo = 2 THEN 1
                        WHEN v.areev_codigo = 1 THEN 2
                        WHEN v.areev_codigo = 3 THEN 3
                        ELSE 4 
                    END ASC,
                    v.areve_fecha_carga DESC NULLS LAST,
                    v.areve_codigo DESC
            ) AS rn
        FROM
            ROOTSISREC.ai_rec_verificables v
        WHERE
            v.areve_estado = 1
    ),
    progresorecomendacion AS (
        SELECT
            dr.arere_codigo,
            COUNT(DISTINCT dr.arers_codigo) AS total_responsables,
            COUNT(DISTINCT
                CASE
                    WHEN ev.areev_descripcion = 'JUSTIFICA' THEN
                        dr.arers_codigo
                END
            )                               AS responsables_cumplidos
        FROM
            ROOTSISREC.ai_rec_detalle_recomendacion dr
            LEFT JOIN ultimosverificables ver ON ver.aredr_codigo = dr.aredr_codigo AND ver.rn = 1
            LEFT JOIN ROOTSISREC.ai_rec_estado_verificable ev ON ev.areev_codigo = ver.areev_codigo
        WHERE
            dr.aredr_estado = 1
        GROUP BY
            dr.arere_codigo
    ),
    progresoinforme AS (
        SELECT
            inf.ainin_codigo,
            COUNT(DISTINCT rec.arere_codigo) AS total_recs,
            COUNT(DISTINCT
                CASE
                    WHEN est.arees_descripcion = 'CUMPLIDA' THEN
                        rec.arere_codigo
                END
            )                                AS recs_cumplidas
        FROM
                 ROOTSISREC.ai_inf_informes inf
            JOIN ROOTSISREC.ai_rec_hallazgos          hall ON hall.ainin_codigo = inf.ainin_codigo
                                          AND hall.areha_estado = 1
            JOIN ROOTSISREC.ai_rec_conclusiones       con ON con.areha_codigo = hall.areha_codigo
                                            AND con.areco_estado = 1
            JOIN ROOTSISREC.ai_rec_recomendaciones    rec ON rec.areco_codigo = con.areco_codigo
                                               AND rec.arere_estado = 1
            JOIN ROOTSISREC.ai_rec_estado_seguimiento est ON est.arees_codigo = rec.arees_codigo
        WHERE
            inf.ainin_estado = 1
        GROUP BY
            inf.ainin_codigo
    )
    SELECT
        JSON_ARRAYAGG(
            JSON_OBJECT(
                'codigoInforme' VALUE nvl(codigoinforme, 0),
                'memoAuditoria' VALUE nvl(memoauditoria, ''),
                'fechaMemorando' VALUE nvl(fechamemorando, ''),
                'memoPresidencia' VALUE nvl(memopresidencia, ''),
                'fechaAprobacion' VALUE nvl(fechaaprobacion, ''),
                'auditor' VALUE nvl(auditor, ''),
                'fechaCreacionInforme' VALUE nvl(fechacreacioninforme, ''),
                'tituloInforme' VALUE nvl(tituloinforme, ''),
                'organismoControl' VALUE nvl(organismocontrol, ''),
                'hallazgo' VALUE nvl(hallazgo, ''),
                'conclusion' VALUE nvl(conclusion, ''),
                'codigoRecomendacion' VALUE nvl(codigorecomendacion, 0),
                'recomendacion' VALUE nvl(recomendacion, ''),
                'fechaVencimiento' VALUE nvl(fechavencimiento, ''),
                'estadoRecomendacion' VALUE nvl(estadorecomendacion, ''),
                'estadoPlazo' VALUE nvl(estadoplazo, ''),
                'diasVencidos' VALUE nvl(diasvencidos, 0),
                'progresoRecomendacion' VALUE nvl(progresorecomendacion, 0),
                'fechaCargaVerificable' VALUE nvl(fechacargaverificable, ''),
                'responsable' VALUE nvl(responsable, ''),
                'estadoVerificable' VALUE nvl(estadoverificable, ''),
                'departamento' VALUE nvl(departamento, ''),
                'descripcionVerificableResponsable' VALUE nvl(descripcionverificableresponsable, ''),
                'observacionAuditor' VALUE nvl(observacionauditor, ''),
                'progresoInforme' VALUE nvl(progresoinforme, 0),
                'fechaConsulta' VALUE nvl(fechaconsulta, '')
            RETURNING CLOB)
        RETURNING CLOB)
    INTO p_json
    FROM
        (
            SELECT
                inf.ainin_codigo                                        AS codigoinforme,
                inf.ainin_memorando                                     AS memoauditoria,
                to_char(inf.ainin_fecha_memo, 'DD/MM/YYYY')             AS fechamemorando,
                inf.ainin_memo_aprobacion                               AS memopresidencia,
                to_char(inf.ainin_fecha_aprobacion, 'DD/MM/YYYY')       AS fechaaprobacion,
                inf.ainin_usuario                                       AS auditor,
                to_char(inf.ainin_fecha, 'DD/MM/YYYY')                  AS fechacreacioninforme,
                to_char(inf.ainin_titulo)                               AS tituloinforme,
                org.ainoc_descripcion                                   AS organismocontrol,
                to_char(hall.areha_titulo)                              AS hallazgo,
                con.areco_conclusion                                    AS conclusion,
                rec.arere_codigo                                        AS codigorecomendacion,
                to_char(rec.arere_descripcion)                          AS recomendacion,
                to_char(rec.arere_fecha_maxima, 'DD/MM/YYYY')           AS fechavencimiento,
                est.arees_descripcion                                   AS estadorecomendacion,
                CASE
                    WHEN est.arees_descripcion = 'CUMPLIDA'      THEN
                        'CUMPLIDA'
                    WHEN rec.arere_fecha_maxima < trunc(sysdate) THEN
                        'VENCIDO'
                    ELSE
                        'EN PROCESO'
                END                                                     AS estadoplazo,
                CASE
                    WHEN rec.arere_fecha_maxima < trunc(sysdate) THEN
                        trunc(sysdate) - trunc(rec.arere_fecha_maxima)
                    ELSE
                        0
                END                                                     AS diasvencidos,
                round((pr.responsables_cumplidos * 100.0) / nullif(pr.total_responsables, 0),
                      2)                                                AS progresorecomendacion,
                to_char(ver.areve_fecha_carga, 'DD/MM/YYYY HH24:MI:SS') AS fechacargaverificable,
                res.arers_nombre
                || ' '
                || res.arers_apellido                                   AS responsable,
                coalesce(ev.areev_descripcion, 'VERIFICABLE NO CREADO') AS estadoverificable,
                res.arers_departamento                                  AS departamento,
                ver.areve_descripcion                                   AS descripcionverificableresponsable,
                ver.areve_observacion                                   AS observacionauditor,
                round((pi.recs_cumplidas * 100.0) / nullif(pi.total_recs, 0),
                      2)                                                AS progresoinforme,
                to_char(sysdate, 'DD/MM/YYYY HH24:MI:SS')               AS fechaconsulta
            FROM
                     ROOTSISREC.ai_inf_informes inf
                JOIN ROOTSISREC.ai_inf_org_cont              org ON inf.ainoc_codigo = org.ainoc_codigo
                JOIN ROOTSISREC.ai_rec_hallazgos             hall ON hall.ainin_codigo = inf.ainin_codigo
                                              AND hall.areha_estado = 1
                JOIN ROOTSISREC.ai_rec_conclusiones          con ON con.areha_codigo = hall.areha_codigo
                                                AND con.areco_estado = 1
                JOIN ROOTSISREC.ai_rec_recomendaciones       rec ON rec.areco_codigo = con.areco_codigo
                                                   AND rec.arere_estado = 1
                JOIN ROOTSISREC.ai_rec_estado_seguimiento    est ON est.arees_codigo = rec.arees_codigo
                JOIN ROOTSISREC.ai_rec_detalle_recomendacion dr ON dr.arere_codigo = rec.arere_codigo
                                                        AND dr.aredr_estado = 1
                JOIN ROOTSISREC.ai_rec_responsables          res ON res.arers_codigo = dr.arers_codigo
                LEFT JOIN ultimosverificables          ver ON ver.aredr_codigo = dr.aredr_codigo
                                                     AND ver.rn = 1
                LEFT JOIN ROOTSISREC.ai_rec_estado_verificable    ev ON ev.areev_codigo = ver.areev_codigo
                LEFT JOIN progresorecomendacion        pr ON pr.arere_codigo = rec.arere_codigo
                LEFT JOIN progresoinforme              pi ON pi.ainin_codigo = inf.ainin_codigo
            WHERE
                    inf.ainin_estado = 1
                AND inf.ainin_fecha_aprobacion BETWEEN v_fecha_ini AND v_fecha_fin
                AND ( organismo_control = 0
                      OR org.ainoc_codigo = organismo_control )
                AND ( estado_seguimiento = 0
                      OR rec.arees_codigo = estado_seguimiento )
                AND ( auditor = '0'
                      OR inf.ainin_usuario = auditor )
                AND ( dpto = '0'
                      OR res.arers_departamento = dpto )
            ORDER BY
                inf.ainin_memorando,
                hall.areha_titulo,
                rec.arere_descripcion,
                res.arers_apellido,
                res.arers_nombre
        );

    IF p_json IS NULL THEN
        p_json := to_clob(
            JSON_OBJECT(
                'mensaje' VALUE 'No se encontraron registros para el reporte general AIJ con los criterios especificados'
            )
        );
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_json := to_clob(
            JSON_OBJECT(
                'error' VALUE sqlerrm
            )
        );
END reportegeneralaij;
/

-- 11. reporte7Auditores
CREATE OR REPLACE PROCEDURE ROOTSISREC.reporte7Auditores (
    p_json              OUT CLOB,
    fecha_inicial       IN  VARCHAR2,
    fecha_fin           IN  VARCHAR2,
    auditor             IN  VARCHAR2
)
IS
    v_fecha_ini    TIMESTAMP;
    v_fecha_fin    TIMESTAMP;
BEGIN
    v_fecha_ini := TO_TIMESTAMP(fecha_inicial || ' 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
    v_fecha_fin := TO_TIMESTAMP(fecha_fin || ' 23:59:59', 'DD/MM/YYYY HH24:MI:SS');

    SELECT JSON_ARRAYAGG(
        JSON_OBJECT(
            'tituloInforme'     VALUE NVL(tituloInforme, ''),
            'recomendacion'     VALUE NVL(recomendacion, ''),
            'enteControl'       VALUE NVL(enteControl, ''),
            'memorando'         VALUE NVL(memorando, ''),
            'memoAprobacion'    VALUE NVL(memoAprobacion, ''),
            'fechaEmision'      VALUE NVL(fechaEmision, ''),
            'fechaMaxima'       VALUE NVL(fechaMaxima, ''),
            'auditor'           VALUE NVL(auditor_val, ''),
            'arereCodigo'       VALUE TO_NUMBER(NVL(arereCodigo, 0)),
            'responsables'      VALUE responsables_json
            RETURNING CLOB
        )
        RETURNING CLOB
    )
    INTO p_json
    FROM (
        SELECT 
            ORGCON.AINOC_DESCRIPCION AS enteControl,
            INF.AININ_MEMORANDO AS memorando,
            INF.AININ_MEMO_APROBACION AS memoAprobacion,
            TO_CHAR(INF.AININ_FECHA_MEMO, 'DD/MM/YYYY') AS fechaEmision,
            INF.AININ_TITULO AS tituloInforme,
            REC.ARERE_DESCRIPCION AS recomendacion,
            TO_CHAR(REC.ARERE_FECHA_MAXIMA, 'DD/MM/YYYY') AS fechaMaxima,
            REC.ARERE_CODIGO AS arereCodigo,
            INF.AININ_USUARIO AS auditor_val,
            NVL((
                SELECT JSON_ARRAYAGG(
                    JSON_OBJECT(
                        'nombreCompleto' VALUE NVL(RESP2.ARERS_NOMBRE || ' ' || RESP2.ARERS_APELLIDO, ''),
                        'observacion' VALUE CASE 
                            WHEN ULT_VER2.AREVE_OBSERVACION IS NULL OR TRIM(ULT_VER2.AREVE_OBSERVACION) = '' 
                            THEN 'no existe observacion' 
                            ELSE ULT_VER2.AREVE_OBSERVACION 
                        END,
                        'fechaHoraCargaVerificable' VALUE CASE 
                            WHEN ULT_VER2.AREVE_FECHA_CARGA IS NOT NULL 
                            THEN TO_CHAR(ULT_VER2.AREVE_FECHA_CARGA, 'DD/MM/YYYY HH24:MI:SS')
                            ELSE NULL
                        END,
                        'fechaHoraCambioEstado' VALUE CASE 
                            WHEN ULT_VER2.AREVE_FECHA IS NOT NULL 
                            THEN TO_CHAR(ULT_VER2.AREVE_FECHA, 'DD/MM/YYYY HH24:MI:SS')
                            ELSE NULL
                        END,
                        'estadoVerificable' VALUE NVL(EST_VER.AREEV_DESCRIPCION, 'VERIFICABLE NO CREADO')
                        RETURNING CLOB
                    )
                    RETURNING CLOB
                )
                FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DET2
                JOIN ROOTSISREC.AI_REC_RESPONSABLES RESP2 
                    ON DET2.ARERS_CODIGO = RESP2.ARERS_CODIGO
                LEFT JOIN (
                    SELECT 
                        VER2.AREDR_CODIGO,
                        VER2.AREVE_FECHA_CARGA,
                        VER2.AREVE_FECHA,
                        VER2.AREVE_OBSERVACION,
                        VER2.AREEV_CODIGO,
                        ROW_NUMBER() OVER (
                            PARTITION BY VER2.AREDR_CODIGO 
                            ORDER BY 
                                CASE 
                                    WHEN VER2.AREEV_CODIGO = 2 THEN 1
                                    WHEN VER2.AREEV_CODIGO = 1 THEN 2
                                    WHEN VER2.AREEV_CODIGO = 3 THEN 3
                                    ELSE 4 
                                END ASC,
                                NVL(VER2.AREVE_FECHA_CARGA, VER2.AREVE_FECHA) DESC NULLS LAST, 
                                VER2.AREVE_CODIGO DESC
                        ) AS RN
                    FROM ROOTSISREC.AI_REC_VERIFICABLES VER2
                    WHERE VER2.AREVE_ESTADO = 1
                ) ULT_VER2 ON DET2.AREDR_CODIGO = ULT_VER2.AREDR_CODIGO 
                    AND ULT_VER2.RN = 1
                LEFT JOIN ROOTSISREC.AI_REC_ESTADO_VERIFICABLE EST_VER 
                    ON ULT_VER2.AREEV_CODIGO = EST_VER.AREEV_CODIGO
                WHERE DET2.ARERE_CODIGO = REC.ARERE_CODIGO
                  AND DET2.AREDR_ESTADO = 1
                  AND RESP2.ARERS_ESTADO = 1
            ), TO_CLOB('[]')) AS responsables_json
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL 
            ON INF.AININ_CODIGO = HALL.AININ_CODIGO
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON 
            ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC 
            ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORGCON 
            ON INF.AINOC_CODIGO = ORGCON.AINOC_CODIGO
        WHERE INF.AININ_FECHA_MEMO BETWEEN v_fecha_ini AND v_fecha_fin
          AND (auditor IS NULL OR auditor = '0' OR auditor = '' OR INF.AININ_USUARIO = auditor)
          AND INF.AININ_ESTADO = 1
          AND HALL.AREHA_ESTADO = 1
          AND CON.ARECO_ESTADO = 1
          AND REC.ARERE_ESTADO = 1
          AND ORGCON.AINOC_ESTADO = 1
    );

    IF p_json IS NULL THEN
        p_json := TO_CLOB(JSON_OBJECT('mensaje' VALUE 'No se encontraron registros con los criterios especificados'));
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END reporte7Auditores;
/

-- 12. reporteprorrogasaij
CREATE OR REPLACE PROCEDURE ROOTSISREC.reporteprorrogasaij (
    p_json OUT CLOB
) AS
BEGIN
    WITH ultimosverificables AS (
        SELECT
            v.aredr_codigo,
            v.areev_codigo,
            ROW_NUMBER() OVER(
                PARTITION BY v.aredr_codigo
                ORDER BY 
                    CASE 
                        WHEN v.areev_codigo = 2 THEN 1
                        WHEN v.areev_codigo = 1 THEN 2
                        WHEN v.areev_codigo = 3 THEN 3
                        ELSE 4 
                    END ASC,
                    NVL(v.areve_fecha_carga, v.areve_fecha) DESC NULLS LAST, 
                    v.areve_codigo DESC
            ) AS rn
        FROM
            ROOTSISREC.ai_rec_verificables v
        WHERE
            v.areve_estado = '1'
    )
    SELECT
        JSON_OBJECT(
            'datos' VALUE JSON_ARRAYAGG(
                JSON_OBJECT(
                    'numeroRecomendacion'      VALUE numero_recomendacion,
                    'descripcionRecomendacion' VALUE descripcion_recomendacion,
                    'fechaMaxima'              VALUE to_char(fecha_maxima, 'YYYY-MM-DD'),
                    'estadoRecomendacion'      VALUE estado_recomendacion,
                    'responsable'              VALUE responsable,
                    'departamento'             VALUE departamento,
                    'estadoVerificable'        VALUE estado_verificable,
                    'totalProrrogas'           VALUE total_prorrogas,
                    'diasVencidos'             VALUE dias_vencidos,
                    'estadoPlazo'              VALUE estado_plazo,
                    'prorrogas' VALUE (
                        SELECT
                            JSON_ARRAYAGG(
                                JSON_OBJECT(
                                    'fechaAnterior' VALUE to_char(inner_sol.areso_fecha_max_ant, 'YYYY-MM-DD'),
                                    'nuevaFecha'    VALUE to_char(inner_sol.areso_fecha_max_nue, 'YYYY-MM-DD')
                                )
                                ORDER BY inner_sol.areso_fecha ASC
                                RETURNING CLOB
                            )
                        FROM
                            ROOTSISREC.ai_rec_solicitudes inner_sol
                        WHERE
                                inner_sol.arere_codigo = arere_codigo_val
                            AND inner_sol.areso_estado = '1'
                    ),
                    'memoPresidencia'          VALUE memo_presidencia,
                    'fechaAprobacion'          VALUE to_char(fecha_aprobacion, 'YYYY-MM-DD'),
                    'auditor'                  VALUE auditor,
                    'tituloInforme'            VALUE titulo_informe,
                    'organismoControl'         VALUE organismo_control,
                    'memoAuditoria'            VALUE memo_auditoria,
                    'fechaMemorando'           VALUE to_char(fecha_memorando, 'YYYY-MM-DD')
                )
            RETURNING CLOB)
        RETURNING CLOB)
    INTO p_json
    FROM
        (
            SELECT
                rec.arere_num_rec                                       AS numero_recomendacion,
                rec.arere_descripcion                                   AS descripcion_recomendacion,
                rec.arere_fecha_maxima                                  AS fecha_maxima,
                es.arees_descripcion                                    AS estado_recomendacion,
                rs.arers_nombre || ' ' || rs.arers_apellido             AS responsable,
                rs.arers_departamento                                   AS departamento,
                coalesce(ev.areev_descripcion, 'VERIFICABLE NO CREADO') AS estado_verificable,
                rec.arere_codigo                                        AS arere_codigo_val,
                (
                    SELECT COUNT(*)
                    FROM ROOTSISREC.ai_rec_solicitudes ctr_sol
                    WHERE ctr_sol.arere_codigo = rec.arere_codigo
                      AND ctr_sol.areso_estado = '1'
                ) AS total_prorrogas,
                CASE
                    WHEN rec.arere_fecha_maxima < trunc(sysdate) THEN
                        trunc(sysdate) - trunc(rec.arere_fecha_maxima)
                    ELSE
                        0
                END AS dias_vencidos,
                CASE
                    WHEN es.arees_descripcion = 'CUMPLIDA' THEN
                        'CUMPLIDA'
                    WHEN rec.arere_fecha_maxima < trunc(sysdate) THEN
                        'VENCIDO'
                    ELSE
                        'EN PROCESO'
                END AS estado_plazo,
                inf.ainin_memo_aprobacion   AS memo_presidencia,
                inf.ainin_fecha_aprobacion  AS fecha_aprobacion,
                inf.ainin_usuario           AS auditor,
                inf.ainin_titulo            AS titulo_informe,
                oc.ainoc_descripcion        AS organismo_control,
                inf.ainin_memorando         AS memo_auditoria,
                inf.ainin_fecha_memo        AS fecha_memorando
            FROM
                     ROOTSISREC.ai_rec_recomendaciones rec
                JOIN ROOTSISREC.ai_rec_estado_seguimiento  es  ON rec.arees_codigo = es.arees_codigo
                JOIN ROOTSISREC.ai_rec_detalle_recomendacion dr ON dr.arere_codigo = rec.arere_codigo 
                                                   AND dr.aredr_estado = '1'
                JOIN ROOTSISREC.ai_rec_responsables        rs  ON dr.arers_codigo = rs.arers_codigo
                JOIN ROOTSISREC.ai_rec_conclusiones        con ON rec.areco_codigo = con.areco_codigo 
                                                   AND con.areco_estado = '1'
                JOIN ROOTSISREC.ai_rec_hallazgos           hall ON con.areha_codigo = hall.areha_codigo 
                                                   AND hall.areha_estado = '1'
                JOIN ROOTSISREC.ai_inf_informes            inf ON hall.ainin_codigo = inf.ainin_codigo 
                                                   AND inf.ainin_estado = '1'
                JOIN ROOTSISREC.ai_inf_org_cont            oc  ON inf.ainoc_codigo = oc.ainoc_codigo
                LEFT JOIN ultimosverificables   uv  ON uv.aredr_codigo = dr.aredr_codigo 
                                                   AND uv.rn = 1
                LEFT JOIN ROOTSISREC.ai_rec_estado_verificable ev ON ev.areev_codigo = uv.areev_codigo
            WHERE
                rec.arere_estado = '1'
                AND EXISTS (
                    SELECT 1
                    FROM ROOTSISREC.ai_rec_solicitudes sol
                    WHERE sol.arere_codigo = rec.arere_codigo
                      AND sol.areso_estado = '1'
                )
        );

    IF p_json IS NULL THEN
        p_json := to_clob(
            JSON_OBJECT(
                'mensaje' VALUE 'No se encontraron registros para el reporte de prórrogas'
            )
        );
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_json := to_clob(
            JSON_OBJECT(
                'error' VALUE sqlerrm
            )
        );
END reporteprorrogasaij;
/

-- 13. reporteFechaCorteRecomendaciones (Restaurado fielmente desde fix-reporteFechaCorteRecomendaciones.sql con priorización de verificables)
CREATE OR REPLACE PROCEDURE ROOTSISREC.reporteFechaCorteRecomendaciones 
    (   
        p_json              OUT CLOB,
        fecha_corte_emision IN  VARCHAR,
        fecha_corte_estado  IN  VARCHAR,
        dpto                IN  VARCHAR,
        org_control         IN  INTEGER
    )
    IS
        v_fecha_corte_emision   TIMESTAMP;
        v_fecha_corte_estado    TIMESTAMP;
    BEGIN
        -- Parseo de fecha_corte_emision (Filtra informes emitidos hasta esta fecha)
        IF fecha_corte_emision IS NULL OR TRIM(fecha_corte_emision) = '' THEN
            v_fecha_corte_emision := SYSTIMESTAMP;
        ELSIF INSTR(fecha_corte_emision, '-') > 0 THEN
            IF LENGTH(TRIM(fecha_corte_emision)) > 10 THEN
                v_fecha_corte_emision := TO_TIMESTAMP(fecha_corte_emision, 'YYYY-MM-DD HH24:MI:SS');
            ELSE
                v_fecha_corte_emision := TO_TIMESTAMP(fecha_corte_emision || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS');
            END IF;
        ELSE
            IF LENGTH(TRIM(fecha_corte_emision)) > 10 THEN
                v_fecha_corte_emision := TO_TIMESTAMP(fecha_corte_emision, 'DD/MM/YYYY HH24:MI:SS');
            ELSE
                v_fecha_corte_emision := TO_TIMESTAMP(fecha_corte_emision || ' 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
            END IF;
        END IF;

        -- Parseo de fecha_corte_estado (Evalúa verificables y avance hasta esta fecha)
        IF fecha_corte_estado IS NULL OR TRIM(fecha_corte_estado) = '' THEN
            v_fecha_corte_estado := SYSTIMESTAMP;
        ELSIF INSTR(fecha_corte_estado, '-') > 0 THEN
            IF LENGTH(TRIM(fecha_corte_estado)) > 10 THEN
                v_fecha_corte_estado := TO_TIMESTAMP(fecha_corte_estado, 'YYYY-MM-DD HH24:MI:SS');
            ELSE
                v_fecha_corte_estado := TO_TIMESTAMP(fecha_corte_estado || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS');
            END IF;
        ELSE
            IF LENGTH(TRIM(fecha_corte_estado)) > 10 THEN
                v_fecha_corte_estado := TO_TIMESTAMP(fecha_corte_estado, 'DD/MM/YYYY HH24:MI:SS');
            ELSE
                v_fecha_corte_estado := TO_TIMESTAMP(fecha_corte_estado || ' 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
            END IF;
        END IF;

        WITH 
        -- 1. Obtenemos el último verificable CARGADO en o antes de la fecha de corte
        verificables_corte AS
        (
            SELECT 
                v.*,
                ROW_NUMBER() OVER (
                    PARTITION BY v.aredr_codigo 
                    ORDER BY 
                        CASE 
                            WHEN v.AREEV_CODIGO = 2 THEN 1
                            WHEN v.AREEV_CODIGO = 1 THEN 2
                            WHEN v.AREEV_CODIGO = 3 THEN 3
                            ELSE 4
                        END ASC,
                        NVL(v.areve_fecha_carga, v.areve_fecha) DESC NULLS LAST, 
                        v.areve_codigo DESC
                ) AS rn
            FROM AI_REC_VERIFICABLES v
            WHERE TO_CHAR(v.areve_estado) = '1'
              AND (NVL(v.areve_fecha_carga, v.areve_fecha) IS NULL OR NVL(v.areve_fecha_carga, v.areve_fecha) <= v_fecha_corte_estado)
        ),
        -- 1b. Concatenamos TODOS los verificables cargados en o antes de la fecha de corte para cada asignación (aredr_codigo)
        verificables_corte_all AS
        (
            SELECT 
                v.aredr_codigo,
                SUBSTRB(
                    LISTAGG(
                        SUBSTRB(REGEXP_REPLACE(REPLACE(REPLACE(REPLACE(v.areve_descripcion, CHR(13), ' '), CHR(10), ' '), '|', '-'), '\s+', ' '), 1, 800),
                        ' ;;; ' ON OVERFLOW TRUNCATE '...'
                    ) WITHIN GROUP (ORDER BY NVL(v.areve_fecha_carga, v.areve_fecha) ASC, v.areve_codigo ASC),
                    1, 3500
                ) AS descripcion_verificable_concat,
                SUBSTRB(
                    LISTAGG(
                        SUBSTRB(NVL(REGEXP_REPLACE(REPLACE(REPLACE(REPLACE(v.areve_observacion, CHR(13), ' '), CHR(10), ' '), '|', '-'), '\s+', ' '), 'Sin observación'), 1, 800),
                        ' ;;; ' ON OVERFLOW TRUNCATE '...'
                    ) WITHIN GROUP (ORDER BY NVL(v.areve_fecha_carga, v.areve_fecha) ASC, v.areve_codigo ASC),
                    1, 3500
                ) AS observacion_auditoria_concat
            FROM AI_REC_VERIFICABLES v
            WHERE TO_CHAR(v.areve_estado) = '1'
              AND (NVL(v.areve_fecha_carga, v.areve_fecha) IS NULL OR NVL(v.areve_fecha_carga, v.areve_fecha) <= v_fecha_corte_estado)
            GROUP BY v.aredr_codigo
        ),
        -- 1c. Prórrogas aprobadas vigentes a la fecha de corte
        prorrogas_corte AS
        (
            SELECT S.ARERE_CODIGO, S.ARERS_CODIGO, MAX(S.ARESO_FECHA_MAX_NUE) AS FECHA_PRORROGA
            FROM ROOTSISREC.AI_REC_SOLICITUDES S
            WHERE TO_CHAR(S.ARESO_ESTADO) = '1' 
              AND TO_CHAR(S.ARESO_ESTADO_APROB) = '3' 
              AND (NVL(S.ARESO_FECHA_CARGA, S.ARESO_FECHA) IS NULL OR NVL(S.ARESO_FECHA_CARGA, S.ARESO_FECHA) <= v_fecha_corte_estado) 
              AND S.ARESO_FECHA_MAX_NUE IS NOT NULL
            GROUP BY S.ARERE_CODIGO, S.ARERS_CODIGO
        ),
        -- 2. Obtenemos cada asignación única de responsable por recomendación (GLOBAL)
        detalle_estado_global AS
        (
            SELECT
                DR.AREDR_CODIGO,
                DR.ARERE_CODIGO,
                RES.ARERS_NOMBRE,
                RES.ARERS_APELLIDO,
                RES.ARERS_DEPARTAMENTO,
                VR.AREEV_CODIGO,
                VR.AREVE_FECHA_VALIDACION,
                COALESCE(PR.FECHA_PRORROGA, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) AS fecha_individual,
                CASE 
                    WHEN (VR.AREEV_CODIGO = 2 AND (VR.AREVE_FECHA_VALIDACION IS NULL OR VR.AREVE_FECHA_VALIDACION <= v_fecha_corte_estado))
                      OR (VR.AREEV_CODIGO IS NULL AND (REC.AREES_CODIGO = 2 OR UPPER(TRIM(ES.AREES_DESCRIPCION)) = 'CUMPLIDA') AND REC.ARERE_FECHA_CUMPLIDA IS NOT NULL AND REC.ARERE_FECHA_CUMPLIDA <= v_fecha_corte_estado)
                      OR (REC.AREES_CODIGO = 4 OR UPPER(TRIM(ES.AREES_DESCRIPCION)) = 'NO APLICA')
                    THEN NULL
                    ELSE FN_CALCULAR_DIAS_LABORABLES(COALESCE(PR.FECHA_PRORROGA, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), TRUNC(v_fecha_corte_estado))
                END AS plazo_individual,
                CASE 
                    WHEN VR.AREVE_FECHA_VALIDACION IS NOT NULL AND VR.AREVE_FECHA_VALIDACION > v_fecha_corte_estado THEN 'PENDIENTE'
                    WHEN VR.AREEV_CODIGO IS NOT NULL THEN COALESCE(UPPER(TRIM(EV.AREEV_DESCRIPCION)), CASE WHEN VR.AREEV_CODIGO = 2 THEN 'JUSTIFICA' WHEN VR.AREEV_CODIGO = 1 THEN 'PENDIENTE' WHEN VR.AREEV_CODIGO = 3 THEN 'NO JUSTIFICA' ELSE 'PENDIENTE' END)
                    WHEN (REC.AREES_CODIGO = 2 OR UPPER(TRIM(ES.AREES_DESCRIPCION)) = 'CUMPLIDA') AND REC.ARERE_FECHA_CUMPLIDA IS NOT NULL AND REC.ARERE_FECHA_CUMPLIDA <= v_fecha_corte_estado THEN 'JUSTIFICA'
                    WHEN REC.AREES_CODIGO = 4 OR UPPER(TRIM(ES.AREES_DESCRIPCION)) = 'NO APLICA' THEN 'NO APLICA'
                    ELSE 'VERIFICABLE NO CREADO'
                END AS estado_verificable,
                CASE
                    WHEN VR.AREEV_CODIGO = 2 AND (VR.AREVE_FECHA_VALIDACION IS NULL OR VR.AREVE_FECHA_VALIDACION <= v_fecha_corte_estado) THEN 'CUMPLIDA'
                    WHEN VR.AREEV_CODIGO IS NULL AND (REC.AREES_CODIGO = 2 OR UPPER(TRIM(ES.AREES_DESCRIPCION)) = 'CUMPLIDA') AND REC.ARERE_FECHA_CUMPLIDA IS NOT NULL AND REC.ARERE_FECHA_CUMPLIDA <= v_fecha_corte_estado THEN 'CUMPLIDA'
                    WHEN REC.AREES_CODIGO = 4 OR UPPER(TRIM(ES.AREES_DESCRIPCION)) = 'NO APLICA' THEN 'NO APLICA'
                    WHEN COALESCE(PR.FECHA_PRORROGA, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) IS NOT NULL AND TRUNC(v_fecha_corte_estado) > TRUNC(COALESCE(PR.FECHA_PRORROGA, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)) THEN 'NO CUMPLIDA'
                    ELSE 'EN PROCESO'
                END AS estado_individual,
                VR.AREVE_CODIGO AS verificable_codigo,
                REGEXP_REPLACE(REPLACE(REPLACE(REPLACE(VR.AREVE_DESCRIPCION, CHR(13), ' '), CHR(10), ' '), '|', '-'), '\s+', ' ') AS descripcion_verificable,
                REGEXP_REPLACE(REPLACE(REPLACE(REPLACE(VR.AREVE_OBSERVACION, CHR(13), ' '), CHR(10), ' '), '|', '-'), '\s+', ' ') AS observacion_auditoria,
                VALL.descripcion_verificable_concat AS descripcion_verificable_all,
                VALL.observacion_auditoria_concat AS observacion_auditoria_all
            FROM 
                (
                    SELECT 
                        dr_inner.*,
                        ROW_NUMBER() OVER (
                            PARTITION BY dr_inner.arere_codigo, dr_inner.arers_codigo 
                            ORDER BY dr_inner.aredr_codigo DESC
                        ) AS rn_resp
                    FROM AI_REC_DETALLE_RECOMENDACION dr_inner
                    JOIN AI_REC_RESPONSABLES res_inner ON dr_inner.arers_codigo = res_inner.arers_codigo AND res_inner.arers_estado = 1
                    JOIN AI_REC_RECOMENDACIONES rec_inner ON dr_inner.arere_codigo = rec_inner.arere_codigo AND rec_inner.arere_estado = 1
                    JOIN AI_REC_CONCLUSIONES con_inner ON rec_inner.areco_codigo = con_inner.areco_codigo AND con_inner.areco_estado = 1
                    JOIN AI_REC_HALLAZGOS hall_inner ON con_inner.areha_codigo = hall_inner.areha_codigo AND hall_inner.areha_estado = 1
                    JOIN AI_INF_INFORMES inf_inner ON hall_inner.ainin_codigo = inf_inner.ainin_codigo AND inf_inner.ainin_estado = 1
                    JOIN AI_INF_ORG_CONT org_inner ON inf_inner.ainoc_codigo = org_inner.ainoc_codigo AND org_inner.ainoc_estado = 1
                    WHERE dr_inner.aredr_estado = 1
                      AND (inf_inner.ainin_fecha_aprobacion <= v_fecha_corte_emision OR inf_inner.ainin_fecha_memo <= v_fecha_corte_emision OR inf_inner.ainin_fecha_aprobacion IS NULL)
                      AND (org_control = 0 OR org_inner.ainoc_codigo = org_control)
                ) DR
            JOIN AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO
            JOIN AI_REC_RECOMENDACIONES REC ON DR.ARERE_CODIGO = REC.ARERE_CODIGO
            JOIN AI_REC_ESTADO_SEGUIMIENTO ES ON REC.AREES_CODIGO = ES.AREES_CODIGO
            LEFT JOIN verificables_corte VR ON DR.AREDR_CODIGO = VR.AREDR_CODIGO AND VR.rn = 1
            LEFT JOIN verificables_corte_all VALL ON DR.AREDR_CODIGO = VALL.AREDR_CODIGO
            LEFT JOIN prorrogas_corte PR ON DR.ARERE_CODIGO = PR.ARERE_CODIGO AND DR.ARERS_CODIGO = PR.ARERS_CODIGO
            LEFT JOIN AI_REC_ESTADO_VERIFICABLE EV ON VR.AREEV_CODIGO = EV.AREEV_CODIGO
            WHERE DR.rn_resp = 1
        ),

        -- 3. Progreso global de cada recomendación (calculado sobre TODOS sus responsables)
        recomendacion_stats_global AS
        (
            SELECT
                ARERE_CODIGO,
                ROUND(
                    (SUM(CASE WHEN estado_verificable LIKE '%JUSTIFICA%' AND estado_verificable NOT LIKE '%NO JUSTIFICA%' THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0)) * 100, 2
                ) AS progreso_recomendacion,
                MAX(fecha_individual) AS fecha_maxima_resp
            FROM detalle_estado_global
            GROUP BY ARERE_CODIGO
        ),

        -- 4. Detalle de responsables filtrado por departamento
        detalle_estado_filtrado AS
        (
            SELECT *
            FROM detalle_estado_global
            WHERE (dpto = '0' OR ARERS_DEPARTAMENTO = dpto)
        ),

        -- 5. Listas concatenadas de responsables y departamentos (filtradas por dpto)
        recomendacion_resp_list AS
        (
            SELECT
                ARERE_CODIGO,
                MAX(fecha_individual) AS fecha_maxima_resp,
                RTRIM(
                    XMLCAST(
                        XMLAGG(
                            XMLELEMENT(e, 
                                TO_CLOB(ARERS_NOMBRE || ' ' || ARERS_APELLIDO || 
                                ' [' || ARERS_DEPARTAMENTO || ']' ||
                                ' (' || estado_verificable || ')' ||
                                ' [V:' || NVL(verificable_codigo, 0) || ']' ||
                                ' [F:' || NVL(TO_CHAR(fecha_individual, 'DD/MM/YYYY'), '—') || ']' ||
                                ' [P:' || NVL(TO_CHAR(plazo_individual), '—') || ']' ||
                                ' [E:' || NVL(estado_individual, 'EN PROCESO') || ']') ||
                                CASE WHEN observacion_auditoria_all IS NOT NULL THEN TO_CLOB(' - ObsAll: ') || SUBSTRB(observacion_auditoria_all, 1, 1500) ELSE TO_CLOB('') END ||
                                CASE WHEN descripcion_verificable_all IS NOT NULL THEN TO_CLOB(' - DescAll: ') || SUBSTRB(descripcion_verificable_all, 1, 1500) ELSE TO_CLOB('') END ||
                                CASE WHEN observacion_auditoria IS NOT NULL THEN TO_CLOB(' - Obs: ') || SUBSTRB(observacion_auditoria, 1, 500) ELSE TO_CLOB('') END ||
                                CASE WHEN descripcion_verificable IS NOT NULL THEN TO_CLOB(' - Desc: ') || SUBSTRB(descripcion_verificable, 1, 500) ELSE TO_CLOB('') END ||
                                ' | '
                            ) ORDER BY ARERS_APELLIDO, ARERS_NOMBRE
                        ) AS CLOB
                    ),
                    ' | '
                ) AS listaResponsables,

                LISTAGG(
                    DISTINCT ARERS_DEPARTAMENTO, 
                    ', ' ON OVERFLOW TRUNCATE '...'
                ) WITHIN GROUP (ORDER BY ARERS_DEPARTAMENTO) AS listaDepartamentos
            FROM detalle_estado_filtrado
            GROUP BY ARERE_CODIGO
        ),

        -- 6. Progreso global del informe a la fecha de corte de estado (sobre TODAS sus recomendaciones)
        informe_stats AS
        (
            SELECT
                INF.AININ_CODIGO,
                ROUND(
                    (SUM(
                        CASE 
                            WHEN rstat.progreso_recomendacion = 100 
                              OR ((REC.AREES_CODIGO = 2 OR UPPER(TRIM(ES.AREES_DESCRIPCION)) = 'CUMPLIDA')
                                  AND REC.ARERE_FECHA_CUMPLIDA IS NOT NULL 
                                  AND REC.ARERE_FECHA_CUMPLIDA <= v_fecha_corte_estado)
                              OR REC.AREES_CODIGO = 4 
                              OR UPPER(TRIM(ES.AREES_DESCRIPCION)) = 'NO APLICA' 
                            THEN 1 ELSE 0 
                        END
                    ) / NULLIF(COUNT(REC.ARERE_CODIGO), 0)) * 100, 2
                ) AS progreso_informe
            FROM AI_INF_INFORMES INF
            JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO
            JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
            JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
            JOIN AI_REC_ESTADO_SEGUIMIENTO ES ON REC.AREES_CODIGO = ES.AREES_CODIGO
            LEFT JOIN recomendacion_stats_global rstat ON REC.ARERE_CODIGO = rstat.ARERE_CODIGO
            WHERE TO_CHAR(INF.AININ_ESTADO) = '1' 
              AND TO_CHAR(HALL.AREHA_ESTADO) = '1' 
              AND TO_CHAR(CON.ARECO_ESTADO) = '1' 
              AND TO_CHAR(REC.ARERE_ESTADO) = '1'
              AND TO_CHAR(ES.AREES_ESTADO) = '1'
              AND (INF.AININ_FECHA_APROBACION <= v_fecha_corte_emision OR INF.AININ_FECHA_MEMO <= v_fecha_corte_emision OR INF.AININ_FECHA_APROBACION IS NULL)
            GROUP BY INF.AININ_CODIGO
        ),

        -- 7. Consulta de datos principal
        datos_raw AS
        (
            SELECT
                INF.AININ_CODIGO AS idInforme,
                INF.AININ_TITULO AS tituloInforme,
                INF.AININ_USUARIO AS auditorInforme,
                ORG.AINOC_DESCRIPCION AS organismoControl,
                INF.AININ_MEMORANDO AS memoAuditoria,
                INF.AININ_MEMO_APROBACION AS memoAprobacion,
                TO_CHAR(INF.AININ_FECHA_MEMO, 'DD/MM/YYYY') AS fechaMemo,
                TO_CHAR(INF.AININ_FECHA_APROBACION, 'DD/MM/YYYY') AS fechaAprobacion,
                CON.ARECO_CONCLUSION AS conclusion,
                REC.ARERE_CODIGO AS arere_codigo,
                REC.ARERE_DESCRIPCION AS recomendacion,
                REC.ARERE_NUM_REC AS numeroRecomendacion,
                CASE 
                    WHEN rstat.progreso_recomendacion = 100 THEN 'CUMPLIDA'
                    WHEN (REC.AREES_CODIGO = 2 OR UPPER(TRIM(ES.AREES_DESCRIPCION)) = 'CUMPLIDA')
                         AND REC.ARERE_FECHA_CUMPLIDA IS NOT NULL 
                         AND REC.ARERE_FECHA_CUMPLIDA <= v_fecha_corte_estado
                    THEN 'CUMPLIDA'
                    WHEN REC.AREES_CODIGO = 4 OR UPPER(TRIM(ES.AREES_DESCRIPCION)) = 'NO APLICA' THEN 'NO APLICA'
                    WHEN COALESCE(rstat.fecha_maxima_resp, REC.ARERE_FECHA_MAXIMA) IS NOT NULL AND TRUNC(v_fecha_corte_estado) > TRUNC(COALESCE(rstat.fecha_maxima_resp, REC.ARERE_FECHA_MAXIMA)) THEN 'NO CUMPLIDA'
                    ELSE 'EN PROCESO'
                END AS estadoRecomendacion,
                REC.AREES_CODIGO AS arees_codigo,
                COALESCE(TO_CHAR(REC.ARERE_FECHA_CUMPLIDA, 'DD/MM/YYYY'), 'RECOMENDACION NO CUMPLIDA') AS fechaRecomendacionCumplida,
                TO_CHAR(COALESCE(rstat.fecha_maxima_resp, REC.ARERE_FECHA_MAXIMA), 'DD/MM/YYYY') AS fechaMaximaCumplimiento,
                INF.AININ_FECHA_APROBACION AS fecha_aprobacion_raw,
                ROW_NUMBER() OVER (PARTITION BY REC.ARERE_CODIGO ORDER BY INF.AININ_FECHA_APROBACION ASC) AS rn_rec
            FROM
                AI_INF_INFORMES INF
                JOIN AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO
                JOIN AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO
                JOIN AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO
                JOIN AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO
                JOIN AI_REC_ESTADO_SEGUIMIENTO ES ON REC.AREES_CODIGO = ES.AREES_CODIGO
                LEFT JOIN recomendacion_stats_global rstat ON REC.ARERE_CODIGO = rstat.ARERE_CODIGO
            WHERE
                TO_CHAR(INF.AININ_ESTADO) = '1'
                AND TO_CHAR(HALL.AREHA_ESTADO) = '1'
                AND TO_CHAR(CON.ARECO_ESTADO) = '1'
                AND TO_CHAR(REC.ARERE_ESTADO) = '1'
                AND TO_CHAR(ES.AREES_ESTADO) = '1'
                AND TO_CHAR(ORG.AINOC_ESTADO) = '1'
                AND (INF.AININ_FECHA_APROBACION <= v_fecha_corte_emision OR INF.AININ_FECHA_MEMO <= v_fecha_corte_emision OR INF.AININ_FECHA_APROBACION IS NULL)
                AND (org_control = 0 OR ORG.AINOC_CODIGO = org_control)
        ),
        datos AS
        (
            SELECT
                d.idInforme,
                d.arere_codigo,
                d.tituloInforme,
                d.auditorInforme,
                d.organismoControl,
                d.memoAuditoria,
                d.memoAprobacion,
                d.fechaMemo,
                d.fechaAprobacion,
                d.conclusion,
                d.recomendacion,
                d.numeroRecomendacion,
                d.estadoRecomendacion,
                d.fechaRecomendacionCumplida,
                d.fechaMaximaCumplimiento,

                COALESCE(istat.progreso_informe, 0) AS progresoInforme,
                CASE 
                    WHEN d.estadoRecomendacion IN ('CUMPLIDA', 'NO APLICA') THEN 100
                    ELSE COALESCE(rstat.progreso_recomendacion, 0)
                END AS progresoRecomendacion,

                rlist.listaResponsables AS listaResponsables,
                rlist.listaDepartamentos AS listaDepartamentos
            FROM datos_raw d
            LEFT JOIN recomendacion_stats_global rstat ON d.arere_codigo = rstat.ARERE_CODIGO
            LEFT JOIN recomendacion_resp_list rlist ON d.arere_codigo = rlist.ARERE_CODIGO
            LEFT JOIN informe_stats istat ON d.idInforme = istat.AININ_CODIGO
            WHERE d.rn_rec = 1
            ORDER BY d.fecha_aprobacion_raw ASC, d.numeroRecomendacion ASC, d.arere_codigo ASC
        )
        SELECT
            JSON_ARRAYAGG
            (
                JSON_OBJECT
                (
                    'idInforme'                  VALUE datos.idInforme,
                    'arere_codigo'               VALUE datos.arere_codigo,
                    'tituloInforme'              VALUE datos.tituloInforme,
                    'auditorInforme'             VALUE datos.auditorInforme,
                    'organismoControl'           VALUE datos.organismoControl,
                    'memoAuditoria'              VALUE datos.memoAuditoria,
                    'memoAprobacion'             VALUE datos.memoAprobacion,
                    'fechaMemo'                  VALUE datos.fechaMemo,
                    'fechaAprobacion'            VALUE datos.fechaAprobacion,
                    'conclusion'                 VALUE datos.conclusion,
                    'recomendacion'              VALUE datos.recomendacion,
                    'numeroRecomendacion'        VALUE datos.numeroRecomendacion,
                    'estadoRecomendacion'        VALUE datos.estadoRecomendacion,
                    'fechaRecomendacionCumplida' VALUE datos.fechaRecomendacionCumplida,
                    'fechaMaximaCumplimiento'    VALUE datos.fechaMaximaCumplimiento,
                    'progresoInforme'            VALUE datos.progresoInforme,
                    'progresoRecomendacion'      VALUE datos.progresoRecomendacion,
                    'listaResponsables'          VALUE datos.listaResponsables,
                    'listaDepartamentos'         VALUE datos.listaDepartamentos
                    RETURNING CLOB
                )
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

    END reporteFechaCorteRecomendaciones;
/

-- 14. reporte2PoRecomendacionesAIJ (/report-estado-seg)
CREATE OR REPLACE PROCEDURE ROOTSISREC.reporte2PoRecomendacionesAIJ (
    p_json                  OUT     CLOB,
    fecha_inicial           IN      VARCHAR,
    fecha_fin               IN      VARCHAR,
    estado_seguimiento      IN      INTEGER,
    auditor_responsable     IN      VARCHAR
)
IS
    v_fecha_ini   TIMESTAMP;
    v_fecha_fin   TIMESTAMP;
    v_first       NUMBER := 1;
    FUNCTION parse_ts(p_val VARCHAR, p_is_end NUMBER) RETURN TIMESTAMP IS
        v_str VARCHAR(60) := TRIM(p_val);
    BEGIN
        IF v_str IS NULL THEN
            RETURN NULL;
        END IF;
        IF LENGTH(v_str) = 10 THEN
            IF INSTR(v_str, '/') > 0 THEN
                IF p_is_end = 1 THEN
                    RETURN TO_TIMESTAMP(v_str || ' 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
                ELSE
                    RETURN TO_TIMESTAMP(v_str || ' 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
                END IF;
            ELSIF INSTR(v_str, '-') > 0 THEN
                IF p_is_end = 1 THEN
                    RETURN TO_TIMESTAMP(v_str || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS');
                ELSE
                    RETURN TO_TIMESTAMP(v_str || ' 00:00:00', 'YYYY-MM-DD HH24:MI:SS');
                END IF;
            END IF;
        END IF;
        IF INSTR(v_str, '-') > 0 THEN
            RETURN TO_TIMESTAMP(v_str, 'YYYY-MM-DD HH24:MI:SS');
        END IF;
        RETURN TO_TIMESTAMP(v_str, 'DD/MM/YYYY HH24:MI:SS');
    EXCEPTION
        WHEN OTHERS THEN
            IF p_is_end = 1 THEN
                RETURN TO_TIMESTAMP(TRIM(p_val) || ' 23:59:59', 'DD/MM/YYYY HH24:MI:SS');
            ELSE
                RETURN TO_TIMESTAMP(TRIM(p_val) || ' 00:00:00', 'DD/MM/YYYY HH24:MI:SS');
            END IF;
    END;
BEGIN
    v_fecha_ini := parse_ts(fecha_inicial, 0);
    v_fecha_fin := parse_ts(fecha_fin, 1);
    DBMS_LOB.CREATETEMPORARY(p_json, TRUE);
    DBMS_LOB.APPEND(p_json, '[');
    FOR rec IN
    (
        WITH recomendaciones_base AS (
            SELECT
                REC.ARERE_CODIGO,
                INF.AININ_CODIGO,
                ORG.AINOC_DESCRIPCION AS organismoControl,
                INF.AININ_MEMORANDO AS memorando,
                INF.AININ_MEMO_APROBACION AS memoAprobacion,
                INF.AININ_FECHA_MEMO AS fechaEmision,
                INF.AININ_FECHA_APROBACION AS fechaAprobacion,
                INF.AININ_TITULO AS tituloInforme,
                REC.ARERE_DESCRIPCION AS recomendacion,
                ES.AREES_DESCRIPCION AS estadoSeguimiento,
                INF.AININ_USUARIO AS auditor
            FROM
                AI_INF_INFORMES INF
                JOIN AI_REC_HALLAZGOS HALL
                    ON INF.AININ_CODIGO = HALL.AININ_CODIGO
                JOIN AI_REC_CONCLUSIONES CON
                    ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
                JOIN AI_REC_RECOMENDACIONES REC
                    ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
                JOIN AI_INF_ORG_CONT ORG
                    ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO
                JOIN AI_REC_ESTADO_SEGUIMIENTO ES
                    ON REC.AREES_CODIGO = ES.AREES_CODIGO
            WHERE
                INF.AININ_FECHA_APROBACION BETWEEN v_fecha_ini AND v_fecha_fin
                AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)
                AND (NVL(auditor_responsable, '0') = '0' OR INF.AININ_USUARIO = auditor_responsable)
                AND INF.AININ_ESTADO = 1
                AND HALL.AREHA_ESTADO = 1
                AND CON.ARECO_ESTADO = 1
                AND REC.ARERE_ESTADO = 1
                AND ORG.AINOC_ESTADO = 1
        ),
        departamentos_agrupados AS (
            SELECT
                DR.ARERE_CODIGO,
                LISTAGG(DISTINCT RES.ARERS_DEPARTAMENTO, ', ')
                    WITHIN GROUP (ORDER BY RES.ARERS_DEPARTAMENTO) AS departamento
            FROM
                AI_REC_DETALLE_RECOMENDACION DR
                JOIN AI_REC_RESPONSABLES RES
                    ON DR.ARERS_CODIGO = RES.ARERS_CODIGO
            WHERE
                DR.AREDR_ESTADO = 1
                AND RES.ARERS_ESTADO = 1
            GROUP BY
                DR.ARERE_CODIGO
        ),
        ult_ver AS (
            SELECT
                V.AREDR_CODIGO,
                V.AREEV_CODIGO,
                ROW_NUMBER() OVER (
                    PARTITION BY V.AREDR_CODIGO
                    ORDER BY 
                        CASE 
                            WHEN V.AREEV_CODIGO = 2 THEN 1
                            WHEN V.AREEV_CODIGO = 1 THEN 2
                            WHEN V.AREEV_CODIGO = 3 THEN 3
                            ELSE 4
                        END ASC,
                        NVL(V.AREVE_FECHA_CARGA, V.AREVE_FECHA) DESC NULLS LAST,
                        V.AREVE_CODIGO DESC
                ) AS rn
            FROM
                AI_REC_VERIFICABLES V
            WHERE
                V.AREVE_ESTADO = 1
        ),
        responsables_agrupados AS (
            SELECT
                DR.ARERE_CODIGO,
                LISTAGG(
                    DISTINCT
                        RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO || ' (' ||
                        COALESCE(EV.AREEV_DESCRIPCION, 'VERIFICABLE NO CREADO') || ')',
                    ', '
                ) WITHIN GROUP (ORDER BY RES.ARERS_NOMBRE) AS responsables
            FROM
                AI_REC_DETALLE_RECOMENDACION DR
                JOIN AI_REC_RESPONSABLES RES
                    ON DR.ARERS_CODIGO = RES.ARERS_CODIGO
                LEFT JOIN ult_ver UV
                    ON UV.AREDR_CODIGO = DR.AREDR_CODIGO
                   AND UV.rn = 1
                LEFT JOIN AI_REC_ESTADO_VERIFICABLE EV
                    ON EV.AREEV_CODIGO = UV.AREEV_CODIGO
            WHERE
                DR.AREDR_ESTADO = 1
                AND RES.ARERS_ESTADO = 1
            GROUP BY
                DR.ARERE_CODIGO
        ),
        progreso_informe AS (
            SELECT
                HALL.AININ_CODIGO,
                ROUND(
                    COUNT(CASE WHEN REC2.AREES_CODIGO = 2 THEN 1 END) * 100.0
                    / NULLIF(COUNT(REC2.ARERE_CODIGO), 0)
                , 0) AS progresoInforme
            FROM
                AI_REC_RECOMENDACIONES REC2
                JOIN AI_REC_CONCLUSIONES CON2 ON CON2.ARECO_CODIGO = REC2.ARECO_CODIGO
                JOIN AI_REC_HALLAZGOS HALL ON HALL.AREHA_CODIGO = CON2.AREHA_CODIGO
            WHERE
                REC2.ARERE_ESTADO = 1
                AND CON2.ARECO_ESTADO = 1
                AND HALL.AREHA_ESTADO = 1
            GROUP BY
                HALL.AININ_CODIGO
        )
        SELECT
            rb.organismoControl,
            rb.memorando,
            rb.memoAprobacion,
            rb.fechaEmision,
            rb.fechaAprobacion,
            rb.tituloInforme,
            rb.recomendacion,
            rb.estadoSeguimiento,
            NVL(ra.responsables, '') AS responsables,
            NVL(pi.progresoInforme, 0) AS progresoInforme,
            rb.auditor,
            dpa.departamento
        FROM recomendaciones_base rb
        LEFT JOIN departamentos_agrupados dpa
            ON dpa.ARERE_CODIGO = rb.ARERE_CODIGO
        LEFT JOIN responsables_agrupados ra
            ON ra.ARERE_CODIGO = rb.ARERE_CODIGO
        LEFT JOIN progreso_informe pi
            ON pi.AININ_CODIGO = rb.AININ_CODIGO
    )
    LOOP
        IF v_first = 1 THEN
            v_first := 0;
        ELSE
            DBMS_LOB.APPEND(p_json, ',');
        END IF;
        DBMS_LOB.APPEND(
            p_json,
            TO_CLOB(
                JSON_OBJECT(
                    'organismoControl' VALUE rec.organismoControl,
                    'memorando' VALUE rec.memorando,
                    'memoAprobacion' VALUE rec.memoAprobacion,
                    'fechaEmision' VALUE TO_CHAR(rec.fechaEmision, 'DD/MM/YYYY'),
                    'fechaAprobacion' VALUE TO_CHAR(rec.fechaAprobacion, 'DD/MM/YYYY'),
                    'tituloInforme' VALUE rec.tituloInforme,
                    'recomendacion' VALUE rec.recomendacion,
                    'estadoSeguimiento' VALUE rec.estadoSeguimiento,
                    'auditor' VALUE NVL(rec.auditor, ''),
                    'departamento' VALUE NVL(rec.departamento, ''),
                    'responsables' VALUE NVL(rec.responsables, ''),
                    'progresoInforme' VALUE NVL(rec.progresoInforme, 0)
                )
            )
        );
    END LOOP;
    DBMS_LOB.APPEND(p_json, ']');
EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB('{"error":"' || REPLACE(SQLERRM, '"', '"') || '"}');
END reporte2PoRecomendacionesAIJ;
/
-- 15. REPORTERECOMENDACIONESVENCIDASAIJ (/inicio/report-recomendaciones-vencidas-aij)
CREATE OR REPLACE PROCEDURE ROOTSISREC.REPORTERECOMENDACIONESVENCIDASAIJ(
    p_json              OUT CLOB,
    fecha_inicial       IN  VARCHAR2,
    fecha_fin           IN  VARCHAR2,
    estado_seguimiento  IN  INTEGER
)
IS
    v_fecha_ini    TIMESTAMP;
    v_fecha_fin    TIMESTAMP;
BEGIN
    -- 1. Parseo de fechas robusto (acepta DD/MM/YYYY, YYYY-MM-DD, con/sin hora, NULL y vacío)
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

    -- 2. Generación del JSON directamente desde la consulta
    SELECT JSON_ARRAYAGG(
        JSON_OBJECT(
            'tituloInforme'     VALUE NVL(tituloInforme, ''),
            'recomendacion'     VALUE NVL(recomendacion, ''),
            'organismoControl'  VALUE NVL(organismoControl, ''),
            'memorando'         VALUE NVL(memorando, ''),
            'memoAprobacion'    VALUE NVL(memoAprobacion, ''),
            'observacion'       VALUE CASE
                                        WHEN observacion IS NULL OR TRIM(observacion) = ''
                                          THEN 'no existe observacion'
                                        ELSE observacion
                                      END,
            'fechaEmision'      VALUE NVL(fechaEmision, ''),
            'fechaMaxima'       VALUE NVL(fechaMaxima, ''),
            'plazo'             VALUE TO_NUMBER(NVL(plazo, 0)),
            'auditor'           VALUE NVL(auditor, ''),
            'responsables'      VALUE NVL(responsables, ''),
            'arereCodigo'       VALUE TO_NUMBER(NVL(arereCodigo, 0))
            RETURNING CLOB
        )
        RETURNING CLOB
    )
    INTO p_json
    FROM (
        SELECT
            ORGCON.AINOC_DESCRIPCION                        AS organismoControl,
            INF.AININ_MEMORANDO                             AS memorando,
            INF.AININ_MEMO_APROBACION                       AS memoAprobacion,
            TO_CHAR(INF.AININ_FECHA_MEMO, 'DD/MM/YYYY')     AS fechaEmision,
            INF.AININ_TITULO                                AS tituloInforme,
            REC.ARERE_DESCRIPCION                           AS recomendacion,
            DBMS_LOB.SUBSTR(MAX(ULT_VER.AREVE_OBSERVACION), 4000) AS observacion,
            TO_CHAR(REC.ARERE_FECHA_MAXIMA, 'DD/MM/YYYY')   AS fechaMaxima,
            TRUNC(REC.ARERE_FECHA_MAXIMA) - TRUNC(SYSDATE)  AS plazo,
            REC.ARERE_CODIGO                                AS arereCodigo,
            INF.AININ_USUARIO                               AS auditor,
            LISTAGG(
                NVL(RESP.ARERS_NOMBRE, '') || ' ' || NVL(RESP.ARERS_APELLIDO, '') ||
                CASE
                    WHEN ULT_VER.AREVE_CODIGO IS NULL THEN ' (VERIFICABLE NO CREADO)'
                    WHEN ULT_VER.AREEV_CODIGO = 1 THEN ' (PENDIENTE)'
                    WHEN ULT_VER.AREEV_CODIGO = 3 THEN ' (NO JUSTIFICA)'
                    ELSE ''
                END ||
                CASE
                    WHEN ULT_VER.AREVE_CODIGO IS NOT NULL
                         AND ULT_VER.AREVE_OBSERVACION IS NOT NULL
                         AND LENGTH(TRIM(DBMS_LOB.SUBSTR(ULT_VER.AREVE_OBSERVACION, 4000))) > 0 THEN
                        ' - ' || TRIM(DBMS_LOB.SUBSTR(ULT_VER.AREVE_OBSERVACION, 4000))
                    ELSE ''
                END,
                ', '
            ) WITHIN GROUP (ORDER BY RESP.ARERS_NOMBRE, RESP.ARERS_APELLIDO) AS responsables
        FROM AI_INF_INFORMES INF
        JOIN AI_REC_HALLAZGOS HALL
            ON INF.AININ_CODIGO = HALL.AININ_CODIGO
        JOIN AI_REC_CONCLUSIONES CON
            ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
        JOIN AI_REC_RECOMENDACIONES REC
            ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
        JOIN AI_INF_ORG_CONT ORGCON
            ON INF.AINOC_CODIGO = ORGCON.AINOC_CODIGO
        JOIN AI_REC_DETALLE_RECOMENDACION DET
            ON REC.ARERE_CODIGO = DET.ARERE_CODIGO
        JOIN AI_REC_RESPONSABLES RESP
            ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO
        LEFT JOIN (
            SELECT
                VER.AREDR_CODIGO,
                VER.AREVE_CODIGO,
                VER.AREEV_CODIGO,
                VER.AREVE_OBSERVACION,
                ROW_NUMBER() OVER (
                    PARTITION BY VER.AREDR_CODIGO
                    ORDER BY 
                        CASE 
                            WHEN VER.AREEV_CODIGO = 2 THEN 1
                            WHEN VER.AREEV_CODIGO = 1 THEN 2
                            WHEN VER.AREEV_CODIGO = 3 THEN 3
                            ELSE 4
                        END ASC,
                        NVL(VER.AREVE_FECHA_CARGA, VER.AREVE_FECHA) DESC NULLS LAST,
                        VER.AREVE_CODIGO DESC
                ) AS RN
            FROM AI_REC_VERIFICABLES VER
            WHERE VER.AREVE_ESTADO = 1
        ) ULT_VER
            ON DET.AREDR_CODIGO = ULT_VER.AREDR_CODIGO
           AND ULT_VER.RN = 1
        WHERE (NVL(INF.AININ_FECHA_APROBACION, INF.AININ_FECHA_MEMO) BETWEEN v_fecha_ini AND v_fecha_fin)
          AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)
          AND INF.AININ_ESTADO = 1
          AND HALL.AREHA_ESTADO = 1
          AND CON.ARECO_ESTADO = 1
          AND REC.ARERE_ESTADO = 1
          AND ORGCON.AINOC_ESTADO = 1
          AND DET.AREDR_ESTADO = 1
          AND RESP.ARERS_ESTADO = 1
          AND (
              ULT_VER.AREVE_CODIGO IS NULL
              OR ULT_VER.AREEV_CODIGO = 1
              OR ULT_VER.AREEV_CODIGO = 3
          )
        GROUP BY
            ORGCON.AINOC_DESCRIPCION,
            INF.AININ_MEMORANDO,
            INF.AININ_MEMO_APROBACION,
            TO_CHAR(INF.AININ_FECHA_MEMO, 'DD/MM/YYYY'),
            INF.AININ_TITULO,
            REC.ARERE_DESCRIPCION,
            TO_CHAR(REC.ARERE_FECHA_MAXIMA, 'DD/MM/YYYY'),
            TRUNC(REC.ARERE_FECHA_MAXIMA) - TRUNC(SYSDATE),
            REC.ARERE_CODIGO,
            INF.AININ_USUARIO
        HAVING COUNT(DISTINCT RESP.ARERS_CODIGO) > 0
    );

    -- 3. Manejo de respuesta vacía
    IF p_json IS NULL THEN
        p_json := TO_CLOB(
          JSON_OBJECT('mensaje' VALUE 'No se encontraron recomendaciones con los criterios especificados')
        );
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END REPORTERECOMENDACIONESVENCIDASAIJ;
/
