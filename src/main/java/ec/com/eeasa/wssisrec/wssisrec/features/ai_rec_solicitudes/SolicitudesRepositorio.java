package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.models.Solicitud;

@Repository
public interface SolicitudesRepositorio extends JpaRepository<Solicitud, Long> {
  @Procedure(name = "SP_SOLICITUDES_DEPARTAMENTAL")
  List<Solicitud> findByFilterDir(
      @Param("ARERS_DEPARTAMENTO") String ARERS_DEPARTAMENTO,
      @Param("ARERS_CODIGO_PER") String ARERS_CODIGO_PER,
      @Param("ANIO") int ANIO,
      @Param("ARESO_ESTADO_APROB") String ARESO_ESTADO_APROB);

  @Procedure(name = "SP_SOLICITUDES_DEPARTAMENTAL_AUD")
  List<Solicitud> findByFilterAud(
      @Param("ARERS_DEPARTAMENTO") String ARERS_DEPARTAMENTO,
      @Param("AININ_USUARIO") String AININ_USUARIO,
      @Param("ANIO") int ANIO,
      @Param("ARESO_ESTADO_APROB") String ARESO_ESTADO_APROB);

    @Query(value = "SELECT S.* FROM AI_REC_SOLICITUDES S " +
                   "JOIN AI_REC_RESPONSABLES RS ON RS.ARERS_CODIGO = S.ARERS_CODIGO " +
                   "WHERE S.ARERE_CODIGO = :arere_codigo " +
                   "AND S.ARESO_ESTADO = '1' " +
                   "AND RS.ARERS_CODIGO_PER = :arers_codigo_per " +
                   "ORDER BY S.ARESO_FECHA_CARGA DESC", nativeQuery = true)
    List<Solicitud> findByRecomendacionResponsablePer(@Param("arere_codigo") Long arere_codigo,
    @Param("arers_codigo_per") String arers_codigo_per);

    @Query(value = "SELECT S.* FROM AI_REC_SOLICITUDES S WHERE S.ARERE_CODIGO = :arere_codigo AND S.ARESO_ESTADO = '1'", nativeQuery = true)
    List<Solicitud> findByRecomendacion(@Param("arere_codigo") Long arere_codigo);

    @Query(value = "SELECT FN_DIRECTOR_RESPONSABLE(:arers_codigo) FROM DUAL", nativeQuery = true)
    String findDirectorByDepartamentoResponsable(@Param("arers_codigo") Long arers_codigo);
    

    @Query(value = "SELECT " +
         "RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO AS nombre " +
         "FROM " +
         "AI_REC_RESPONSABLES RES " +
         "WHERE " +
         "RES.ARERS_CODIGO = :arers_codigo_per", nativeQuery = true)
    String findResponsable(@Param("arers_codigo_per") Long arers_codigo_per);

    @Query(value = "SELECT AI_REC_SOLICITUD_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
    Long obtenerProximoNumeroSolicitud();

    @Query(value = "SELECT " +
         "DISTINCT TRIM(USU.ASERU_NOMBRE || ' ' || USU.ASERU_APELLIDO) AS auditor " +
         "FROM " +
         "AI_INF_INFORMES INF " +
         "JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
         "JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
         "JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
         "JOIN AI_SEG_ROL_USU USU ON USU.ASERU_CUENTA = INF.AININ_USUARIO " +
         "WHERE " +
         "REC.ARERE_ESTADO = 1 " +
         "AND " +
         "REC.ARERE_CODIGO = :arere_codigo", nativeQuery = true)
    Optional<String> findAuditorByRecomendacion(@Param("arere_codigo") Long arere_codigo);

    @Query(value = "SELECT DISTINCT TRIM(INF.AININ_USUARIO) FROM AI_INF_INFORMES INF " +
         "JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
         "JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
         "JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
         "WHERE REC.ARERE_ESTADO = 1 AND REC.ARERE_CODIGO = :arere_codigo", nativeQuery = true)
    Optional<String> findAuditorUsuarioByRecomendacion(@Param("arere_codigo") Long arere_codigo);

    @Query(value = "SELECT " +
         "DISTINCT TRIM(USU.ASERU_NOMBRE || ' ' || USU.ASERU_APELLIDO) AS nombre " +
         "FROM " +
         "AI_SEG_ROL_USU USU " +
         "WHERE " +
         "(UPPER(USU.ASERU_DEPARTAMENTO) = UPPER(:departamento) OR UPPER(USU.ASERU_DEPARTAMENTO) LIKE UPPER('%' || :departamento || '%')) " +
         "AND USU.ASERO_CODIGO IN (5,2) " +
         "AND USU.ASERU_ESTADO = '1' " +
         "AND USU.ASERU_NOMBRE IS NOT NULL " +
         "ORDER BY CASE WHEN USU.ASERO_CODIGO = 5 THEN 1 WHEN USU.ASERO_CODIGO = 2 THEN 2 ELSE 3 END " +
         "FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    Optional<String> findDirectorByDepartamento(@Param("departamento") String departamento);

    @Query(value = "SELECT " +
         "TRIM(USU.ASERU_DEPARTAMENTO) AS departamento " +
         "FROM " +
         "AI_SEG_ROL_USU USU " +
         "WHERE " +
         "USU.ASERU_CODIGO_PER = :codigo_per " +
         "AND USU.ASERU_ESTADO = '1' " +
         "FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    Optional<String> findDepartamentoByCodigoPer(@Param("codigo_per") String codigo_per);

    @Query(value = "SELECT " +
         "TRIM(RES.ARERS_CARGO) AS cargo " +
         "FROM " +
         "AI_REC_RESPONSABLES RES " +
         "WHERE " +
         "RES.ARERS_CODIGO_PER = :codigo_per " +
         "AND RES.ARERS_ESTADO = 1 " +
         "ORDER BY RES.ARERS_FECHA DESC " +
         "FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    Optional<String> findCargoByCodigoPer(@Param("codigo_per") String codigo_per);

    @Query(value = "SELECT " +
         "INF.AININ_MEMORANDO AS memorando " +
         "FROM " +
         "AI_INF_INFORMES INF " +
         "JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
         "JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
         "JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
         "WHERE " +
         "REC.ARERE_ESTADO = 1 " +
         "AND " +
         "REC.ARERE_CODIGO = :arere_codigo", nativeQuery = true)
    Optional<String> findMemorandoByRecomendacion(@Param("arere_codigo") Long arere_codigo);

    @Query(value = "SELECT " +
         "REC.ARERE_NUM_REC AS num_rec " +
         "FROM " +
         "AI_REC_RECOMENDACIONES REC " +
         "WHERE " +
         "REC.ARERE_ESTADO = 1 " +
         "AND " +
         "REC.ARERE_CODIGO = :arere_codigo", nativeQuery = true)
    Optional<String> findNumRecByRecomendacion(@Param("arere_codigo") Long arere_codigo);

    @Query(value = "SELECT S.* FROM AI_REC_SOLICITUDES S " +
                   "WHERE S.ARESO_ESTADO = '1' " +
                   "AND S.ARESO_ESTADO_APROB IN ('1', '2') " +
                   "AND TRUNC(S.ARESO_FECHA_MAX_NUE) < TRUNC(CURRENT_TIMESTAMP)", nativeQuery = true)
    List<Solicitud> findSolicitudesPendientesCaducadas();

    @Query(value = "SELECT S.* FROM AI_REC_SOLICITUDES S " +
                   "WHERE S.ARERE_CODIGO = :arere_codigo " +
                   "AND S.ARESO_ESTADO = '1' " +
                   "AND S.ARESO_ESTADO_APROB IN ('1', '2') " +
                   "AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP) " +
                   "ORDER BY S.ARESO_FECHA_CARGA DESC FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    Optional<Solicitud> findSolicitudPendienteVigenteByRecomendacion(@Param("arere_codigo") Long arere_codigo);

    @Query(value = "SELECT S.* FROM AI_REC_SOLICITUDES S " +
                   "JOIN AI_REC_RESPONSABLES R ON S.ARERS_CODIGO = R.ARERS_CODIGO " +
                   "WHERE S.ARERE_CODIGO = :arere_codigo " +
                   "AND R.ARERS_CODIGO_PER = :arers_codigo_per " +
                   "AND S.ARESO_ESTADO = '1' " +
                   "AND S.ARESO_ESTADO_APROB IN ('1', '2') " +
                   "AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP) " +
                   "ORDER BY S.ARESO_FECHA_CARGA DESC FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    Optional<Solicitud> findSolicitudPendienteVigenteByRecomendacionYResponsablePer(@Param("arere_codigo") Long arere_codigo, @Param("arers_codigo_per") String arers_codigo_per);

    @Query(value = "SELECT COUNT(S.ARESO_CODIGO) FROM AI_REC_SOLICITUDES S " +
                   "WHERE S.ARERE_CODIGO = :arere_codigo " +
                   "AND S.ARESO_CODIGO <> :areso_codigo " +
                   "AND S.ARESO_ESTADO = '1' " +
                   "AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))", nativeQuery = true)
    long countSolicitudesPosterioresVigentes(@Param("arere_codigo") Long arere_codigo, @Param("areso_codigo") Long areso_codigo);

    @Query(value = "SELECT COUNT(S.ARESO_CODIGO) FROM AI_REC_SOLICITUDES S " +
                   "JOIN AI_REC_RESPONSABLES R ON S.ARERS_CODIGO = R.ARERS_CODIGO " +
                   "WHERE S.ARERE_CODIGO = :arere_codigo " +
                   "AND R.ARERS_CODIGO_PER = :arers_codigo_per " +
                   "AND S.ARESO_CODIGO <> :areso_codigo " +
                   "AND S.ARESO_ESTADO = '1' " +
                   "AND (S.ARESO_ESTADO_APROB = '3' OR (S.ARESO_ESTADO_APROB IN ('1', '2') AND TRUNC(S.ARESO_FECHA_MAX_NUE) >= TRUNC(CURRENT_TIMESTAMP)))", nativeQuery = true)
    long countSolicitudesPosterioresVigentesPorResponsable(@Param("arere_codigo") Long arere_codigo, @Param("arers_codigo_per") String arers_codigo_per, @Param("areso_codigo") Long areso_codigo);

    @Query(value = "SELECT COUNT(V.AREVE_CODIGO) " +
                   "FROM AI_REC_VERIFICABLES V " +
                   "JOIN AI_REC_DETALLE_RECOMENDACION DR ON V.AREDR_CODIGO = DR.AREDR_CODIGO " +
                   "WHERE DR.ARERE_CODIGO = :arere_codigo " +
                   "AND DR.ARERS_CODIGO = :arers_codigo " +
                   "AND V.AREEV_CODIGO = 2 " +
                   "AND V.AREVE_ESTADO = '1'", nativeQuery = true)
    long countVerificablesJustificadosPorResponsable(@Param("arere_codigo") Long arere_codigo, @Param("arers_codigo") Long arers_codigo);
}


