package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.models.EstadoVerificable;

@Repository
public interface EstadoVerRepositorio extends JpaRepository<EstadoVerificable, Long> {

}
