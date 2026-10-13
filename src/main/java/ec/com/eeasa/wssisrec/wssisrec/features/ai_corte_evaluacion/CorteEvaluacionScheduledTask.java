package ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.models.CorteEvaluacion;

@Component
public class CorteEvaluacionScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(CorteEvaluacionScheduledTask.class);

    @Autowired
    private CorteEvaluacionRepositorio corteEvaluacionRepositorio;

    @Autowired
    private CorteEvaluacionInformeRepositorio corteEvaluacionInformeRepositorio;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Tarea programada que se ejecuta automáticamente todos los días a las 23:59:00 (Cierre de día)
     * Congela/Actualiza la fotografía inmutable de los periodos de evaluación cuya fecha de corte haya llegado.
     */
    @Scheduled(cron = "0 59 23 * * ?")
    public void ejecutarSnapshotDiarioAutomotico() {
        logger.info("Iniciando tarea programada nocturna de congelamiento automático de snapshots...");
        try {
            List<CorteEvaluacion> periodosActivos = corteEvaluacionRepositorio.findAllActivos();
            for (CorteEvaluacion p : periodosActivos) {
                try {
                    corteEvaluacionInformeRepositorio.autoAsignarInformesPorFechaCorte(p.getArcevCodigo(), p.getArcevFechaCorte());
                } catch (Exception ex) {
                    logger.warn("Error al autoasignar nocturno para periodo {}: {}", p.getArcevCodigo(), ex.getMessage());
                }
                if (p.getArcevFechaCorte() != null && !p.getArcevFechaCorte().after(new java.util.Date())) {
                    jdbcTemplate.update("CALL SP_GENERAR_SNAPSHOT_CORTE(?)", p.getArcevCodigo());
                    logger.info("Snapshot nocturno automático generado exitosamente para periodo ID: {} ({})", 
                        p.getArcevCodigo(), p.getArcevNombre());
                }
            }
        } catch (Exception e) {
            logger.error("Error durante la ejecución del snapshot nocturno automático: {}", e.getMessage(), e);
        }
    }
}
