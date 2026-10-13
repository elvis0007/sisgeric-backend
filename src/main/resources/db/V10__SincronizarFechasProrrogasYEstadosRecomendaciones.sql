-- =============================================================================
-- MIGRACIÓN V10: Sincronización de fechas de prórrogas aprobadas y corrección de estados
-- =============================================================================

-- 1. Sincronizar fecha global ARERE_FECHA_MAXIMA en AI_REC_RECOMENDACIONES
-- con la fecha máxima aprobada de sus solicitudes de prórroga activas (ARESO_ESTADO_APROB = '3')
UPDATE ROOTSISREC.AI_REC_RECOMENDACIONES REC
SET REC.ARERE_FECHA_MAXIMA = (
    SELECT MAX(S.ARESO_FECHA_MAX_NUE)
    FROM ROOTSISREC.AI_REC_SOLICITUDES S
    WHERE S.ARERE_CODIGO = REC.ARERE_CODIGO
      AND S.ARESO_ESTADO_APROB = '3'
      AND S.ARESO_ESTADO = '1'
)
WHERE REC.ARERE_ESTADO = 1
  AND EXISTS (
    SELECT 1
    FROM ROOTSISREC.AI_REC_SOLICITUDES S
    WHERE S.ARERE_CODIGO = REC.ARERE_CODIGO
      AND S.ARESO_ESTADO_APROB = '3'
      AND S.ARESO_ESTADO = '1'
      AND (REC.ARERE_FECHA_MAXIMA IS NULL OR S.ARESO_FECHA_MAX_NUE > REC.ARERE_FECHA_MAXIMA)
  );

-- 2. Sincronizar fecha máxima individual del detalle del responsable específico
-- que solicitó y obtuvo la prórroga aprobada (no toca a los co-responsables)
UPDATE ROOTSISREC.AI_REC_DETALLE_RECOMENDACION D
SET D.AREDR_FECHA_MAXIMA = (
    SELECT MAX(S.ARESO_FECHA_MAX_NUE)
    FROM ROOTSISREC.AI_REC_SOLICITUDES S
    WHERE S.ARERE_CODIGO = D.ARERE_CODIGO
      AND S.ARERS_CODIGO = D.ARERS_CODIGO
      AND S.ARESO_ESTADO_APROB = '3'
      AND S.ARESO_ESTADO = '1'
)
WHERE D.AREDR_ESTADO = '1'
  AND EXISTS (
      SELECT 1 FROM ROOTSISREC.AI_REC_SOLICITUDES S
      WHERE S.ARERE_CODIGO = D.ARERE_CODIGO
        AND S.ARERS_CODIGO = D.ARERS_CODIGO
        AND S.ARESO_ESTADO_APROB = '3'
        AND S.ARESO_ESTADO = '1'
        AND (D.AREDR_FECHA_MAXIMA IS NULL OR S.ARESO_FECHA_MAX_NUE > D.AREDR_FECHA_MAXIMA)
  );

-- 3. Corregir recomendaciones que tenían prórroga aprobada vigente (>= SYSDATE)
-- pero fueron marcadas incorrectamente como NO CUMPLIDA (3). Deben ser EN PROCESO (1)
UPDATE ROOTSISREC.AI_REC_RECOMENDACIONES REC
SET REC.AREES_CODIGO = 1
WHERE REC.ARERE_ESTADO = 1
  AND REC.AREES_CODIGO = 3
  AND TRUNC(REC.ARERE_FECHA_MAXIMA) >= TRUNC(SYSDATE);

COMMIT;

