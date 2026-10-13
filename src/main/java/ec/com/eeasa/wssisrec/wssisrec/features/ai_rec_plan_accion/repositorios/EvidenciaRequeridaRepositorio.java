package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.models.EvidenciaRequerida;

@Repository
public interface EvidenciaRequeridaRepositorio extends JpaRepository<EvidenciaRequerida, Long> {

  @Query("SELECT e FROM EvidenciaRequerida e WHERE e.recomendacion.arere_codigo = :arereCodigo AND e.arer_estado = 'A' ORDER BY e.arer_codigo ASC")
  List<EvidenciaRequerida> findByRecomendacion(@Param("arereCodigo") Long arereCodigo);

  @Query("SELECT e FROM EvidenciaRequerida e WHERE e.recomendacion.arere_codigo = :arereCodigo AND LOWER(TRIM(e.arer_descripcion)) = LOWER(TRIM(:descripcion)) AND e.arer_estado = 'A'")
  List<EvidenciaRequerida> findByRecomendacionAndDescripcion(@Param("arereCodigo") Long arereCodigo, @Param("descripcion") String descripcion);
}
