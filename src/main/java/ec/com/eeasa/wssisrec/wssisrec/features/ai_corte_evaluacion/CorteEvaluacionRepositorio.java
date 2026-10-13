package ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.models.CorteEvaluacion;

@Repository
public interface CorteEvaluacionRepositorio extends JpaRepository<CorteEvaluacion, Long> {

    @Query(value = "SELECT * FROM AI_CORTE_EVALUACION WHERE ARCEV_ESTADO = 1 ORDER BY ARCEV_ANIO DESC, ARCEV_FECHA_CORTE DESC", nativeQuery = true)
    List<CorteEvaluacion> findAllActivos();

    @Query(value = "SELECT * FROM AI_CORTE_EVALUACION WHERE ARCEV_ESTADO = 1 AND ARCEV_ANIO = :anio ORDER BY ARCEV_FECHA_CORTE ASC", nativeQuery = true)
    List<CorteEvaluacion> findByAnio(@Param("anio") Integer anio);
}
