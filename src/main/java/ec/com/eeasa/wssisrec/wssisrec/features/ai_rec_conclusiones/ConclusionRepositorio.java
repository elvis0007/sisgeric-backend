  package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.models.Conclusion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.models.Hallazgo;

public interface ConclusionRepositorio extends JpaRepository<Conclusion, Long> {

  List<Conclusion> findByHallazgo(Hallazgo hallazgo);

  @Query(value = "SELECT COUNT(*) FROM AI_REC_CONCLUSIONES WHERE AREHA_CODIGO = :arehaCodigo AND ARECO_ESTADO = '1'", nativeQuery = true)
  Long countActivasByHallazgo(@Param("arehaCodigo") Long arehaCodigo);

  @Query(value = "SELECT C.* FROM AI_REC_CONCLUSIONES C " +
      "WHERE C.AREHA_CODIGO = :arehaCodigo " +
      "AND C.ARECO_ESTADO = '1' " +
      "AND EXISTS (SELECT 1 FROM AI_REC_RECOMENDACIONES R " +
      "  WHERE R.ARECO_CODIGO = C.ARECO_CODIGO " +
      "  AND R.ARERE_ESTADO = '1' " +
      "  AND R.ARERE_NUM_REC = :numRecomendacion)", nativeQuery = true)
  List<Conclusion> findByHallazgoAndRecomendacionNumero(@Param("arehaCodigo") Long arehaCodigo, @Param("numRecomendacion") Integer numRecomendacion);

  @Query(value = "SELECT C.* FROM AI_REC_CONCLUSIONES C " +
      "WHERE C.AREHA_CODIGO = :arehaCodigo " +
      "AND C.ARECO_ESTADO = '1' " +
      "AND EXISTS (SELECT 1 FROM AI_REC_RECOMENDACIONES R " +
      "  WHERE R.ARECO_CODIGO = C.ARECO_CODIGO " +
      "  AND R.ARERE_ESTADO = '1' " +
      "  AND UPPER(R.ARERE_DESCRIPCION) LIKE UPPER(:terminoPatron))", nativeQuery = true)
  List<Conclusion> findByHallazgoAndRecomendacionDescripcion(@Param("arehaCodigo") Long arehaCodigo, @Param("terminoPatron") String terminoPatron);

  @Query(value = "SELECT C.* FROM AI_REC_CONCLUSIONES C, AI_REC_RECOMENDACIONES R WHERE C.ARECO_CODIGO = R.ARECO_CODIGO AND R.ARERE_CODIGO = :arere_codigo", nativeQuery = true)
  Conclusion findByRecomendacion(@Param("arere_codigo") Long arere_codigo);
}
