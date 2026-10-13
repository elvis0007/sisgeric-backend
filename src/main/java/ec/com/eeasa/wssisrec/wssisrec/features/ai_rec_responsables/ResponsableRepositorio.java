package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models.Responsable;

@Repository
public interface ResponsableRepositorio extends JpaRepository<Responsable, Long> {

  @Query(value = "SELECT R.* FROM AI_REC_RESPONSABLES R WHERE R.ARERS_CODIGO IN (SELECT D.ARERS_CODIGO FROM AI_REC_DETALLE_RECOMENDACION D WHERE D.ARERE_CODIGO IN(SELECT E.ARERE_CODIGO FROM AI_REC_RECOMENDACIONES E WHERE E.ARERE_CODIGO = :arere_codigo))", nativeQuery = true)
  List<Responsable> findByRecomendacion(Long arere_codigo);

  @Query(value = " SELECT R.ARERS_DEPARTAMENTO FROM AI_REC_RESPONSABLES R WHERE R.ARERS_ESTADO = 1", nativeQuery = true)
  List<String> getDepartamentos();

  @Query(value = "SELECT * FROM ("
      + " SELECT R.* FROM AI_REC_RESPONSABLES R"
      + " WHERE R.ARERS_CODIGO_PER = :codigo_per"
      + " AND R.ARERS_ESTADO = '1'"
      + " ORDER BY R.ARERS_FECHA DESC"
      + ") WHERE ROWNUM = 1", nativeQuery = true)
  Optional<Responsable> findTopByCodigoPer(String codigo_per);
}
