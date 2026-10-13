-- =============================================================================
-- SCRIPT DE MIGRACIÓN: CREACIÓN Y MIGRACIÓN DEL HISTORIAL DE RECOMENDACIONES
-- EEASA - SISREC
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. CREACIÓN DE SECUENCIA E INDICES
-- -----------------------------------------------------------------------------

-- Crear secuencia para la clave primaria de historial si no existe
DECLARE
    v_exists NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_exists FROM ALL_SEQUENCES WHERE SEQUENCE_NAME = 'AI_REC_HISTORIAL_SEQ' AND SEQUENCE_OWNER = 'ROOTSISREC';
    IF v_exists = 0 THEN
        EXECUTE IMMEDIATE 'CREATE SEQUENCE ROOTSISREC.AI_REC_HISTORIAL_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE';
    END IF;
END;
/

-- Crear tabla AI_REC_HISTORIAL (Se aumenta a VARCHAR2(4000) para evitar desbordamiento)
DECLARE
    v_exists NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_exists FROM ALL_TABLES WHERE TABLE_NAME = 'AI_REC_HISTORIAL' AND OWNER = 'ROOTSISREC';
    IF v_exists = 0 THEN
        EXECUTE IMMEDIATE '
        CREATE TABLE ROOTSISREC.AI_REC_HISTORIAL (
            AREHI_CODIGO          NUMBER(19, 0) NOT NULL,
            ARERE_CODIGO          NUMBER(19, 0) NOT NULL,
            AREHI_FECHA           TIMESTAMP NOT NULL,
            AREHI_ACCION          VARCHAR2(50) NOT NULL,
            AREHI_DETALLE         VARCHAR2(4000) NOT NULL,
            AREHI_RESPONSABLE_ANT VARCHAR2(250),
            AREHI_RESPONSABLE_NUE VARCHAR2(250),
            AREHI_FECHA_MAX_ANT   DATE,
            AREHI_FECHA_MAX_NUE   DATE,
            AREHI_AUDITOR         VARCHAR2(250),
            AREHI_OBSERVACION     VARCHAR2(4000),
            AREHI_USUARIO         VARCHAR2(100) NOT NULL,
            CONSTRAINT PK_AI_REC_HISTORIAL PRIMARY KEY (AREHI_CODIGO),
            CONSTRAINT FK_HISTORIAL_RECOMENDACION FOREIGN KEY (ARERE_CODIGO) 
                REFERENCES ROOTSISREC.AI_REC_RECOMENDACIONES (ARERE_CODIGO) ON DELETE CASCADE
        )';
        
        -- Crear índices para optimizar búsquedas
        EXECUTE IMMEDIATE 'CREATE INDEX ROOTSISREC.IDX_HISTORIAL_RECOMENDACION ON ROOTSISREC.AI_REC_HISTORIAL (ARERE_CODIGO)';
        EXECUTE IMMEDIATE 'CREATE INDEX ROOTSISREC.IDX_HISTORIAL_FECHA ON ROOTSISREC.AI_REC_HISTORIAL (AREHI_FECHA)';
        
        -- Añadir comentarios de documentación
        EXECUTE IMMEDIATE 'COMMENT ON TABLE ROOTSISREC.AI_REC_HISTORIAL IS ''Tabla para almacenar el historial de cambios y acciones de cada recomendación''';
        EXECUTE IMMEDIATE 'COMMENT ON COLUMN ROOTSISREC.AI_REC_HISTORIAL.AREHI_CODIGO IS ''Código único incremental del historial''';
        EXECUTE IMMEDIATE 'COMMENT ON COLUMN ROOTSISREC.AI_REC_HISTORIAL.ARERE_CODIGO IS ''Código de la recomendación asociada''';
        EXECUTE IMMEDIATE 'COMMENT ON COLUMN ROOTSISREC.AI_REC_HISTORIAL.AREHI_FECHA IS ''Fecha y hora en que ocurrió el evento''';
        EXECUTE IMMEDIATE 'COMMENT ON COLUMN ROOTSISREC.AI_REC_HISTORIAL.AREHI_ACCION IS ''Tipo de acción realizada (CREACION, PRORROGA, ASIGNACION_RESPONSABLE, ENVIO_VERIFICABLE, VALIDACION_VERIFICABLE, VENCIMIENTO)''';
        EXECUTE IMMEDIATE 'COMMENT ON COLUMN ROOTSISREC.AI_REC_HISTORIAL.AREHI_DETALLE IS ''Descripción legible del evento''';
        EXECUTE IMMEDIATE 'COMMENT ON COLUMN ROOTSISREC.AI_REC_HISTORIAL.AREHI_AUDITOR IS ''Nombre completo del auditor asignado (del informe asociado)''';
        EXECUTE IMMEDIATE 'COMMENT ON COLUMN ROOTSISREC.AI_REC_HISTORIAL.AREHI_OBSERVACION IS ''Observaciones adicionales (ej. justificación de prórroga o comentarios de validación del auditor)''';
    END IF;
END;
/


-- -----------------------------------------------------------------------------
-- 2. MIGRACIÓN DE DATOS DEL PASADO (Poblar Historial)
-- -----------------------------------------------------------------------------

DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM ROOTSISREC.AI_REC_HISTORIAL;
    
    -- Solo migramos si la tabla de historial está completamente vacía (evita duplicados)
    IF v_count = 0 THEN
        
        -- 2.1 Evento de CREACIÓN de la recomendación
        INSERT INTO ROOTSISREC.AI_REC_HISTORIAL (
            AREHI_CODIGO, ARERE_CODIGO, AREHI_FECHA, AREHI_ACCION, AREHI_DETALLE, AREHI_AUDITOR, AREHI_USUARIO
        )
        WITH auditor_recomendacion AS (
            SELECT DISTINCT
                REC.ARERE_CODIGO,
                COALESCE(
                    (SELECT MAX(RU.ASERU_NOMBRE || ' ' || RU.ASERU_APELLIDO) 
                     FROM ROOTSISREC.AI_SEG_ROL_USU RU 
                     WHERE RU.ASERU_CUENTA = INF.AININ_USUARIO),
                    INF.AININ_USUARIO
                ) AS AUDITOR_NOMBRE
            FROM ROOTSISREC.AI_REC_RECOMENDACIONES REC
            JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO
            JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO
            JOIN ROOTSISREC.AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO
        )
        SELECT
            ROOTSISREC.AI_REC_HISTORIAL_SEQ.NEXTVAL,
            R.ARERE_CODIGO,
            COALESCE(R.ARERE_FECHA, SYSDATE),
            'CREACION',
            SUBSTR('Creacion e inicio de control de la recomendacion N ' || R.ARERE_NUM_REC || '.', 1, 4000),
            AUD.AUDITOR_NOMBRE,
            COALESCE(R.ARERE_USUARIO, 'SYSTEM')
        FROM ROOTSISREC.AI_REC_RECOMENDACIONES R
        JOIN auditor_recomendacion AUD ON R.ARERE_CODIGO = AUD.ARERE_CODIGO;
        
        -- 2.2 Historial de ASIGNACIÓN de responsables
        INSERT INTO ROOTSISREC.AI_REC_HISTORIAL (
            AREHI_CODIGO, ARERE_CODIGO, AREHI_FECHA, AREHI_ACCION, AREHI_DETALLE,
            AREHI_RESPONSABLE_ANT, AREHI_RESPONSABLE_NUE, AREHI_AUDITOR, AREHI_USUARIO
        )
        WITH auditor_recomendacion AS (
            SELECT DISTINCT
                REC.ARERE_CODIGO,
                COALESCE(
                    (SELECT MAX(RU.ASERU_NOMBRE || ' ' || RU.ASERU_APELLIDO) 
                     FROM ROOTSISREC.AI_SEG_ROL_USU RU 
                     WHERE RU.ASERU_CUENTA = INF.AININ_USUARIO),
                    INF.AININ_USUARIO
                ) AS AUDITOR_NOMBRE
            FROM ROOTSISREC.AI_REC_RECOMENDACIONES REC
            JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO
            JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO
            JOIN ROOTSISREC.AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO
        ),
        asignaciones AS (
            SELECT
                DR.ARERE_CODIGO,
                COALESCE(DR.AREDR_FECHA, SYSDATE) AS FECHA,
                R.ARERS_NOMBRE || ' ' || R.ARERS_APELLIDO AS RESPONSABLE_NUE,
                R.ARERS_CARGO,
                AUD.AUDITOR_NOMBRE AS AUDITOR,
                COALESCE(DR.AREDR_USUARIO, 'SYSTEM') AS USUARIO,
                LAG(R.ARERS_NOMBRE || ' ' || R.ARERS_APELLIDO) OVER (
                    PARTITION BY DR.ARERE_CODIGO
                    ORDER BY COALESCE(DR.AREDR_FECHA, SYSDATE) ASC, DR.AREDR_CODIGO ASC
                ) AS RESPONSABLE_ANT
            FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR
            JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON DR.ARERS_CODIGO = R.ARERS_CODIGO
            JOIN auditor_recomendacion AUD ON DR.ARERE_CODIGO = AUD.ARERE_CODIGO
        )
        SELECT
            ROOTSISREC.AI_REC_HISTORIAL_SEQ.NEXTVAL,
            a.ARERE_CODIGO,
            a.FECHA,
            'ASIGNACION_RESPONSABLE',
            SUBSTR('Se asigno como responsable a: ' || a.RESPONSABLE_NUE || ' (' || COALESCE(a.ARERS_CARGO, 'Sin cargo') || ').', 1, 4000),
            a.RESPONSABLE_ANT,
            a.RESPONSABLE_NUE,
            a.AUDITOR,
            a.USUARIO
        FROM asignaciones a;

        -- 2.3 Historial de Carga / ENVÍO de verificables
        INSERT INTO ROOTSISREC.AI_REC_HISTORIAL (
            AREHI_CODIGO, ARERE_CODIGO, AREHI_FECHA, AREHI_ACCION, AREHI_DETALLE, AREHI_RESPONSABLE_NUE, AREHI_AUDITOR, AREHI_USUARIO
        )
        WITH auditor_recomendacion AS (
            SELECT DISTINCT
                REC.ARERE_CODIGO,
                COALESCE(
                    (SELECT MAX(RU.ASERU_NOMBRE || ' ' || RU.ASERU_APELLIDO) 
                     FROM ROOTSISREC.AI_SEG_ROL_USU RU 
                     WHERE RU.ASERU_CUENTA = INF.AININ_USUARIO),
                    INF.AININ_USUARIO
                ) AS AUDITOR_NOMBRE
            FROM ROOTSISREC.AI_REC_RECOMENDACIONES REC
            JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO
            JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO
            JOIN ROOTSISREC.AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO
        ),
        datos_envios AS (
            -- Caso 1: Envíos con la fecha de carga real (se busca quién subió el archivo por su usuario)
            SELECT
                DR.ARERE_CODIGO,
                V.AREVE_FECHA_CARGA AS FECHA,
                SUBSTR('Se cargo el archivo verificable: "' || V.AREVE_DESCRIPCION || '" (Adjunto: ' || V.AREVE_ADJUNTO || ').', 1, 4000) AS DETALLE,
                COALESCE(
                    (SELECT MAX(RU.ASERU_NOMBRE || ' ' || RU.ASERU_APELLIDO) 
                     FROM ROOTSISREC.AI_SEG_ROL_USU RU 
                     WHERE UPPER(RU.ASERU_CUENTA) = UPPER(V.AREVE_USUARIO)),
                    (SELECT MAX(R2.ARERS_NOMBRE || ' ' || R2.ARERS_APELLIDO)
                     FROM ROOTSISREC.AI_REC_RESPONSABLES R2
                     WHERE UPPER(R2.ARERS_USUARIO) = UPPER(V.AREVE_USUARIO)),
                    (SELECT MAX(R2.ARERS_NOMBRE || ' ' || R2.ARERS_APELLIDO)
                     FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR2
                     JOIN ROOTSISREC.AI_REC_RESPONSABLES R2 ON DR2.ARERS_CODIGO = R2.ARERS_CODIGO
                     WHERE DR2.ARERE_CODIGO = DR.ARERE_CODIGO
                       AND DR2.AREDR_FECHA <= V.AREVE_FECHA_CARGA),
                    R.ARERS_NOMBRE || ' ' || R.ARERS_APELLIDO
                ) AS RESPONSABLE,
                AUD.AUDITOR_NOMBRE AS AUDITOR,
                COALESCE(V.AREVE_USUARIO, 'RESPONSABLE') AS USUARIO
            FROM ROOTSISREC.AI_REC_VERIFICABLES V
            JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON V.AREDR_CODIGO = DR.AREDR_CODIGO
            JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON DR.ARERS_CODIGO = R.ARERS_CODIGO
            JOIN auditor_recomendacion AUD ON DR.ARERE_CODIGO = AUD.ARERE_CODIGO
            WHERE V.AREVE_FECHA_CARGA IS NOT NULL
            
            UNION ALL
            
            -- Caso 2: Envío original estimado cuando se re-subió un verificable y se sobreescribió la fecha de carga,
            -- pero ya existía una fecha de validación previa
            SELECT
                DR.ARERE_CODIGO,
                (V.AREVE_FECHA_VALIDACION - 5/1440) AS FECHA, -- 5 minutos antes de la validación
                SUBSTR('Se cargo el archivo verificable original (validado posteriormente el ' || TO_CHAR(V.AREVE_FECHA_VALIDACION, 'DD/MM/YYYY') || ').', 1, 4000) AS DETALLE,
                COALESCE(
                    (SELECT MAX(R2.ARERS_NOMBRE || ' ' || R2.ARERS_APELLIDO)
                     FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR2
                     JOIN ROOTSISREC.AI_REC_RESPONSABLES R2 ON DR2.ARERS_CODIGO = R2.ARERS_CODIGO
                     WHERE DR2.ARERE_CODIGO = DR.ARERE_CODIGO
                       AND DR2.AREDR_FECHA <= V.AREVE_FECHA_VALIDACION),
                    R.ARERS_NOMBRE || ' ' || R.ARERS_APELLIDO
                ) AS RESPONSABLE,
                AUD.AUDITOR_NOMBRE AS AUDITOR,
                COALESCE(V.AREVE_USUARIO, 'RESPONSABLE') AS USUARIO
            FROM ROOTSISREC.AI_REC_VERIFICABLES V
            JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON V.AREDR_CODIGO = DR.AREDR_CODIGO
            JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON DR.ARERS_CODIGO = R.ARERS_CODIGO
            JOIN auditor_recomendacion AUD ON DR.ARERE_CODIGO = AUD.ARERE_CODIGO
            WHERE V.AREVE_FECHA_VALIDACION IS NOT NULL 
              AND V.AREVE_FECHA_CARGA IS NOT NULL 
              AND V.AREVE_FECHA_CARGA > V.AREVE_FECHA_VALIDACION
        )
        SELECT
            ROOTSISREC.AI_REC_HISTORIAL_SEQ.NEXTVAL,
            e.ARERE_CODIGO,
            e.FECHA,
            'ENVIO_VERIFICABLE',
            e.DETALLE,
            e.RESPONSABLE,
            e.AUDITOR,
            e.USUARIO
        FROM datos_envios e;

        -- 2.4 Historial de VALIDACIÓN / Calificación de verificables (Incluyendo la Observación del Auditor)
        INSERT INTO ROOTSISREC.AI_REC_HISTORIAL (
            AREHI_CODIGO, ARERE_CODIGO, AREHI_FECHA, AREHI_ACCION, AREHI_DETALLE, AREHI_RESPONSABLE_NUE, AREHI_AUDITOR, AREHI_OBSERVACION, AREHI_USUARIO
        )
        WITH auditor_recomendacion AS (
            SELECT DISTINCT
                REC.ARERE_CODIGO,
                COALESCE(
                    (SELECT MAX(RU.ASERU_NOMBRE || ' ' || RU.ASERU_APELLIDO) 
                     FROM ROOTSISREC.AI_SEG_ROL_USU RU 
                     WHERE RU.ASERU_CUENTA = INF.AININ_USUARIO),
                    INF.AININ_USUARIO
                ) AS AUDITOR_NOMBRE
            FROM ROOTSISREC.AI_REC_RECOMENDACIONES REC
            JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO
            JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO
            JOIN ROOTSISREC.AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO
        )
        SELECT
            ROOTSISREC.AI_REC_HISTORIAL_SEQ.NEXTVAL,
            DR.ARERE_CODIGO,
            V.AREVE_FECHA_VALIDACION,
            'VALIDACION_VERIFICABLE',
            SUBSTR('Evaluacion de verificable por auditor. Estado: ' || EV.AREEV_DESCRIPCION || '.', 1, 4000),
            COALESCE(
                (SELECT MAX(R2.ARERS_NOMBRE || ' ' || R2.ARERS_APELLIDO)
                 FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR2
                 JOIN ROOTSISREC.AI_REC_RESPONSABLES R2 ON DR2.ARERS_CODIGO = R2.ARERS_CODIGO
                 WHERE DR2.ARERE_CODIGO = DR.ARERE_CODIGO
                   AND DR2.AREDR_FECHA <= V.AREVE_FECHA_VALIDACION),
                R.ARERS_NOMBRE || ' ' || R.ARERS_APELLIDO
            ),
            AUD.AUDITOR_NOMBRE,
            SUBSTR(V.AREVE_OBSERVACION, 1, 4000), -- Aquí se guarda la observación del auditor (ej. por qué no justifica)
            COALESCE(V.AREVE_USUARIO, 'AUDITOR') -- Guardamos el usuario auditor que realizó la validación
        FROM ROOTSISREC.AI_REC_VERIFICABLES V
        JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON V.AREDR_CODIGO = DR.AREDR_CODIGO
        JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON DR.ARERS_CODIGO = R.ARERS_CODIGO
        JOIN ROOTSISREC.AI_REC_ESTADO_VERIFICABLE EV ON V.AREEV_CODIGO = EV.AREEV_CODIGO
        JOIN auditor_recomendacion AUD ON DR.ARERE_CODIGO = AUD.ARERE_CODIGO
        WHERE V.AREVE_FECHA_VALIDACION IS NOT NULL;

        INSERT INTO ROOTSISREC.AI_REC_HISTORIAL (
            AREHI_CODIGO, ARERE_CODIGO, AREHI_FECHA, AREHI_ACCION, AREHI_DETALLE,
            AREHI_RESPONSABLE_NUE, AREHI_FECHA_MAX_ANT, AREHI_FECHA_MAX_NUE, AREHI_AUDITOR, AREHI_USUARIO
        )
        WITH auditor_recomendacion AS (
            SELECT DISTINCT
                REC.ARERE_CODIGO,
                COALESCE(
                    (SELECT MAX(RU.ASERU_NOMBRE || ' ' || RU.ASERU_APELLIDO) 
                     FROM ROOTSISREC.AI_SEG_ROL_USU RU 
                     WHERE RU.ASERU_CUENTA = INF.AININ_USUARIO),
                    INF.AININ_USUARIO
                ) AS AUDITOR_NOMBRE
            FROM ROOTSISREC.AI_REC_RECOMENDACIONES REC
            JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO
            JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO
            JOIN ROOTSISREC.AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO
        ),
        bitacora_base AS (
            SELECT
                b.asebi_usuario AS realizado_por,
                b.asebi_fecha   AS fecha_cambio,
                DBMS_LOB.SUBSTR(b.asebi_detalle, 3000, 1) AS detalle_texto,
                TO_NUMBER(
                    REGEXP_SUBSTR(
                        DBMS_LOB.SUBSTR(b.asebi_detalle, 3000, 1),
                        'arere_codigo:\s*([0-9]+)', 1, 1, NULL, 1
                    )
                ) AS arere_codigo
            FROM ROOTSISREC.AI_SEG_BITACORA b
            WHERE b.asebi_tabla = 'AI_REC_RECOMENDACIONES'
              AND (b.asebi_operacion LIKE 'ACTUALIZAR%' OR b.asebi_operacion = 'Actua')
        ),
        bitacora_con_fecha AS (
            SELECT
                bb.realizado_por,
                bb.fecha_cambio,
                bb.arere_codigo,
                CASE
                    WHEN INSTR(bb.detalle_texto, 'arere_fecha_maxima: ') > 0 THEN
                        CASE
                            WHEN SUBSTR(bb.detalle_texto, INSTR(bb.detalle_texto, 'arere_fecha_maxima: ') + 20, 1) BETWEEN '0' AND '9' THEN
                                TO_DATE(SUBSTR(bb.detalle_texto, INSTR(bb.detalle_texto, 'arere_fecha_maxima: ') + 20, 10), 'YYYY-MM-DD')
                            ELSE
                                TO_DATE(
                                    SUBSTR(bb.detalle_texto, INSTR(bb.detalle_texto, 'arere_fecha_maxima: ') + 20 + 8, 2) || '-' ||
                                    SUBSTR(bb.detalle_texto, INSTR(bb.detalle_texto, 'arere_fecha_maxima: ') + 20 + 4, 3) || '-' ||
                                    SUBSTR(bb.detalle_texto, INSTR(bb.detalle_texto, 'arere_fecha_maxima: ') + 20 + 24, 4),
                                    'DD-MON-YYYY',
                                    'NLS_DATE_LANGUAGE=AMERICAN'
                                )
                        END
                    ELSE NULL
                END AS fecha_maxima
            FROM bitacora_base bb
            WHERE bb.arere_codigo IS NOT NULL
        ),
        historial_con_lag AS (
            SELECT
                bcf.realizado_por,
                bcf.fecha_cambio,
                bcf.arere_codigo,
                bcf.fecha_maxima AS fecha_cambiada,
                LAG(bcf.fecha_maxima) OVER (
                    PARTITION BY bcf.arere_codigo
                    ORDER BY bcf.fecha_cambio ASC
                ) AS fecha_anterior
            FROM bitacora_con_fecha bcf
            WHERE bcf.fecha_maxima IS NOT NULL
        ),
        cambios_manuales AS (
            SELECT
                h.realizado_por,
                h.fecha_cambio,
                h.arere_codigo,
                h.fecha_cambiada,
                h.fecha_anterior
            FROM historial_con_lag h
            WHERE h.fecha_anterior IS NOT NULL
              AND h.fecha_cambiada <> h.fecha_anterior
        )
        SELECT
            ROOTSISREC.AI_REC_HISTORIAL_SEQ.NEXTVAL,
            c.arere_codigo,
            c.fecha_cambio,
            'PRORROGA',
            SUBSTR('Modificacion de la fecha maxima de cumplimiento. Nueva: ' || TO_CHAR(c.fecha_cambiada, 'DD/MM/YYYY') || ' (Anterior: ' || TO_CHAR(c.fecha_anterior, 'DD/MM/YYYY') || ').', 1, 4000),
            (SELECT MAX(R.ARERS_NOMBRE || ' ' || R.ARERS_APELLIDO)
             FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR
             JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON DR.ARERS_CODIGO = R.ARERS_CODIGO
             WHERE DR.ARERE_CODIGO = c.arere_codigo
               AND (DR.AREDR_ESTADO = '1' OR DR.AREDR_ESTADO = 1)),
            c.fecha_anterior,
            c.fecha_cambiada,
            AUD.AUDITOR_NOMBRE,
            c.realizado_por
        FROM cambios_manuales c
        JOIN auditor_recomendacion AUD ON c.arere_codigo = AUD.ARERE_CODIGO;

        COMMIT;
    END IF;
END;
/
