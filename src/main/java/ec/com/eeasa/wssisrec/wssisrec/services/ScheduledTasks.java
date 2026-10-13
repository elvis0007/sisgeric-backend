package ec.com.eeasa.wssisrec.wssisrec.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.Tuple;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.NotificacionesRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.NotificacionesServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.dtos.NuevaNotificacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.enums.TipoNotificacionEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.RecomendacionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.RecomendacionServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.DetallePlantillaCorreoRecordatorioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.UsuarioRecordatorioNotificacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.UtilsFunctions;

/*
 * Autor: Mateo Díaz
 */

@Service
@Profile("!test")
public class ScheduledTasks {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    // VAR
    @Autowired
    private RecomendacionRepositorio recomendacionRepositorio;
    @Autowired
    private EmailService emailService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private RecomendacionServicio recomendacionServicio;
    @Autowired
    private NotificacionesServicio notificacionesServicio;
    @Autowired
    private NotificacionesRepositorio notificacionesRepositorio;

    // TAREA PROGRAMADA PARA RECORDATORIO DE 7 DIAS (RECOMENDACIONES EN PROCESO DE CUMPLIMIENTO)
    @Scheduled(cron = "0 0 8 * * MON")
    @Transactional
    public void verificarRecomendacionesPorVencer() {
        try {
            // Guardar correos de usuarios en una lista
            List<Object[]> usuariosNotificar = recomendacionRepositorio
                            .findResponsablesActivosConRecomendacionesProximas();

            List<UsuarioRecordatorioNotificacionDTO> usuariosConvertidos = new ArrayList<>();

                for (Object[] resultado : usuariosNotificar) {
                String correo = (String) resultado[0];
                String usuario = (String) resultado[1];

                UsuarioRecordatorioNotificacionDTO dto = new UsuarioRecordatorioNotificacionDTO(correo, usuario);
                usuariosConvertidos.add(dto);
            }

            // verificar que la lista de usuarios no se encuentre vacia y procede con el envio
            if (!usuariosConvertidos.isEmpty()) {
                this.procesarNotificacionesUsuarios(usuariosConvertidos);
            }

        } catch (Exception e) {
            this.manejarError("Error al obtener los correos de los usuarios. ", e);
        }
    }

    // TAREA PROGRAMADA PARA CORREO DE RECOMENDACIONES VENCIDAS
    @Scheduled(cron = "0 0 8 * * MON")
    @Transactional
    public void verificarRecomendacionesVencidas(){
        try {
            List<UsuarioRecordatorioNotificacionDTO> datosUsuario = this.recomendacionServicio.obtenerCorreoUsuarioResponsablesRecomendacionesVencidas();
            if(!datosUsuario.isEmpty()){
                this.procesarNotificacionesUsuariosRecVencidas(datosUsuario);
            }
        } catch (Exception e) {
            this.manejarError("Error al obtener los correos de los usuarios. ", e);
        }
    }

    @Autowired
    private ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.SolicitudesServicio solicitudesServicio;

    //TAREA PROGRAMADA PARA ACTUALIZAR EL ESTADO DE LAS TAREAS CON FECHA MAX DE CUMPLIMIENTO VENCIDAS
    @Scheduled(cron = "0 59 23 * * ?")
    @Transactional
    public void cambiarEstadoRecVencidas(){
        try {
            this.recomendacionServicio.actualizarEstadoRecVencidas();
        } catch (Exception e) {
            this.manejarError("Error al actualizar la fecha max de cumplimiento de las recomendaciones correspondientes. ", e);
        }
    }

    // TAREA PROGRAMADA PARA DEPURAR SOLICITUDES PENDIENTES DE PRÓRROGA CADUCADAS (DIARIO A LA MEDIANOCHE Y AL INICIAR)
    @Scheduled(cron = "0 0 0 * * ?")
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    @Transactional
    public void depurarSolicitudesCaducadas() {
        try {
            logger.info("[SCHEDULED] Ejecutando depuración de solicitudes pendientes caducadas...");
            this.solicitudesServicio.depurarSolicitudesCaducadas();
        } catch (Exception e) {
            this.manejarError("Error al depurar solicitudes caducadas. ", e);
        }
    }

    // TAREA PROGRAMADA PARA RECORDATORIO DE PLAN DE ACCION (1 HORA ANTES DE CUMPLIRSE LAS 48 HORAS DEL INFORME)
    @Scheduled(cron = "0 */10 * * * ?")
    @Transactional
    public void verificarRecordatorioPlanAccion48Horas() {
        try {
            List<Tuple> pendientes = this.recomendacionRepositorio.obtenerPendientesPlanAccion48Horas();
            if (pendientes != null && !pendientes.isEmpty()) {
                logger.info("[SCHEDULED] Se encontraron {} recomendaciones pendientes de plan de acción para notificar", pendientes.size());
                this.procesarRecordatorioPlanAccion(pendientes);
            }
        } catch (Exception e) {
            this.manejarError("Error al verificar recordatorios de plan de acción de 48 horas. ", e);
        }
    }

    public int procesarRecordatorioPlanAccion(List<Tuple> pendientes) {
        if (pendientes == null || pendientes.isEmpty()) {
            return 0;
        }

        // Agrupar por usuario + informe
        Map<String, List<Tuple>> agrupados = new java.util.LinkedHashMap<>();
        for (Tuple t : pendientes) {
            String usuario = t.get("arersUsuarioPer", String.class);
            Object infCodObj = t.get("aininCodigo");
            String infCod = infCodObj != null ? infCodObj.toString() : "0";
            String key = usuario + "_" + infCod;
            agrupados.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        int enviados = 0;
        for (Map.Entry<String, List<Tuple>> entry : agrupados.entrySet()) {
            List<Tuple> lista = entry.getValue();
            if (lista.isEmpty()) continue;

            Tuple primer = lista.get(0);
            String correo = primer.get("arersCorreo", String.class);
            String usuario = primer.get("arersUsuarioPer", String.class);
            String nombres = primer.get("arersNombre", String.class);
            String apellidos = primer.get("arersApellido", String.class);
            String titulo = primer.get("aininTitulo", String.class);
            String memo = primer.get("aininMemo", String.class);
            Object infCodObj = primer.get("aininCodigo");
            String aininCodigo = infCodObj != null ? infCodObj.toString() : "";

            if (correo == null || correo.trim().isEmpty()) {
                continue;
            }

            StringBuilder sbRecs = new StringBuilder();
            for (Tuple recTuple : lista) {
                String desc = recTuple.get("arereDescripcion", String.class);
                String fMax = recTuple.get("recFechaMax", String.class);
                if (desc == null) desc = "";
                desc = desc.replace("\n", "<br/>");

                sbRecs.append("<tr>")
                      .append("<td style=\"padding: 12px 14px; color: #1e293b; font-size: 13px; line-height: 1.5; border-bottom: 1px solid #e2e8f0; text-align: justify;\">")
                      .append("<span>").append(desc).append("</span>")
                      .append("</td>")
                      .append("<td style=\"padding: 12px 14px; color: #b91c1c; font-size: 13px; text-align: center; border-bottom: 1px solid #e2e8f0; font-weight: bold;\">")
                      .append("<strong>").append(fMax != null ? fMax : "-").append("</strong>")
                      .append("</td>")
                      .append("</tr>");
            }

            Map<String, Object> vars = new HashMap<>();
            vars.put("nombres", nombres != null ? nombres : "");
            vars.put("apellidos", apellidos != null ? apellidos : "");
            vars.put("tituloInforme", titulo != null ? titulo : "");
            vars.put("memoAprobacion", memo != null ? memo : "");
            vars.put("fecha", UtilsFunctions.getDateHour());
            vars.put("recomendaciones", sbRecs.toString());

            boolean ok = this.emailService.sendRecordatorioPlanAccion(correo, vars);
            if (ok) {
                enviados++;
                logger.info("[SCHEDULED] Recordatorio de Plan de Acción enviado a {} para informe {}", correo, aininCodigo);

                // Enviar aviso por websocket si está conectado
                try {
                    this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + usuario,
                            "Recordatorio: Le queda 1 hora para registrar su Plan de Acción. Ingrese a SISREC ⚠️");
                } catch (Exception ex) {
                    logger.warn("[SCHEDULED] No se pudo enviar websocket a {}: {}", usuario, ex.getMessage());
                }

                // Registrar en AI_NOTIFICACIONES para trazabilidad y evitar duplicados
                try {
                    NuevaNotificacionDTO notif = new NuevaNotificacionDTO(
                            "Recordatorio Plan de Acción",
                            "Le resta aproximadamente 1 hora para registrar su Plan de Acción para el informe: " + titulo,
                            TipoNotificacionEnum.PLAN_ACCION_RECORDATORIO,
                            TipoNotificacionEnum.PLAN_ACCION_RECORDATORIO,
                            java.time.LocalDate.now().getYear(),
                            null,
                            null,
                            usuario
                    );
                    notif.setAnotif_url_sistema("/inicio/plan-accion-responsables");
                    this.notificacionesServicio.registrarNotificacion(notif);
                } catch (Exception ex) {
                    logger.error("[SCHEDULED] Error al registrar notificación interna de recordatorio plan de acción: {}", ex.getMessage());
                }
            }
        }
        return enviados;
    }

    // TAREA PROGRAMADA DIARIA PARA ALERTA DE PLAN DE ACCION VENCIDO (>48 HORAS) - SE EJECUTA A LAS 08:00 AM
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void verificarPlanAccionVencidoDiario() {
        try {
            List<Tuple> pendientes = this.recomendacionRepositorio.obtenerPendientesPlanAccionVencidos();
            if (pendientes != null && !pendientes.isEmpty()) {
                logger.info("[SCHEDULED] Se encontraron {} recomendaciones con plan de acción vencido (>48h) para alertar diariamente", pendientes.size());
                this.procesarPlanAccionVencido(pendientes);
            }
        } catch (Exception e) {
            this.manejarError("Error al verificar alertas diarias de plan de acción vencido. ", e);
        }
    }

    public int procesarPlanAccionVencido(List<Tuple> pendientes) {
        if (pendientes == null || pendientes.isEmpty()) {
            return 0;
        }

        // Agrupar por usuario + informe
        Map<String, List<Tuple>> agrupados = new java.util.LinkedHashMap<>();
        for (Tuple t : pendientes) {
            String usuario = t.get("arersUsuarioPer", String.class);
            Object infCodObj = t.get("aininCodigo");
            String infCod = infCodObj != null ? infCodObj.toString() : "0";
            String key = usuario + "_" + infCod;
            agrupados.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        int enviados = 0;
        for (Map.Entry<String, List<Tuple>> entry : agrupados.entrySet()) {
            List<Tuple> lista = entry.getValue();
            if (lista.isEmpty()) continue;

            Tuple primer = lista.get(0);
            String correo = primer.get("arersCorreo", String.class);
            String usuario = primer.get("arersUsuarioPer", String.class);
            String nombres = primer.get("arersNombre", String.class);
            String apellidos = primer.get("arersApellido", String.class);
            String titulo = primer.get("aininTitulo", String.class);
            String memo = primer.get("aininMemo", String.class);
            Object infCodObj = primer.get("aininCodigo");
            String aininCodigo = infCodObj != null ? infCodObj.toString() : "";
            Object diasObj = primer.get("diasRetraso");
            int diasRetraso = (diasObj instanceof Number) ? ((Number) diasObj).intValue() : 1;

            if (correo == null || correo.trim().isEmpty()) {
                continue;
            }

            StringBuilder sbRecs = new StringBuilder();
            for (Tuple recTuple : lista) {
                String desc = recTuple.get("arereDescripcion", String.class);
                String fMax = recTuple.get("recFechaMax", String.class);
                if (desc == null) desc = "";
                desc = desc.replace("\n", "<br/>");

                sbRecs.append("<tr>")
                      .append("<td style=\"padding: 12px 14px; color: #1e293b; font-size: 13px; line-height: 1.5; border-bottom: 1px solid #e2e8f0; text-align: justify;\">")
                      .append("<span>").append(desc).append("</span>")
                      .append("</td>")
                      .append("<td style=\"padding: 12px 14px; color: #b91c1c; font-size: 13px; text-align: center; border-bottom: 1px solid #e2e8f0; font-weight: bold;\">")
                      .append("<strong>").append(fMax != null ? fMax : "-").append("</strong>")
                      .append("</td>")
                      .append("</tr>");
            }

            Map<String, Object> vars = new HashMap<>();
            vars.put("nombres", nombres != null ? nombres : "");
            vars.put("apellidos", apellidos != null ? apellidos : "");
            vars.put("tituloInforme", titulo != null ? titulo : "");
            vars.put("memoAprobacion", memo != null ? memo : "");
            vars.put("diasRetraso", String.valueOf(diasRetraso));
            vars.put("fecha", UtilsFunctions.getDateHour());
            vars.put("recomendaciones", sbRecs.toString());

            boolean ok = this.emailService.sendRetrasoPlanAccion(correo, vars);
            if (ok) {
                enviados++;
                logger.info("[SCHEDULED] Alerta diaria de retraso Plan de Acción enviada a {} ({} días) para informe {}", correo, diasRetraso, aininCodigo);

                // Enviar websocket
                try {
                    this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + usuario,
                            "Alerta: Tiene " + diasRetraso + " día(s) sin registrar su Plan de Acción. Ingrese a SISREC ⚠️");
                } catch (Exception ex) {
                    logger.warn("[SCHEDULED] No se pudo enviar websocket a {}: {}", usuario, ex.getMessage());
                }

                // Registrar en AI_NOTIFICACIONES
                try {
                    NuevaNotificacionDTO notif = new NuevaNotificacionDTO(
                            "Plan de Acción Fuera de Plazo (" + diasRetraso + " días)",
                            "Tiene " + diasRetraso + " día(s) sin cumplir con el registro del Plan de Acción para el informe: " + titulo,
                            TipoNotificacionEnum.PLAN_ACCION_VENCIDO,
                            TipoNotificacionEnum.PLAN_ACCION_VENCIDO,
                            java.time.LocalDate.now().getYear(),
                            null,
                            null,
                            usuario
                    );
                    notif.setAnotif_url_sistema("/inicio/plan-accion-responsables");
                    this.notificacionesServicio.registrarNotificacion(notif);
                } catch (Exception ex) {
                    logger.error("[SCHEDULED] Error al registrar notificación interna de retraso plan de acción: {}", ex.getMessage());
                }
            }
        }
        return enviados;
    }

    // SE ENVIA COMO TAL EL CORREO MEDIANTE EL SERVICIO DE EMAIL'S (EmailService)
    private void procesarNotificacionesUsuarios(List<UsuarioRecordatorioNotificacionDTO> usuarios) {
        for (UsuarioRecordatorioNotificacionDTO usuario : usuarios) {
            try {
                Map<String, Object> variables = this.prepararVariablesPlantilla(usuario);

                // envio de correo
                this.emailService.sendRecomendacionRecordatorio(usuario.getCorreo(), variables);

                // enviar notificacion websocket al usuario especifico
                this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + usuario.getUsuario(),
                        "Tiene recomendaciones próximas a vencer. Revise su correo institucional 📨");

                // Solo insertar si no existe ya una notificación pendiente sin leer del mismo tipo
                if (this.notificacionesRepositorio.existeNotificacionPendienteUsuarioTipo(usuario.getUsuario(), TipoNotificacionEnum.RECOMENDACION_VENCER) == 0) {
                    NuevaNotificacionDTO notificacion = new NuevaNotificacionDTO("Tiene recomendaciones proximas a vencer", "Revise su bandeja de entrada, tiene recomendaciones pendientes con fecha max. de cumplimiento a 7 dias de vencer. \n Nota: En caso de requerir Prórroga para el cumplimiento de la recomendación, debe realizarlo en el transcurso de 48 HORAS una vez recibida esta notificación.", TipoNotificacionEnum.RECOMENDACION_VENCER, TipoNotificacionEnum.RECOMENDACION_VENCER, java.time.LocalDate.now().getYear(), null, null, usuario.getUsuario());
                    this.notificacionesServicio.registrarNotificacion(notificacion);
                }
            } catch (Exception e) {
                this.manejarError("Error enviando el correo y notificando al usuario " + usuario.getCorreo(), e);
            }
        }
    }

    private void procesarNotificacionesUsuariosRecVencidas(List<UsuarioRecordatorioNotificacionDTO> usuarios){
        for (UsuarioRecordatorioNotificacionDTO usuario : usuarios){
            try {
                Map<String, Object> variables = this.prepararVariablesPlantillaRecVencidas(usuario);

                this.emailService.sendRecomendacionVencimiento(usuario.getCorreo(), variables);
                // enviar notificacion websocket al usuario especifico
                this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + usuario.getUsuario(), "Tiene recomendaciones VENCIDAS. Revise su correo 📨");
                // Solo insertar si no existe ya una notificación pendiente sin leer del mismo tipo
                if (this.notificacionesRepositorio.existeNotificacionPendienteUsuarioTipo(usuario.getUsuario(), TipoNotificacionEnum.RECOMENDACION_VENCIDA) == 0) {
                    NuevaNotificacionDTO notificacion = new NuevaNotificacionDTO("Tiene recomendaciones VENCIDAS", "Revise su bandeja de entrada, tiene recomendaciones que VENCIERON.", TipoNotificacionEnum.RECOMENDACION_VENCIDA, TipoNotificacionEnum.RECOMENDACION_VENCIDA, java.time.LocalDate.now().getYear(), null, null, usuario.getUsuario());
                    this.notificacionesServicio.registrarNotificacion(notificacion);
                }
            } catch (Exception e) {
                this.manejarError("Error enviando el correo y notificando al usuario " + usuario.getUsuario(), e);
            }
        }
    }

    // OBTENER EL DETALLE PARA EL RECORDATORIO DE LAS RECOMENDACIONES
    private List<DetallePlantillaCorreoRecordatorioDTO> obtenerDetallePlantillaRecordatorio(String usuario) {
        List<DetallePlantillaCorreoRecordatorioDTO> detalleProcesado = new ArrayList<>();
        try {
            List<Object[]> detalleCrudo = this.recomendacionRepositorio.obtenerRecomendacionesPorUsuario(usuario);
            if (detalleCrudo != null) {
                // TRATAR DATOS CRUDOS DE LA BD AL DTO
                for (Object[] detalle : detalleCrudo) {
                    String nombres = (String) detalle[0];
                    String apellidos = (String) detalle[1];
                    String tituloInforme = (String) detalle[2];
                    int conteoRecomendaciones = ((Number) detalle[3]).intValue();
                    detalleProcesado.add(new DetallePlantillaCorreoRecordatorioDTO(nombres, apellidos, tituloInforme, conteoRecomendaciones));
                }
            }
        } catch (Exception e) {
            this.manejarError("Error al intentar obtener el detalle de recomendaciones del usuario.", e);
        }
        return detalleProcesado;
    }


    //OBTENER DETALLE DE RECOMENDACIONES VENCIDAS EN BASE AL USUARIO DEL RESPONSABLE
    private List<DetallePlantillaCorreoRecordatorioDTO> obtenerDetalleRecomendacionesVencidas(String usuario){
        List<DetallePlantillaCorreoRecordatorioDTO> detalle = this.recomendacionServicio.obtenerDetalleRecomendacionesVencidas(usuario);
        return detalle;
    }

    // ARMADO DE VARIABLES DINÁMICAS QUE SE ENVIARÁN EN LA PLANTILLAS
    private Map<String, Object> prepararVariablesPlantilla(UsuarioRecordatorioNotificacionDTO usuario) {
        Map<String, Object> variables = new HashMap<>();
        List<DetallePlantillaCorreoRecordatorioDTO> detalle = this
                .obtenerDetallePlantillaRecordatorio(usuario.getUsuario());
        if (!detalle.isEmpty()) {
            variables.put("fecha", UtilsFunctions.getDateHour());
            variables.put("nombres", detalle.get(0).getNombres());
            variables.put("apellidos", detalle.get(0).getApellidos());
            variables.put("datos", detalle);
        } else {
            variables.put("fecha", UtilsFunctions.getDateHour());
            variables.put("nombres", "");
            variables.put("apellidos", "");
            variables.put("datos", null);
        }
        return variables;
    }

    private Map<String, Object> prepararVariablesPlantillaRecVencidas(UsuarioRecordatorioNotificacionDTO usuario){
        Map<String, Object> variables = new HashMap<>();
        List<DetallePlantillaCorreoRecordatorioDTO> detalle = this.obtenerDetalleRecomendacionesVencidas(usuario.getUsuario());
        if (!detalle.isEmpty()) {
            variables.put("fecha", UtilsFunctions.getDateHour());
            variables.put("nombres", detalle.get(0).getNombres());
            variables.put("apellidos", detalle.get(0).getApellidos());
            variables.put("datos", detalle);
        } else {
            variables.put("fecha", UtilsFunctions.getDateHour());
            variables.put("nombres", "");
            variables.put("apellidos", "");
            variables.put("datos", null);
        }
        return variables;
    }

    // MÉTODO MANEJO DE ERRORES
    private void manejarError(String mensaje, Exception e) {
        // por consola del servidor
        logger.error("Error al intentar enviar el recordatorio. " + mensaje, e);
        // por websocket
        this.simpMessagingTemplate.convertAndSend("/topic/notificacion",
                "Error al intentar enviar el recordatorio. " + mensaje);
    }

}
