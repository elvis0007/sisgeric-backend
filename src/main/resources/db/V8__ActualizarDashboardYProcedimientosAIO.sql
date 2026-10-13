-- V8__ActualizarDashboardYProcedimientosAIO.sql
-- Actualización de procedimientos del Dashboard y gestión de solicitudes para Auditor Operativo:
-- 1. ReporteDashboardS1AIO: Corrige sombreado de variable arere_usuario y filtra recomendaciones y solicitudes del usuario operativo.
-- 2. ObtenerDataGrafica7AniosS2AIO: Permite filtrar recomendaciones por informe o recomendación asignada.
-- 3. ObtenerDataGraficaPorAnioAIOS3: Permite filtrar recomendaciones por informe o recomendación asignada.
-- 4. SP_SOLICITUDES_DEPARTAMENTAL_AUD: Permite listar solicitudes pertenecientes al auditor tanto por informe como por recomendación.

-- ============================================================================
-- 1. ReporteDashboardS1AIO
-- ============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.ReporteDashboardS1AIO (
    arere_usuario IN VARCHAR2,
    p_json OUT CLOB
) IS
    v_usuario VARCHAR2(50) := TRIM(arere_usuario);
BEGIN
    WITH rec_estatus_aio AS (
        SELECT 
            REC.ARERE_CODIGO,
            TRIM(RESP.ARERS_DEPARTAMENTO) AS DEPARTAMENTO,
            MAX(REC.AREES_CODIGO) AS AREES_CODIGO,
            FN_CALCULAR_DIAS_LABORABLES(MAX(COALESCE(DET.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA))) AS PLAZO_DIAS,
            COUNT(CASE WHEN NVL(ULT_VER.AREEV_CODIGO, 0) = 2 THEN 1 END) AS RESPS_CUMPLIDOS,
            COUNT(CASE WHEN NVL(ULT_VER.AREEV_CODIGO, 0) <> 2 THEN 1 END) AS RESPS_PENDIENTES,
            MAX(CASE WHEN ULT_VER.AREEV_CODIGO = 3 THEN 1 ELSE 0 END) AS TIENE_EVALUACION_NO_JUSTIFICA
        FROM AI_INF_INFORMES INF
        JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO
        JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
        JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
        JOIN AI_INF_ORG_CONT ORGCON ON INF.AINOC_CODIGO = ORGCON.AINOC_CODIGO
        JOIN AI_REC_DETALLE_RECOMENDACION DET ON REC.ARERE_CODIGO = DET.ARERE_CODIGO
        JOIN AI_REC_RESPONSABLES RESP ON DET.ARERS_CODIGO = RESP.ARERS_CODIGO
        LEFT JOIN (
            SELECT VER.AREDR_CODIGO, VER.AREEV_CODIGO,
                   ROW_NUMBER() OVER (PARTITION BY VER.AREDR_CODIGO ORDER BY VER.AREVE_FECHA_CARGA DESC) AS RN
            FROM AI_REC_VERIFICABLES VER
            WHERE VER.AREVE_ESTADO = 1
        ) ULT_VER ON DET.AREDR_CODIGO = ULT_VER.AREDR_CODIGO AND ULT_VER.RN = 1
        WHERE INF.AININ_ESTADO = 1
          AND HALL.AREHA_ESTADO = 1
          AND CON.ARECO_ESTADO = 1
          AND REC.ARERE_ESTADO = 1
          AND ORGCON.AINOC_ESTADO = 1
          AND DET.AREDR_ESTADO = 1
          AND RESP.ARERS_ESTADO = 1
          AND (v_usuario IS NULL OR v_usuario = '0' OR INF.AININ_USUARIO = v_usuario OR REC.ARERE_USUARIO = v_usuario)
        GROUP BY REC.ARERE_CODIGO, TRIM(RESP.ARERS_DEPARTAMENTO)
    ),
    rec_clasificadas AS (
        SELECT 
            ARERE_CODIGO,
            DEPARTAMENTO,
            CASE 
                WHEN AREES_CODIGO = 4 THEN 4
                WHEN AREES_CODIGO = 2 OR RESPS_PENDIENTES = 0 THEN 2
                WHEN TIENE_EVALUACION_NO_JUSTIFICA = 1 OR PLAZO_DIAS < 0 THEN 3
                ELSE 1
            END AS ESTADO_FINAL
        FROM rec_estatus_aio
    ),
    totales_gen AS (
        SELECT 
            COUNT(DISTINCT CASE WHEN ESTADO_FINAL = 1 THEN ARERE_CODIGO END) AS EN_PROC,
            COUNT(DISTINCT CASE WHEN ESTADO_FINAL = 2 THEN ARERE_CODIGO END) AS CUMP,
            COUNT(DISTINCT CASE WHEN ESTADO_FINAL = 3 THEN ARERE_CODIGO END) AS NO_CUMP,
            COUNT(DISTINCT CASE WHEN ESTADO_FINAL = 4 THEN ARERE_CODIGO END) AS NO_APL,
            COUNT(DISTINCT ARERE_CODIGO) AS TOTAL_REC
        FROM rec_clasificadas
    ),
    totales_dpto AS (
        SELECT 
            DEPARTAMENTO,
            COUNT(DISTINCT ARERE_CODIGO) AS TOTAL,
            COUNT(DISTINCT CASE WHEN ESTADO_FINAL = 1 THEN ARERE_CODIGO END) AS EN_PROC,
            COUNT(DISTINCT CASE WHEN ESTADO_FINAL = 2 THEN ARERE_CODIGO END) AS CUMP,
            COUNT(DISTINCT CASE WHEN ESTADO_FINAL = 3 THEN ARERE_CODIGO END) AS NO_CUMP,
            COUNT(DISTINCT CASE WHEN ESTADO_FINAL = 4 THEN ARERE_CODIGO END) AS NO_APL
        FROM rec_clasificadas
        GROUP BY DEPARTAMENTO
    ),
    ult_mes AS (
        SELECT COUNT(DISTINCT REC.ARERE_CODIGO) AS CNT
        FROM AI_INF_INFORMES INF
        JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO
        JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
        JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
        WHERE INF.AININ_ESTADO = 1
          AND HALL.AREHA_ESTADO = 1
          AND CON.ARECO_ESTADO = 1
          AND REC.ARERE_ESTADO = 1
          AND (v_usuario IS NULL OR v_usuario = '0' OR INF.AININ_USUARIO = v_usuario OR REC.ARERE_USUARIO = v_usuario)
          AND REC.ARERE_FECHA >= ADD_MONTHS(SYSDATE, -1)
    ),
    solic_pend AS (
        SELECT COUNT(DISTINCT S.ARESO_CODIGO) AS CNT
        FROM AI_REC_SOLICITUDES S
        JOIN AI_REC_RECOMENDACIONES REC ON S.ARERE_CODIGO = REC.ARERE_CODIGO
        JOIN AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO
        JOIN AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO
        JOIN AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO
        WHERE S.ARESO_ESTADO = 1
          AND S.ARESO_ESTADO_APROB = 2
          AND (v_usuario IS NULL OR v_usuario = '0' OR INF.AININ_USUARIO = v_usuario OR REC.ARERE_USUARIO = v_usuario)
    ),
    solic_esp AS (
        SELECT COUNT(DISTINCT S.ARESO_CODIGO) AS CNT
        FROM AI_REC_SOLICITUDES S
        JOIN AI_REC_RECOMENDACIONES REC ON S.ARERE_CODIGO = REC.ARERE_CODIGO
        JOIN AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO
        JOIN AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO
        JOIN AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO
        WHERE S.ARESO_ESTADO = 1
          AND S.ARESO_ESTADO_APROB = 1
          AND (v_usuario IS NULL OR v_usuario = '0' OR INF.AININ_USUARIO = v_usuario OR REC.ARERE_USUARIO = v_usuario)
    )
    SELECT JSON_OBJECT(
        'cantRecIngreUltimoMes' VALUE NVL((SELECT CNT FROM ult_mes), 0),
        'cantRecPendientesAprobacion' VALUE NVL((SELECT CNT FROM solic_pend), 0),
        'cantSolicitudesEnEspera' VALUE NVL((SELECT CNT FROM solic_esp), 0),
        'general' VALUE JSON_OBJECT(
            'enProceso' VALUE NVL((SELECT EN_PROC FROM totales_gen), 0),
            'cumplidas' VALUE NVL((SELECT CUMP FROM totales_gen), 0),
            'noCumplidas' VALUE NVL((SELECT NO_CUMP FROM totales_gen), 0),
            'noAplica' VALUE NVL((SELECT NO_APL FROM totales_gen), 0)
        ),
        'totalesGlobales' VALUE JSON_OBJECT(
            'totalRecomendaciones' VALUE NVL((SELECT TOTAL_REC FROM totales_gen), 0),
            'enProceso' VALUE NVL((SELECT EN_PROC FROM totales_gen), 0),
            'cumplidas' VALUE NVL((SELECT CUMP FROM totales_gen), 0),
            'noCumplidas' VALUE NVL((SELECT NO_CUMP FROM totales_gen), 0),
            'noAplica' VALUE NVL((SELECT NO_APL FROM totales_gen), 0)
        ),
        'departamentos' VALUE (
            SELECT JSON_ARRAYAGG(
                JSON_OBJECT(
                    'departamento' VALUE DEPARTAMENTO,
                    'totalRecomendaciones' VALUE TOTAL,
                    'enProceso' VALUE EN_PROC,
                    'cumplidas' VALUE CUMP,
                    'noCumplidas' VALUE NO_CUMP,
                    'noAplica' VALUE NO_APL
                )
                RETURNING CLOB
            )
            FROM totales_dpto
        )
    RETURNING CLOB)
    INTO p_json
    FROM DUAL;

EXCEPTION
    WHEN OTHERS THEN
        p_json := JSON_OBJECT('error' VALUE SQLERRM);
END ReporteDashboardS1AIO;
/

-- ============================================================================
-- 2. ObtenerDataGrafica7AniosS2AIO
-- ============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.ObtenerDataGrafica7AniosS2AIO (
    p_json OUT CLOB,
    arere_usuario IN VARCHAR2
)
IS
    v_anio_inicio VARCHAR2(4) := TO_CHAR(ADD_MONTHS(TRUNC(SYSDATE, 'YYYY'), -12*6), 'YYYY');
    v_anio_fin    VARCHAR2(4) := TO_CHAR(TRUNC(SYSDATE, 'YYYY'), 'YYYY');
    v_usuario     VARCHAR2(50) := TRIM(arere_usuario);
BEGIN
    -- Validación de usuario
    IF v_usuario IS NULL OR v_usuario = '0' THEN
        p_json := '{"anios": []}';
        RETURN;
    END IF;

    WITH anios AS (
        SELECT
            TO_CHAR(INF.AININ_FECHA_APROBACION, 'YYYY') AS anio,
            COUNT(DISTINCT REC.ARERE_CODIGO) AS total_recomendaciones,
            COUNT(DISTINCT CASE WHEN EST.AREES_DESCRIPCION = 'EN PROCESO' THEN REC.ARERE_CODIGO END) AS en_proceso,
            COUNT(DISTINCT CASE WHEN EST.AREES_DESCRIPCION = 'CUMPLIDA' THEN REC.ARERE_CODIGO END) AS cumplidas,
            COUNT(DISTINCT CASE WHEN EST.AREES_DESCRIPCION = 'NO CUMPLIDA' THEN REC.ARERE_CODIGO END) AS no_cumplidas,
            COUNT(DISTINCT CASE WHEN EST.AREES_DESCRIPCION = 'NO APLICA' THEN REC.ARERE_CODIGO END) AS no_aplica
        FROM
            AI_REC_RECOMENDACIONES REC
            JOIN AI_REC_ESTADO_SEGUIMIENTO EST ON REC.AREES_CODIGO = EST.AREES_CODIGO
            JOIN AI_REC_CONCLUSIONES CON ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
            JOIN AI_REC_HALLAZGOS HALL ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
            JOIN AI_INF_INFORMES INF ON INF.AININ_CODIGO = HALL.AININ_CODIGO
        WHERE 
            TO_CHAR(INF.AININ_FECHA_APROBACION, 'YYYY') BETWEEN v_anio_inicio AND v_anio_fin 
            AND (INF.AININ_USUARIO = v_usuario OR REC.ARERE_USUARIO = v_usuario)
            AND REC.ARERE_ESTADO = 1
            AND CON.ARECO_ESTADO = 1
            AND HALL.AREHA_ESTADO = 1
            AND INF.AININ_ESTADO = 1
        GROUP BY TO_CHAR(INF.AININ_FECHA_APROBACION, 'YYYY')
        ORDER BY anio
    )
    SELECT JSON_OBJECT(
        'anios' VALUE (
            SELECT JSON_ARRAYAGG(
                JSON_OBJECT(
                    'anio' VALUE a.anio,
                    'detalles' VALUE JSON_OBJECT(
                        'totalRecomendaciones' VALUE a.total_recomendaciones,
                        'enProceso' VALUE a.en_proceso,
                        'cumplidas' VALUE a.cumplidas,
                        'noCumplidas' VALUE a.no_cumplidas,
                        'noAplica' VALUE a.no_aplica
                    )
                )
                ORDER BY a.anio
            ) FROM anios a
        )
        RETURNING CLOB
    )
    INTO p_json
    FROM dual;

EXCEPTION
    WHEN OTHERS THEN
        p_json := JSON_OBJECT('error' VALUE SQLERRM);
END ObtenerDataGrafica7AniosS2AIO;
/

-- ============================================================================
-- 3. ObtenerDataGraficaPorAnioAIOS3
-- ============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.ObtenerDataGraficaPorAnioAIOS3 (
    p_json OUT CLOB,
    arere_usuario VARCHAR2
)
IS
    v_usuario VARCHAR2(50) := TRIM(arere_usuario);
BEGIN
    WITH datos_agrupados AS (
        SELECT
            TO_CHAR(INF.AININ_FECHA_APROBACION, 'YYYY') AS anio,
            RES.ARERS_DEPARTAMENTO AS departamento,
            RES.ARERS_DEPARTAMENTO_ABREV AS departamentoAbrev,
            COUNT(DISTINCT REC.ARERE_CODIGO) AS total_recomendaciones,
            COUNT(DISTINCT CASE WHEN EST.AREES_DESCRIPCION = 'EN PROCESO' THEN REC.ARERE_CODIGO END) AS en_proceso,
            COUNT(DISTINCT CASE WHEN EST.AREES_DESCRIPCION = 'CUMPLIDA' THEN REC.ARERE_CODIGO END) AS cumplidas,
            COUNT(DISTINCT CASE WHEN EST.AREES_DESCRIPCION = 'NO CUMPLIDA' THEN REC.ARERE_CODIGO END) AS no_cumplidas,
            COUNT(DISTINCT CASE WHEN EST.AREES_DESCRIPCION = 'NO APLICA' THEN REC.ARERE_CODIGO END) AS no_aplica
        FROM
            AI_REC_RECOMENDACIONES REC 
            JOIN AI_REC_DETALLE_RECOMENDACION DR ON REC.ARERE_CODIGO = DR.ARERE_CODIGO
            JOIN AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO
            JOIN AI_REC_ESTADO_SEGUIMIENTO EST ON REC.AREES_CODIGO = EST.AREES_CODIGO
            JOIN AI_REC_CONCLUSIONES CON ON CON.ARECO_CODIGO = REC.ARECO_CODIGO
            JOIN AI_REC_HALLAZGOS HALL ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO
            JOIN AI_INF_INFORMES INF ON INF.AININ_CODIGO = HALL.AININ_CODIGO
        WHERE 
            INF.AININ_FECHA_APROBACION IS NOT NULL 
            AND (v_usuario IS NULL OR v_usuario = '0' OR INF.AININ_USUARIO = v_usuario OR REC.ARERE_USUARIO = v_usuario)
            AND REC.ARERE_ESTADO = 1
            AND DR.AREDR_ESTADO = 1
            AND RES.ARERS_ESTADO = 1
            AND CON.ARECO_ESTADO = 1
            AND HALL.AREHA_ESTADO = 1
            AND INF.AININ_ESTADO = 1
        GROUP BY TO_CHAR(INF.AININ_FECHA_APROBACION, 'YYYY'), RES.ARERS_DEPARTAMENTO, RES.ARERS_DEPARTAMENTO_ABREV
    )
    SELECT JSON_OBJECT(
        'anios' VALUE JSON_ARRAYAGG(
            JSON_OBJECT(
                'anio' VALUE anio,
                'departamentos' VALUE JSON_OBJECTAGG(
                    departamento,
                    JSON_OBJECT(
                        'departamentoAbrev' VALUE departamentoAbrev,
                        'totalRecomendaciones' VALUE total_recomendaciones,
                        'enProceso' VALUE en_proceso,
                        'cumplidas' VALUE cumplidas,
                        'noCumplidas' VALUE no_cumplidas,
                        'noAplica' VALUE no_aplica
                        RETURNING CLOB
                    )
                    RETURNING CLOB
                )
                RETURNING CLOB
            )
            RETURNING CLOB
        )
        RETURNING CLOB
    )
    INTO p_json
    FROM datos_agrupados
    GROUP BY anio;
EXCEPTION
    WHEN OTHERS THEN
        p_json := JSON_OBJECT('error' VALUE SQLERRM);
END ObtenerDataGraficaPorAnioAIOS3;
/

-- ============================================================================
-- 4. SP_SOLICITUDES_DEPARTAMENTAL_AUD
-- ============================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.SP_SOLICITUDES_DEPARTAMENTAL_AUD(
    ARERS_DEPARTAMENTO VARCHAR2,
    AININ_USUARIO VARCHAR2,
    ANIO NUMBER,
    ARESO_ESTADO_APROB VARCHAR2,
    RESULTADO OUT SYS_REFCURSOR
)
IS
    SENTENCIA VARCHAR2(4000);
BEGIN
    SENTENCIA := 'SELECT L.* 
                  FROM ROOTSISREC.AI_INF_INFORMES I
                  JOIN ROOTSISREC.AI_REC_HALLAZGOS H ON I.AININ_CODIGO = H.AININ_CODIGO
                  JOIN ROOTSISREC.AI_REC_CONCLUSIONES C ON H.AREHA_CODIGO = C.AREHA_CODIGO
                  JOIN ROOTSISREC.AI_REC_RECOMENDACIONES R ON C.ARECO_CODIGO = R.ARECO_CODIGO
                  JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON R.ARERE_CODIGO = DR.ARERE_CODIGO
                  JOIN ROOTSISREC.AI_REC_RESPONSABLES RS ON DR.ARERS_CODIGO = RS.ARERS_CODIGO    
                  JOIN ROOTSISREC.AI_REC_SOLICITUDES L ON L.ARERE_CODIGO = R.ARERE_CODIGO
                  AND L.ARERS_CODIGO = RS.ARERS_CODIGO
                  WHERE L.ARESO_ESTADO = 1 
                    AND (I.AININ_USUARIO = ''' || AININ_USUARIO || ''' OR R.ARERE_USUARIO = ''' || AININ_USUARIO || ''') ';

    IF ARERS_DEPARTAMENTO IS NOT NULL AND ARERS_DEPARTAMENTO <> '0' THEN
        SENTENCIA := SENTENCIA || ' AND RS.ARERS_DEPARTAMENTO = ''' || ARERS_DEPARTAMENTO || ''' ';
    END IF;

    IF ANIO IS NOT NULL AND ANIO <> 0 THEN
        SENTENCIA := SENTENCIA || ' AND EXTRACT(YEAR FROM L.ARESO_FECHA_CARGA) = ' || ANIO;
    END IF;

    IF ARESO_ESTADO_APROB IS NOT NULL AND ARESO_ESTADO_APROB <> '0' THEN
        SENTENCIA := SENTENCIA || ' AND L.ARESO_ESTADO_APROB = ''' || ARESO_ESTADO_APROB || '''';
    END IF;
    
    SENTENCIA := SENTENCIA || ' ORDER BY L.ARESO_FECHA_CARGA DESC';

    OPEN RESULTADO FOR SENTENCIA;
END SP_SOLICITUDES_DEPARTAMENTAL_AUD;
/
