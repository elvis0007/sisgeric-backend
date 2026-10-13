-- =========================================================================================
-- V13__LimpiarDuplicadosYRestriccionUnicaCorteEvaluacionInforme.sql
-- Objetivo:
-- 1. Eliminar filas duplicadas en AI_CORTE_EVALUACION_INFORME (conservando el primer registro).
-- 2. Añadir restricción UNIQUE sobre (ARCEV_CODIGO, AININ_CODIGO) para evitar asignaciones duplicadas.
-- =========================================================================================

-- 1. Depuración de duplicados existentes
DELETE FROM ROOTSISREC.AI_CORTE_EVALUACION_INFORME 
WHERE ROWID NOT IN (
    SELECT MIN(ROWID) 
    FROM ROOTSISREC.AI_CORTE_EVALUACION_INFORME 
    GROUP BY ARCEV_CODIGO, AININ_CODIGO
);

COMMIT;

-- 2. Creación de la restricción UNIQUE para prevenir futuras duplicaciones
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE ROOTSISREC.AI_CORTE_EVALUACION_INFORME ADD CONSTRAINT UQ_CORTE_EVAL_INF UNIQUE (ARCEV_CODIGO, AININ_CODIGO)';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2261 THEN -- ORA-02261: already unique or existing constraint
            RAISE;
        END IF;
END;
/
