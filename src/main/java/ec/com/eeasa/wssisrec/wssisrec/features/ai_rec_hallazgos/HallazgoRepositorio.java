package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models.Informe;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.models.Hallazgo;

public interface HallazgoRepositorio extends JpaRepository<Hallazgo, Long> {

  List<Hallazgo> findByInforme(Informe informe);

  @Query(value = "SELECT H.* FROM AI_REC_HALLAZGOS H " +
      "WHERE H.AININ_CODIGO = :aininCodigo " +
      "AND H.AREHA_ESTADO = '1' " +
      "AND EXISTS (SELECT 1 FROM AI_REC_CONCLUSIONES C " +
      "  INNER JOIN AI_REC_RECOMENDACIONES R ON R.ARECO_CODIGO = C.ARECO_CODIGO " +
      "  WHERE C.AREHA_CODIGO = H.AREHA_CODIGO " +
      "  AND C.ARECO_ESTADO = '1' " +
      "  AND R.ARERE_ESTADO = '1' " +
      "  AND R.ARERE_NUM_REC = :numRecomendacion)", nativeQuery = true)
  List<Hallazgo> findByInformeAndRecomendacionNumero(@Param("aininCodigo") Long aininCodigo, @Param("numRecomendacion") Integer numRecomendacion);

  @Query(value = "SELECT H.* FROM AI_REC_HALLAZGOS H " +
      "WHERE H.AININ_CODIGO = :aininCodigo " +
      "AND H.AREHA_ESTADO = '1' " +
      "AND EXISTS (SELECT 1 FROM AI_REC_CONCLUSIONES C " +
      "  INNER JOIN AI_REC_RECOMENDACIONES R ON R.ARECO_CODIGO = C.ARECO_CODIGO " +
      "  WHERE C.AREHA_CODIGO = H.AREHA_CODIGO " +
      "  AND C.ARECO_ESTADO = '1' " +
      "  AND R.ARERE_ESTADO = '1' " +
      "  AND UPPER(R.ARERE_DESCRIPCION) LIKE UPPER(:terminoPatron))", nativeQuery = true)
  List<Hallazgo> findByInformeAndRecomendacionDescripcion(@Param("aininCodigo") Long aininCodigo, @Param("terminoPatron") String terminoPatron);

  @Query(value = "SELECT AININ_ESTADO_SEG FROM AI_INF_INFORMES WHERE AININ_CODIGO = :ainin_codigo", nativeQuery = true)
  Character findStateInforme(Long ainin_codigo);

  @Query(value = "SELECT AREHA_ESTADO_SEG FROM AI_REC_HALLAZGOS WHERE AREHA_CODIGO = :areha_codigo", nativeQuery = true)
  Character findStateSegHallazgo(Long areha_codigo);

  @Query(value = "SELECT COUNT(*) FROM AI_REC_CONCLUSIONES  WHERE AREHA_CODIGO = :areha_codigo", nativeQuery = true)
  Integer countConclusiones(Long areha_codigo);

}
