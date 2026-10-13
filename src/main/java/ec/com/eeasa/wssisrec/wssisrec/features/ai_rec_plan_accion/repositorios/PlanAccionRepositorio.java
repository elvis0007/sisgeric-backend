package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.repositorios;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.models.PlanAccion;

@Repository
public interface PlanAccionRepositorio extends JpaRepository<PlanAccion, Long> {

  @Query("SELECT p FROM PlanAccion p WHERE p.recomendacion.arere_codigo = :arereCodigo AND p.detalleRecomendacion.aredr_codigo = :aredrCodigo AND p.arpa_estado = 'A'")
  Optional<PlanAccion> findByRecomendacionAndDetalle(@Param("arereCodigo") Long arereCodigo, @Param("aredrCodigo") Long aredrCodigo);

  @Query("SELECT p FROM PlanAccion p WHERE p.recomendacion.arere_codigo = :arereCodigo AND p.arpa_estado = 'A'")
  List<PlanAccion> findByRecomendacion(@Param("arereCodigo") Long arereCodigo);
}
