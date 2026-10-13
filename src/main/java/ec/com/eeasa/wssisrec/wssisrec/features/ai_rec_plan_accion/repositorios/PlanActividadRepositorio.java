package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.models.PlanActividad;

@Repository
public interface PlanActividadRepositorio extends JpaRepository<PlanActividad, Long> {

  @Query("SELECT a FROM PlanActividad a WHERE a.planAccion.arpa_codigo = :arpaCodigo AND a.arpac_estado = 'A' ORDER BY a.arpac_num_actividad ASC")
  List<PlanActividad> findByPlanAccion(@Param("arpaCodigo") Long arpaCodigo);

  @Modifying
  @Query("UPDATE PlanActividad a SET a.arpac_estado = 'I' WHERE a.planAccion.arpa_codigo = :arpaCodigo")
  void desactivarActividadesPorPlan(@Param("arpaCodigo") Long arpaCodigo);
}
