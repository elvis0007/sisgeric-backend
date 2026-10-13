-- Migración de evidencias requeridas desde la columna ARERE_EVIDENCIA_REQUERIDA hacia AI_REC_EVIDENCIAS_REQUERIDAS
DECLARE
    v_count NUMBER := 0;
BEGIN
    FOR r IN (
        SELECT ARERE_CODIGO, ARERE_EVIDENCIA_REQUERIDA 
        FROM ROOTSISREC.AI_REC_RECOMENDACIONES 
        WHERE ARERE_EVIDENCIA_REQUERIDA IS NOT NULL 
          AND TRIM(ARERE_EVIDENCIA_REQUERIDA) IS NOT NULL
    ) LOOP
        -- Verificar si ya existen evidencias para esta recomendación
        SELECT COUNT(*) INTO v_count
        FROM ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS
        WHERE ARERE_CODIGO = r.ARERE_CODIGO;

        IF v_count = 0 THEN
            INSERT INTO ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS (
                ARER_CODIGO,
                ARERE_CODIGO,
                ARER_DESCRIPCION,
                ARER_ESTADO
            ) VALUES (
                ROOTSISREC.AI_REC_EVIDENCIA_REQ_SEQ.NEXTVAL,
                r.ARERE_CODIGO,
                SUBSTR(TRIM(r.ARERE_EVIDENCIA_REQUERIDA), 1, 500),
                'A'
            );
        END IF;
    END LOOP;
    COMMIT;
END;
/
