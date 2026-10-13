package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_historial;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_historial.models.HistorialRecomendacion;

public interface HistorialRecomendacionRepositorio extends JpaRepository<HistorialRecomendacion, Long> {

  @Query(value = "SELECT H.AREHI_CODIGO, H.ARERE_CODIGO, H.AREHI_FECHA, H.AREHI_ACCION, H.AREHI_DETALLE, " +
                 "H.AREHI_RESPONSABLE_ANT, " +
                 "CASE " +
                 "  WHEN H.AREHI_ACCION IN ('CREACION', 'VENCIMIENTO') THEN H.AREHI_RESPONSABLE_NUE " +
                 "  ELSE " +
                 "    COALESCE(" +
                 "      H.AREHI_RESPONSABLE_NUE, " +
                 "      (SELECT MAX(R.ARERS_NOMBRE || ' ' || R.ARERS_APELLIDO) " +
                 "       FROM ROOTSISREC.AI_REC_VERIFICABLES V " +
                 "       JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON V.AREDR_CODIGO = DR.AREDR_CODIGO " +
                 "       JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON DR.ARERS_CODIGO = R.ARERS_CODIGO " +
                 "       WHERE DR.ARERE_CODIGO = H.ARERE_CODIGO " +
                 "         AND (ABS(CAST(V.AREVE_FECHA_VALIDACION AS DATE) - CAST(H.AREHI_FECHA AS DATE)) * 86400 < 5 " +
                 "              OR ABS(CAST(V.AREVE_FECHA_CARGA AS DATE) - CAST(H.AREHI_FECHA AS DATE)) * 86400 < 5)), " +
                 "      CASE " +
                 "        WHEN H.AREHI_ACCION = 'ENVIO_VERIFICABLE' THEN " +
                 "          (SELECT MAX(RU.ASERU_NOMBRE || ' ' || RU.ASERU_APELLIDO) " +
                 "           FROM ROOTSISREC.AI_SEG_ROL_USU RU " +
                 "           WHERE TRANSLATE(UPPER(RU.ASERU_CUENTA), 'ÁÉÍÓÚÑ', 'AEIOUN') = TRANSLATE(UPPER(H.AREHI_USUARIO), 'ÁÉÍÓÚÑ', 'AEIOUN')) 	" +
                 "        ELSE NULL " +
                 "      END, " +
                 "      (SELECT MAX(R.ARERS_NOMBRE || ' ' || R.ARERS_APELLIDO) " +
                 "       FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR " +
                 "       JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON DR.ARERS_CODIGO = R.ARERS_CODIGO " +
                 "       WHERE DR.ARERE_CODIGO = H.ARERE_CODIGO " +
                 "         AND ABS(CAST(DR.AREDR_FECHA AS DATE) - CAST(H.AREHI_FECHA AS DATE)) * 86400 < 5), " +
                 "      (SELECT MAX(R.ARERS_NOMBRE || ' ' || R.ARERS_APELLIDO) " +
                 "       FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR " +
                 "       JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON DR.ARERS_CODIGO = R.ARERS_CODIGO " +
                 "       WHERE DR.ARERE_CODIGO = H.ARERE_CODIGO " +
                 "         AND (DR.AREDR_ESTADO = '1' OR DR.AREDR_ESTADO = 1))" +
                 "    ) " +
                 "END AS AREHI_RESPONSABLE_NUE, " +
                 "H.AREHI_FECHA_MAX_ANT, H.AREHI_FECHA_MAX_NUE, " +
                 "COALESCE(" +
                 "  H.AREHI_AUDITOR, " +
                 "  (SELECT MAX(RU.ASERU_NOMBRE || ' ' || RU.ASERU_APELLIDO) " +
                 "   FROM ROOTSISREC.AI_REC_RECOMENDACIONES REC " +
                 "   JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
                 "   JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
                 "   JOIN ROOTSISREC.AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
                 "   JOIN ROOTSISREC.AI_SEG_ROL_USU RU ON RU.ASERU_CUENTA = INF.AININ_USUARIO " +
                 "   WHERE REC.ARERE_CODIGO = H.ARERE_CODIGO)" +
                 ") AS AREHI_AUDITOR, " +
                 "H.AREHI_OBSERVACION, " +
                 "COALESCE(" +
                 "  (SELECT MAX(RU.ASERU_NOMBRE || ' ' || RU.ASERU_APELLIDO) " +
                 "   FROM ROOTSISREC.AI_SEG_ROL_USU RU " +
                 "   WHERE TRANSLATE(UPPER(RU.ASERU_CUENTA), 'ÁÉÍÓÚÑ', 'AEIOUN') = TRANSLATE(UPPER(H.AREHI_USUARIO), 'ÁÉÍÓÚÑ', 'AEIOUN')), 	" +
                 "  H.AREHI_USUARIO" +
                 ") AS AREHI_USUARIO " +
                 "FROM ROOTSISREC.AI_REC_HISTORIAL H " +
                 "WHERE H.ARERE_CODIGO = :arereCodigo " +
                 "ORDER BY H.AREHI_FECHA ASC, H.AREHI_CODIGO ASC", nativeQuery = true)
  List<HistorialRecomendacion> findByRecomendacionCodigo(@Param("arereCodigo") Long arereCodigo);
}
