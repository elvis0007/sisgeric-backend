package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones;

import java.util.List;
import javax.persistence.Tuple;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;

@Repository
public interface RecomendacionRepositorio extends JpaRepository<Recomendacion, Long> {

        @Query(value = "SELECT * FROM RECOMENDACION R " +
                        "WHERE R.ARECO_CODIGO IN ( " +
                        "    SELECT C.ARECO_CODIGO " +
                        "    FROM AI_REC_CONCLUSIONES C " +
                        "    WHERE C.AREHA_CODIGO IN ( " +
                        "        SELECT H.AREHA_CODIGO " +
                        "        FROM AI_REC_HALLAZGOS H " +
                        "        WHERE H.AININ_CODIGO = :aininCodigo " +
                        "        AND H.AREHA_ESTADO = 1 " +
                        "    ) " +
                        ") " +
                        "ORDER BY R.ARERE_NUM_REC", nativeQuery = true)
        List<Recomendacion> findRecomendacionesByInformeId(@Param("aininCodigo") Long aininCodigo);

        @Query(value = "SELECT COALESCE(MAX(R.ARERE_NUM_REC), 0) AS MAX_NUM_REC" +
                        " FROM AI_REC_RECOMENDACIONES R " +
                        " WHERE R.ARECO_CODIGO IN (" +
                        "   SELECT C.ARECO_CODIGO FROM AI_REC_CONCLUSIONES C" +
                        "   WHERE C.AREHA_CODIGO IN (" +
                        "       SELECT H.AREHA_CODIGO FROM AI_REC_HALLAZGOS H" +
                        "       WHERE H.AININ_CODIGO = :ainin_codigo" +
                        "       AND H.AREHA_ESTADO = 1 " +
                        "       )" +
                        "   )", nativeQuery = true)
        Integer findMaxNumRecPorInforme(@Param("ainin_codigo") Long ainin_codigo);

        @Query(value = "SELECT * FROM AI_REC_RECOMENDACIONES WHERE ARECO_CODIGO = :areco_codigo", nativeQuery = true)
        List<Recomendacion> findByConclusion(Long areco_codigo);

        @Query(value = "SELECT COUNT(*) FROM AI_REC_RECOMENDACIONES WHERE ARECO_CODIGO = :arecoCodigo AND ARERE_ESTADO = '1'", nativeQuery = true)
        Long countActivasByConclusion(@Param("arecoCodigo") Long arecoCodigo);

        @Query(value = "SELECT R.* FROM AI_INF_INFORMES I, AI_REC_HALLAZGOS H, AI_REC_CONCLUSIONES C, AI_REC_RECOMENDACIONES R WHERE C.ARECO_CODIGO = R.ARECO_CODIGO AND H.AREHA_CODIGO=C.AREHA_CODIGO AND I.AININ_CODIGO=H.AININ_CODIGO AND I.AININ_CODIGO = :ainin_codigo", nativeQuery = true)
        List<Recomendacion> findByInforme(Long ainin_codigo);

        @Query(value = "SELECT R.* FROM AI_REC_RECOMENDACIONES R " +
                        "WHERE R.AREES_CODIGO != :estadoVencido " +
                        "AND R.AREES_CODIGO != :estadoCumplido " +
                        "AND NOT EXISTS ( " +
                        "    SELECT 1 FROM AI_REC_DETALLE_RECOMENDACION D " +
                        "    WHERE D.ARERE_CODIGO = R.ARERE_CODIGO " +
                        "    AND D.AREDR_ESTADO = 1 " +
                        "    AND ( " +
                        "        TRUNC(COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = D.ARERE_CODIGO AND S.ARERS_CODIGO = D.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), D.AREDR_FECHA_MAXIMA, R.ARERE_FECHA_MAXIMA)) >= TO_DATE(:fechaActual, 'YYYY-MM-DD') " +
                        "        OR EXISTS ( " +
                        "            SELECT 1 FROM AI_REC_VERIFICABLES V " +
                        "            WHERE V.AREDR_CODIGO = D.AREDR_CODIGO " +
                        "            AND V.AREVE_ESTADO = '1' " +
                        "            AND V.AREEV_CODIGO = 2 " +
                        "        ) " +
                        "    ) " +
                        ")", nativeQuery = true)
        List<Recomendacion> findRecomendacionesVencidas(
                        @Param("fechaActual") String fechaActual, // Se debe pasar como String en el formato correcto
                        @Param("estadoVencido") Long estadoVencido,
                        @Param("estadoCumplido") Long estadoCumplido);

        @Query(value = "SELECT R.* FROM AI_REC_RECOMENDACIONES R " +
                        "JOIN AI_REC_DETALLE_RECOMENDACION D ON R.ARERE_CODIGO = D.ARERE_CODIGO " +
                        "JOIN AI_REC_VERIFICABLES V ON D.AREDR_CODIGO = V.AREDR_CODIGO " +
                        "WHERE V.AREVE_CODIGO = :areve_codigo AND ROWNUM = 1", nativeQuery = true)
        Recomendacion findByVerificable(@Param("areve_codigo") Long areve_codigo);

        // CONSULTAR DETALLES ACTIVOS VENCIDOS NO JUSTIFICADOS
        @Query(value = "SELECT COUNT(D.AREDR_CODIGO) FROM AI_REC_DETALLE_RECOMENDACION D " +
                       "WHERE D.ARERE_CODIGO = :arere_codigo " +
                       "AND D.AREDR_ESTADO = '1' " +
                       "AND TRUNC(COALESCE((SELECT MAX(S1.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S1 WHERE S1.ARERE_CODIGO = D.ARERE_CODIGO AND S1.ARERS_CODIGO = D.ARERS_CODIGO AND S1.ARESO_ESTADO_APROB = '3' AND S1.ARESO_ESTADO = '1'), D.AREDR_FECHA_MAXIMA, (SELECT R.ARERE_FECHA_MAXIMA FROM AI_REC_RECOMENDACIONES R WHERE R.ARERE_CODIGO = D.ARERE_CODIGO))) < TRUNC(SYSDATE) " +
                       "AND NOT EXISTS ( " +
                       "    SELECT 1 FROM AI_REC_VERIFICABLES V " +
                       "    WHERE V.AREDR_CODIGO = D.AREDR_CODIGO " +
                       "    AND V.AREVE_ESTADO = '1' " +
                       "    AND V.AREEV_CODIGO = 2 " +
                       ") " +
                       "AND NOT EXISTS ( " +
                       "    SELECT 1 FROM AI_REC_DETALLE_RECOMENDACION D2 " +
                       "    WHERE D2.ARERE_CODIGO = D.ARERE_CODIGO " +
                       "    AND D2.AREDR_ESTADO = '1' " +
                       "    AND ( " +
                       "        TRUNC(COALESCE((SELECT MAX(S2.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S2 WHERE S2.ARERE_CODIGO = D2.ARERE_CODIGO AND S2.ARERS_CODIGO = D2.ARERS_CODIGO AND S2.ARESO_ESTADO_APROB = '3' AND S2.ARESO_ESTADO = '1'), D2.AREDR_FECHA_MAXIMA, (SELECT R2.ARERE_FECHA_MAXIMA FROM AI_REC_RECOMENDACIONES R2 WHERE R2.ARERE_CODIGO = D2.ARERE_CODIGO))) >= TRUNC(SYSDATE) " +
                       "        OR EXISTS ( " +
                       "            SELECT 1 FROM AI_REC_VERIFICABLES V2 " +
                       "            WHERE V2.AREDR_CODIGO = D2.AREDR_CODIGO " +
                       "            AND V2.AREVE_ESTADO = '1' " +
                       "            AND V2.AREEV_CODIGO = 2 " +
                       "        ) " +
                       "    ) " +
                       ")", nativeQuery = true)
        long contarDetallesVencidosNoJustificados(@Param("arere_codigo") Long arere_codigo);

        // CONSULTAR CORREOS DE USUARIOS CON RECOMENDACIONES A 7 DIAS POR VENCER
        @Query(value = "SELECT DISTINCT R.ARERS_CORREO AS correo, R.ARERS_USUARIO_PER AS usuarioPer " +
                        "FROM AI_REC_RESPONSABLES R " +
                        "INNER JOIN AI_REC_DETALLE_RECOMENDACION D ON R.ARERS_CODIGO = D.ARERS_CODIGO " +
                        "INNER JOIN AI_REC_RECOMENDACIONES REC ON D.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "WHERE COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = D.ARERE_CODIGO AND S.ARERS_CODIGO = D.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), D.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) BETWEEN SYSDATE AND SYSDATE + 7 " +
                        "AND R.ARERS_ESTADO = 1 " +
                        "AND REC.AREES_CODIGO IN (1, 3) " +
                        "AND D.AREDR_ESTADO = 1 " +
                        "AND REC.ARERE_ESTADO = 1 " +
                        "AND NOT EXISTS (" +
                        "    SELECT 1 " +
                        "    FROM AI_REC_VERIFICABLES VER " +
                        "    WHERE VER.AREDR_CODIGO = D.AREDR_CODIGO " +
                        "    AND VER.AREVE_ESTADO = '1' " +
                        "    AND VER.AREEV_CODIGO = 2" +
                        ")", nativeQuery = true)
        List<Object[]> findResponsablesActivosConRecomendacionesProximas();

        // CONSULTAR CORREOS Y USUARIOS DE PERSONAS CON RECOMENDACIONES VENCIDAS
        @Query(value = "SELECT DISTINCT R.ARERS_CORREO AS correo, R.ARERS_USUARIO_PER AS usuario " +
                        "FROM AI_REC_RESPONSABLES R " +
                        "INNER JOIN AI_REC_DETALLE_RECOMENDACION D ON R.ARERS_CODIGO = D.ARERS_CODIGO " +
                        "INNER JOIN AI_REC_RECOMENDACIONES REC ON D.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "WHERE COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = D.ARERE_CODIGO AND S.ARERS_CODIGO = D.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), D.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) < SYSDATE " +
                        "AND R.ARERS_ESTADO = 1 " +
                        "AND REC.AREES_CODIGO IN (1, 3) " +
                        "AND D.AREDR_ESTADO = 1 " +
                        "AND REC.ARERE_ESTADO = 1 " +
                        "AND NOT EXISTS (" +
                        "    SELECT 1 " +
                        "    FROM AI_REC_VERIFICABLES VER " +
                        "    WHERE VER.AREDR_CODIGO = D.AREDR_CODIGO " +
                        "    AND VER.AREVE_ESTADO = '1' " +
                        "    AND VER.AREEV_CODIGO = 2" +
                        ")", nativeQuery = true)
        List<Tuple> obtenerResponsablesConRecomendacionesVencidas();

        @Query(value = "SELECT R.ARERS_NOMBRE AS nombres, " +
                        "R.ARERS_APELLIDO AS apellidos, " +
                        "INF.AININ_TITULO AS tituloInforme, " +
                        "COUNT(DR.ARERE_CODIGO) AS conteoRecomendacionesPendientes " +
                        "FROM AI_REC_RESPONSABLES R " +
                        "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR ON R.ARERS_CODIGO = DR.ARERS_CODIGO " +
                        "INNER JOIN AI_REC_RECOMENDACIONES REC ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "INNER JOIN AI_REC_CONCLUSIONES CON ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
                        "INNER JOIN AI_REC_HALLAZGOS HALL ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
                        "INNER JOIN AI_INF_INFORMES INF ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
                        "WHERE R.ARERS_USUARIO_PER = :usuario " +
                        "AND REC.AREES_CODIGO IN (1, 3) " +
                        "AND COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO AND S.ARERS_CODIGO = DR.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) < SYSDATE " +
                        "AND R.ARERS_ESTADO = 1 " +
                        "AND DR.AREDR_ESTADO = 1 " +
                        "AND REC.ARERE_ESTADO = 1 " +
                        "AND CON.ARECO_ESTADO = 1 " +
                        "AND HALL.AREHA_ESTADO = 1 " +
                        "AND INF.AININ_ESTADO = 1 " +
                        "AND NOT EXISTS (" +
                        "    SELECT 1 " +
                        "    FROM AI_REC_VERIFICABLES VER " +
                        "    WHERE VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "    AND VER.AREVE_ESTADO = '1' " +
                        "    AND VER.AREEV_CODIGO = 2" +
                        ") " +
                        "GROUP BY R.ARERS_NOMBRE, R.ARERS_APELLIDO, INF.AININ_TITULO", nativeQuery = true)
        List<Tuple> obtenerRecomendacionesVencidas(@Param("usuario") String usuario);


        //OBTENER DATOS PARA PLANTILLA DE CORREO DE RECORDATORIO DE VENCIMIENTO DE RECOMENDACIONES
        @Query(value = "SELECT R.ARERS_NOMBRE, " +
        "R.ARERS_APELLIDO, " +
        "INF.AININ_TITULO, " +
        "COUNT(DR.ARERE_CODIGO) AS TOTAL_REC_PENDIENTES " +
        "FROM AI_REC_RESPONSABLES R " +
        "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR " +
        "ON R.ARERS_CODIGO = DR.ARERS_CODIGO " +
        "INNER JOIN AI_REC_RECOMENDACIONES REC " +
        "ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
        "INNER JOIN AI_REC_CONCLUSIONES CON " +
        "ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
        "INNER JOIN AI_REC_HALLAZGOS HALL " +
        "ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
        "INNER JOIN AI_INF_INFORMES INF " +
        "ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
        "WHERE R.ARERS_USUARIO_PER = :arers_usuario_per " +
        "AND REC.AREES_CODIGO IN (1, 3) " +
        "AND COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO AND S.ARERS_CODIGO = DR.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA) BETWEEN SYSDATE AND SYSDATE + 7 " +
        "AND R.ARERS_ESTADO = 1 " +
        "AND DR.AREDR_ESTADO = 1 " +
        "AND REC.ARERE_ESTADO = 1 " +
        "AND CON.ARECO_ESTADO = 1 " +
        "AND HALL.AREHA_ESTADO = 1 " +
        "AND INF.AININ_ESTADO = 1 " +
        "AND NOT EXISTS (" +
        "    SELECT 1 " +
        "    FROM AI_REC_VERIFICABLES VER " +
        "    WHERE VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
        "    AND VER.AREVE_ESTADO = '1' " +
        "    AND VER.AREEV_CODIGO = 2" +
        ") " +
        "GROUP BY R.ARERS_NOMBRE, R.ARERS_APELLIDO, INF.AININ_TITULO", nativeQuery = true)
        List<Object[]> obtenerRecomendacionesPorUsuario(@Param("arers_usuario_per") String arers_usuario_per);

        //OBTENER DETALLE DE RECOMENDACIONES DE TODOS LOS RESPONSABLES, POR INFORME.
        @Query(value = 
        "SELECT " +
                "JSON_OBJECT(" +
                "'responsableDTO' VALUE JSON_OBJECT(" +
                        "'nombres' VALUE RES.ARERS_NOMBRE, " +
                        "'apellidos' VALUE RES.ARERS_APELLIDO, " + 
                        "'correo' VALUE RES.ARERS_CORREO, " +
                        "'usuarioPer' VALUE RES.ARERS_USUARIO_PER" +
                "), " +
                "'informeDTO' VALUE JSON_OBJECT(" +
                        "'titulo' VALUE INF.AININ_TITULO, " +
                        "'memoAprobacion' VALUE INF.AININ_MEMO_APROBACION, " +
                        "'memorando' VALUE INF.AININ_MEMORANDO " +
                "), " +
                "'recomendacionesDTO' VALUE JSON_ARRAYAGG(" +
                        "JSON_OBJECT(" +
                        "'descripcion' VALUE REC.ARERE_DESCRIPCION, " +
                        "'fechaMaxima' VALUE TO_CHAR(COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO AND S.ARERS_CODIGO = DR.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'YYYY-MM-DD')" +
                        ") ORDER BY COALESCE(DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)" +
                " RETURNING CLOB) " +
                " RETURNING CLOB FORMAT JSON) AS DATA " +
        "FROM " +
                "AI_REC_RECOMENDACIONES REC " +
                "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                "INNER JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                "INNER JOIN AI_REC_CONCLUSIONES CON ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
                "INNER JOIN AI_REC_HALLAZGOS HALL ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
                "INNER JOIN AI_INF_INFORMES INF ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
        "WHERE " +
                "REC.AREES_CODIGO IN (1, 3) " +
                "AND INF.AININ_CODIGO = :ainin_codigo " +
                "AND REC.ARERE_ESTADO = 1 " +
                "AND CON.ARECO_ESTADO = 1 " +
                "AND HALL.AREHA_ESTADO = 1 " +
                "AND INF.AININ_ESTADO = 1 " +
                "AND DR.AREDR_ESTADO = 1 " +
                "AND RES.ARERS_ESTADO = 1 " +
                "AND NOT EXISTS (" +
                "    SELECT 1 FROM AI_REC_VERIFICABLES V2 " +
                "    WHERE V2.AREDR_CODIGO = DR.AREDR_CODIGO " +
                "      AND V2.AREVE_ESTADO = '1' " +
                "      AND V2.AREEV_CODIGO = 2" +
                ") " +
        "GROUP BY " +
                "RES.ARERS_NOMBRE, " +
                "RES.ARERS_APELLIDO, " +
                "RES.ARERS_CORREO, " +
                "RES.ARERS_USUARIO_PER, " +
                "INF.AININ_TITULO, " +
                "INF.AININ_MEMO_APROBACION, " +
                "INF.AININ_MEMORANDO", nativeQuery = true)
        List<Object> obtenerDetalleRecomendacionesPorInformeDTO(@Param("ainin_codigo") Long ainin_codigo);

        @Query(value = "SELECT DISTINCT " +
                        "RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO AS nombreApellidoRS, " +
                        "RES.ARERS_USUARIO_PER AS responsableUsuarioPer, " +
                        "RES.ARERS_CORREO AS responsableCorreo, " +
                        "INF.AININ_TITULO AS infTitulo, " +
                        "INF.AININ_MEMO_APROBACION AS infMemoAprobacion, " +
                        "INF.AININ_MEMORANDO AS infMemorando, " +
                        "COALESCE((SELECT EV2.AREEV_DESCRIPCION FROM AI_REC_VERIFICABLES V2 INNER JOIN AI_REC_ESTADO_VERIFICABLE EV2 ON V2.AREEV_CODIGO = EV2.AREEV_CODIGO WHERE V2.AREDR_CODIGO = DR.AREDR_CODIGO AND V2.AREVE_ESTADO = '1' AND V2.AREVE_FECHA_CARGA = (SELECT MAX(V3.AREVE_FECHA_CARGA) FROM AI_REC_VERIFICABLES V3 WHERE V3.AREDR_CODIGO = DR.AREDR_CODIGO AND V3.AREVE_ESTADO = '1') AND ROWNUM = 1), 'VERIFICABLE NO CREADO') AS estadoVerificable, " +
                        "TO_CHAR(COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO AND S.ARERS_CODIGO = DR.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'YYYY-MM-DD') AS recFechaMaxCumplimiento, " +
                        "REC.ARERE_DESCRIPCION AS recDescripcion " +
                        "FROM AI_REC_RECOMENDACIONES REC " +
                        "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "INNER JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                        "INNER JOIN AI_REC_CONCLUSIONES CON ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
                        "INNER JOIN AI_REC_HALLAZGOS HALL ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
                        "INNER JOIN AI_INF_INFORMES INF ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
                        "WHERE REC.ARERE_CODIGO = :arere_codigo " +
                        "AND (:arers_codigo IS NULL OR RES.ARERS_CODIGO = :arers_codigo) " +
                        "AND REC.ARERE_ESTADO = 1 " +
                        "AND DR.AREDR_ESTADO = 1 " +
                        "AND RES.ARERS_ESTADO = 1 " +
                        "AND CON.ARECO_ESTADO = 1 " +
                        "AND HALL.AREHA_ESTADO = 1 " +
                        "AND INF.AININ_ESTADO = 1 " +
                        "AND NOT EXISTS (" +
                        "    SELECT 1 FROM AI_REC_VERIFICABLES VER " +
                        "    WHERE VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "    AND VER.AREVE_ESTADO = '1' " +
                        "    AND VER.AREEV_CODIGO = 2" +
                        ")", nativeQuery = true)
        List<Tuple> obtenerDetalleCambioFechaMaxCumplimiento(@Param("arere_codigo") Long arere_codigo,
                        @Param("arers_codigo") Long arers_codigo);

        default List<Tuple> obtenerDetalleCambioFechaMaxCumplimiento(Long arere_codigo) {
                return obtenerDetalleCambioFechaMaxCumplimiento(arere_codigo, null);
        }

        @Modifying
        @Transactional
        @Query(value = "UPDATE AI_REC_RECOMENDACIONES REC " +
                        "SET REC.AREES_CODIGO = 3 " +
                        "WHERE EXISTS ( " +
                        "    SELECT 1 FROM AI_REC_DETALLE_RECOMENDACION D " +
                        "    WHERE D.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "    AND D.AREDR_ESTADO = '1' " +
                        "    AND TRUNC(COALESCE((SELECT MAX(S1.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S1 WHERE S1.ARERE_CODIGO = D.ARERE_CODIGO AND S1.ARERS_CODIGO = D.ARERS_CODIGO AND S1.ARESO_ESTADO_APROB = '3' AND S1.ARESO_ESTADO = '1'), D.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)) < TRUNC(SYSDATE) " +
                        "    AND NOT EXISTS ( " +
                        "        SELECT 1 FROM AI_REC_VERIFICABLES V " +
                        "        WHERE V.AREDR_CODIGO = D.AREDR_CODIGO " +
                        "        AND V.AREVE_ESTADO = '1' " +
                        "        AND V.AREEV_CODIGO = 2 " +
                        "    ) " +
                        ") " +
                        "AND NOT EXISTS ( " +
                        "    SELECT 1 FROM AI_REC_DETALLE_RECOMENDACION D2 " +
                        "    WHERE D2.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "    AND D2.AREDR_ESTADO = '1' " +
                        "    AND ( " +
                        "        TRUNC(COALESCE((SELECT MAX(S2.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S2 WHERE S2.ARERE_CODIGO = D2.ARERE_CODIGO AND S2.ARERS_CODIGO = D2.ARERS_CODIGO AND S2.ARESO_ESTADO_APROB = '3' AND S2.ARESO_ESTADO = '1'), D2.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA)) >= TRUNC(SYSDATE) " +
                        "        OR EXISTS ( " +
                        "            SELECT 1 FROM AI_REC_VERIFICABLES V2 " +
                        "            WHERE V2.AREDR_CODIGO = D2.AREDR_CODIGO " +
                        "            AND V2.AREVE_ESTADO = '1' " +
                        "            AND V2.AREEV_CODIGO = 2 " +
                        "        ) " +
                        "    ) " +
                        ") " +
                        "AND REC.AREES_CODIGO = 1 " +
                        "AND REC.ARERE_ESTADO = 1", nativeQuery = true)
        int actualizarEstadoRecVencidas();

        @Modifying
        @Transactional
        @Query(value = "UPDATE AI_REC_RECOMENDACIONES REC " +
                        "SET REC.AREES_CODIGO = 2, REC.ARERE_RIESGO_VERIFICADO = 'BAJO' " +
                        "WHERE REC.ARERE_ESTADO = 1 " +
                        "AND REC.AREES_CODIGO IN (1, 3) " +
                        "AND EXISTS ( " +
                        "    SELECT 1 " +
                        "    FROM AI_REC_DETALLE_RECOMENDACION DR " +
                        "    WHERE DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "      AND DR.AREDR_ESTADO = 1 " +
                        ") " +
                        "AND NOT EXISTS ( " +
                        "    SELECT 1 " +
                        "    FROM AI_REC_DETALLE_RECOMENDACION DR " +
                        "    WHERE DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "      AND DR.AREDR_ESTADO = 1 " +
                        "      AND NOT EXISTS ( " +
                        "          SELECT 1 " +
                        "          FROM AI_REC_VERIFICABLES VER " +
                        "          WHERE VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "            AND VER.AREVE_ESTADO = 1 " +
                        "            AND VER.AREEV_CODIGO = 2 " +
                        "      ) " +
                        ")", nativeQuery = true)
        int actualizarEstadoRecCumplidasAutomatico();

        @Modifying
        @Transactional
        @Query(value = "UPDATE AI_REC_RECOMENDACIONES SET ARERE_RIESGO_VERIFICADO = 'BAJO' " +
                        "WHERE ARERE_ESTADO = 1 AND AREES_CODIGO = 2 " +
                        "AND (ARERE_RIESGO_VERIFICADO IS NULL OR ARERE_RIESGO_VERIFICADO <> 'BAJO')", nativeQuery = true)
        int normalizarRiesgoCumplidas();

        @Query(value = "SELECT COUNT(*) " +
                        "FROM AI_REC_DETALLE_RECOMENDACION DR " +
                        "WHERE DR.ARERE_CODIGO = :arere_codigo " +
                        "AND DR.AREDR_ESTADO = 1", nativeQuery = true)
        int contarDetallesActivosPorRecomendacion(@Param("arere_codigo") Long arere_codigo);

        @Query(value = "SELECT COUNT(*) " +
                        "FROM AI_REC_DETALLE_RECOMENDACION DR " +
                        "WHERE DR.ARERE_CODIGO = :arere_codigo " +
                        "AND DR.AREDR_ESTADO = 1 " +
                        "AND EXISTS ( " +
                        "    SELECT 1 " +
                        "    FROM AI_REC_VERIFICABLES VER " +
                        "    WHERE VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "      AND VER.AREVE_ESTADO = 1 " +
                        "      AND VER.AREEV_CODIGO = 2 " +
                        ")", nativeQuery = true)
        int contarDetallesActivosConUltimoVerificableJustifica(@Param("arere_codigo") Long arere_codigo);


        @Query(value = "SELECT " + 
                "COALESCE(INF.AININ_MEMORANDO, INF.AININ_MEMO_APROBACION) AS memorando " +
                "FROM " +
                "AI_INF_INFORMES INF " +
                "JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
                "JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
                "JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
                "WHERE " +
                "REC.ARERE_CODIGO = :arere_codigo " +
                "AND REC.ARERE_ESTADO = 1 " +
                "AND CON.ARECO_ESTADO = 1 " +
                "AND HALL.AREHA_ESTADO = 1 " +
                "AND INF.AININ_ESTADO = 1", nativeQuery = true)
        String obtenerMemoInforme(@Param("arere_codigo") Long arere_codigo);

        @Query(value = "SELECT " +
                        "LISTAGG(" +
                        "RES.ARERS_NOMBRE || ' ' || " + 
                        "RES.ARERS_APELLIDO || ' (' || " + 
                        "COALESCE(EV.AREEV_DESCRIPCION, 'VERIFICABLE NO CREADO') || " + 
                        "')' , ', ') " + 
                        "WITHIN GROUP (ORDER BY RES.ARERS_NOMBRE, RES.ARERS_APELLIDO) AS responsables " +
                        "FROM ROOTSISREC.AI_REC_RESPONSABLES RES " +
                        "JOIN ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                        "JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " + 
                        "LEFT JOIN (" +
                        "    SELECT AREDR_CODIGO, AREEV_CODIGO, ROW_NUMBER() OVER (" +
                        "        PARTITION BY AREDR_CODIGO " +
                        "        ORDER BY " +
                        "            CASE " +
                        "                WHEN AREEV_CODIGO = 2 THEN 1 " +
                        "                WHEN AREEV_CODIGO = 1 THEN 2 " +
                        "                WHEN AREEV_CODIGO = 3 THEN 3 " +
                        "                ELSE 4 " +
                        "            END ASC, " +
                        "            NVL(AREVE_FECHA_CARGA, AREVE_FECHA) DESC NULLS LAST, " +
                        "            AREVE_CODIGO DESC" +
                        "    ) AS rn " +
                        "    FROM ROOTSISREC.AI_REC_VERIFICABLES " +
                        "    WHERE TO_CHAR(AREVE_ESTADO) = '1' " +
                        ") ULT ON DR.AREDR_CODIGO = ULT.AREDR_CODIGO AND ULT.rn = 1 " +
                        "LEFT JOIN ROOTSISREC.AI_REC_ESTADO_VERIFICABLE EV ON ULT.AREEV_CODIGO = EV.AREEV_CODIGO " +
                        "WHERE " +
                        "RES.ARERS_ESTADO = 1 " +
                        "AND DR.AREDR_ESTADO = 1 " +
                        "AND REC.ARERE_ESTADO = 1 " +
                        "AND REC.ARERE_CODIGO = :arere_codigo", nativeQuery = true)
        String obtenerResponsablesPorRecomendacion(@Param("arere_codigo") Long arere_codigo);

        @Query(value = "SELECT DISTINCT RES.ARERS_USUARIO_PER FROM AI_REC_RESPONSABLES RES " +
                       "JOIN AI_REC_DETALLE_RECOMENDACION DR ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                       "WHERE DR.ARERE_CODIGO = :arereCodigo AND DR.AREDR_ESTADO = '1' AND RES.ARERS_ESTADO = '1'", nativeQuery = true)
        List<String> findResponsablesByRecomendacion(@Param("arereCodigo") Long arereCodigo);

        // CONSULTAR RESPONSABLES PENDIENTES DE PLAN DE ACCION APROX. 1 HORA ANTES DE CUMPLIRSE 48 HORAS DEL INFORME
        @Query(value = "SELECT " +
                        "INF.AININ_CODIGO AS aininCodigo, " +
                        "INF.AININ_TITULO AS aininTitulo, " +
                        "COALESCE(INF.AININ_MEMORANDO, INF.AININ_MEMO_APROBACION) AS aininMemo, " +
                        "RES.ARERS_NOMBRE AS arersNombre, " +
                        "RES.ARERS_APELLIDO AS arersApellido, " +
                        "RES.ARERS_CORREO AS arersCorreo, " +
                        "RES.ARERS_USUARIO_PER AS arersUsuarioPer, " +
                        "REC.ARERE_CODIGO AS arereCodigo, " +
                        "REC.ARERE_DESCRIPCION AS arereDescripcion, " +
                        "TO_CHAR(COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO AND S.ARERS_CODIGO = DR.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'YYYY-MM-DD') AS recFechaMax " +
                        "FROM AI_INF_INFORMES INF " +
                        "INNER JOIN AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
                        "INNER JOIN AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
                        "INNER JOIN AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
                        "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "INNER JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                        "WHERE INF.AININ_ESTADO = 1 " +
                        "  AND INF.AININ_NOTIFICADO = '1' " +
                        "  AND HALL.AREHA_ESTADO = 1 " +
                        "  AND CON.ARECO_ESTADO = 1 " +
                        "  AND REC.ARERE_ESTADO = 1 " +
                        "  AND (REC.AREES_CODIGO IS NULL OR REC.AREES_CODIGO NOT IN (2, 4)) " +
                        "  AND DR.AREDR_ESTADO = 1 " +
                        "  AND RES.ARERS_ESTADO = 1 " +
                        "  AND INF.AININ_FECHA IS NOT NULL " +
                        "  AND (CAST(SYSDATE AS DATE) - CAST(INF.AININ_FECHA AS DATE)) * 24 >= 47 " +
                        "  AND (CAST(SYSDATE AS DATE) - CAST(INF.AININ_FECHA AS DATE)) * 24 <= 48.5 " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_REC_PLAN_ACCION PA " +
                        "      WHERE PA.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "        AND PA.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "        AND PA.ARPA_ESTADO = 'A' " +
                        "  ) " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_REC_VERIFICABLES VER " +
                        "      WHERE VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "        AND VER.AREVE_ESTADO = '1' " +
                        "        AND VER.AREEV_CODIGO = 2 " +
                        "  ) " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_NOTIFICACIONES N " +
                        "      WHERE N.ANOTIF_USUARIO_PER = RES.ARERS_USUARIO_PER " +
                        "        AND N.ANOTIF_TIPO = 'plan-accion-recordatorio' " +
                        "        AND N.ANOTIF_URL_SISTEMA LIKE '%' || INF.AININ_CODIGO || '%' " +
                        "  ) " +
                        "ORDER BY INF.AININ_CODIGO, RES.ARERS_USUARIO_PER", nativeQuery = true)
        List<Tuple> obtenerPendientesPlanAccion48Horas();

        // CONSULTAR RESPONSABLES PENDIENTES DE PLAN DE ACCION POR INFORME ESPECIFICO (PARA PRUEBAS O MANUAL)
        @Query(value = "SELECT " +
                        "INF.AININ_CODIGO AS aininCodigo, " +
                        "INF.AININ_TITULO AS aininTitulo, " +
                        "COALESCE(INF.AININ_MEMORANDO, INF.AININ_MEMO_APROBACION) AS aininMemo, " +
                        "RES.ARERS_NOMBRE AS arersNombre, " +
                        "RES.ARERS_APELLIDO AS arersApellido, " +
                        "RES.ARERS_CORREO AS arersCorreo, " +
                        "RES.ARERS_USUARIO_PER AS arersUsuarioPer, " +
                        "REC.ARERE_CODIGO AS arereCodigo, " +
                        "REC.ARERE_DESCRIPCION AS arereDescripcion, " +
                        "TO_CHAR(COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO AND S.ARERS_CODIGO = DR.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'YYYY-MM-DD') AS recFechaMax " +
                        "FROM AI_INF_INFORMES INF " +
                        "INNER JOIN AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
                        "INNER JOIN AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
                        "INNER JOIN AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
                        "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "INNER JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                        "WHERE INF.AININ_CODIGO = :ainin_codigo " +
                        "  AND INF.AININ_ESTADO = 1 " +
                        "  AND HALL.AREHA_ESTADO = 1 " +
                        "  AND CON.ARECO_ESTADO = 1 " +
                        "  AND REC.ARERE_ESTADO = 1 " +
                        "  AND (REC.AREES_CODIGO IS NULL OR REC.AREES_CODIGO NOT IN (2, 4)) " +
                        "  AND DR.AREDR_ESTADO = 1 " +
                        "  AND RES.ARERS_ESTADO = 1 " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_REC_PLAN_ACCION PA " +
                        "      WHERE PA.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "        AND PA.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "        AND PA.ARPA_ESTADO = 'A' " +
                        "  ) " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_REC_VERIFICABLES VER " +
                        "      WHERE VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "        AND VER.AREVE_ESTADO = '1' " +
                        "        AND VER.AREEV_CODIGO = 2 " +
                        "  ) " +
                        "ORDER BY RES.ARERS_USUARIO_PER", nativeQuery = true)
        List<Tuple> obtenerPendientesPlanAccionPorInforme(@Param("ainin_codigo") Long ainin_codigo);

        // CONSULTAR RESPONSABLES PENDIENTES DE PLAN DE ACCION POR RECOMENDACION ESPECIFICA (PARA PRUEBAS O MANUAL)
        @Query(value = "SELECT " +
                        "INF.AININ_CODIGO AS aininCodigo, " +
                        "INF.AININ_TITULO AS aininTitulo, " +
                        "COALESCE(INF.AININ_MEMORANDO, INF.AININ_MEMO_APROBACION) AS aininMemo, " +
                        "RES.ARERS_NOMBRE AS arersNombre, " +
                        "RES.ARERS_APELLIDO AS arersApellido, " +
                        "RES.ARERS_CORREO AS arersCorreo, " +
                        "RES.ARERS_USUARIO_PER AS arersUsuarioPer, " +
                        "REC.ARERE_CODIGO AS arereCodigo, " +
                        "REC.ARERE_DESCRIPCION AS arereDescripcion, " +
                        "TO_CHAR(COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO AND S.ARERS_CODIGO = DR.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'YYYY-MM-DD') AS recFechaMax " +
                        "FROM AI_REC_RECOMENDACIONES REC " +
                        "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "INNER JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                        "INNER JOIN AI_REC_CONCLUSIONES CON ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
                        "INNER JOIN AI_REC_HALLAZGOS HALL ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
                        "INNER JOIN AI_INF_INFORMES INF ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
                        "WHERE REC.ARERE_CODIGO = :arere_codigo " +
                        "  AND (REC.AREES_CODIGO IS NULL OR REC.AREES_CODIGO NOT IN (2, 4)) " +
                        "  AND DR.AREDR_ESTADO = 1 " +
                        "  AND RES.ARERS_ESTADO = 1 " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_REC_PLAN_ACCION PA " +
                        "      WHERE PA.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "        AND PA.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "        AND PA.ARPA_ESTADO = 'A' " +
                        "  ) " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_REC_VERIFICABLES VER " +
                        "      WHERE VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "        AND VER.AREVE_ESTADO = '1' " +
                        "        AND VER.AREEV_CODIGO = 2 " +
                        "  ) " +
                        "ORDER BY RES.ARERS_USUARIO_PER", nativeQuery = true)
        List<Tuple> obtenerPendientesPlanAccionPorRecomendacion(@Param("arere_codigo") Long arere_codigo);

        // CONSULTAR RESPONSABLES PENDIENTES DE PLAN DE ACCION CON PLAZO DE 48 HORAS VENCIDO (DIARIO)
        @Query(value = "SELECT " +
                        "INF.AININ_CODIGO AS aininCodigo, " +
                        "INF.AININ_TITULO AS aininTitulo, " +
                        "COALESCE(INF.AININ_MEMORANDO, INF.AININ_MEMO_APROBACION) AS aininMemo, " +
                        "RES.ARERS_NOMBRE AS arersNombre, " +
                        "RES.ARERS_APELLIDO AS arersApellido, " +
                        "RES.ARERS_CORREO AS arersCorreo, " +
                        "RES.ARERS_USUARIO_PER AS arersUsuarioPer, " +
                        "REC.ARERE_CODIGO AS arereCodigo, " +
                        "REC.ARERE_DESCRIPCION AS arereDescripcion, " +
                        "TO_CHAR(COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO AND S.ARERS_CODIGO = DR.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'YYYY-MM-DD') AS recFechaMax, " +
                        "COALESCE(GREATEST(1, TRUNC(SYSDATE) - TRUNC(CAST(INF.AININ_FECHA AS DATE) + 2)), 1) AS diasRetraso " +
                        "FROM AI_INF_INFORMES INF " +
                        "INNER JOIN AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
                        "INNER JOIN AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
                        "INNER JOIN AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
                        "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "INNER JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                        "WHERE INF.AININ_ESTADO = 1 " +
                        "  AND INF.AININ_NOTIFICADO = '1' " +
                        "  AND HALL.AREHA_ESTADO = 1 " +
                        "  AND CON.ARECO_ESTADO = 1 " +
                        "  AND REC.ARERE_ESTADO = 1 " +
                        "  AND (REC.AREES_CODIGO IS NULL OR REC.AREES_CODIGO NOT IN (2, 4)) " +
                        "  AND DR.AREDR_ESTADO = 1 " +
                        "  AND RES.ARERS_ESTADO = 1 " +
                        "  AND INF.AININ_FECHA IS NOT NULL " +
                        "  AND (CAST(SYSDATE AS DATE) - CAST(INF.AININ_FECHA AS DATE)) * 24 > 48 " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_REC_PLAN_ACCION PA " +
                        "      WHERE PA.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "        AND PA.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "        AND PA.ARPA_ESTADO = 'A' " +
                        "  ) " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_REC_VERIFICABLES VER " +
                        "      WHERE VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "        AND VER.AREVE_ESTADO = '1' " +
                        "        AND VER.AREEV_CODIGO = 2 " +
                        "  ) " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_NOTIFICACIONES N " +
                        "      WHERE N.ANOTIF_USUARIO_PER = RES.ARERS_USUARIO_PER " +
                        "        AND N.ANOTIF_TIPO = 'plan-accion-vencido' " +
                        "        AND N.ANOTIF_URL_SISTEMA LIKE '%' || INF.AININ_CODIGO || '%' " +
                        "        AND TRUNC(N.ANOTIF_FECHA) = TRUNC(SYSDATE) " +
                        "  ) " +
                        "ORDER BY INF.AININ_CODIGO, RES.ARERS_USUARIO_PER", nativeQuery = true)
        List<Tuple> obtenerPendientesPlanAccionVencidos();

        // CONSULTAR RESPONSABLES PENDIENTES DE PLAN DE ACCION VENCIDO POR INFORME (PARA PRUEBAS O MANUAL)
        @Query(value = "SELECT " +
                        "INF.AININ_CODIGO AS aininCodigo, " +
                        "INF.AININ_TITULO AS aininTitulo, " +
                        "COALESCE(INF.AININ_MEMORANDO, INF.AININ_MEMO_APROBACION) AS aininMemo, " +
                        "RES.ARERS_NOMBRE AS arersNombre, " +
                        "RES.ARERS_APELLIDO AS arersApellido, " +
                        "RES.ARERS_CORREO AS arersCorreo, " +
                        "RES.ARERS_USUARIO_PER AS arersUsuarioPer, " +
                        "REC.ARERE_CODIGO AS arereCodigo, " +
                        "REC.ARERE_DESCRIPCION AS arereDescripcion, " +
                        "TO_CHAR(COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO AND S.ARERS_CODIGO = DR.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'YYYY-MM-DD') AS recFechaMax, " +
                        "COALESCE(GREATEST(1, TRUNC(SYSDATE) - TRUNC(CAST(INF.AININ_FECHA AS DATE) + 2)), 1) AS diasRetraso " +
                        "FROM AI_INF_INFORMES INF " +
                        "INNER JOIN AI_REC_HALLAZGOS HALL ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
                        "INNER JOIN AI_REC_CONCLUSIONES CON ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
                        "INNER JOIN AI_REC_RECOMENDACIONES REC ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
                        "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "INNER JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                        "WHERE INF.AININ_CODIGO = :ainin_codigo " +
                        "  AND INF.AININ_ESTADO = 1 " +
                        "  AND HALL.AREHA_ESTADO = 1 " +
                        "  AND CON.ARECO_ESTADO = 1 " +
                        "  AND REC.ARERE_ESTADO = 1 " +
                        "  AND (REC.AREES_CODIGO IS NULL OR REC.AREES_CODIGO NOT IN (2, 4)) " +
                        "  AND DR.AREDR_ESTADO = 1 " +
                        "  AND RES.ARERS_ESTADO = 1 " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_REC_PLAN_ACCION PA " +
                        "      WHERE PA.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "        AND PA.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "        AND PA.ARPA_ESTADO = 'A' " +
                        "  ) " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_REC_VERIFICABLES VER " +
                        "      WHERE VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "        AND VER.AREVE_ESTADO = '1' " +
                        "        AND VER.AREEV_CODIGO = 2 " +
                        "  ) " +
                        "ORDER BY RES.ARERS_USUARIO_PER", nativeQuery = true)
        List<Tuple> obtenerPendientesPlanAccionVencidosPorInforme(@Param("ainin_codigo") Long ainin_codigo);

        // CONSULTAR RESPONSABLES PENDIENTES DE PLAN DE ACCION VENCIDO POR RECOMENDACION (PARA PRUEBAS O MANUAL)
        @Query(value = "SELECT " +
                        "INF.AININ_CODIGO AS aininCodigo, " +
                        "INF.AININ_TITULO AS aininTitulo, " +
                        "COALESCE(INF.AININ_MEMORANDO, INF.AININ_MEMO_APROBACION) AS aininMemo, " +
                        "RES.ARERS_NOMBRE AS arersNombre, " +
                        "RES.ARERS_APELLIDO AS arersApellido, " +
                        "RES.ARERS_CORREO AS arersCorreo, " +
                        "RES.ARERS_USUARIO_PER AS arersUsuarioPer, " +
                        "REC.ARERE_CODIGO AS arereCodigo, " +
                        "REC.ARERE_DESCRIPCION AS arereDescripcion, " +
                        "TO_CHAR(COALESCE((SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = DR.ARERE_CODIGO AND S.ARERS_CODIGO = DR.ARERS_CODIGO AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'), DR.AREDR_FECHA_MAXIMA, REC.ARERE_FECHA_MAXIMA), 'YYYY-MM-DD') AS recFechaMax, " +
                        "COALESCE(GREATEST(1, TRUNC(SYSDATE) - TRUNC(CAST(INF.AININ_FECHA AS DATE) + 2)), 1) AS diasRetraso " +
                        "FROM AI_REC_RECOMENDACIONES REC " +
                        "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "INNER JOIN AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
                        "INNER JOIN AI_REC_CONCLUSIONES CON ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
                        "INNER JOIN AI_REC_HALLAZGOS HALL ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
                        "INNER JOIN AI_INF_INFORMES INF ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
                        "WHERE REC.ARERE_CODIGO = :arere_codigo " +
                        "  AND (REC.AREES_CODIGO IS NULL OR REC.AREES_CODIGO NOT IN (2, 4)) " +
                        "  AND DR.AREDR_ESTADO = 1 " +
                        "  AND RES.ARERS_ESTADO = 1 " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_REC_PLAN_ACCION PA " +
                        "      WHERE PA.ARERE_CODIGO = REC.ARERE_CODIGO " +
                        "        AND PA.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "        AND PA.ARPA_ESTADO = 'A' " +
                        "  ) " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM AI_REC_VERIFICABLES VER " +
                        "      WHERE VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
                        "        AND VER.AREVE_ESTADO = '1' " +
                        "        AND VER.AREEV_CODIGO = 2 " +
                        "  ) " +
                        "ORDER BY RES.ARERS_USUARIO_PER", nativeQuery = true)
        List<Tuple> obtenerPendientesPlanAccionVencidosPorRecomendacion(@Param("arere_codigo") Long arere_codigo);
}
