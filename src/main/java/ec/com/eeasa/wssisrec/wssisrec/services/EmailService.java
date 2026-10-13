package ec.com.eeasa.wssisrec.wssisrec.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.NotificacionesServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.dtos.NuevaNotificacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.RecomendacionServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.DetalleRecomendacionesPorInformeDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.RecomendacionCambioFechaDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.DirectorInformacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.UtilsFunctions;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.enums.TipoNotificacionEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.InformeRepositorio;



@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private RolUsuarioRepositorio rolUsuarioRepositorio;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecomendacionServicio recomendacionServicio;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private NotificacionesServicio notificacionesServicio;

    @Autowired
    private RolUsuarioServicio rolUsuarioServicio;

    @Autowired
    private WsCorreoService wsCorreoService;

    @Autowired
    private InformeRepositorio informeRepositorio;

    /**
     * Método base para enviar correos usando una plantilla Thymeleaf.
     * Renderiza el HTML localmente y lo envía a través del microservicio ws_correo.
     *
     * @param to           Dirección de correo del destinatario
     * @param subject      Asunto del correo
     * @param templateName Nombre de la plantilla Thymeleaf
     * @param variables    Variables dinámicas para la plantilla
     * @return true si el envío fue exitoso, false en caso contrario
     */
    /**
     * Preprocesa las variables para asegurar compatibilidad con el renderizador
     * remoto.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> preprocesarVariables(Map<String, Object> variables) {
        if (variables == null) {
            return new HashMap<>();
        }
        try {
            // Convierte DTOs complejos a Maps/Lists de Maps
            Map<String, Object> processed = objectMapper.convertValue(variables, Map.class);
            Map<String, Object> flatMap = new HashMap<>(processed);

            // 1. Aplanar "datos" si es DetalleRecomendacionesPorInformeDTO (objeto Map)
            if (processed.containsKey("datos") && processed.get("datos") instanceof Map) {
                Map<String, Object> datosMap = (Map<String, Object>) processed.get("datos");

                if (datosMap.containsKey("responsableDTO") && datosMap.get("responsableDTO") instanceof Map) {
                    Map<String, Object> respMap = (Map<String, Object>) datosMap.get("responsableDTO");
                    flatMap.put("nombres", respMap.get("nombres"));
                    flatMap.put("apellidos", respMap.get("apellidos"));
                    flatMap.put("correo", respMap.get("correo"));
                    flatMap.put("usuarioPer", respMap.get("usuarioPer"));
                }

                if (datosMap.containsKey("informeDTO") && datosMap.get("informeDTO") instanceof Map) {
                    Map<String, Object> infMap = (Map<String, Object>) datosMap.get("informeDTO");
                    flatMap.put("tituloInforme", infMap.get("titulo"));
                    Object memoVal = infMap.get("memorando") != null ? infMap.get("memorando")
                            : infMap.get("memoAprobacion");
                    flatMap.put("memoAprobacion", memoVal);
                    infMap.put("memoAprobacion", memoVal);
                }

                if (datosMap.containsKey("recomendacionesDTO") && datosMap.get("recomendacionesDTO") instanceof List) {
                    List<Map<String, Object>> recs = (List<Map<String, Object>>) datosMap.get("recomendacionesDTO");
                    StringBuilder sb = new StringBuilder();
                    for (Map<String, Object> rec : recs) {
                        String desc = rec.get("descripcion") != null ? rec.get("descripcion").toString() : "";
                        String fechaMax = rec.get("fechaMaxima") != null ? rec.get("fechaMaxima").toString() : "";
                        desc = desc.replace("\n", "<br/>");

                        sb.append("<tr>")
                                .append("<td style=\"padding: 12px 14px; color: #1e293b; font-size: 13px; line-height: 1.5; border-bottom: 1px solid #e2e8f0; text-align: justify;\">")
                                .append("<span>").append(desc).append("</span>")
                                .append("</td>")
                                .append("<td style=\"padding: 12px 14px; color: #b91c1c; font-size: 13px; text-align: center; border-bottom: 1px solid #e2e8f0; font-weight: bold;\">")
                                .append("<strong>").append(fechaMax).append("</strong>")
                                .append("</td>")
                                .append("</tr>");
                    }
                    flatMap.put("recomendaciones", sb.toString());
                }
            }

            // 2. Aplanar "datos" si es lista de recordatorios o vencidas (List de Maps)
            if (processed.containsKey("datos") && processed.get("datos") instanceof List) {
                List<Map<String, Object>> listaDatos = (List<Map<String, Object>>) processed.get("datos");
                if (!listaDatos.isEmpty()) {
                    Map<String, Object> primerElemento = listaDatos.get(0);
                    flatMap.put("nombres", primerElemento.get("nombres"));
                    flatMap.put("apellidos", primerElemento.get("apellidos"));
                }

                StringBuilder sb = new StringBuilder();
                for (Map<String, Object> item : listaDatos) {
                    String titulo = item.get("tituloInforme") != null ? item.get("tituloInforme").toString() : "";
                    String conteo = item.get("conteoRecomendacionesPendientes") != null
                            ? item.get("conteoRecomendacionesPendientes").toString()
                            : (item.get("conteoRecomendaciones") != null ? item.get("conteoRecomendaciones").toString()
                                    : "0");

                    sb.append("<tr>")
                            .append("<td style=\"padding: 12px 0; color: #2c2c2c; font-size: 13px;\">")
                            .append("<span>").append(titulo).append("</span>")
                            .append("</td>")
                            .append("<td style=\"padding: 12px 0; color: #2c2c2c; font-size: 13px; text-align: center;\">")
                            .append("<strong>").append(conteo).append("</strong>")
                            .append("</td>")
                            .append("</tr>");
                }
                flatMap.put("datos", sb.toString());
            }

            // 3. Aplanar "responsables" (List<String> o String)
            if (processed.containsKey("responsables")) {
                Object respObj = processed.get("responsables");
                StringBuilder sb = new StringBuilder();
                if (respObj instanceof List) {
                    List<Object> resps = (List<Object>) respObj;
                    for (Object resp : resps) {
                        if (resp != null && !resp.toString().trim().isEmpty()) {
                            sb.append("<tr>")
                                    .append("<td style=\"padding: 10px 12px; color: #1e293b; font-size: 13px; border-bottom: 1px solid #e2e8f0; font-weight: 600;\">")
                                    .append("<span style=\"color: #192A67; margin-right: 8px; font-size: 14px;\">&#9679;</span>")
                                    .append("<span>").append(resp.toString().trim()).append("</span>")
                                    .append("</td>")
                                    .append("</tr>");
                        }
                    }
                } else if (respObj instanceof String && !respObj.toString().trim().isEmpty()) {
                    sb.append("<tr>")
                            .append("<td style=\"padding: 10px 12px; color: #1e293b; font-size: 13px; border-bottom: 1px solid #e2e8f0; font-weight: 600;\">")
                            .append("<span style=\"color: #192A67; margin-right: 8px; font-size: 14px;\">&#9679;</span>")
                            .append("<span>").append(respObj.toString().trim()).append("</span>")
                            .append("</td>")
                            .append("</tr>");
                }
                if (sb.length() == 0) {
                    sb.append("<tr>")
                            .append("<td style=\"padding: 10px 12px; color: #64748b; font-size: 13px; font-style: italic;\">")
                            .append("<span>No se registraron responsables asignados.</span>")
                            .append("</td>")
                            .append("</tr>");
                }
                flatMap.put("responsables", sb.toString());
            }

            // 4. Aplanar "recomendacion"
            if (processed.containsKey("recomendacion") && processed.get("recomendacion") instanceof Map) {
                Map<String, Object> recMap = (Map<String, Object>) processed.get("recomendacion");
                Object infMemo = recMap.get("infMemorando") != null ? recMap.get("infMemorando")
                        : recMap.get("infMemoAprobacion");
                recMap.put("infMemoAprobacion", infMemo);

                for (Map.Entry<String, Object> entry : recMap.entrySet()) {
                    flatMap.put("recomendacion." + entry.getKey(), entry.getValue());
                }
                flatMap.put("nombreApellidoRS", recMap.get("nombreApellidoRS"));
                flatMap.put("getNombreApellidoRS", recMap.get("nombreApellidoRS"));
                flatMap.put("infTitulo", recMap.get("infTitulo"));
                flatMap.put("infMemoAprobacion", infMemo);
                flatMap.put("recomendacion.infMemoAprobacion", infMemo);
                flatMap.put("recDescripcion", recMap.get("recDescripcion"));
                flatMap.put("recFechaMaxCumplimiento", recMap.get("recFechaMaxCumplimiento"));
            }

            // Duplicar variables dentro de objetos anidados comunes para redundancia
            // interna en el JSON
            Map<String, Object> variablesCopy = new HashMap<>(flatMap);
            flatMap.put("variables", variablesCopy);
            flatMap.put("parametros", variablesCopy);
            flatMap.put("params", variablesCopy);
            flatMap.put("valores", variablesCopy);
            flatMap.put("campos", variablesCopy);

            return flatMap;
        } catch (Exception e) {
            logger.error("[MAIL] Error al preprocesar variables de plantilla: {}", e.getMessage(), e);
            return variables;
        }
    }

    public boolean sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Map<String, Object> variablesProcesadas = preprocesarVariables(variables);
            // Enviar a través del microservicio ws_correo usando la plantilla remota
            return this.wsCorreoService.enviarCorreoConPlantilla(to, subject, templateName, variablesProcesadas, null);
        } catch (Exception e) {
            logger.error("[MAIL] Error al enviar correo a {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Envía un correo con adjunto usando una plantilla remota.
     */
    public boolean sendTemplateEmailWithAttachment(String to, String subject, String templateName,
            Map<String, Object> variables, byte[] attachmentBytes, String attachmentFilename) {
        try {
            Map<String, Object> variablesProcesadas = preprocesarVariables(variables);
            // Enviar a través del microservicio ws_correo con plantilla y adjunto
            return this.wsCorreoService.enviarCorreoConPlantillaYAdjunto(
                    to, subject, templateName, variablesProcesadas, attachmentBytes, attachmentFilename, null);
        } catch (Exception e) {
            logger.error("[MAIL] Error al enviar correo con adjunto a {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Envía recordatorio de recomendaciones pendientes próximas a vencer.
     */
    public void sendRecomendacionRecordatorio(String to, Map<String, Object> variables) {
        this.sendTemplateEmail(to,
                "Recordatorio: Recomendaciones pendientes vencen en 7 días",
                "recordatory-template",
                variables);
    }

    /**
     * Envía correo recordatorio de Plan de Acción (1 hora antes de cumplirse las 48 horas).
     */
    public boolean sendRecordatorioPlanAccion(String to, Map<String, Object> variables) {
        return this.sendTemplateEmail(to,
                "Recordatorio Urgente: Plazo para Plan de Acción próximo a vencer - SISREC",
                "recordatorio-plan-accion-template",
                variables);
    }

    /**
     * Envía notificación diaria de alerta por retraso en el registro del Plan de Acción (plazo de 48 horas superado).
     */
    public boolean sendRetrasoPlanAccion(String to, Map<String, Object> variables) {
        Object dias = variables.get("diasRetraso");
        String diasTexto = dias != null ? dias.toString() : "varios";
        return this.sendTemplateEmail(to,
                "Alerta: Tiene " + diasTexto + " día(s) sin cumplir con el Plan de Acción - SISREC",
                "retraso-plan-accion-template",
                variables);
    }

    /**
     * Envía notificación de recomendaciones vencidas.
     */
    public void sendRecomendacionVencimiento(String to, Map<String, Object> variables) {
        this.sendTemplateEmail(to,
                "Notificación Tiene recomendaciones VENCIDAS.",
                "expired-template",
                variables);
    }

    /**
     * Envía correo al responsable notificando su asignación a una recomendación.
     */
    public void sendCorreoAsignacion(String to, Map<String, Object> variables) {
        this.sendTemplateEmail(to,
                "Notificación: Se le ha asignado una recomendación",
                "asignacion-template",
                variables);
    }

    /**
     * Envía correo al nuevo responsable notificando su reasignación a una recomendación.
     */
    public void sendCorreoReasignacion(String to, Map<String, Object> variables) {
        this.sendTemplateEmail(to,
                "Notificación: Se le ha reasignado una recomendación",
                "reasignacion-template",
                variables);
    }

    /**
     * Envía notificación a la auditora operativa cuando se registra un verificable pendiente de revisión.
     */
    public boolean sendCorreoVerificablePendienteAuditor(String to, Map<String, Object> variables) {
        return this.sendTemplateEmail(
                to,
                "Notificación: Verificable pendiente de revisión - SISREC",
                "correo-verificable-pendiente",
                variables);
    }

    /**
     * Obtiene los correos de los directores asociados a un informe.
     */
    public String[] obtenerCorreosDirectoresByInforme(Long ainin_codigo) {
        try {
            List<String> correos = this.rolUsuarioRepositorio.getCorreosDirectoresByInforme(ainin_codigo);
            return correos != null && !correos.isEmpty() ? correos.toArray(new String[0]) : new String[0];
        } catch (Exception e) {
            logger.error("[MAIL] Error al obtener correos de directores por informe {}: {}", ainin_codigo,
                    e.getMessage(), e);
            return new String[0];
        }
    }

    /**
     * Obtiene el correo del auditor responsable de un informe.
     */
    private String obtenerAuditorResponsable(Long ainin_codigo) {
        try {
            return this.rolUsuarioRepositorio.getCorreoAuditorResponsable(ainin_codigo);
        } catch (Exception e) {
            logger.error("[MAIL] Error al obtener auditor responsable del informe {}: {}", ainin_codigo, e.getMessage(),
                    e);
            return null;
        }
    }

    /**
     * Envía el detalle de recomendaciones a cada responsable del informe indicado.
     * Usa el microservicio ws_correo para el envío.
     */
    public ResponseEntity<String> sendCorreoDetalleRecomendaciones(Long ainin_codigo, String subject) {
        try {
            System.out.println("=== [DEBUG] sendCorreoDetalleRecomendaciones INICIO ===");
            System.out.println("[DEBUG] ainin_codigo: " + ainin_codigo);
            System.out.println("[DEBUG] subject: " + subject);

            List<DetalleRecomendacionesPorInformeDTO> data = this.recomendacionServicio
                    .obtRecomendacionesPorInformeDTO(ainin_codigo);

            System.out.println("[DEBUG] data obtenida, tamaño: " + (data != null ? data.size() : "NULL"));

            if (data.isEmpty()) {
                System.out.println("[DEBUG] data está vacía, retornando NO_CONTENT");
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("No hay recomendaciones disponibles para enviar.");
            }

            List<String> todosResponsables = new ArrayList<>();
            for (DetalleRecomendacionesPorInformeDTO det : data) {
                if (det.getResponsableDTO() != null) {
                    String nom = det.getResponsableDTO().getNombres() != null ? det.getResponsableDTO().getNombres() : "";
                    String ape = det.getResponsableDTO().getApellidos() != null ? det.getResponsableDTO().getApellidos() : "";
                    String nombreCompleto = (nom + " " + ape).trim();
                    if (!nombreCompleto.isEmpty() && !todosResponsables.contains(nombreCompleto)) {
                        todosResponsables.add(nombreCompleto);
                    }
                }
            }

            ArrayList<String> listaResponsables = new ArrayList<>();
            int enviados = 0, fallidos = 0;

            for (int i = 0; i < data.size(); i++) {
                DetalleRecomendacionesPorInformeDTO detalle = data.get(i);
                System.out.println("\n[DEBUG] --- Procesando responsable #" + (i + 1) + " ---");
                try {
                    String correoResponsable = detalle.getResponsableDTO().getCorreo();
                    System.out.println("[DEBUG] correoResponsable: " + correoResponsable);
                    System.out.println("[DEBUG] nombres: " + detalle.getResponsableDTO().getNombres());
                    System.out.println("[DEBUG] apellidos: " + detalle.getResponsableDTO().getApellidos());
                    System.out.println("[DEBUG] usuarioPer: " + detalle.getResponsableDTO().getUsuarioPer());

                    if (correoResponsable == null || correoResponsable.isEmpty()) {
                        System.out.println("[DEBUG] Correo vacío o null, saltando...");
                        fallidos++;
                        continue;
                    }

                    Map<String, Object> variables = new HashMap<>();
                    variables.put("fecha", UtilsFunctions.getDateHour());
                    variables.put("datos", detalle);
                    System.out.println("[DEBUG] variables armadas: " + variables.keySet());

                    System.out.println("[DEBUG] Llamando sendTemplateEmail con plantilla 'email-template'...");
                    boolean enviado = this.sendTemplateEmail(correoResponsable, subject, "email-template", variables);
                    System.out.println("[DEBUG] resultado sendTemplateEmail: " + enviado);

                    if (enviado) {
                        String nom = (detalle.getResponsableDTO() != null && detalle.getResponsableDTO().getNombres() != null)
                                ? detalle.getResponsableDTO().getNombres() : "";
                        String ape = (detalle.getResponsableDTO() != null && detalle.getResponsableDTO().getApellidos() != null)
                                ? detalle.getResponsableDTO().getApellidos() : "";
                        String nombreCompleto = (nom + " " + ape).trim();
                        if (!nombreCompleto.isEmpty() && !listaResponsables.contains(nombreCompleto)) {
                            listaResponsables.add(nombreCompleto);
                        }
                        enviados++;
                        System.out.println("[DEBUG] Enviado OK. Total enviados hasta ahora: " + enviados);

                        // Notificar también en el sistema (campanita + WebSocket) para formulación de Plan de Acción
                        try {
                            String usuarioPer = detalle.getResponsableDTO() != null ? detalle.getResponsableDTO().getUsuarioPer() : null;
                            if (usuarioPer != null && !usuarioPer.trim().isEmpty()) {
                                String memo = "";
                                if (detalle.getInformeDTO() != null) {
                                    memo = (detalle.getInformeDTO().getMemorando() != null && !detalle.getInformeDTO().getMemorando().trim().isEmpty())
                                            ? detalle.getInformeDTO().getMemorando()
                                            : (detalle.getInformeDTO().getMemoAprobacion() != null ? detalle.getInformeDTO().getMemoAprobacion() : "");
                                }
                                String mensajePlan = "Dispone de un plazo de hasta 48 horas posteriores a esta notificación para formular y registrar su Plan de Acción en el sistema SISREC"
                                        + (memo != null && !memo.trim().isEmpty() ? (" para el informe con memorando " + memo) : "") + ".";

                                NuevaNotificacionDTO notifPlan = new NuevaNotificacionDTO(
                                        "Registro de Plan de Acción",
                                        mensajePlan,
                                        "/inicio/plan-accion-responsables",
                                        TipoNotificacionEnum.PLAN_ACCION_REQUERIDO,
                                        java.time.LocalDate.now().getYear(),
                                        ainin_codigo,
                                        null,
                                        usuarioPer.trim()
                                );
                                this.notificacionesServicio.registrarNotificacion(notifPlan);
                                this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + usuarioPer.trim(),
                                        "Tiene una nueva notificación: Registro obligatorio de Plan de Acción (plazo máximo 48 horas).");
                                logger.info("[MAIL-NOTIF] Notificación en sistema de Plan de Acción registrada y enviada a responsable {}", usuarioPer);
                            }
                        } catch (Exception exNotif) {
                            logger.error("[MAIL-NOTIF] Error al registrar notificación de Plan de Acción en sistema: {}", exNotif.getMessage());
                        }
                    } else {
                        fallidos++;
                        System.out.println("[DEBUG] Envío fallido. Total fallidos hasta ahora: " + fallidos);
                    }

                } catch (Exception e) {
                    fallidos++;
                    System.out.println("[DEBUG] EXCEPCIÓN en responsable #" + (i + 1) + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("\n[DEBUG] RESUMEN — enviados: " + enviados + " | fallidos: " + fallidos);

            if (enviados == 0) {
                System.out.println("[DEBUG] ⚠ enviados == 0, retornando INTERNAL_SERVER_ERROR");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("No se pudo enviar ningún correo. Fallidos: " + fallidos);
            }

            if (enviados > 0) {
                this.informeRepositorio.findById(ainin_codigo).ifPresent(informe -> {
                    informe.setAinin_notificado('1');
                    this.informeRepositorio.save(informe);
                });
            }

            // Notificar al auditor responsable con la lista de responsables notificados
            List<String> responsablesParaAuditor = !listaResponsables.isEmpty() ? listaResponsables : todosResponsables;
            boolean auditorNotificado = this.enviarCorreoAuditoresPorInforme(ainin_codigo, responsablesParaAuditor);
            if (auditorNotificado && !data.isEmpty() && data.get(0).getInformeDTO() != null) {
                String memo = (data.get(0).getInformeDTO().getMemorando() != null && !data.get(0).getInformeDTO().getMemorando().trim().isEmpty())
                        ? data.get(0).getInformeDTO().getMemorando()
                        : data.get(0).getInformeDTO().getMemoAprobacion();
                this.enviarCorreoDirectoresByInforme(
                        ainin_codigo,
                        data.get(0).getInformeDTO().getTitulo(),
                        memo);
            }

            return ResponseEntity.ok(
                    "Usuarios notificados correctamente. Enviados: " + enviados + " | Fallidos: " + fallidos);

        } catch (Exception e) {
            System.out.println("[DEBUG] EXCEPCIÓN GENERAL en sendCorreoDetalleRecomendaciones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Hubo un error al enviar los correos.");
        }
    }

    /**
     * Envía notificación al auditor responsable del informe indicando
     * qué responsables fueron notificados. Usa ws_correo.
     */
    private boolean enviarCorreoAuditoresPorInforme(Long ainin_codigo, List<String> responsables) {
        try {
            String auditorResponsable = this.obtenerAuditorResponsable(ainin_codigo);
            if (auditorResponsable == null || auditorResponsable.trim().isEmpty()) {
                logger.warn("[MAIL] No se encontró auditor responsable para el informe {}", ainin_codigo);
                return false;
            }

            Map<String, Object> variables = new HashMap<>();
            variables.put("auditor", this.rolUsuarioRepositorio.getNombreAuditorResponsable(ainin_codigo));
            variables.put("responsables", (responsables != null && !responsables.isEmpty()) ? responsables : new ArrayList<>());
            variables.put("fecha", UtilsFunctions.getDateHour());

            List<DetalleRecomendacionesPorInformeDTO> data = this.recomendacionServicio
                    .obtRecomendacionesPorInformeDTO(ainin_codigo);
            if (!data.isEmpty() && data.get(0).getInformeDTO() != null) {
                variables.put("datos", data.get(0));
                variables.put("tituloInforme", data.get(0).getInformeDTO().getTitulo());
                String memo = (data.get(0).getInformeDTO().getMemorando() != null && !data.get(0).getInformeDTO().getMemorando().trim().isEmpty())
                        ? data.get(0).getInformeDTO().getMemorando()
                        : data.get(0).getInformeDTO().getMemoAprobacion();
                variables.put("memoAprobacion", memo);
            }

            logger.info("[MAIL] Enviando notificación al auditor {} para informe {} con {} responsable(s): {}",
                    auditorResponsable, ainin_codigo, (responsables != null ? responsables.size() : 0), variables.get("responsables"));

            // Enviar a través del microservicio ws_correo usando plantilla remota
            return this.sendTemplateEmail(
                    auditorResponsable,
                    "Notificación: Responsables del informe con código " + ainin_codigo + " notificados correctamente",
                    "correo-auditores",
                    variables);

        } catch (Exception e) {
            logger.error("[MAIL] Error al enviar correo al auditor del informe {}: {}",
                    ainin_codigo, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Envía notificación a los directores departamentales asociados al informe.
     * Cada envío se realiza individualmente vía ws_correo para que un error
     * en un director no detenga el envío a los demás.
     */
    private boolean enviarCorreoDirectoresByInforme(Long ainin_codigo, String tituloInforme, String memoAprobacion) {
        String[] correos = this.obtenerCorreosDirectoresByInforme(ainin_codigo);
        int enviados = 0, fallidos = 0;

        for (String correo : correos) {
            try {
                DirectorInformacionDTO director = this.obtenerNombreDirectorDepartamental(correo);

                Map<String, Object> variables = new HashMap<>();
                variables.put("director", director.getDirector());
                variables.put("directorNombre", director.getDirector());
                variables.put("nombreDirector", director.getDirector());
                variables.put("departamento", director.getDepartamento());
                variables.put("departamentoNombre", director.getDepartamento());
                variables.put("nombreDepartamento", director.getDepartamento());
                variables.put("fecha", UtilsFunctions.getDateHour());
                variables.put("tituloInforme", tituloInforme);
                variables.put("memoAprobacion", memoAprobacion);

                // Enviar a través del microservicio ws_correo usando plantilla remota
                boolean enviado = this.sendTemplateEmail(
                        correo,
                        "Notificación: Los responsables de cumplimiento del departamento "
                                + director.getDepartamento() + " han sido notificados correctamente.",
                        "correo-directores",
                        variables);

                if (enviado) {
                    enviados++;
                } else {
                    fallidos++;
                }

            } catch (Exception e) {
                fallidos++;
                logger.error("[MAIL] Error al enviar correo al director {}: {}", correo, e.getMessage(), e);
            }
        }

        logger.info("[MAIL] Correos a directores — enviados: {}, fallidos: {}", enviados, fallidos);
        return fallidos == 0;
    }

    /**
     * Obtiene la información del director departamental por su correo.
     */
    private DirectorInformacionDTO obtenerNombreDirectorDepartamental(String correo) {
        return this.rolUsuarioServicio.obtenerInfoDirector(correo);
    }

    /**
     * Envía correos notificando el cambio de fecha máxima de cumplimiento
     * de una recomendación. Usa ws_correo.
     */
    public ResponseEntity<String> sendEmailDateChangedMax(Long arere_codigo, String subject) {
        return sendEmailDateChangedMax(arere_codigo, null, subject);
    }

    public ResponseEntity<String> sendEmailDateChangedMax(Long arere_codigo, Long arers_codigo, String subject) {
        try {
            System.out.println("=== [DEBUG] sendEmailDateChangedMax INICIO ===");
            System.out.println("[DEBUG] arere_codigo: " + arere_codigo);
            System.out.println("[DEBUG] arers_codigo: " + arers_codigo);
            System.out.println("[DEBUG] subject: " + subject);

            List<RecomendacionCambioFechaDTO> data = this.recomendacionServicio
                    .obtenerDetalleCambioFechaMaxCumplimientoProcesado(arere_codigo, arers_codigo);

            System.out.println("[DEBUG] data obtenida, tamaño: " + (data != null ? data.size() : "NULL"));

            if (data.isEmpty()) {
                System.out.println("[DEBUG] data vacía, retornando INTERNAL_SERVER_ERROR");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("No se encontró datos acerca de la recomendación solicitada.");
            }

            int enviados = 0, fallidos = 0;

            for (int i = 0; i < data.size(); i++) {
                RecomendacionCambioFechaDTO recomendacion = data.get(i);
                System.out.println("\n[DEBUG] --- Procesando recomendación #" + (i + 1) + " ---");
                try {
                    String correoResponsable = recomendacion.getResponsableCorreo();
                    System.out.println("[DEBUG] correoResponsable: " + correoResponsable);
                    System.out.println("[DEBUG] usuarioPer: " + recomendacion.getResponsableUsuarioPer());

                    if (correoResponsable == null || correoResponsable.isEmpty()) {
                        System.out.println("[DEBUG] Correo vacío o null, saltando...");
                        fallidos++;
                        continue;
                    }

                    Map<String, Object> variables = new HashMap<>();
                    variables.put("fecha", UtilsFunctions.getDateHour());
                    variables.put("recomendacion", recomendacion);
                    System.out.println("[DEBUG] variables armadas: " + variables.keySet());

                    System.out.println("[DEBUG] Llamando sendTemplateEmail con plantilla 'fecha-maxima'...");
                    boolean enviado = this.sendTemplateEmail(correoResponsable, subject, "fecha-maxima", variables);
                    System.out.println("[DEBUG] resultado sendTemplateEmail: " + enviado);

                    if (enviado) {
                        enviados++;
                        System.out.println("[DEBUG] Enviado OK. Total enviados hasta ahora: " + enviados);
                    } else {
                        fallidos++;
                        System.out.println("[DEBUG] Envío fallido. Total fallidos hasta ahora: " + fallidos);
                    }

                } catch (Exception e) {
                    fallidos++;
                    System.out.println("[DEBUG] EXCEPCIÓN en recomendación #" + (i + 1) + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("\n[DEBUG] RESUMEN — enviados: " + enviados + " | fallidos: " + fallidos);

            if (enviados == 0) {
                System.out.println("[DEBUG] enviados == 0, retornando INTERNAL_SERVER_ERROR");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("No se pudo enviar ningún correo. Fallidos: " + fallidos);
            }

            return ResponseEntity.ok("Usuarios notificados acerca del cambio de fecha."
                    + " Enviados: " + enviados + " | Fallidos: " + fallidos);

        } catch (Exception e) {
            System.out.println("[DEBUG] EXCEPCIÓN GENERAL en sendEmailDateChangedMax: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Hubo un error al enviar los correos.");
        }
    }
}