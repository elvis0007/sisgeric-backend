-- Migración V18: Crear procedimientos almacenados separados para el reporte de Planes de Acción y Evidencias por Responsable
-- 1. reportePlanesYEvidenciasAIJ: Alcance institucional para Auditor Interno Jefe (permite consultar todos o filtrar por auditor)
-- 2. reportePlanesYEvidenciasAO: Alcance operativo estricto para Auditor Operativo (solo sus informes asignados)
-- 3. reportePlanesYEvidencias: Procedimiento puente / retrocompatible

-- ====================================================================================================
-- 1. PROCEDIMIENTO PARA AUDITOR INTERNO JEFE (AIJ)
-- ====================================================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.reportePlanesYEvidenciasAIJ (
    p_json              OUT CLOB,
    fecha_inicial       IN  VARCHAR2,
    fecha_fin           IN  VARCHAR2,
    organismo_control   IN  INTEGER  DEFAULT 0,
    estado_seguimiento  IN  INTEGER  DEFAULT 0,
    auditor_responsable IN  VARCHAR2 DEFAULT '0',
    dpto                IN  VARCHAR2 DEFAULT '0',
    responsable         IN  VARCHAR2 DEFAULT '0'
) IS
    v_fecha_ini TIMESTAMP;
    v_fecha_fin TIMESTAMP;
    v_clean_dpto VARCHAR2(250);
BEGIN
    -- Parseo de fecha inicial
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

    -- Parseo de fecha final
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

    v_clean_dpto := TRIM(REPLACE(REPLACE(REPLACE(UPPER(NVL(dpto, '0')), 'DPTO.', ''), 'DEPARTAMENTO', ''), 'DIRECCION', ''));

    WITH evidencias_distintas AS (
        SELECT DISTINCT
            ER.ARERE_CODIGO,
            TRIM(ER.ARER_DESCRIPCION) AS ARER_DESCRIPCION,
            MIN(ER.ARER_CODIGO) AS MIN_COD
        FROM ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS ER
        WHERE ER.ARER_ESTADO IN ('A', '1')
          AND ER.ARER_DESCRIPCION IS NOT NULL
        GROUP BY ER.ARERE_CODIGO, TRIM(ER.ARER_DESCRIPCION)
    ),
    evidencias_rel AS (
        SELECT 
            ED.ARERE_CODIGO,
            LISTAGG(ED.ARER_DESCRIPCION, ' | ') WITHIN GROUP (ORDER BY ED.MIN_COD) AS evidencias_concatenadas
        FROM evidencias_distintas ED
        GROUP BY ED.ARERE_CODIGO
    ),
    planes_accion_totales AS (
        SELECT 
            PA.AREDR_CODIGO,
            PA.ARERE_CODIGO,
            COUNT(DISTINCT CASE WHEN ACT.ARPAC_ESTADO = 'A' THEN ACT.ARPAC_CODIGO END) AS total_actividades
        FROM ROOTSISREC.AI_REC_PLAN_ACCION PA
        LEFT JOIN ROOTSISREC.AI_REC_PLAN_ACTIVIDADES ACT ON ACT.ARPA_CODIGO = PA.ARPA_CODIGO
        WHERE PA.ARPA_ESTADO = 'A'
        GROUP BY PA.AREDR_CODIGO, PA.ARERE_CODIGO
    ),
    ultimas_prorrogas AS (
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
            V.AREVE_FECHA_VALIDACION,
            ROW_NUMBER() OVER (
                PARTITION BY V.AREDR_CODIGO
                ORDER BY 
                    CASE 
                        WHEN V.AREEV_CODIGO = 2 THEN 1
                        WHEN V.AREEV_CODIGO = 5 THEN 2
                        WHEN V.AREEV_CODIGO = 1 THEN 3
                        WHEN V.AREEV_CODIGO = 3 THEN 4
                        ELSE 5 
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
            NVL(INF.AININ_FECHA_APROBACION, NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA)) AS informefechaorden,
            TO_CHAR(COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'DD/MM/YYYY') AS fechavencimiento,
            EST.AREES_DESCRIPCION                                      AS estadorecomendacion,
            CASE
                WHEN UV.AREEV_CODIGO = 2                                THEN 'CUMPLIDA'
                WHEN COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) < TRUNC(SYSDATE) THEN 'VENCIDO'
                ELSE 'EN PROCESO'
            END AS estadoplazo,
            CASE
                WHEN UV.AREEV_CODIGO = 2                                THEN 0
                WHEN COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) < TRUNC(SYSDATE)
                THEN TRUNC(SYSDATE) - TRUNC(COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA))
                ELSE 0
            END AS diasvencidos,
            ROUND((PR.responsables_cumplidos * 100.0) / NULLIF(PR.total_responsables, 0), 2) AS progresorecomendacion,
            TO_CHAR(UV.AREVE_FECHA_CARGA, 'DD/MM/YYYY HH24:MI:SS')     AS fechacargaverificable,
            TO_CHAR(UV.AREVE_FECHA_VALIDACION, 'DD/MM/YYYY HH24:MI:SS') AS fechavalidacionverificable,
            TRIM(RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO)         AS responsable,
            NVL(EV.AREEV_DESCRIPCION, 'SIN VERIFICABLE')                AS estadoverificable,
            RES.ARERS_DEPARTAMENTO                                      AS departamento,
            UV.AREVE_DESCRIPCION                                        AS descripcionverificableresponsable,
            UV.AREVE_OBSERVACION                                        AS observacionauditor,
            ROUND((PI.recs_cumplidas * 100.0) / NULLIF(PI.total_recs, 0), 2) AS progresoinforme,
            TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS')                  AS fechaconsulta,
            REC.ARERE_RIESGO_VERIFICADO                                 AS riesgo,
            COALESCE(EREL.evidencias_concatenadas, REC.ARERE_EVIDENCIA_REQUERIDA, 'N/A') AS evidenciarequerida,
            CASE WHEN PAT.AREDR_CODIGO IS NOT NULL THEN 'SÍ' ELSE 'NO' END AS tieneplanaccion,
            NVL(PAT.total_actividades, 0)                               AS cantidadactividadesplan,
            DR.AREDR_CODIGO                                             AS aredrcodigo,
            RES.ARERS_CODIGO                                            AS arerscodigo,
            RES.ARERS_CODIGO_PER                                        AS arerscodigoper
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO AND HALL.AREHA_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO AND CON.ARECO_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO AND REC.ARERE_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_ESTADO_SEGUIMIENTO EST ON EST.AREES_CODIGO = REC.AREES_CODIGO
        JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO AND DR.AREDR_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO AND RES.ARERS_ESTADO = 1
        LEFT JOIN evidencias_rel EREL ON EREL.ARERE_CODIGO = REC.ARERE_CODIGO
        LEFT JOIN planes_accion_totales PAT ON PAT.AREDR_CODIGO = DR.AREDR_CODIGO AND PAT.ARERE_CODIGO = DR.ARERE_CODIGO
        LEFT JOIN ultimas_prorrogas UPR ON UPR.ARERE_CODIGO = DR.ARERE_CODIGO AND UPR.ARERS_CODIGO = DR.ARERS_CODIGO
        LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DR.AREDR_CODIGO AND UV.rn = 1
        LEFT JOIN ROOTSISREC.AI_REC_ESTADO_VERIFICABLE EV ON EV.AREEV_CODIGO = UV.AREEV_CODIGO
        LEFT JOIN progreso_recomendacion PR ON PR.ARERE_CODIGO = REC.ARERE_CODIGO
        LEFT JOIN progreso_informe PI ON PI.AININ_CODIGO = INF.AININ_CODIGO
        WHERE INF.AININ_ESTADO = 1
          -- Rango de fechas por fecha de aprobación, fecha de memo o fecha de registro
          AND (
                NVL(INF.AININ_FECHA_APROBACION, NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA)) BETWEEN v_fecha_ini AND v_fecha_fin
             OR INF.AININ_FECHA_APROBACION BETWEEN v_fecha_ini AND v_fecha_fin
             OR INF.AININ_FECHA_MEMO BETWEEN v_fecha_ini AND v_fecha_fin
          )
          AND (organismo_control = 0 OR ORG.AINOC_CODIGO = organismo_control)
          AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)
          -- Para AIJ: Si auditor='0' o null o vacío consulta todos, sino filtra por el auditor seleccionado
          AND (
                auditor_responsable IS NULL 
             OR auditor_responsable = '0' 
             OR auditor_responsable = '' 
             OR UPPER(TRIM(INF.AININ_USUARIO)) = UPPER(TRIM(auditor_responsable))
             OR UPPER(TRIM(REC.ARERE_USUARIO)) = UPPER(TRIM(auditor_responsable))
          )
          AND (
                dpto IS NULL
             OR dpto = '0'
             OR UPPER(TRIM(RES.ARERS_DEPARTAMENTO)) = UPPER(TRIM(dpto))
             OR (v_clean_dpto IS NOT NULL AND v_clean_dpto <> '0' AND (
                    UPPER(RES.ARERS_DEPARTAMENTO) LIKE '%' || v_clean_dpto || '%'
                 OR UPPER(RES.ARERS_DEPARTAMENTO_ABREV) LIKE '%' || v_clean_dpto || '%'
             ))
          )
          AND (
                responsable IS NULL
             OR responsable = '0'
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
                'numeroRecomendacion'               VALUE NVL(f.numrec, 0),
                'recomendacion'                     VALUE NVL(f.recomendacion, ''),
                'fechaVencimiento'                  VALUE NVL(f.fechavencimiento, ''),
                'estadoRecomendacion'               VALUE NVL(f.estadorecomendacion, ''),
                'estadoPlazo'                       VALUE NVL(f.estadoplazo, ''),
                'diasVencidos'                      VALUE NVL(f.diasvencidos, 0),
                'progresoRecomendacion'             VALUE NVL(f.progresorecomendacion, 0),
                'fechaCargaVerificable'             VALUE NVL(f.fechacargaverificable, ''),
                'fechaValidacionVerificable'        VALUE NVL(f.fechavalidacionverificable, ''),
                'responsable'                       VALUE NVL(f.responsable, ''),
                'estadoVerificable'                 VALUE NVL(f.estadoverificable, ''),
                'departamento'                      VALUE NVL(f.departamento, ''),
                'descripcionVerificableResponsable' VALUE NVL(f.descripcionverificableresponsable, ''),
                'observacionAuditor'                VALUE NVL(f.observacionauditor, ''),
                'progresoInforme'                   VALUE NVL(f.progresoinforme, 0),
                'fechaConsulta'                     VALUE NVL(f.fechaconsulta, ''),
                'riesgo'                            VALUE NVL(f.riesgo, ''),
                'evidenciaRequerida'                VALUE NVL(f.evidenciarequerida, ''),
                'tienePlanAccion'                   VALUE NVL(f.tieneplanaccion, 'NO'),
                'cantidadActividadesPlan'           VALUE NVL(f.cantidadactividadesplan, 0),
                'aredrCodigo'                       VALUE NVL(f.aredrcodigo, 0),
                'arersCodigo'                       VALUE NVL(f.arerscodigo, 0),
                'arersCodigoPer'                    VALUE NVL(f.arerscodigoper, '')
                RETURNING CLOB
            )
            ORDER BY f.informefechaorden ASC NULLS LAST, f.codigoinforme ASC, f.numrec ASC NULLS LAST, f.codigorecomendacion ASC, f.responsable ASC
            RETURNING CLOB
        )
    INTO p_json
    FROM filas f;

    IF p_json IS NULL THEN
        p_json := TO_CLOB('[]');
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END reportePlanesYEvidenciasAIJ;
/

-- ====================================================================================================
-- 2. PROCEDIMIENTO PARA AUDITOR OPERATIVO (AO)
-- ====================================================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.reportePlanesYEvidenciasAO (
    p_json              OUT CLOB,
    fecha_inicial       IN  VARCHAR2,
    fecha_fin           IN  VARCHAR2,
    organismo_control   IN  INTEGER  DEFAULT 0,
    estado_seguimiento  IN  INTEGER  DEFAULT 0,
    auditor_responsable IN  VARCHAR2,
    dpto                IN  VARCHAR2 DEFAULT '0',
    responsable         IN  VARCHAR2 DEFAULT '0'
) IS
    v_fecha_ini TIMESTAMP;
    v_fecha_fin TIMESTAMP;
    v_clean_dpto VARCHAR2(250);
BEGIN
    -- Parseo de fecha inicial
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

    -- Parseo de fecha final
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

    v_clean_dpto := TRIM(REPLACE(REPLACE(REPLACE(UPPER(NVL(dpto, '0')), 'DPTO.', ''), 'DEPARTAMENTO', ''), 'DIRECCION', ''));

    WITH evidencias_distintas AS (
        SELECT DISTINCT
            ER.ARERE_CODIGO,
            TRIM(ER.ARER_DESCRIPCION) AS ARER_DESCRIPCION,
            MIN(ER.ARER_CODIGO) AS MIN_COD
        FROM ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS ER
        WHERE ER.ARER_ESTADO IN ('A', '1')
          AND ER.ARER_DESCRIPCION IS NOT NULL
        GROUP BY ER.ARERE_CODIGO, TRIM(ER.ARER_DESCRIPCION)
    ),
    evidencias_rel AS (
        SELECT 
            ED.ARERE_CODIGO,
            LISTAGG(ED.ARER_DESCRIPCION, ' | ') WITHIN GROUP (ORDER BY ED.MIN_COD) AS evidencias_concatenadas
        FROM evidencias_distintas ED
        GROUP BY ED.ARERE_CODIGO
    ),
    planes_accion_totales AS (
        SELECT 
            PA.AREDR_CODIGO,
            PA.ARERE_CODIGO,
            COUNT(DISTINCT CASE WHEN ACT.ARPAC_ESTADO = 'A' THEN ACT.ARPAC_CODIGO END) AS total_actividades
        FROM ROOTSISREC.AI_REC_PLAN_ACCION PA
        LEFT JOIN ROOTSISREC.AI_REC_PLAN_ACTIVIDADES ACT ON ACT.ARPA_CODIGO = PA.ARPA_CODIGO
        WHERE PA.ARPA_ESTADO = 'A'
        GROUP BY PA.AREDR_CODIGO, PA.ARERE_CODIGO
    ),
    ultimas_prorrogas AS (
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
            V.AREVE_FECHA_VALIDACION,
            ROW_NUMBER() OVER (
                PARTITION BY V.AREDR_CODIGO
                ORDER BY 
                    CASE 
                        WHEN V.AREEV_CODIGO = 2 THEN 1
                        WHEN V.AREEV_CODIGO = 5 THEN 2
                        WHEN V.AREEV_CODIGO = 1 THEN 3
                        WHEN V.AREEV_CODIGO = 3 THEN 4
                        ELSE 5 
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
            NVL(INF.AININ_FECHA_APROBACION, NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA)) AS informefechaorden,
            TO_CHAR(COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'DD/MM/YYYY') AS fechavencimiento,
            EST.AREES_DESCRIPCION                                      AS estadorecomendacion,
            CASE
                WHEN UV.AREEV_CODIGO = 2                                THEN 'CUMPLIDA'
                WHEN COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) < TRUNC(SYSDATE) THEN 'VENCIDO'
                ELSE 'EN PROCESO'
            END AS estadoplazo,
            CASE
                WHEN UV.AREEV_CODIGO = 2                                THEN 0
                WHEN COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) < TRUNC(SYSDATE)
                THEN TRUNC(SYSDATE) - TRUNC(COALESCE(UPR.fecha_prorroga, DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA))
                ELSE 0
            END AS diasvencidos,
            ROUND((PR.responsables_cumplidos * 100.0) / NULLIF(PR.total_responsables, 0), 2) AS progresorecomendacion,
            TO_CHAR(UV.AREVE_FECHA_CARGA, 'DD/MM/YYYY HH24:MI:SS')     AS fechacargaverificable,
            TO_CHAR(UV.AREVE_FECHA_VALIDACION, 'DD/MM/YYYY HH24:MI:SS') AS fechavalidacionverificable,
            TRIM(RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO)         AS responsable,
            NVL(EV.AREEV_DESCRIPCION, 'SIN VERIFICABLE')                AS estadoverificable,
            RES.ARERS_DEPARTAMENTO                                      AS departamento,
            UV.AREVE_DESCRIPCION                                        AS descripcionverificableresponsable,
            UV.AREVE_OBSERVACION                                        AS observacionauditor,
            ROUND((PI.recs_cumplidas * 100.0) / NULLIF(PI.total_recs, 0), 2) AS progresoinforme,
            TO_CHAR(SYSDATE, 'DD/MM/YYYY HH24:MI:SS')                  AS fechaconsulta,
            REC.ARERE_RIESGO_VERIFICADO                                 AS riesgo,
            COALESCE(EREL.evidencias_concatenadas, REC.ARERE_EVIDENCIA_REQUERIDA, 'N/A') AS evidenciarequerida,
            CASE WHEN PAT.AREDR_CODIGO IS NOT NULL THEN 'SÍ' ELSE 'NO' END AS tieneplanaccion,
            NVL(PAT.total_actividades, 0)                               AS cantidadactividadesplan,
            DR.AREDR_CODIGO                                             AS aredrcodigo,
            RES.ARERS_CODIGO                                            AS arerscodigo,
            RES.ARERS_CODIGO_PER                                        AS arerscodigoper
        FROM ROOTSISREC.AI_INF_INFORMES INF
        JOIN ROOTSISREC.AI_INF_ORG_CONT ORG ON INF.AINOC_CODIGO = ORG.AINOC_CODIGO
        JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO AND HALL.AREHA_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO AND CON.ARECO_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO AND REC.ARERE_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_ESTADO_SEGUIMIENTO EST ON EST.AREES_CODIGO = REC.AREES_CODIGO
        JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO AND DR.AREDR_ESTADO = 1
        JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO AND RES.ARERS_ESTADO = 1
        LEFT JOIN evidencias_rel EREL ON EREL.ARERE_CODIGO = REC.ARERE_CODIGO
        LEFT JOIN planes_accion_totales PAT ON PAT.AREDR_CODIGO = DR.AREDR_CODIGO AND PAT.ARERE_CODIGO = DR.ARERE_CODIGO
        LEFT JOIN ultimas_prorrogas UPR ON UPR.ARERE_CODIGO = DR.ARERE_CODIGO AND UPR.ARERS_CODIGO = DR.ARERS_CODIGO
        LEFT JOIN ultimos_verificables UV ON UV.AREDR_CODIGO = DR.AREDR_CODIGO AND UV.rn = 1
        LEFT JOIN ROOTSISREC.AI_REC_ESTADO_VERIFICABLE EV ON EV.AREEV_CODIGO = UV.AREEV_CODIGO
        LEFT JOIN progreso_recomendacion PR ON PR.ARERE_CODIGO = REC.ARERE_CODIGO
        LEFT JOIN progreso_informe PI ON PI.AININ_CODIGO = INF.AININ_CODIGO
        WHERE INF.AININ_ESTADO = 1
          -- Rango de fechas por fecha de aprobación, fecha de memo o fecha de registro
          AND (
                NVL(INF.AININ_FECHA_APROBACION, NVL(INF.AININ_FECHA_MEMO, INF.AININ_FECHA)) BETWEEN v_fecha_ini AND v_fecha_fin
             OR INF.AININ_FECHA_APROBACION BETWEEN v_fecha_ini AND v_fecha_fin
             OR INF.AININ_FECHA_MEMO BETWEEN v_fecha_ini AND v_fecha_fin
          )
          AND (organismo_control = 0 OR ORG.AINOC_CODIGO = organismo_control)
          AND (estado_seguimiento = 0 OR REC.AREES_CODIGO = estado_seguimiento)
          -- Para AO: OBLIGATORIAMENTE debe coincidir con el auditor operativo asignado
          AND (
                UPPER(TRIM(INF.AININ_USUARIO)) = UPPER(TRIM(auditor_responsable))
             OR UPPER(TRIM(REC.ARERE_USUARIO)) = UPPER(TRIM(auditor_responsable))
          )
          AND (
                dpto IS NULL
             OR dpto = '0'
             OR UPPER(TRIM(RES.ARERS_DEPARTAMENTO)) = UPPER(TRIM(dpto))
             OR (v_clean_dpto IS NOT NULL AND v_clean_dpto <> '0' AND (
                    UPPER(RES.ARERS_DEPARTAMENTO) LIKE '%' || v_clean_dpto || '%'
                 OR UPPER(RES.ARERS_DEPARTAMENTO_ABREV) LIKE '%' || v_clean_dpto || '%'
             ))
          )
          AND (
                responsable IS NULL
             OR responsable = '0'
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
                'numeroRecomendacion'               VALUE NVL(f.numrec, 0),
                'recomendacion'                     VALUE NVL(f.recomendacion, ''),
                'fechaVencimiento'                  VALUE NVL(f.fechavencimiento, ''),
                'estadoRecomendacion'               VALUE NVL(f.estadorecomendacion, ''),
                'estadoPlazo'                       VALUE NVL(f.estadoplazo, ''),
                'diasVencidos'                      VALUE NVL(f.diasvencidos, 0),
                'progresoRecomendacion'             VALUE NVL(f.progresorecomendacion, 0),
                'fechaCargaVerificable'             VALUE NVL(f.fechacargaverificable, ''),
                'fechaValidacionVerificable'        VALUE NVL(f.fechavalidacionverificable, ''),
                'responsable'                       VALUE NVL(f.responsable, ''),
                'estadoVerificable'                 VALUE NVL(f.estadoverificable, ''),
                'departamento'                      VALUE NVL(f.departamento, ''),
                'descripcionVerificableResponsable' VALUE NVL(f.descripcionverificableresponsable, ''),
                'observacionAuditor'                VALUE NVL(f.observacionauditor, ''),
                'progresoInforme'                   VALUE NVL(f.progresoinforme, 0),
                'fechaConsulta'                     VALUE NVL(f.fechaconsulta, ''),
                'riesgo'                            VALUE NVL(f.riesgo, ''),
                'evidenciaRequerida'                VALUE NVL(f.evidenciarequerida, ''),
                'tienePlanAccion'                   VALUE NVL(f.tieneplanaccion, 'NO'),
                'cantidadActividadesPlan'           VALUE NVL(f.cantidadactividadesplan, 0),
                'aredrCodigo'                       VALUE NVL(f.aredrcodigo, 0),
                'arersCodigo'                       VALUE NVL(f.arerscodigo, 0),
                'arersCodigoPer'                    VALUE NVL(f.arerscodigoper, '')
                RETURNING CLOB
            )
            ORDER BY f.informefechaorden ASC NULLS LAST, f.codigoinforme ASC, f.numrec ASC NULLS LAST, f.codigorecomendacion ASC, f.responsable ASC
            RETURNING CLOB
        )
    INTO p_json
    FROM filas f;

    IF p_json IS NULL THEN
        p_json := TO_CLOB('[]');
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        p_json := TO_CLOB(JSON_OBJECT('error' VALUE SQLERRM));
END reportePlanesYEvidenciasAO;
/

-- ====================================================================================================
-- 3. PROCEDIMIENTO PUENTE RETROCOMPATIBLE (reportePlanesYEvidencias)
-- ====================================================================================================
CREATE OR REPLACE PROCEDURE ROOTSISREC.reportePlanesYEvidencias (
    p_json              OUT CLOB,
    fecha_inicial       IN  VARCHAR2,
    fecha_fin           IN  VARCHAR2,
    organismo_control   IN  INTEGER  DEFAULT 0,
    estado_seguimiento  IN  INTEGER  DEFAULT 0,
    auditor_responsable IN  VARCHAR2 DEFAULT '0',
    dpto                IN  VARCHAR2 DEFAULT '0',
    responsable         IN  VARCHAR2 DEFAULT '0'
) IS
BEGIN
    ROOTSISREC.reportePlanesYEvidenciasAIJ(
        p_json,
        fecha_inicial,
        fecha_fin,
        organismo_control,
        estado_seguimiento,
        auditor_responsable,
        dpto,
        responsable
    );
END reportePlanesYEvidencias;
/
