package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;

@Repository
public interface DetalleRepositorio extends JpaRepository<DetalleRecomendacion, Long> {

  @Query(value = "SELECT * FROM AI_REC_DETALLE_RECOMENDACION WHERE  ARERE_CODIGO = :arere_codigo", nativeQuery = true)
  List<DetalleRecomendacion> findByRecomendacion(Long arere_codigo);

  @Query(value = "SELECT * FROM AI_REC_DETALLE_RECOMENDACION WHERE ARERE_CODIGO = :arere_codigo AND ARERS_CODIGO = :arers_codigo", nativeQuery = true)
  DetalleRecomendacion findByRecomendacionResponsable(Long arere_codigo, Long arers_codigo);

  @Query(value = "SELECT * FROM AI_REC_DETALLE_RECOMENDACION  D JOIN  AI_REC_RESPONSABLES RS  ON D.ARERS_CODIGO=  RS.ARERS_CODIGO JOIN  AI_REC_RECOMENDACIONES R  ON D.ARERE_CODIGO=  R.ARERE_CODIGO WHERE R.ARERE_CODIGO= :arere_codigo AND RS.ARERS_CODIGO_PER=:arers_codigo_per AND D.AREDR_ESTADO = '1'", nativeQuery = true)
  DetalleRecomendacion findByRecomendacionResponsablePer(Long arere_codigo, String arers_codigo_per);

  @Query(value = "SELECT D.* FROM AI_REC_DETALLE_RECOMENDACION D " +
      "JOIN AI_REC_VERIFICABLES V ON D.AREDR_CODIGO = V.AREDR_CODIGO " +
      "WHERE V.AREVE_CODIGO = :areve_codigo AND ROWNUM = 1", nativeQuery = true)
  DetalleRecomendacion findByVerificable(@Param("areve_codigo") Long areve_codigo);

}
