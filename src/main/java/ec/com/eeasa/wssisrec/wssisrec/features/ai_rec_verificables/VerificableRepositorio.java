package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.models.Verificable;

@Repository
public interface VerificableRepositorio extends JpaRepository<Verificable, Long> {

  @Query(value = "SELECT * FROM ROOTSISREC.AI_REC_VERIFICABLES WHERE AREDR_CODIGO = :aredr_codigo", nativeQuery = true)
  List<Verificable> findByDetalleRecomendacion(@Param("aredr_codigo") Long aredr_codigo);

  @Query(value = "SELECT * FROM ROOTSISREC.AI_REC_VERIFICABLES " +
         "WHERE AREDR_CODIGO = :aredr_codigo " +
         "ORDER BY CASE " +
         "  WHEN AREEV_CODIGO = 2 THEN 1 " +
         "  WHEN AREEV_CODIGO = 1 THEN 2 " +
         "  WHEN AREEV_CODIGO = 3 THEN 3 " +
         "  ELSE 4 END ASC, " +
         "AREVE_FECHA_CARGA DESC NULLS LAST, AREVE_CODIGO DESC " +
         "FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
  Optional<Verificable> findUltimoVerificablePorDetalleRecomendacion(@Param("aredr_codigo") Long aredr_codigo);

  @Query(value = "SELECT ev.AREEV_DESCRIPCION FROM ROOTSISREC.AI_REC_ESTADO_VERIFICABLE ev " +
         "WHERE ev.AREEV_CODIGO IN (SELECT v.AREEV_CODIGO FROM ROOTSISREC.AI_REC_VERIFICABLES v " +
         "WHERE v.AREDR_CODIGO = :aredr_codigo " +
         "ORDER BY CASE " +
         "  WHEN v.AREEV_CODIGO = 2 THEN 1 " +
         "  WHEN v.AREEV_CODIGO = 1 THEN 2 " +
         "  WHEN v.AREEV_CODIGO = 3 THEN 3 " +
         "  ELSE 4 END ASC, " +
         "v.AREVE_FECHA_CARGA DESC NULLS LAST, v.AREVE_CODIGO DESC " +
         "FETCH FIRST 1 ROWS ONLY)", nativeQuery = true)
  Optional<String> findUltimaDescripcionEstadoPorDetalleRecomendacion(@Param("aredr_codigo") Long aredr_codigo);

  @Query(value = "SELECT " +
         "INF.AININ_USUARIO AS auditor " +
         "FROM " +
         "AI_INF_INFORMES INF " +
         "JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
         "JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
         "JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
         "WHERE " +
         "REC.ARERE_ESTADO = 1 " +
         "AND " +
         "REC.ARERE_CODIGO = :arere_codigo AND ROWNUM = 1", nativeQuery = true)
  Optional<String> findAuditorByRecomendacion(@Param("arere_codigo") Long arere_codigo);

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
         "REC.ARERE_CODIGO = :arere_codigo AND ROWNUM = 1", nativeQuery = true)
  Optional<String> findMemorandoByRecomendacion(@Param("arere_codigo") Long arere_codigo);

  @Query(value = "SELECT " +
         "INF.AININ_MEMORANDO, INF.AININ_TITULO " +
         "FROM AI_INF_INFORMES INF " +
         "JOIN AI_REC_HALLAZGOS HALL ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
         "JOIN AI_REC_CONCLUSIONES CON ON HALL.AREHA_CODIGO = CON.AREHA_CODIGO " +
         "JOIN AI_REC_RECOMENDACIONES REC ON CON.ARECO_CODIGO = REC.ARECO_CODIGO " +
         "WHERE REC.ARERE_CODIGO = :arere_codigo AND ROWNUM = 1", nativeQuery = true)
  Optional<Object[]> findInformeInfoByRecomendacion(@Param("arere_codigo") Long arere_codigo);

  @Query(value = "SELECT " +
         "RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO AS nombre " +
         "FROM " +
         "AI_REC_RESPONSABLES RES " +
         "WHERE " +
         "RES.ARERS_CODIGO_PER = :arers_codigo_per AND ROWNUM = 1", nativeQuery = true)
  Optional<String> findNombreResponsable(@Param("arers_codigo_per") String arers_codigo_per);

  @Query(value = "SELECT " +
         "RES.ARERS_USUARIO_PER AS responsable " +
         "FROM " +
         "AI_REC_VERIFICABLES VER " +
         "JOIN AI_REC_DETALLE_RECOMENDACION DR ON VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
         "JOIN AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO " +
         "WHERE " +
         "VER.AREVE_CODIGO = :areve_codigo " +
         "AND " +
         "VER.AREVE_ESTADO = 1 AND ROWNUM = 1", nativeQuery = true)
  Optional<String> findResponsableByVerificable(@Param("areve_codigo") Long areve_codigo);

  @Query(value = "SELECT " +
         "RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO AS nombre " +
         "FROM " +
         "AI_REC_VERIFICABLES VER " +
         "JOIN AI_REC_DETALLE_RECOMENDACION DR ON VER.AREDR_CODIGO = DR.AREDR_CODIGO " +
         "JOIN AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO " +
         "WHERE " +
         "VER.AREVE_CODIGO = :areve_codigo " +
         "AND " +
         "VER.AREVE_ESTADO = 1 AND ROWNUM = 1", nativeQuery = true)
  Optional<String> findNombreResponsableByVerificable(@Param("areve_codigo") Long areve_codigo);


  @Query(value="SELECT verificarEstadoRecomendacionPorVerificable(:in_areve_codigo) FROM DUAL", nativeQuery = true)
  Optional<String> verificarEstadoRecomendacionPorVerificable(@Param("in_areve_codigo") Long in_areve_codigo);

  @Query(value = "SELECT " +
         "REC.ARERE_NUM_REC AS num_rec " +
         "FROM " +
         "AI_REC_RECOMENDACIONES REC " +
         "WHERE " +
         "REC.ARERE_ESTADO = 1 " +
         "AND " +
         "REC.ARERE_CODIGO = :arere_codigo AND ROWNUM = 1", nativeQuery = true)
  Optional<String> findNumRecByRecomendacion(@Param("arere_codigo") Long arere_codigo);

  @Query(value = "SELECT " +
         "INF.AININ_MEMORANDO AS memorando, " +
         "REC.ARERE_NUM_REC AS num_rec, " +
         "VERIFI.AREVE_OBSERVACION AS observacion " +
         "FROM AI_INF_INFORMES INF " +
         "JOIN AI_REC_HALLAZGOS HALL " +
         "    ON INF.AININ_CODIGO = HALL.AININ_CODIGO " +
         "JOIN AI_REC_CONCLUSIONES CO " +
         "    ON HALL.AREHA_CODIGO = CO.AREHA_CODIGO " +
         "JOIN AI_REC_RECOMENDACIONES REC " +
         "    ON REC.ARECO_CODIGO = CO.ARECO_CODIGO " +
         "JOIN AI_REC_DETALLE_RECOMENDACION DR " +
         "    ON REC.ARERE_CODIGO = DR.ARERE_CODIGO " +
         "JOIN AI_REC_RESPONSABLES RESP " +
         "    ON RESP.ARERS_CODIGO = DR.ARERS_CODIGO " +
         "JOIN AI_REC_VERIFICABLES VERIFI " +
         "    ON VERIFI.AREDR_CODIGO = DR.AREDR_CODIGO " +
         "WHERE VERIFI.AREVE_ESTADO = 1 AND VERIFI.AREVE_CODIGO = :areve_codigo AND ROWNUM = 1", nativeQuery = true)
  Optional<Object[]> findMemorando_NumRec_ObservacionByVerificable(@Param("areve_codigo") Long areve_codigo);
}