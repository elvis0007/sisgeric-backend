package ec.com.eeasa.wssisrec.wssisrec.features.correos;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ec.com.eeasa.wssisrec.wssisrec.services.EmailService;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Autor: Mateo Díaz
 */

@RestController
@RequestMapping("api/email")
public class CorreosController {
    private static final Logger logger = LoggerFactory.getLogger(CorreosController.class);
    // VAR
    @Autowired
    private EmailService emailService;

    @GetMapping("/send-date-changed")
    public ResponseEntity<LinkedHashMap<String, Object>> sendEmailDateChangedMax(
            @RequestParam Long arere_codigo,
            @RequestParam(required = false) Long arers_codigo) {
        try {
            ResponseEntity<String> response = this.emailService.sendEmailDateChangedMax(arere_codigo, arers_codigo,
                    "Notificación: Cambio de fecha max de cumplimiento de recomendaciones pendientes");
            return EstadoRespuestas.respuestaCreacion(response.getBody());
        } catch (Exception e) {
            logger.error("Error al enviar el correo de cambio de fecha max de cumplimiento (controlador de correos).", e);
            return EstadoRespuestas.respuestaError(
                    "Error al enviar el correo de cambio de fecha max de cumplimiento (controlador de correos).");
        }
    }

    @GetMapping("/notificar-informe/{ainin_codigo}")
    public ResponseEntity<LinkedHashMap<String, Object>> sendEmailNotification(@PathVariable Long ainin_codigo) {
        try {
            // Llamar al metodo del servicio
            ResponseEntity<String> response = this.emailService.sendCorreoDetalleRecomendaciones(ainin_codigo,
                    "Notificación SISREC: Asignación de Recomendaciones y Registro de Plan de Acción");
            
            // Validar que el código de estado retornado por el servicio sea exitoso (HttpStatus.OK)
            if (response.getStatusCode() != HttpStatus.OK) {
                return EstadoRespuestas.respuestaError(response.getBody());
            }
            
            // respuesta exitosa
            return EstadoRespuestas.respuestaCreacion(response.getBody());
        } catch (Exception e) {
            logger.error("Error al enviar el correo de notificacion a nivel de informe (controlador de correos).", e);
            return EstadoRespuestas.respuestaError(
                    "Error al enviar el correo de notificacion a nivel de informe (controlador de correos).");
        }
    }

    @GetMapping("/notificar-informe-debug/{ainin_codigo}")
    public ResponseEntity<byte[]> sendEmailNotificationDebug(@PathVariable Long ainin_codigo) {
        ec.com.eeasa.wssisrec.wssisrec.shared.utils.LogCapturer.start();
        try {
            logger.info("[DEBUG] Iniciando proceso de notificación para informe {}", ainin_codigo);
            ResponseEntity<String> response = this.emailService.sendCorreoDetalleRecomendaciones(ainin_codigo,
                    "Notificación SISREC: Asignación de Recomendaciones y Registro de Plan de Acción");
            logger.info("[DEBUG] Proceso finalizado. Respuesta del servicio: {}", response);
        } catch (Exception e) {
            logger.error("[DEBUG] Excepción capturada en el controlador al notificar informe:", e);
        } finally {
            String logs = ec.com.eeasa.wssisrec.wssisrec.shared.utils.LogCapturer.stopAndGet();
            byte[] fileBytes = logs.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);
            headers.setContentDisposition(org.springframework.http.ContentDisposition.builder("attachment")
                    .filename("logs_notificacion_informe_" + ainin_codigo + ".txt")
                    .build());
            
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        }
    }

    @Autowired
    private ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.RecomendacionRepositorio recomendacionRepositorio;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private ec.com.eeasa.wssisrec.wssisrec.services.ScheduledTasks scheduledTasks;

    @GetMapping("/test-recordatorio-plan-accion/{codigo}")
    public ResponseEntity<LinkedHashMap<String, Object>> testRecordatorioPlanAccion(@PathVariable Long codigo) {
        try {
            // 1. Probar si el código corresponde a una recomendación (ARERE_CODIGO)
            List<javax.persistence.Tuple> pendientes = this.recomendacionRepositorio.obtenerPendientesPlanAccionPorRecomendacion(codigo);
            String tipoOrigen = "recomendación " + codigo;

            // 2. Si no encontró por recomendación, probar si es un informe (AININ_CODIGO)
            if (pendientes == null || pendientes.isEmpty()) {
                pendientes = this.recomendacionRepositorio.obtenerPendientesPlanAccionPorInforme(codigo);
                tipoOrigen = "informe " + codigo;
            }

            if (pendientes != null && !pendientes.isEmpty()) {
                int enviados = this.scheduledTasks.procesarRecordatorioPlanAccion(pendientes);
                return EstadoRespuestas.respuestaCreacion("Recordatorio de plan de acción procesado correctamente para " + tipoOrigen + ". Correos enviados: " + enviados);
            }

            return EstadoRespuestas.respuestaObtencion(
                    "No se encontraron responsables pendientes de plan de acción para el código " + codigo + 
                    " (todos ya tienen plan de acción activo o no hay recomendaciones asignadas vigentes).", null);

        } catch (Exception e) {
            logger.error("Error al probar recordatorio de plan de acción para código {}", codigo, e);
            return EstadoRespuestas.respuestaError("Error al procesar recordatorio de plan de acción: " + e.getMessage());
        }
    }

    @GetMapping("/test-retraso-plan-accion/{codigo}")
    public ResponseEntity<LinkedHashMap<String, Object>> testRetrasoPlanAccion(@PathVariable Long codigo) {
        try {
            // 1. Probar si el código corresponde a una recomendación (ARERE_CODIGO)
            List<javax.persistence.Tuple> pendientes = this.recomendacionRepositorio.obtenerPendientesPlanAccionVencidosPorRecomendacion(codigo);
            String tipoOrigen = "recomendación " + codigo;

            // 2. Si no encontró por recomendación, probar si es un informe (AININ_CODIGO)
            if (pendientes == null || pendientes.isEmpty()) {
                pendientes = this.recomendacionRepositorio.obtenerPendientesPlanAccionVencidosPorInforme(codigo);
                tipoOrigen = "informe " + codigo;
            }

            if (pendientes != null && !pendientes.isEmpty()) {
                int enviados = this.scheduledTasks.procesarPlanAccionVencido(pendientes);
                return EstadoRespuestas.respuestaCreacion("Alerta de retraso de plan de acción procesada correctamente para " + tipoOrigen + ". Correos enviados: " + enviados);
            }

            return EstadoRespuestas.respuestaObtencion(
                    "No se encontraron responsables con retraso de plan de acción para el código " + codigo + 
                    " (todos ya tienen plan de acción activo, verificable en JUSTIFICA, o recomendación cumplida).", null);

        } catch (Exception e) {
            logger.error("Error al probar alerta de retraso de plan de acción para código {}", codigo, e);
            return EstadoRespuestas.respuestaError("Error al procesar alerta de retraso de plan de acción: " + e.getMessage());
        }
    }
}
