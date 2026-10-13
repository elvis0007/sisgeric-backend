PROCEDURE reportegeneralaij (
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
    -- 1. Parseo de fechas (mismo formato que usas en otros SP)
    v_fecha_ini := TO_TIMESTAMP ( fecha_inicial, 'DD/MM/YYYY HH24:MI:SS' );
    v_fecha_fin := TO_TIMESTAMP ( fecha_fin, 'DD/MM/YYYY HH24:MI:SS' );
    WITH ultimosverificables AS (
        SELECT
            v.*,
            ROW_NUMBER()
            OVER(PARTITION BY v.aredr_codigo
                 ORDER BY
                     v.areve_fecha_carga DESC NULLS LAST
            ) AS rn
        FROM
            ai_rec_verificables v
        WHERE
            v.areve_estado = 1
    ),
    -- progreso de recomendaciones (por responsables)
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
            ai_rec_detalle_recomendacion dr
            LEFT JOIN ai_rec_verificables          v ON v.aredr_codigo = dr.aredr_codigo
            LEFT JOIN ai_rec_estado_verificable    ev ON ev.areev_codigo = v.areev_codigo
        WHERE
            dr.aredr_estado = 1
        GROUP BY
            dr.arere_codigo
    ),
    -- progreso de informes (por recomendaciones)
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
                 ai_inf_informes inf
            JOIN ai_rec_hallazgos          hall ON hall.ainin_codigo = inf.ainin_codigo
                                          AND hall.areha_estado = 1
            JOIN ai_rec_conclusiones       con ON con.areha_codigo = hall.areha_codigo
                                            AND con.areco_estado = 1
            JOIN ai_rec_recomendaciones    rec ON rec.areco_codigo = con.areco_codigo
                                               AND rec.arere_estado = 1
            JOIN ai_rec_estado_seguimiento est ON est.arees_codigo = rec.arees_codigo
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
                     ai_inf_informes inf
                JOIN ai_inf_org_cont              org ON inf.ainoc_codigo = org.ainoc_codigo
                JOIN ai_rec_hallazgos             hall ON hall.ainin_codigo = inf.ainin_codigo
                                              AND hall.areha_estado = 1
                JOIN ai_rec_conclusiones          con ON con.areha_codigo = hall.areha_codigo
                                                AND con.areco_estado = 1
                JOIN ai_rec_recomendaciones       rec ON rec.areco_codigo = con.areco_codigo
                                                   AND rec.arere_estado = 1
                JOIN ai_rec_estado_seguimiento    est ON est.arees_codigo = rec.arees_codigo
                JOIN ai_rec_detalle_recomendacion dr ON dr.arere_codigo = rec.arere_codigo
                                                        AND dr.aredr_estado = 1
                JOIN ai_rec_responsables          res ON res.arers_codigo = dr.arers_codigo
                LEFT JOIN ultimosverificables          ver ON ver.aredr_codigo = dr.aredr_codigo
                                                     AND ver.rn = 1
                LEFT JOIN ai_rec_estado_verificable    ev ON ev.areev_codigo = ver.areev_codigo
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
END;