package ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.dtos.NotificacionesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.dtos.NuevaNotificacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.enums.TipoNotificacionEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.mapper.NotificacionesMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.models.Notificaciones;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.enums.RolEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class NotificacionesServicio {
    private static final Logger logger = LoggerFactory.getLogger(NotificacionesServicio.class);
    private static final int MAX_TITULO = 200;
    private static final int MAX_MENSAJE = 500;
    private static final int MAX_URL = 500;
    private static final Pattern AUDITOR_ASIGNADO_PATTERN = Pattern.compile("\\(auditor asignado\\)\\(([^)]+)\\)");
    private static final Pattern ANIO_PATTERN = Pattern.compile("\\b(20\\d{2})\\b");
    @Autowired
    NotificacionesRepositorio notificacionesRepositorio;
    @Autowired
    NotificacionesMapper notificacionesMapper;
    @Autowired
    BitacoraServicio bitacoraServicio;
    @Autowired
    private RolUsuarioRepositorio rolUsuarioRepositorio;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    
    @Transactional
    public Notificaciones registrarNotificacion(NuevaNotificacionDTO dto){
        try {
            dto = sanitizarLongitudes(dto);
            logger.info("[Notificaciones] registrarNotificacion - titulo='{}', usuarioDestino='{}'",
                    dto.getAnotif_titulo(), dto.getAnotif_usuario_per());

            Notificaciones notificacion = this.notificacionesMapper.convertirDTOModel(dto);
            Notificaciones respuesta = this.notificacionesRepositorio.save(notificacion);
            logger.info("[Notificaciones] Notificación principal guardada, codigo={}", 
                    respuesta != null ? respuesta.getAnotif_codigo() : null);

            // Si la notificación va dirigida a un Auditor Operativo,
            // replicarla también para el Auditor Interno Jefe activo.
            try {
                String usuarioDestino = dto.getAnotif_usuario_per();
                if (usuarioDestino != null && !usuarioDestino.trim().isEmpty()) {
                    String usuarioNormalizado = usuarioDestino.trim();
                    logger.info("[Notificaciones] Verificando roles para usuarioDestino='{}' (normalizado='{}')",
                            usuarioDestino, usuarioNormalizado);

                    Integer cantidadRolesAuditorOperativo = rolUsuarioRepositorio
                            .findByDsgus_cuentaAndAsero_codigo(usuarioNormalizado,
                                    RolEnum.AUDITOR_OPERATIVO.getCode());

                    logger.info("[Notificaciones] Resultado rol AUDITOR_OPERATIVO para '{}': cantidad={}",
                            usuarioNormalizado, cantidadRolesAuditorOperativo);

                    Integer cantidadRolesAuditorJefe = rolUsuarioRepositorio
                            .findByDsgus_cuentaAndAsero_codigo(usuarioNormalizado,
                                    RolEnum.AUDITOR_JEFE_INTERNO.getCode());

                    boolean esDestinatarioJefe = cantidadRolesAuditorJefe != null && cantidadRolesAuditorJefe > 0;

                    boolean esNotificacionNoReplicable = TipoNotificacionEnum.RECOMENDACION_ASIGNADA.equals(dto.getAnotif_tipo())
                            || TipoNotificacionEnum.RECOMENDACION_REASIGNADA.equals(dto.getAnotif_tipo())
                            || TipoNotificacionEnum.PLAN_ACCION_REQUERIDO.equals(dto.getAnotif_tipo())
                            || TipoNotificacionEnum.PLAN_ACCION_RECORDATORIO.equals(dto.getAnotif_tipo())
                            || TipoNotificacionEnum.PLAN_ACCION_VENCIDO.equals(dto.getAnotif_tipo())
                            || TipoNotificacionEnum.SOLICITUD_ELIMINADA_AUDITOR.equals(dto.getAnotif_tipo())
                            || TipoNotificacionEnum.RECOMENDACION_CUMPLIDA.equals(dto.getAnotif_tipo())
                            || dto.getAnotif_padre_id() != null;

                    if (!esNotificacionNoReplicable && !esDestinatarioJefe && cantidadRolesAuditorOperativo != null && cantidadRolesAuditorOperativo > 0) {
                        List<String> auditoresJefe = rolUsuarioRepositorio.getCuentasAuditoresJefe();
                        logger.info("[Notificaciones] Auditores Jefe activos ({}): {}", 
                                auditoresJefe != null ? auditoresJefe.size() : 0,
                                auditoresJefe);

                        if (auditoresJefe != null && !auditoresJefe.isEmpty()) {
                            for (String auditorJefe : auditoresJefe) {
                                if (auditorJefe == null || auditorJefe.trim().isEmpty()
                                        || auditorJefe.trim().equalsIgnoreCase(usuarioNormalizado)) {
                                    logger.info("[Notificaciones] AuditorJefe='{}' omitido (vacío o igual al destino)",
                                            auditorJefe);
                                    continue;
                                }

                                logger.info("[Notificaciones] Replicando notificación para AuditorJefe='{}'",
                                        auditorJefe.trim());

                                String sufijoJefe = " (auditor asignado)(" + usuarioNormalizado + ")";
                                String mensajeBase = dto.getAnotif_mensaje() != null ? dto.getAnotif_mensaje() : "";
                                String mensajeParaJefe = mensajeBase;
                                if (!mensajeBase.contains("(auditor asignado)(")) {
                                    int maxTextoOriginal = MAX_MENSAJE - sufijoJefe.getBytes(StandardCharsets.UTF_8).length;
                                    if (maxTextoOriginal > 0) {
                                        mensajeParaJefe = truncar(mensajeBase, maxTextoOriginal) + sufijoJefe;
                                    } else {
                                        mensajeParaJefe = truncar(mensajeBase, MAX_MENSAJE);
                                    }
                                }

                                NuevaNotificacionDTO dtoAuditorJefe = new NuevaNotificacionDTO(
                                        dto.getAnotif_titulo(),
                                        mensajeParaJefe,
                                        dto.getAnotif_url_sistema(),
                                        dto.getAnotif_tipo(),
                                        dto.getAnotif_anio(),
                                        dto.getAnotif_ainin_codigo(),
                                        dto.getAnotif_arere_codigo(),
                                        auditorJefe.trim());

                                dtoAuditorJefe.setAnotif_titulo(truncar(dtoAuditorJefe.getAnotif_titulo(), MAX_TITULO));
                                dtoAuditorJefe.setAnotif_url_sistema(truncar(dtoAuditorJefe.getAnotif_url_sistema(), MAX_URL));
                                dtoAuditorJefe.setAnotif_tipo(truncar(dtoAuditorJefe.getAnotif_tipo(), 50));
                                if (respuesta != null) {
                                    dtoAuditorJefe.setAnotif_padre_id(respuesta.getAnotif_codigo());
                                }

                                Notificaciones notificacionAuditorJefe = this.notificacionesMapper
                                        .convertirDTOModel(dtoAuditorJefe);
                                this.notificacionesRepositorio.save(notificacionAuditorJefe);
                                logger.info("[Notificaciones] Notificación replicada guardada para AuditorJefe='{}'",
                                        auditorJefe.trim());

                                // Notificación en tiempo real para el Auditor Interno Jefe
                                try {
                                    String mensajeSocket = mensajeParaJefe != null
                                        ? mensajeParaJefe
                                            : "Tiene una nueva notificación.";
                                    simpMessagingTemplate.convertAndSend(
                                            "/topic/notificacion/" + auditorJefe.trim(),
                                            mensajeSocket);
                                } catch (Exception e) {
                                    logger.error(
                                            "[Notificaciones] Error al enviar la notificación en tiempo real al Auditor Interno Jefe {}: {}",
                                            auditorJefe, e.getMessage());
                                }
                            }
                        } else {
                            logger.warn("[Notificaciones] No se encontraron Auditores Jefe activos para replicar la notificación.");
                        }
                    } else {
                        logger.info("[Notificaciones] UsuarioDestino='{}' NO tiene rol AUDITOR_OPERATIVO, no se replica al AI Jefe.",
                                usuarioNormalizado);
                    }
                } else {
                    logger.warn("[Notificaciones] UsuarioDestino nulo o vacío, no se puede evaluar réplica al AI Jefe.");
                }
            } catch (Exception e) {
                logger.error("[Notificaciones] Error general al replicar notificación para el Auditor Interno Jefe: {}", e.getMessage());
            }

            return respuesta;
        } catch (Exception e) {
            logger.error("Error al registrar notificacion: {}", e.getMessage());
            return null;
        }
    }

    private NuevaNotificacionDTO sanitizarLongitudes(NuevaNotificacionDTO dto) {
        if (dto == null) {
            return null;
        }
        dto.setAnotif_titulo(truncar(dto.getAnotif_titulo(), MAX_TITULO));
        dto.setAnotif_mensaje(truncar(dto.getAnotif_mensaje(), MAX_MENSAJE));
        dto.setAnotif_url_sistema(truncar(dto.getAnotif_url_sistema(), MAX_URL));
        dto.setAnotif_tipo(truncar(dto.getAnotif_tipo(), 50));
        return dto;
    }

    private String truncar(String valor, int maxLen) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.getBytes(StandardCharsets.UTF_8).length <= maxLen) {
            return limpio;
        }

        int end = limpio.length();
        while (end > 0 && limpio.substring(0, end).getBytes(StandardCharsets.UTF_8).length > maxLen) {
            end--;
        }
        return limpio.substring(0, end);
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerNotificacionesPorUsuario(String anotif_usuario_per){
        return obtenerNotificacionesPorUsuarioConLimite(anotif_usuario_per, 100);
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerNotificacionesPorUsuarioConLimite(String anotif_usuario_per, int limite){
        int finalLimit = limite > 0 ? limite : 100;
        try {
            Integer cantidadRolesAuditorJefe = rolUsuarioRepositorio
                    .findByDsgus_cuentaAndAsero_codigo(anotif_usuario_per, RolEnum.AUDITOR_JEFE_INTERNO.getCode());
            boolean esAuditorInternoJefe = cantidadRolesAuditorJefe != null && cantidadRolesAuditorJefe > 0;

            // Backfill: si el operativo ya leyó la notificación original, marcar la réplica del jefe como leída.
            if (esAuditorInternoJefe) {
                this.notificacionesRepositorio.sincronizarNotificacionesJefePorPadreLeido(anotif_usuario_per);
            }

            List<Tuple> data = this.notificacionesRepositorio.obtenerNotificacionesPorUsuarioConLimite(anotif_usuario_per, finalLimit);

            List<NotificacionesDTO> notificaciones =  data.stream().map( tuple -> {
                String tipo = tuple.get("anotif_tipo", String.class);
                Integer anio = convertToInteger(tuple.get("anotif_anio", Object.class));
                if (anio == null) {
                    String mensaje = tuple.get("anotif_mensaje", String.class);
                    if (mensaje != null) {
                        java.util.regex.Matcher m = ANIO_PATTERN.matcher(mensaje);
                        if (m.find()) {
                            try { anio = Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
                        }
                    }
                }
                
                Integer codigo = convertToInteger(tuple.get("anotif_codigo", Object.class));
                String titulo = tuple.get("anotif_titulo", String.class);
                String mensaje = tuple.get("anotif_mensaje", String.class);
                Integer estadoVal = convertToInteger(tuple.get("anotif_estado", Object.class));
                Date fechaCreacion = tuple.get("anotif_fecha_creacion", Date.class);
                Date fechaLeida = tuple.get("anotif_fecha_leida", Date.class);
                String urlSistema = tuple.get("anotif_url_sistema", String.class);
                String usuarioPer = tuple.get("anotif_usuario_per", String.class);
                Long arereCodigo = convertToLong(tuple.get("anotif_arere_codigo", Object.class));
                String auditorAsignado = esAuditorInternoJefe ? extractAuditorAsignado(mensaje, arereCodigo) : null;
                Long aininCodigo = convertToLong(tuple.get("anotif_ainin_codigo", Object.class));
                Long anotifPadreId = convertToLong(tuple.get("anotif_padre_id", Object.class));

                return new NotificacionesDTO(
                    codigo,
                    titulo,
                    mensaje,
                    estadoVal,
                    fechaCreacion,
                    fechaLeida,
                    urlSistema,
                    tipo,
                    anio,
                    usuarioPer,
                    auditorAsignado,
                    aininCodigo,
                    arereCodigo,
                    anotifPadreId
                );
            }).collect(Collectors.toList());

            return EstadoRespuestas.respuestaObtencion("Notificaciones Obtenidas exitosamente.", notificaciones);
        } catch (Exception e) {
            logger.error("Error al obtener las notificaciones por usuario: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al obtener las notificaciones por usuario (notificaciones servicio).");
        }
    }
    
    // Método de conversión auxiliar
    private Integer convertToInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof BigDecimal) return ((BigDecimal) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        logger.error("Error al convertir a Integer: {}", value.getClass());
        return null;
    }

    private Long convertToLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof BigDecimal) return ((BigDecimal) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        logger.error("Error al convertir a Long: {}", value.getClass());
        return null;
    }

    private String extractAuditorAsignado(String mensaje, Long arereCodigo) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return null;
        }
        Matcher matcher = AUDITOR_ASIGNADO_PATTERN.matcher(mensaje);
        if (matcher.find()) {
            String auditor = matcher.group(1);
            return auditor != null && !auditor.trim().isEmpty() ? auditor.trim() : null;
        }
        return null;
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> actualizarEstadoNotificaciones(Long anotif_codigo){
        try {
            java.util.Optional<Notificaciones> notificacionOpt = this.notificacionesRepositorio.findById(anotif_codigo);
            if (notificacionOpt.isPresent()) {
                Notificaciones notificacion = notificacionOpt.get();
                String mensaje = notificacion.getAnotif_mensaje();

                // Validar permisos del usuario logueado para marcar como leída
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String usuarioLogueado = auth != null ? auth.getName() : null;
                if (usuarioLogueado != null) {
                    usuarioLogueado = usuarioLogueado.trim();
                    String auditorAsignado = extractAuditorAsignado(mensaje, notificacion.getAnotif_arere_codigo());
                    if (auditorAsignado != null && !auditorAsignado.trim().equalsIgnoreCase(usuarioLogueado)) {
                        return EstadoRespuestas.respuestaError("No tiene permisos para modificar el estado de esta notificación.");
                    }
                }

                // 1. Marcar como leída únicamente la notificación puntual del usuario
                this.notificacionesRepositorio.actualizarEstadoNotificaciones(anotif_codigo);

                // 2. Si la notificación leída tiene réplica en los jefes (es el padre), actualizar la réplica del jefe a "En revisión" (manteniéndola en estado=0)
                int replicasActualizadas = this.notificacionesRepositorio.actualizarReplicaJefeEnRevision(anotif_codigo);
                logger.info("[Notificaciones] Notificación {} marcada como leída. Réplicas del jefe puestas en revisión: {}", anotif_codigo, replicasActualizadas);

                // 3. Notificar a los Jefes via WebSocket para que refresquen su panel
                try {
                    List<String> jefes = rolUsuarioRepositorio.getCuentasAuditoresJefe();
                    if (jefes != null && !jefes.isEmpty()) {
                        String wsMsg = "refresh";
                        for (String jefe : jefes) {
                            if (jefe != null && !jefe.trim().isEmpty()) {
                                simpMessagingTemplate.convertAndSend(
                                        "/topic/notificacion/" + jefe.trim(), wsMsg);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("[Notificaciones] Error al notificar Jefes via WebSocket: {}", e.getMessage());
                }

                return EstadoRespuestas.respuestaObtencion("Notificacion actualizada correctamente.", anotif_codigo);
            }
            return EstadoRespuestas.noEncontrado("Notificacion no encontrada (Controlador).");
        } catch (Exception e) {
            logger.error("Error al actualizar el estado de la notificacion: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al intentar actualizar el estado de la notificacion con codigo: " + anotif_codigo);
        }
    }

    public ResponseEntity<LinkedHashMap<String, Object>> obtenerIdsPorMemoYNumRec(String memorando, Integer numRec) {
        try {
            logger.info("[obtenerIdsPorMemoYNumRec] Querying database for memorando='{}' and numRec={}", memorando, numRec);
            Tuple result = this.notificacionesRepositorio.obtenerIdsPorMemoYNumRec(memorando, numRec);
            if (result != null) {
                Integer ainin_codigo = convertToInteger(result.get("ainin_codigo", Object.class));
                Integer arere_codigo = convertToInteger(result.get("arere_codigo", Object.class));
                Integer areco_codigo = convertToInteger(result.get("areco_codigo", Object.class));
                Integer areha_codigo = convertToInteger(result.get("areha_codigo", Object.class));
                logger.info("[obtenerIdsPorMemoYNumRec] Database returned: ainin_codigo={}, arere_codigo={}", ainin_codigo, arere_codigo);
                
                LinkedHashMap<String, Object> responseData = new LinkedHashMap<>();
                responseData.put("ainin_codigo", ainin_codigo);
                responseData.put("arere_codigo", arere_codigo);
                responseData.put("areco_codigo", areco_codigo);
                responseData.put("areha_codigo", areha_codigo);
                return EstadoRespuestas.respuestaObtencion("IDs obtenidos exitosamente.", responseData);
            }
            logger.warn("[obtenerIdsPorMemoYNumRec] No database row found for memorando='{}' and numRec={}", memorando, numRec);
            return EstadoRespuestas.noEncontrado("IDs no encontrados para el memorando y número de recomendación indicados.");
        } catch (Exception e) {
            logger.error("Error al obtener IDs por memorando y numRec: {}", e.getMessage(), e);
            return EstadoRespuestas.respuestaError("Error al obtener IDs.");
        }
    }

    public ResponseEntity<LinkedHashMap<String, Object>> obtenerCadenaIdsPorArereCodigo(Long arereCodigo) {
        try {
            Tuple result = this.notificacionesRepositorio.obtenerCadenaIdsPorArereCodigo(arereCodigo);
            if (result != null) {
                Integer ainin_codigo = convertToInteger(result.get("ainin_codigo", Object.class));
                Integer arere_codigo = convertToInteger(result.get("arere_codigo", Object.class));
                Integer areco_codigo = convertToInteger(result.get("areco_codigo", Object.class));
                Integer areha_codigo = convertToInteger(result.get("areha_codigo", Object.class));

                LinkedHashMap<String, Object> responseData = new LinkedHashMap<>();
                responseData.put("ainin_codigo", ainin_codigo);
                responseData.put("arere_codigo", arere_codigo);
                responseData.put("areco_codigo", areco_codigo);
                responseData.put("areha_codigo", areha_codigo);
                return EstadoRespuestas.respuestaObtencion("Cadena de IDs obtenida exitosamente.", responseData);
            }
            return EstadoRespuestas.noEncontrado("IDs no encontrados para la recomendación.");
        } catch (Exception e) {
            logger.error("Error al obtener cadena de IDs: {}", e.getMessage(), e);
            return EstadoRespuestas.respuestaError("Error al obtener cadena de IDs.");
        }
    }

    @Transactional
    public ResponseEntity<LinkedHashMap<String, Object>> crearNotificacionesPrueba(String usuario) {
        try {
            // === SIN LEER (estado=0) ===

            // 1. Verificable pendiente — recién subido por responsable
            NuevaNotificacionDTO dto1 = new NuevaNotificacionDTO(
                "Verificable pendiente de revisión.",
                "MERCEDES ISABEL YANEZ BAYAS subió un verificable del informe con memorando PE-0686-2026 Num. Recomendacion: 1",
                TipoNotificacionEnum.VERIFICABLE_PENDIENTE,
                TipoNotificacionEnum.VERIFICABLE_PENDIENTE,
                2026, 2L, 2L, usuario);
            this.notificacionesRepositorio.save(notificacionesMapper.convertirDTOModel(dto1));

            // 2. Solicitud rechazada
            NuevaNotificacionDTO dto2 = new NuevaNotificacionDTO(
                "Solicitud rechazada",
                "Solicitud de la recomendacion num: 1, del informe: PE-0686-2026, ha sido RECHAZADA",
                TipoNotificacionEnum.SOLICITUD_RECHAZADA,
                TipoNotificacionEnum.SOLICITUD_RECHAZADA,
                2026, 2L, 2L, usuario);
            this.notificacionesRepositorio.save(notificacionesMapper.convertirDTOModel(dto2));

            // 3. Solicitud caducada
            NuevaNotificacionDTO dto3 = new NuevaNotificacionDTO(
                "Solicitud caducada",
                "Su solicitud de prórroga N° 123 (Rec: 1, memo: PE-0686-2026) ha sido rechazada automáticamente por caducidad.",
                TipoNotificacionEnum.SOLICITUD_CADUCADA,
                TipoNotificacionEnum.SOLICITUD_CADUCADA,
                2026, 2L, 2L, usuario);
            this.notificacionesRepositorio.save(notificacionesMapper.convertirDTOModel(dto3));

            // === YA LEÍDAS (estado=1) ===

            // 4. Verificable pendiente ATENDIDO sin cambios (checkbox)
            Notificaciones n4 = notificacionesMapper.convertirDTOModel(new NuevaNotificacionDTO(
                "Atendido: Pendiente de revisión",
                "MERCEDES ISABEL YANEZ BAYAS subió un verificable del informe con memorando PE-0686-2026 Num. Recomendacion: 1 — Atendido sin cambios en el verificable.",
                TipoNotificacionEnum.VERIFICABLE_PENDIENTE,
                TipoNotificacionEnum.VERIFICABLE_PENDIENTE,
                2026, 2L, 2L, usuario));
            n4.setAnotif_estado(1);
            n4.setAnotif_fecha_leida(new Date());
            this.notificacionesRepositorio.save(n4);

            // 5. Verificable JUSTIFICADO (auditor revisó y justificó)
            Notificaciones n5 = notificacionesMapper.convertirDTOModel(new NuevaNotificacionDTO(
                "Atendido: Verificable JUSTIFICADO",
                "MERCEDES ISABEL YANEZ BAYAS subió un verificable del informe con memorando PE-0686-2026 Num. Recomendacion: 1 — RESUELTO: JUSTIFICA por el auditor.",
                TipoNotificacionEnum.VERIFICABLE_PENDIENTE,
                TipoNotificacionEnum.VERIFICABLE_PENDIENTE,
                2026, 2L, 2L, usuario));
            n5.setAnotif_estado(1);
            n5.setAnotif_fecha_leida(new Date());
            this.notificacionesRepositorio.save(n5);

            // 6. Recomendación cumplida
            Notificaciones n6 = notificacionesMapper.convertirDTOModel(new NuevaNotificacionDTO(
                "Recomendación cumplida",
                "La recomendación N° 1 del informe PE-0686-2026 ha sido marcada como CUMPLIDA.",
                TipoNotificacionEnum.RECOMENDACION_CUMPLIDA,
                TipoNotificacionEnum.RECOMENDACION_CUMPLIDA,
                2026, 2L, 2L, usuario));
            n6.setAnotif_estado(1);
            n6.setAnotif_fecha_leida(new Date());
            this.notificacionesRepositorio.save(n6);

            return EstadoRespuestas.respuestaObtencion("6 notificaciones de prueba: 3 sin leer + 3 leídas para: " + usuario, null);
        } catch (Exception e) {
            logger.error("Error al crear notificaciones de prueba: {}", e.getMessage());
            return EstadoRespuestas.respuestaError("Error al crear notificaciones de prueba.");
        }
    }
}
