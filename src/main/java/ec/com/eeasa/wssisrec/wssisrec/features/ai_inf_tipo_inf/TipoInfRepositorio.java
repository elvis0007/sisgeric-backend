package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.models.TipoInforme;

public interface TipoInfRepositorio extends JpaRepository<TipoInforme, Long> {

  @Query(value = "SELECT * FROM ROOTSISREC.AI_INF_TIPO_INF WHERE AINTI_DESCRIPCION = :ainti_descripcion", nativeQuery = true)
  List<TipoInforme> findByAinti_descripcion(String ainti_descripcion);

}
