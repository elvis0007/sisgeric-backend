package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.repositorios;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.models.EvidenciaCotejo;

@Repository
public interface EvidenciaCotejoRepositorio extends JpaRepository<EvidenciaCotejo, Long> {

  @Query("SELECT c FROM EvidenciaCotejo c WHERE c.detalleRecomendacion.aredr_codigo = :aredrCodigo AND c.arec_estado = 'A'")
  List<EvidenciaCotejo> findByDetalleResponsable(@Param("aredrCodigo") Long aredrCodigo);

  @Query("SELECT c FROM EvidenciaCotejo c WHERE c.evidenciaRequerida.arer_codigo = :arerCodigo AND c.detalleRecomendacion.aredr_codigo = :aredrCodigo AND c.arec_estado = 'A'")
  Optional<EvidenciaCotejo> findByEvidenciaAndResponsable(@Param("arerCodigo") Long arerCodigo, @Param("aredrCodigo") Long aredrCodigo);
}
