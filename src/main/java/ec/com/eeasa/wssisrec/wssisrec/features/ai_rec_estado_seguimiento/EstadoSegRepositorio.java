package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.models.EstadoSeg;

public interface EstadoSegRepositorio extends JpaRepository<EstadoSeg, Long> {

  @Query(value = "SELECT * FROM AI_REC_ESTADO_SEGUIMIENTO WHERE AREES_DESCRIPCION = :arees_descripcion", nativeQuery = true)
  EstadoSeg findByDescripcion(String arees_descripcion);

  @Query(value = "SELECT * FROM AI_REC_ESTADO_SEGUIMIENTO WHERE AREES_ESTADO = '1'", nativeQuery = true)
  List<EstadoSeg> findByEstadoActivo();

}
