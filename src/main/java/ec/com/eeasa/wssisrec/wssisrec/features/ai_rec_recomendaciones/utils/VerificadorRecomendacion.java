package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.RecomendacionServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;

@Component
@EnableScheduling
public class VerificadorRecomendacion {
  private static final Logger logger = LoggerFactory.getLogger(VerificadorRecomendacion.class);

  @Autowired
  private RecomendacionServicio recomendacionServicio;

  @Scheduled(cron = "0 0 7 * * *")
  public void verificarRecomendacionesVencidas() {
    // logger.info("Iniciando verificación de recomendaciones vencidas");

    List<Recomendacion> recomendacionesVencidas = recomendacionServicio.obtenerRecomendacionesVencidas();

    for (Recomendacion recomendacion : recomendacionesVencidas) {
      try {
        recomendacionServicio.recalcularEstadoRecomendacion(recomendacion.getArere_codigo());
        // logger.info("Recomendación {} actualizada a estado VENCIDO",
        // recomendacion.getArere_codigo());
        try {
          // List<DetalleRecomendacion> detalleRecomendacions = detalleServicio
          // .obtenerDetalleRecomendacionVencidas(recomendacion.getArere_codigo());
          // detalleRecomendacions
          // .forEach(detalle -> detalleServicio.actualizarEstadoSeg(detalle,
          // EstadoSegEnum.NO_CUMPLIDA));
        } catch (Exception e) {
          // TODO: handle exception
          logger.error("Error al procesar recomendación {}: {}",
              recomendacion.getArere_codigo(), e.getMessage());
        }

      } catch (Exception e) {
        // TODO: handle exception
        logger.error("Error al procesar recomendación {}: {}",
            recomendacion.getArere_codigo(), e.getMessage());
      }
    }
    // logger.info("Verificación de recomendaciones completada. {} recomendaciones
    // procesadas",
    // recomendacionesVencidas.size());
  }
}
