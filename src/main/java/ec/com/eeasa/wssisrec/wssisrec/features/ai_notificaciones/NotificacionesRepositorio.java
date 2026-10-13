package ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones;

import java.util.List;

import javax.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.models.Notificaciones;


@Repository
public interface NotificacionesRepositorio extends JpaRepository<Notificaciones, Long>{
    @Query(value = "SELECT * FROM AI_NOTIFICACIONES NOTI WHERE NOTI.ANOTIF_USUARIO_PER = :anotif_usuario_per ORDER BY NOTI.ANOTIF_FECHA_CREACION DESC FETCH FIRST :limite ROWS ONLY", nativeQuery = true)
    List<Tuple> obtenerNotificacionesPorUsuarioConLimite(@Param("anotif_usuario_per") String anotif_usuario_per, @Param("limite") int limite);

    @Modifying
    @Query(value = "UPDATE AI_NOTIFICACIONES NOTIF " +
        "SET NOTIF.ANOTIF_ESTADO = 1, NOTIF.ANOTIF_FECHA_LEIDA = SYSDATE, " +
        "NOTIF.ANOTIF_TITULO = :nuevoTitulo, " +
        "NOTIF.ANOTIF_MENSAJE = :nuevoMensaje || CASE WHEN NOTIF.ANOTIF_MENSAJE LIKE '%(auditor asignado)(%' THEN SUBSTR(NOTIF.ANOTIF_MENSAJE, INSTR(NOTIF.ANOTIF_MENSAJE, ' (auditor asignado)(')) ELSE '' END " +
        "WHERE ((NOTIF.ANOTIF_ARERE_CODIGO IS NOT NULL AND :arereCodigo IS NOT NULL AND NOTIF.ANOTIF_ARERE_CODIGO = :arereCodigo) " +
        "    OR (UPPER(NOTIF.ANOTIF_MENSAJE) LIKE '%' || UPPER(:memorando) || '%' " +
        "         AND (UPPER(NOTIF.ANOTIF_MENSAJE) LIKE '%RECOMENDAC%' || TO_CHAR(:numRec) || '%' " +
        "              OR UPPER(NOTIF.ANOTIF_MENSAJE) LIKE '%REC%' || TO_CHAR(:numRec) || '%'))) " +
        "AND (NOTIF.ANOTIF_TIPO = 'verificable-pendiente' " +
        "     OR UPPER(NOTIF.ANOTIF_TITULO) LIKE '%VERIFICABLE%PENDIENTE%' " +
        "     OR UPPER(NOTIF.ANOTIF_TITULO) LIKE '%PENDIENTE%REVISIÓN%' " +
        "     OR UPPER(NOTIF.ANOTIF_TITULO) LIKE '%EN REVISIÓN%') " +
        "AND (NOTIF.ANOTIF_TIPO IS NULL OR NOT (LOWER(NOTIF.ANOTIF_TIPO) LIKE 'solicitud%')) " +
        "AND NOT (LOWER(NOTIF.ANOTIF_MENSAJE) LIKE 'solicitud%') " +
        "AND (:nombreResponsable IS NULL OR UPPER(NOTIF.ANOTIF_MENSAJE) LIKE '%' || UPPER(:nombreResponsable) || '%') " +
        "AND NOTIF.ANOTIF_USUARIO_PER IN (" +
        "    SELECT DISTINCT USU.ASERU_CUENTA FROM AI_SEG_ROL_USU USU WHERE USU.ASERO_CODIGO IN (2, 3) AND USU.ASERU_ESTADO = 1" +
        ")", nativeQuery = true)
    int actualizarNotificacionesPorArereCodigoConResultado(@Param("arereCodigo") Long arereCodigo,
        @Param("memorando") String memorando, @Param("numRec") Integer numRec,
        @Param("nuevoTitulo") String nuevoTitulo, @Param("nuevoMensaje") String nuevoMensaje,
        @Param("nombreResponsable") String nombreResponsable);

    @Query(value = "SELECT COUNT(NOTIF.ANOTIF_CODIGO) FROM AI_NOTIFICACIONES NOTIF " +
                   "WHERE NOTIF.ANOTIF_USUARIO_PER = :usuario " +
                   "  AND NOTIF.ANOTIF_TIPO = :tipo " +
                   "  AND NOTIF.ANOTIF_ESTADO = 0", nativeQuery = true)
    int existeNotificacionPendienteUsuarioTipo(@Param("usuario") String usuario, @Param("tipo") String tipo);

    @Modifying
    @Query(value = "UPDATE AI_NOTIFICACIONES NOTIF SET NOTIF.ANOTIF_ESTADO = 1, NOTIF.ANOTIF_FECHA_LEIDA = SYSDATE WHERE NOTIF.ANOTIF_CODIGO = :anotif_codigo", nativeQuery = true)
    void actualizarEstadoNotificaciones(@Param("anotif_codigo") Long anotif_codigo);

    @Modifying
    @Query(value = "UPDATE AI_NOTIFICACIONES JEF " +
            "SET JEF.ANOTIF_TITULO = CASE " +
            "        WHEN (LOWER(JEF.ANOTIF_TIPO) LIKE 'solicitud%' OR LOWER(JEF.ANOTIF_TITULO) LIKE 'solicitud%' OR LOWER(JEF.ANOTIF_MENSAJE) LIKE 'solicitud%') " +
            "        THEN 'Solicitud en revisión' " +
            "        ELSE 'Verificable en revisión' " +
            "    END, " +
            "    JEF.ANOTIF_MENSAJE = CASE WHEN NOT (JEF.ANOTIF_MENSAJE LIKE '%(en revisión)%') THEN JEF.ANOTIF_MENSAJE || ' (en revisión)' ELSE JEF.ANOTIF_MENSAJE END " +
            "WHERE JEF.ANOTIF_PADRE_ID = :anotif_codigo AND JEF.ANOTIF_ESTADO = 0 " +
            "AND NOT (UPPER(JEF.ANOTIF_TITULO) LIKE 'ATENDIDO%')", nativeQuery = true)
    int actualizarReplicaJefeEnRevision(@Param("anotif_codigo") Long anotif_codigo);

    @Query(value = "SELECT INF.AININ_CODIGO AS ainin_codigo, REC.ARERE_CODIGO AS arere_codigo, CON.ARECO_CODIGO AS areco_codigo, HALL.AREHA_CODIGO AS areha_codigo " +
            "FROM AI_REC_RECOMENDACIONES REC " +
            "JOIN AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
            "JOIN AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
            "JOIN AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
            "WHERE REC.ARERE_NUM_REC = :numRec " +
            "AND INF.AININ_MEMORANDO = :memorando " +
            "AND REC.ARERE_ESTADO = 1 " +
            "AND CON.ARECO_ESTADO = 1 " +
            "AND HALL.AREHA_ESTADO = 1 " +
            "AND INF.AININ_ESTADO = 1 " +
            "FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    Tuple obtenerIdsPorMemoYNumRec(@Param("memorando") String memorando, @Param("numRec") Integer numRec);

    @Query(value = "SELECT INF.AININ_CODIGO AS ainin_codigo, REC.ARERE_CODIGO AS arere_codigo, CON.ARECO_CODIGO AS areco_codigo, HALL.AREHA_CODIGO AS areha_codigo " +
            "FROM AI_REC_RECOMENDACIONES REC " +
            "JOIN AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
            "JOIN AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
            "JOIN AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
            "WHERE REC.ARERE_CODIGO = :arereCodigo " +
            "AND REC.ARERE_ESTADO = 1 " +
            "AND CON.ARECO_ESTADO = 1 " +
            "AND HALL.AREHA_ESTADO = 1 " +
            "AND INF.AININ_ESTADO = 1 " +
            "FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    Tuple obtenerCadenaIdsPorArereCodigo(@Param("arereCodigo") Long arereCodigo);

    @Modifying
    @Query(value = "UPDATE AI_NOTIFICACIONES NOTIF " +
            "SET NOTIF.ANOTIF_ESTADO = 1, NOTIF.ANOTIF_FECHA_LEIDA = SYSDATE, " +
            "NOTIF.ANOTIF_TITULO = 'Verificable no atendido', " +
            "NOTIF.ANOTIF_MENSAJE = NOTIF.ANOTIF_MENSAJE || ' — El verificable no fue atendido; sin embargo, la notificación ha cambiado a leída, por favor revise nuevamente.' " +
            "WHERE NOTIF.ANOTIF_ESTADO = 0 " +
            "AND NOTIF.ANOTIF_ARERE_CODIGO = :arereCodigo " +
            "AND (UPPER(NOTIF.ANOTIF_TITULO) LIKE '%VERIFICABLE%PENDIENTE%' " +
            "     OR UPPER(NOTIF.ANOTIF_TITULO) LIKE '%PENDIENTE%REVISIÓN%') " +
            "AND (NOTIF.ANOTIF_TIPO IS NULL OR NOT (LOWER(NOTIF.ANOTIF_TIPO) LIKE 'solicitud%')) " +
            "AND NOT (LOWER(NOTIF.ANOTIF_MENSAJE) LIKE 'solicitud%')", nativeQuery = true)
    int actualizarNotificacionesPendientesSinCambiosPorArereCodigo(@Param("arereCodigo") Long arereCodigo);

    @Modifying
    @Query(value = "UPDATE AI_NOTIFICACIONES NOTIF " +
            "SET NOTIF.ANOTIF_ESTADO = 1, NOTIF.ANOTIF_FECHA_LEIDA = SYSDATE " +
            "WHERE NOTIF.ANOTIF_ESTADO = 0 " +
            "AND NOTIF.ANOTIF_ARERE_CODIGO = :arereCodigo " +
            "AND (NOTIF.ANOTIF_TIPO IS NULL OR NOT (LOWER(NOTIF.ANOTIF_TIPO) LIKE 'solicitud%')) " +
            "AND NOT (LOWER(NOTIF.ANOTIF_MENSAJE) LIKE 'solicitud%')", nativeQuery = true)
    int actualizarEstadoNotificacionesPorArereCodigo(@Param("arereCodigo") Long arereCodigo);

    @Modifying
    @Query(value = "UPDATE AI_NOTIFICACIONES JEF " +
            "SET JEF.ANOTIF_ESTADO = 1, JEF.ANOTIF_FECHA_LEIDA = SYSDATE " +
            "WHERE JEF.ANOTIF_USUARIO_PER = :auditorJefe " +
            "AND JEF.ANOTIF_ESTADO = 0 " +
            "AND EXISTS (SELECT 1 FROM AI_NOTIFICACIONES OPE WHERE OPE.ANOTIF_CODIGO = JEF.ANOTIF_PADRE_ID AND OPE.ANOTIF_ESTADO = 1 AND UPPER(OPE.ANOTIF_TITULO) LIKE 'ATENDIDO%')", nativeQuery = true)
    int sincronizarNotificacionesJefePorPadreLeido(@Param("auditorJefe") String auditorJefe);

    @Modifying
    @Query(value = "UPDATE AI_NOTIFICACIONES NOTIF " +
            "SET NOTIF.ANOTIF_ESTADO = 1, NOTIF.ANOTIF_FECHA_LEIDA = SYSDATE, " +
            "    NOTIF.ANOTIF_TITULO = :nuevoTitulo, " +
            "    NOTIF.ANOTIF_MENSAJE = CASE " +
            "        WHEN NOTIF.ANOTIF_MENSAJE LIKE '%Atendido:%' THEN REPLACE(NOTIF.ANOTIF_MENSAJE, ' (en revisión)', '') " +
            "        WHEN NOTIF.ANOTIF_MENSAJE LIKE '%(auditor asignado)(%' THEN " +
            "            REPLACE(SUBSTR(NOTIF.ANOTIF_MENSAJE, 1, INSTR(NOTIF.ANOTIF_MENSAJE, ' (auditor asignado)(') - 1), ' (en revisión)', '') || " +
            "            ' - ' || :sufijoAtendido || ' ' || " +
            "            REPLACE(SUBSTR(NOTIF.ANOTIF_MENSAJE, INSTR(NOTIF.ANOTIF_MENSAJE, ' (auditor asignado)(')), ' (en revisión)', '') " +
            "        ELSE REPLACE(NOTIF.ANOTIF_MENSAJE, ' (en revisión)', '') || ' - ' || :sufijoAtendido " +
            "    END " +
            "WHERE ((:aresoCodigo IS NOT NULL AND (NOTIF.ANOTIF_MENSAJE LIKE '%Solicitud ' || TO_CHAR(:aresoCodigo) || ' %' " +
            "                                     OR NOTIF.ANOTIF_MENSAJE LIKE '%Solicitud ' || TO_CHAR(:aresoCodigo) || 'Rec:%' " +
            "                                     OR NOTIF.ANOTIF_MENSAJE LIKE '%Solicitud %' || TO_CHAR(:aresoCodigo) || '%' " +
            "                                     OR NOTIF.ANOTIF_MENSAJE LIKE '%Solicitud N° ' || TO_CHAR(:aresoCodigo) || '%' " +
            "                                     OR NOTIF.ANOTIF_MENSAJE LIKE '%N. Solicitud ' || TO_CHAR(:aresoCodigo) || '%')) " +
            "       OR (:aresoCodigo IS NULL AND NOTIF.ANOTIF_ARERE_CODIGO = :arereCodigo)) " +
            "  AND (NOTIF.ANOTIF_TIPO IN ('solicitud-preaprobada', 'solicitud-preaprobada-director', 'solicitud-nueva') " +
            "       OR UPPER(NOTIF.ANOTIF_TITULO) LIKE '%SOLICITUD%' " +
            "       OR LOWER(NOTIF.ANOTIF_MENSAJE) LIKE '%solicitud%')", nativeQuery = true)
    int actualizarNotificacionesSolicitudAtendida(@Param("arereCodigo") Long arereCodigo,
            @Param("aresoCodigo") Long aresoCodigo,
            @Param("nuevoTitulo") String nuevoTitulo,
            @Param("sufijoAtendido") String sufijoAtendido);
}
