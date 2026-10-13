-- V4__ActualizarReportesYMetricasDirector.sql
-- Actualización de procedimientos y funciones para soportar plazos multi-responsable y desglose en reportes y dashboard del Director.

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
            'arereCodigo'       VALUE TO_NUMBER(NVL(datos.arereCodigo, 0))
            RETURNING CLOB
        )
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

-- 2. ObtenerReporteS1Dpto
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
        p_json := JSON_OBJECT('error' VALUE sqlerrm);
END ObtenerReporteS1Dpto;
/

-- 3. reporteRecomendacionesPorDirectorDepartamental
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
            REC.ARERE_CODIGO,
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
            ORGCON.AINOC_DESCRIPCION,
            INF.AININ_USUARIO,
            INF.AININ_MEMORANDO,
            INF.AININ_MEMO_APROBACION,
            TO_CHAR(NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA_APROBACION), 'yyyy-MM-dd'),
            INF.AININ_TITULO,
            REC.ARERE_DESCRIPCION,
            CON.ARECO_CODIGO,
            REC.ARERE_CODIGO
    ),
    rec_clasificadas AS (
        SELECT 
            codigoInforme,
            organismoControl,
            auditorInforme,
            memorandoAuditoria,
            memoAprobacion,
            fechaEmision,
            tituloInforme,
            recomendacion,
            codigoConclusion,
            fechaACumplir,
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
                'estadoSeguimiento' VALUE rec_clasificadas.estadoSeguimiento
                RETURNING CLOB
            )
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

-- 4. FN_RECOMENDACIONES_INFORME
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
                       AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))
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
                                    AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))
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
                           AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))
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
                       AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))
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
                           AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))
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
