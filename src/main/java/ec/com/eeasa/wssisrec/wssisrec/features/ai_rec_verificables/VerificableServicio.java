package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.enums.RolEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.InformeServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models.Informe;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.NotificacionesRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.NotificacionesServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.dtos.NuevaNotificacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.enums.TipoNotificacionEnum;
import java.util.Calendar;
import java.util.Date;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.DetalleServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.enums.EstadoSegEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.EstadoVerRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.enums.EstadoVerEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.models.EstadoVerificable;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.RecomendacionServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.RecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.ResponsableServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models.Responsable;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.ActualizarVerificableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.FiltroVerificableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.NuevoVerificableADTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.NotificarEnvioVerificablesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.RecomendacionVerificableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.ValidarVerificableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.VerificableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.mapper.VerificableMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.models.Verificable;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.ConfiguracionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.enums.ConfiguracionEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.models.Configuracion;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_historial.HistorialRecomendacionServicio;

import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.FileMapper;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.Ftp.FtpService;

@Service
public class VerificableServicio {
  private static final Logger logger = LoggerFactory.getLogger(VerificableServicio.class);

  @Autowired
  private BitacoraServicio bitacoraServicio;

  @Autowired
  private HistorialRecomendacionServicio historialServicio;

  @Autowired
  private FtpService ftpService;

  @Autowired
  private VerificableRepositorio verificableRepositorio;

  @Autowired
  private ConfiguracionRepositorio configuracionRepositorio;

  @Autowired
  private VerificableMapper verificableMapper;

  @Autowired
  private EstadoVerRepositorio estadoVerRepositorio;

  @Autowired
  private ResponsableServicio responsableServicio;

  @Autowired
  @Lazy
  private RecomendacionServicio recomendacionServicio;

  @Autowired
  private DetalleServicio detalleServicio;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private InformeServicio informeServicio;

  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  @Autowired
  private NotificacionesServicio notificacionesServicio;

  @Autowired
  private NotificacionesRepositorio notificacionesRepositorio;

  @Autowired
  private RolUsuarioRepositorio rolUsuarioRepositorio;

  @Autowired
  @Lazy
  private ec.com.eeasa.wssisrec.wssisrec.services.EmailService emailService;

  // Cooldown de 20 minutos para envíos de correo por informe, recomendación, responsable y auditor
  private static final long TIEMPO_COOLDOWN_MINUTOS = 20;
  private static final ConcurrentHashMap<String, Instant> ultimoCorreoEnviadoCooldown = new ConcurrentHashMap<>();
//
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> registrarVerificableResponsable(
      NuevoVerificableADTO nuevoVerificableDTO) {
    boolean conexionEstablecida = false;
    try {

      // Obtener el detalle recomendacion
      DetalleRecomendacion detalleRecomendacion = detalleServicio
          .obtenerPorRecomendacionResponsablePer(nuevoVerificableDTO.getArere_codigo(),
              nuevoVerificableDTO.getArers_codigo_per());
              
      // Verificar si el detalle de recomendación existe
      if (detalleRecomendacion == null) {
        return EstadoRespuestas.respuestaError("No se encontró el detalle de recomendación para el responsable especificado");
      }

      boolean esVerificableAdicional = (nuevoVerificableDTO.getAreev_codigo() != null &&
          nuevoVerificableDTO.getAreev_codigo().equals(EstadoVerEnum.ADICIONAL.getCodigo())) ||
          (detalleRecomendacion.getRecomendacion() != null &&
           detalleRecomendacion.getRecomendacion().getEstadoSeg() != null &&
           EstadoSegEnum.CUMPLIDA.getCodigo().equals(detalleRecomendacion.getRecomendacion().getEstadoSeg().getArees_codigo()));

      if (esVerificableAdicional && nuevoVerificableDTO.getAreev_codigo() == null) {
        nuevoVerificableDTO.setAreev_codigo(EstadoVerEnum.ADICIONAL.getCodigo());
      }
      if (esVerificableAdicional && (nuevoVerificableDTO.getAreve_motivo_estado() == null || nuevoVerificableDTO.getAreve_motivo_estado().trim().isEmpty())) {
        nuevoVerificableDTO.setAreve_motivo_estado("Adicional");
      }

      // Validar el tiempo maximo de cumplimiento (solo si no es verificable adicional)
      if (!esVerificableAdicional && recomendacionServicio.validarFechaMaxima(detalleRecomendacion)) {
        return EstadoRespuestas.respuestaError("Fecha máxima cumplida");
      }

      // Convertir auditoria
      AuditoriaDTO auditoriaDTO = convertirAuditoriaJSONDTO(nuevoVerificableDTO.getAuditoria());

      // Obtener la configuracion ftp
      Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).get();

      // Validar tamaño del archivo antes de conectar al FTP
      if (!ftpService.validarTamanoArchivo(nuevoVerificableDTO.getAreve_adjunto(), configuracion.getAseco_size_file_mb())) {
        double tamanoArchivoMB = ftpService.obtenerTamanoArchivoMB(nuevoVerificableDTO.getAreve_adjunto());
        return EstadoRespuestas.respuestaError(
          String.format("El archivo excede el límite permitido. Tamaño del archivo: %.2f MB, Límite máximo: %d MB", 
            tamanoArchivoMB, configuracion.getAseco_size_file_mb()));
      }

      Long codEstadoCreacion = (nuevoVerificableDTO.getAreev_codigo() != null)
          ? nuevoVerificableDTO.getAreev_codigo()
          : (esVerificableAdicional ? EstadoVerEnum.ADICIONAL.getCodigo() : EstadoVerEnum.PENDIENTE_REVISION.getCodigo());
      EstadoVerificable estadoSeg = estadoVerRepositorio.findById(codEstadoCreacion)
          .orElseGet(() -> {
            if (EstadoVerEnum.PENDIENTE_REVISION.getCodigo().equals(codEstadoCreacion)) {
              try {
                EstadoVerificable nuevo = new EstadoVerificable();
                nuevo.setAreev_codigo(EstadoVerEnum.PENDIENTE_REVISION.getCodigo());
                nuevo.setAreev_descripcion("PENDIENTE DE REVISIÓN");
                nuevo.setAreev_color("#f59e0b");
                nuevo.setAreev_usuario("ROOTSISREC");
                nuevo.setAreev_fecha(new Date());
                nuevo.setAreev_estado('1');
                return estadoVerRepositorio.save(nuevo);
              } catch (Exception ex) {
                logger.warn("No se pudo crear automáticamente estado PENDIENTE DE REVISIÓN en BD: {}", ex.getMessage());
              }
            }
            return estadoVerRepositorio.findById(EstadoVerEnum.PENDIENTE.getCodigo()).orElse(null);
          });

      // Conectar al servidor DTP
      Boolean estaConectado = ftpService.conectarServidorFTP(configuracion.getAseco_host(),
          configuracion.getAseco_user(),
          configuracion.getAseco_contrasenia());
      if (!estaConectado) {
        return EstadoRespuestas.respuestaError("Error del servidor externo (FTP/NAS): No se pudo establecer conexión. Consulte con el administrador del sistema.");
      }
      
      conexionEstablecida = true;

      // Metodo renombrar el archivo
      String nombreArchivo = FileMapper.getFileName(nuevoVerificableDTO.getAreve_adjunto().getOriginalFilename(),
          "verificable");

      // Metodo renombrar el archivo
      Informe informe = informeServicio.obtenerInformeByRecomendacion(nuevoVerificableDTO.getArere_codigo());
      // Obtener códigos y descripciones adicionales
      Long ainin_codigo = informe.getAinin_codigo();
      String organismoControl = informe.getOrganismoControl().getAinoc_descripcion();

      // Metodo retorna la ruta relativa
      String rutaRelativa = FileMapper.obtenerRutaArchivoFtp(configuracion.getAseco_directorio(),
          organismoControl, ainin_codigo,
          "Verificables");

      // REGISTRAR EL ARCHIVO Y DEVUELVE SI SE REGISTRO
      Boolean respuesCrearArchivo = ftpService.subirArchivoAlServidorFTP(nuevoVerificableDTO.getAreve_adjunto(),
          rutaRelativa, nombreArchivo);

      if (!respuesCrearArchivo) {
        ftpService.desconectarServidorFTP();
        conexionEstablecida = false;
        return EstadoRespuestas.respuestaError(("Error al crear el archivo"));
      }

      ftpService.desconectarServidorFTP();
      conexionEstablecida = false;

      // CREAR NUEVA RUTA PARA GUARDAR EN LA BASE DE DATOS
      String rutaArchivo = FileMapper.obtenerRutaArchivoBd(configuracion.getAseco_nombre_host(), rutaRelativa,
          nombreArchivo);

      Verificable verificable = verificableMapper.convertirCrearResDTOModel(nuevoVerificableDTO, estadoSeg,
          auditoriaDTO,
          detalleRecomendacion, rutaArchivo);

      Verificable respuesta = verificableRepositorio.save(verificable);
      recomendacionServicio.actualizarEstadoRecomendacion(respuesta.getAreve_codigo());

      String nombreResponsable = "No asignado";
      try {
        nombreResponsable = verificableRepositorio.findNombreResponsable(nuevoVerificableDTO.getArers_codigo_per()).orElse("No asignado").trim();
      } catch (Exception ex) {
        logger.error("Error al obtener nombre del responsable: {}", ex.getMessage());
      }

      // Registrar en el historial de recomendaciones
      historialServicio.registrarEvento(
          nuevoVerificableDTO.getArere_codigo(),
          "ENVIO_VERIFICABLE",
          "Se cargó el archivo verificable: \"" + respuesta.getAreve_descripcion() + "\" (Adjunto: " + nombreArchivo + ") por el responsable: " + nombreResponsable + ".",
          null, nombreResponsable, null, null, null, null,
          auditoriaDTO.getUsuario()
      );

      bitacoraServicio.registrarBitacora(respuesta, auditoriaDTO, "CREAR");
      if (respuesta.getAreve_codigo() != null) {
        String auditor = verificableRepositorio.findAuditorByRecomendacion(nuevoVerificableDTO.getArere_codigo()).orElse("").trim();
        String nombreResponsableDet = verificableRepositorio.findNombreResponsable(nuevoVerificableDTO.getArers_codigo_per()).orElse("No asignado").trim();
        String memorando = verificableRepositorio.findMemorandoByRecomendacion(nuevoVerificableDTO.getArere_codigo()).orElse("").trim();
        String num_rec = verificableRepositorio.findNumRecByRecomendacion(nuevoVerificableDTO.getArere_codigo()).orElse("").trim();

        logger.info("[Verificable] Registrado verificable AREVE_CODIGO={}, auditor='{}', responsable='{}', memorando='{}', numRec='{}'",
            respuesta.getAreve_codigo(), auditor, nombreResponsableDet, memorando, num_rec);

        boolean omitirNotifYCorreo = Boolean.TRUE.equals(nuevoVerificableDTO.getOmitirCorreo());

        if (!omitirNotifYCorreo) {
          if (!auditor.isEmpty()) {
            simpMessagingTemplate.convertAndSend("/topic/notificacion/" + auditor, "Tiene un verificable pendiente de revisar.");
          }
          String mensaje = "Verificable del responsable " + nombreResponsableDet + " con memorando " + memorando + " Num. Recomendacion: " + num_rec + " pendiente de revisión.";

          logger.info("[Verificable] Enviando NuevaNotificacionDTO al auditor='{}' con mensaje='{}'", auditor, mensaje);

          Integer anioVerificable = anioSeguro(detalleRecomendacion.getRecomendacion().getArere_fecha_inicio_control(), informe.getAinin_otanio());
          NuevaNotificacionDTO notificacion = new NuevaNotificacionDTO("Verificable pendiente de revisión.", mensaje, TipoNotificacionEnum.VERIFICABLE_PENDIENTE, TipoNotificacionEnum.VERIFICABLE_PENDIENTE, anioVerificable, ainin_codigo, nuevoVerificableDTO.getArere_codigo(), auditor);
          notificacionesServicio.registrarNotificacion(notificacion);

          // Enviar correo electrónico a la auditora operativa responsable
          try {
            String correoAuditor = null;
            if (!auditor.isEmpty()) {
              correoAuditor = rolUsuarioRepositorio.getCorreoByCuenta(auditor);
            }
            if (correoAuditor == null || correoAuditor.trim().isEmpty()) {
              correoAuditor = rolUsuarioRepositorio.getCorreoAuditorResponsable(ainin_codigo);
            }

            if (correoAuditor != null && !correoAuditor.trim().isEmpty()) {
              String claveControl = "INF_" + ainin_codigo + "_REC_" + nuevoVerificableDTO.getArere_codigo() 
                  + "_RESP_" + nuevoVerificableDTO.getArers_codigo_per() + "_AUD_" + correoAuditor.trim().toLowerCase();

              Instant ahora = Instant.now();
              Instant ultimoEnvio = ultimoCorreoEnviadoCooldown.get(claveControl);

              boolean debeEnviarCorreo = (ultimoEnvio == null) 
                  || Duration.between(ultimoEnvio, ahora).toMinutes() >= TIEMPO_COOLDOWN_MINUTOS;

              if (debeEnviarCorreo) {
                String nombreAuditor = null;
                if (!auditor.isEmpty()) {
                  nombreAuditor = rolUsuarioRepositorio.getNombreByCuenta(auditor);
                }
                if (nombreAuditor == null || nombreAuditor.trim().isEmpty()) {
                  nombreAuditor = rolUsuarioRepositorio.getNombreAuditorResponsable(ainin_codigo);
                }

                java.util.Map<String, Object> vars = new java.util.HashMap<>();
                vars.put("fecha", ec.com.eeasa.wssisrec.wssisrec.shared.utils.UtilsFunctions.getDateHour());
                vars.put("auditor", nombreAuditor != null ? nombreAuditor : auditor);
                vars.put("tituloInforme", informe != null && informe.getAinin_titulo() != null ? informe.getAinin_titulo() : "");
                vars.put("memoAprobacion", memorando != null ? memorando : "");
                vars.put("numRecomendacion", num_rec != null ? num_rec : "");
                vars.put("recDescripcion", detalleRecomendacion.getRecomendacion() != null && detalleRecomendacion.getRecomendacion().getArere_descripcion() != null 
                    ? detalleRecomendacion.getRecomendacion().getArere_descripcion() : "");
                vars.put("nombreResponsable", nombreResponsableDet != null ? nombreResponsableDet : "");
                vars.put("descripcionVerificable", respuesta.getAreve_descripcion() != null ? respuesta.getAreve_descripcion() : "Verificable cargado");
                vars.put("nombreArchivo", nombreArchivo != null ? nombreArchivo : "Documento adjunto");

                emailService.sendCorreoVerificablePendienteAuditor(correoAuditor, vars);
                ultimoCorreoEnviadoCooldown.put(claveControl, ahora);
                logger.info("[Verificable] Correo de verificable pendiente enviado exitosamente al auditor: {} (Clave: {})", correoAuditor, claveControl);

                // Limpieza preventiva si el mapa crece
                if (ultimoCorreoEnviadoCooldown.size() > 500) {
                  ultimoCorreoEnviadoCooldown.entrySet().removeIf(entry -> 
                      Duration.between(entry.getValue(), ahora).toMinutes() >= TIEMPO_COOLDOWN_MINUTOS * 2);
                }
              } else {
                long minutosTranscurridos = Duration.between(ultimoEnvio, ahora).toMinutes();
                logger.info("[Verificable] Correo omitido por enfriamiento temporal (hace {} min se notificó esta recomendación al auditor {}. Ventana: {} min. Clave: {})", 
                    minutosTranscurridos, correoAuditor, TIEMPO_COOLDOWN_MINUTOS, claveControl);
              }
            } else {
              logger.warn("[Verificable] No se encontró correo para el auditor del informe {} (auditor='{}')", ainin_codigo, auditor);
            }
          } catch (Exception eMail) {
            logger.error("[Verificable] Error al enviar correo de verificable pendiente al auditor: {}", eMail.getMessage(), eMail);
          }
        }
      } 
      return EstadoRespuestas.respuestaCreacion("Verificable registrado exitosamente");
    } catch (Exception e) {
      logger.error("Error al registrar verificable: {}", e.getMessage());
      if (conexionEstablecida) {
        try {
          ftpService.desconectarServidorFTP();
        } catch (Exception ex) {
          logger.error("Error al desconectar del servidor FTP: {}", ex.getMessage());
        }
      }
      return EstadoRespuestas.respuestaError("Error al registrar verificable: " + e.getMessage());
    }
  }
//
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> registarVerificableAuditor(
      NuevoVerificableADTO nuevoVerificableDTO) {
    boolean conexionEstablecida = false;
    try {
      // Obtener el detalle recomendacion
      DetalleRecomendacion detalleRecomendacion = detalleServicio
          .obtenerPorRecomendacionResponsablePer(nuevoVerificableDTO.getArere_codigo(),
              nuevoVerificableDTO.getArers_codigo_per());    
      // Verificar si el detalle de recomendación existe
      if (detalleRecomendacion == null) {
        return EstadoRespuestas.respuestaError("No se encontró el detalle de recomendación para el responsable especificado");
      }

      boolean esVerificableAdicional = (nuevoVerificableDTO.getAreev_codigo() != null &&
          nuevoVerificableDTO.getAreev_codigo().equals(EstadoVerEnum.ADICIONAL.getCodigo())) ||
          (detalleRecomendacion.getRecomendacion() != null &&
           detalleRecomendacion.getRecomendacion().getEstadoSeg() != null &&
           EstadoSegEnum.CUMPLIDA.getCodigo().equals(detalleRecomendacion.getRecomendacion().getEstadoSeg().getArees_codigo()));

      if (esVerificableAdicional && nuevoVerificableDTO.getAreev_codigo() == null) {
        nuevoVerificableDTO.setAreev_codigo(EstadoVerEnum.ADICIONAL.getCodigo());
      }
      if (esVerificableAdicional && (nuevoVerificableDTO.getAreve_motivo_estado() == null || nuevoVerificableDTO.getAreve_motivo_estado().trim().isEmpty())) {
        nuevoVerificableDTO.setAreve_motivo_estado("Adicional");
      }

      // Validar el tiempo maximo de cumplimiento (solo si no es verificable adicional)
      if (!esVerificableAdicional && recomendacionServicio.validarFechaMaxima(detalleRecomendacion)) {
        return EstadoRespuestas.respuestaError("Fecha máxima cumplida");
      }

      // Convertir auditoria
      AuditoriaDTO auditoriaDTO = convertirAuditoriaJSONDTO(nuevoVerificableDTO.getAuditoria());

      // Obtener la configuracion ftp
      Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo())
          .get();

      // Validar tamaño del archivo antes de conectar al FTP
      if (!ftpService.validarTamanoArchivo(nuevoVerificableDTO.getAreve_adjunto(), configuracion.getAseco_size_file_mb())) {
        double tamanoArchivoMB = ftpService.obtenerTamanoArchivoMB(nuevoVerificableDTO.getAreve_adjunto());
        return EstadoRespuestas.respuestaError(
          String.format("El archivo excede el límite permitido. Tamaño del archivo: %.2f MB, Límite máximo: %d MB", 
            tamanoArchivoMB, configuracion.getAseco_size_file_mb()));
      }

      Long codEstadoCreacion = (nuevoVerificableDTO.getAreev_codigo() != null)
          ? nuevoVerificableDTO.getAreev_codigo()
          : (esVerificableAdicional ? EstadoVerEnum.ADICIONAL.getCodigo() : EstadoVerEnum.PENDIENTE_REVISION.getCodigo());
      EstadoVerificable estadoSeg = estadoVerRepositorio.findById(codEstadoCreacion)
          .orElseGet(() -> {
            if (EstadoVerEnum.PENDIENTE_REVISION.getCodigo().equals(codEstadoCreacion)) {
              try {
                EstadoVerificable nuevo = new EstadoVerificable();
                nuevo.setAreev_codigo(EstadoVerEnum.PENDIENTE_REVISION.getCodigo());
                nuevo.setAreev_descripcion("PENDIENTE DE REVISIÓN");
                nuevo.setAreev_color("#f59e0b");
                nuevo.setAreev_usuario("ROOTSISREC");
                nuevo.setAreev_fecha(new Date());
                nuevo.setAreev_estado('1');
                return estadoVerRepositorio.save(nuevo);
              } catch (Exception ex) {
                logger.warn("No se pudo crear automáticamente estado PENDIENTE DE REVISIÓN en BD: {}", ex.getMessage());
              }
            }
            return estadoVerRepositorio.findById(EstadoVerEnum.PENDIENTE.getCodigo()).orElse(null);
          });

      // Conectar al servidor DTP
      Boolean estaConectado = ftpService.conectarServidorFTP(configuracion.getAseco_host(),
          configuracion.getAseco_user(),
          configuracion.getAseco_contrasenia());
      
      if (!estaConectado) {
        return EstadoRespuestas.respuestaError("Error del servidor externo (FTP/NAS): No se pudo establecer conexión. Consulte con el administrador del sistema.");
      }
      
      conexionEstablecida = true;

      // Metodo renombrar el archivo
      String nombreArchivo = FileMapper.getFileName(nuevoVerificableDTO.getAreve_adjunto().getOriginalFilename(),
          "verificable");

      // Metodo renombrar el archivo
      Informe informe = informeServicio.obtenerInformeByRecomendacion(nuevoVerificableDTO.getArere_codigo());
      // Obtener códigos y descripciones adicionales
      Long ainin_codigo = informe.getAinin_codigo();
      String organismoControl = informe.getOrganismoControl().getAinoc_descripcion();

      // Metodo retorna la ruta relativa
      String rutaRelativa = FileMapper.obtenerRutaArchivoFtp(configuracion.getAseco_directorio(),
          organismoControl, ainin_codigo,
          "Verificables");

      // REGISTRAR EL ARCHIVO Y DEVUELVE SI SE REGISTRO
      Boolean respuesCrearArchivo = ftpService.subirArchivoAlServidorFTP(nuevoVerificableDTO.getAreve_adjunto(),
          rutaRelativa, nombreArchivo);

      if (!respuesCrearArchivo) {
        ftpService.desconectarServidorFTP();
        return EstadoRespuestas.respuestaError(("Error al crear el archivo"));
      }

      ftpService.desconectarServidorFTP();
      conexionEstablecida = false;

      // CREAR NUEVA RUTA PARA GUARDAR EN LA BASE DE DATOS
      String rutaArchivo = FileMapper.obtenerRutaArchivoBd(configuracion.getAseco_nombre_host(), rutaRelativa,
          nombreArchivo);

      Verificable verificable = verificableMapper.convertirCrearAudDTOModel(nuevoVerificableDTO,
          auditoriaDTO,
          detalleRecomendacion, estadoSeg, rutaArchivo);

      Verificable respuesta = verificableRepositorio.save(verificable);
      recomendacionServicio.actualizarEstadoRecomendacion(respuesta.getAreve_codigo());
     
      String auditorNombre = historialServicio.obtenerNombreCompletoPorUsuario(auditoriaDTO.getUsuario());
      String nombreResponsable = "No asignado";
      try {
        if (nuevoVerificableDTO.getArers_codigo_per() != null) {
          nombreResponsable = verificableRepositorio.findNombreResponsable(nuevoVerificableDTO.getArers_codigo_per()).orElse("No asignado").trim();
        }
      } catch (Exception ex) {
        logger.error("Error al obtener nombre del responsable: {}", ex.getMessage());
      }

      // Registrar en el historial de recomendaciones
      historialServicio.registrarEvento(
          nuevoVerificableDTO.getArere_codigo(),
          "ENVIO_VERIFICABLE",
          "Se cargó el verificable por el auditor: \"" + respuesta.getAreve_descripcion() + "\" (Adjunto: " + nombreArchivo + ") por: " + auditorNombre + ".",
          null, nombreResponsable, null, null, null, null,
          auditoriaDTO.getUsuario()
      );

      bitacoraServicio.registrarBitacora(respuesta, auditoriaDTO, "CREAR");

      // Notificar al responsable y replicar a Jefes
      if (respuesta.getAreve_codigo() != null) {
        try {
          String responsable = verificableRepositorio.findResponsableByVerificable(respuesta.getAreve_codigo()).orElse(null);
          if (responsable != null && !responsable.trim().isEmpty()) {
            String memorando = verificableRepositorio.findMemorandoByRecomendacion(nuevoVerificableDTO.getArere_codigo()).orElse("(sin memorando)").trim();
            String numRec = verificableRepositorio.findNumRecByRecomendacion(nuevoVerificableDTO.getArere_codigo()).orElse("?").trim();
            String estadoVerAud = obtenerEstadoVerificable(codEstadoCreacion);
            String mensaje = "El auditor ha subido un verificable del informe con memorando " + memorando + " Num. Recomendacion: " + numRec + " con estado " + estadoVerAud;
            simpMessagingTemplate.convertAndSend("/topic/notificacion/" + responsable, "Tiene una nueva notificación");
            Integer anioVerAud = anioSeguro(detalleRecomendacion.getRecomendacion().getArere_fecha_inicio_control(), informe.getAinin_otanio());
            notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
                "Verificable " + estadoVerAud + " (subido por auditor)",
                mensaje,
                TipoNotificacionEnum.VERIFICABLE_ACTUALIZADO,
                TipoNotificacionEnum.VERIFICABLE_ACTUALIZADO,
                anioVerAud,
                ainin_codigo,
                nuevoVerificableDTO.getArere_codigo(),
                responsable));
          }
        } catch (Exception notifEx) {
          logger.warn("Error al notificar verificable subido por auditor: {}", notifEx.getMessage());
        }
      }

      return EstadoRespuestas.respuestaCreacion("Verificable registrado exitosamente");
    } catch (Exception e) {
      logger.error("Error al registrar verificable: {}", e.getMessage());
      if (conexionEstablecida) {
        try {
          ftpService.desconectarServidorFTP();
        } catch (Exception ex) {
          logger.error("Error al desconectar del servidor FTP: {}", ex.getMessage());
        }
      }
      return EstadoRespuestas.respuestaError("Error al registrar verificable");
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarVerificableAuditor(Long areve_codigo,
      ActualizarVerificableDTO actVerificableDTO) {
    boolean conexionEstablecida = false;
    try {
      // Obtener el detalle recomendacion
      DetalleRecomendacion detalleRecomendacion = detalleServicio
          .obtenerPorRecomendacionResponsablePer(actVerificableDTO.getArere_codigo(),
              actVerificableDTO.getArers_codigo_per());
      
      if (detalleRecomendacion == null) {
        return EstadoRespuestas.respuestaError("No se encontró el detalle de recomendación para el responsable especificado");
      }

      // Validar el tiempo maximo de cumplimiento (Solo si NO es auditor jefe)
      if (!esUsuarioAuditorJefe() && recomendacionServicio.validarFechaMaxima(detalleRecomendacion)) {
        return EstadoRespuestas.respuestaError("Fecha máxima cumplida");
      }

      // // Validar el estado del detalle de recomendacion
      // if
      // (detalleServicio.validarEstadoSeguimiento(detalleRecomendacion.getAredr_codigo(),
      // EstadoSegEnum.CUMPLIDA.getCodigo())) {
      // return EstadoRespuestas.respuestaError("Estado de recomendación cumplido");
      // }
      // Metodo renombrar el archivo
      Informe informe = informeServicio.obtenerInformeByRecomendacion(actVerificableDTO.getArere_codigo());
      // Obtener códigos y descripciones adicionales
      Long ainin_codigo = informe.getAinin_codigo();
      String organismoControl = informe.getOrganismoControl().getAinoc_descripcion();

      // COnvertir auditoria
      AuditoriaDTO auditoriaDTO = convertirAuditoriaJSONDTO(actVerificableDTO.getAuditoria());
      if (auditoriaDTO == null) {
        return EstadoRespuestas.respuestaError("Error al procesar la información de auditoría");
      }

      // Verificar que el verificable existe
      if (!verificableRepositorio.existsById(areve_codigo)) {
        return EstadoRespuestas.noEncontrado("Verificable no encontrado");
      }

      // Obtener verificable antiguo
      Verificable verificableOld = verificableRepositorio.findById(areve_codigo).get();

      // Validar si el verificable fue subido por el responsable
      String usuarioCarga = verificableOld.getAreve_usuario() != null ? verificableOld.getAreve_usuario().trim() : "";
      String usuarioResp = "";
      if (verificableOld.getDetalleRecomendacion() != null && verificableOld.getDetalleRecomendacion().getResponsable() != null) {
        Responsable resp = verificableOld.getDetalleRecomendacion().getResponsable();
        if (resp.getArers_usuario_per() != null && !resp.getArers_usuario_per().trim().isEmpty()) {
          usuarioResp = resp.getArers_usuario_per().trim();
        } else if (resp.getArers_usuario() != null) {
          usuarioResp = resp.getArers_usuario().trim();
        }
      }
      String auditorRec = verificableRepositorio.findAuditorByRecomendacion(actVerificableDTO.getArere_codigo()).orElse("").trim();

      boolean subidoPorResponsable = !usuarioCarga.isEmpty() && !usuarioResp.isEmpty() && usuarioCarga.equalsIgnoreCase(usuarioResp);
      if (subidoPorResponsable && !usuarioCarga.equalsIgnoreCase(auditorRec)) {
        return EstadoRespuestas.respuestaError("No se puede editar este verificable porque fue cargado directamente por el responsable.");
      }

      // Obtener ruta del archivo en BD
      String rutaArchivoBD = verificableOld.getAreve_adjunto();

      if (actVerificableDTO.getAreve_adjunto() != null && !actVerificableDTO.getAreve_adjunto().isEmpty()) {
        // Obtener la configuración FTP
        Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).orElse(null);
        if (configuracion == null) {
          return EstadoRespuestas.respuestaError("No se encontró la configuración FTP");
        }

        // Validar tamaño del archivo antes de conectar al FTP
        if (!ftpService.validarTamanoArchivo(actVerificableDTO.getAreve_adjunto(), configuracion.getAseco_size_file_mb())) {
          double tamanoArchivoMB = ftpService.obtenerTamanoArchivoMB(actVerificableDTO.getAreve_adjunto());
          return EstadoRespuestas.respuestaError(
            String.format("El archivo excede el límite permitido. Tamaño del archivo: %.2f MB, Límite máximo: %d MB", 
              tamanoArchivoMB, configuracion.getAseco_size_file_mb()));
        }

        // Conectar al servidor FTP
        Boolean estaConectado = ftpService.conectarServidorFTP(
            configuracion.getAseco_host(),
            configuracion.getAseco_user(),
            configuracion.getAseco_contrasenia());
        if (!estaConectado) {
          return EstadoRespuestas.respuestaError("Error del servidor externo (FTP/NAS): No se pudo establecer conexión. Consulte con el administrador del sistema.");
        }
        
        conexionEstablecida = true;

        // Obtener la ruta del archivo anterior desde la base de datos
        String rutaRelativaFtp = verificableOld.getAreve_adjunto();

        // Validar que existe un archivo anterior para eliminar
        if (rutaRelativaFtp != null && !rutaRelativaFtp.trim().isEmpty()) {
          // Valida si el archivo contiene el nombre del host
          if (rutaRelativaFtp.contains(configuracion.getAseco_nombre_host())) {
            rutaRelativaFtp = rutaRelativaFtp.split(configuracion.getAseco_nombre_host())[1];
          }

          // Elimina el archivo en el servidor FTP
          boolean eliminacionExitosa = ftpService.eliminarArchivoDeServidorFTP(rutaRelativaFtp);
          if (!eliminacionExitosa) {
            ftpService.desconectarServidorFTP();
            conexionEstablecida = false;
            return EstadoRespuestas.respuestaError("Error al eliminar el archivo del NAS");
          }
        }

        // Obtener el nombre del archivo
        String nombreArchivo = FileMapper.getFileName(
            actVerificableDTO.getAreve_adjunto().getOriginalFilename(),
            "verificable");

        // Obtener la ruta relativa
        String rutaRelativa = FileMapper.obtenerRutaArchivoFtp(
            configuracion.getAseco_directorio(),
            organismoControl,
            ainin_codigo,
            "Verificables");

        // Registrar el archivo en el servidor FTP
        Boolean respuestaCrearArchivo = ftpService.subirArchivoAlServidorFTP(
            actVerificableDTO.getAreve_adjunto(),
            rutaRelativa,
            nombreArchivo);
        if (!respuestaCrearArchivo) {
          ftpService.desconectarServidorFTP();
          conexionEstablecida = false;
          return EstadoRespuestas.respuestaError("Error al crear el archivo");
        }

        // Desconectar del servidor FTP
        ftpService.desconectarServidorFTP();
        conexionEstablecida = false;

        // Crear nueva ruta para guardar en la base de datos
        rutaArchivoBD = FileMapper.obtenerRutaArchivoBd(
            configuracion.getAseco_nombre_host(),
            rutaRelativa,
            nombreArchivo);
      }

      // Verificar que el estado del verificable existe si fue especificado, sino mantener el actual
      EstadoVerificable estadoSeg;
      if (actVerificableDTO.getAreev_codigo() != null) {
        if (!estadoVerRepositorio.existsById(actVerificableDTO.getAreev_codigo())) {
          return EstadoRespuestas.respuestaError("Estado de verificable no encontrado");
        }
        estadoSeg = estadoVerRepositorio.findById(actVerificableDTO.getAreev_codigo()).get();
      } else {
        estadoSeg = verificableOld.getEstadoVer();
      }

      Verificable verificable = verificableMapper.convertirActualizarAudDTOModel(areve_codigo, verificableOld,
          estadoSeg,
          detalleRecomendacion,
          actVerificableDTO,
          rutaArchivoBD,
          auditoriaDTO);

      Verificable respuesta = verificableRepositorio.save(verificable);

      String responsableNombre = "Sin asignar";
      if (verificableOld.getDetalleRecomendacion() != null && verificableOld.getDetalleRecomendacion().getResponsable() != null) {
        responsableNombre = verificableOld.getDetalleRecomendacion().getResponsable().getArers_nombre() + " " +
                            verificableOld.getDetalleRecomendacion().getResponsable().getArers_apellido();
      }
      String auditorNombre = historialServicio.obtenerNombreCompletoPorUsuario(auditoriaDTO.getUsuario());

      // Registrar en el historial de recomendaciones
      if (actVerificableDTO.getAreev_codigo() != null) {
        historialServicio.registrarEvento(
            actVerificableDTO.getArere_codigo(),
            "VALIDACION_VERIFICABLE",
            "Se actualizó la validación del verificable de " + responsableNombre + " por el auditor: " + auditorNombre + ". Estado: " + obtenerEstadoVerificable(actVerificableDTO.getAreev_codigo()) + ".",
            null, responsableNombre, null, null, null,
            actVerificableDTO.getAreve_motivo_estado(),
            auditoriaDTO.getUsuario()
        );
      } else {
        historialServicio.registrarEvento(
            actVerificableDTO.getArere_codigo(),
            "EDICION_VERIFICABLE",
            "Se actualizó la información del verificable de " + responsableNombre + " por el auditor: " + auditorNombre + ".",
            null, responsableNombre, null, null, null,
            null,
            auditoriaDTO.getUsuario()
        );
      }

      // if (!recomendacionServicio.actualizarEstadoRecomendacion(
      // detalleRecomendacion.getRecomendacion().getArere_codigo())) {
      // return EstadoRespuestas.respuestaError("Error al actualizar los estados de
      // segumimiento de recomendaciones.");
      // }

      // Recalcular estado automáticamente (cumplida/no cumplida/en proceso)
      recomendacionServicio.actualizarEstadoRecomendacion(areve_codigo);
      // Notificacion de cambio de estado del verificable para el responsable y auditor
      String responsable = (verificableOld != null && verificableOld.getDetalleRecomendacion() != null && verificableOld.getDetalleRecomendacion().getResponsable() != null)
          ? verificableOld.getDetalleRecomendacion().getResponsable().getArers_usuario_per()
          : verificableRepositorio.findResponsableByVerificable(areve_codigo).orElse(null);
      if(responsable != null && actVerificableDTO.getAreev_codigo() != null){
        
        Optional<Object[]> resultOpt = verificableRepositorio.findMemorando_NumRec_ObservacionByVerificable(areve_codigo);
        
        if(resultOpt.isPresent()){
          Object[] result = resultOpt.get();
          
          // Separar los datos en variables individuales desde el Object[] anidado
          String memorando = "";
          String numRec = "";
          
          if(result.length >= 1 && result[0] instanceof Object[]) {
            Object[] dataArray = (Object[]) result[0];
            
            if(dataArray.length >= 1) {
              memorando = dataArray[0] != null ? dataArray[0].toString() : "";
              numRec = dataArray[1] != null ? dataArray[1].toString() : "";
            }
          }
          
          String estadoVerificable = obtenerEstadoVerificable(actVerificableDTO.getAreev_codigo());
          String nombreResponsableNotif = "";
          if (verificableOld != null && verificableOld.getDetalleRecomendacion() != null && verificableOld.getDetalleRecomendacion().getResponsable() != null) {
            nombreResponsableNotif = (verificableOld.getDetalleRecomendacion().getResponsable().getArers_nombre() + " " +
                                      verificableOld.getDetalleRecomendacion().getResponsable().getArers_apellido()).trim();
          }
          if (nombreResponsableNotif.isEmpty()) {
            nombreResponsableNotif = verificableRepositorio.findNombreResponsableByVerificable(areve_codigo).orElse("Responsable").trim();
          }

          this.simpMessagingTemplate.convertAndSend("/topic/notificacion/"+responsable, "Tiene una nueva notificación");
          Integer anioAct = anioSeguro(detalleRecomendacion.getRecomendacion().getArere_fecha_inicio_control(), informe.getAinin_otanio());
          String tituloEspecifico = "Verificable " + estadoVerificable;
          this.notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
            tituloEspecifico,
            "El estado de su verificable (Responsable: " + nombreResponsableNotif + ") del informe con memorando " + memorando + ", y num. recomendacion " + numRec + ", ha sido actualizado a " + estadoVerificable + " por el auditor.",
            TipoNotificacionEnum.VERIFICABLE_ACTUALIZADO,
            TipoNotificacionEnum.VERIFICABLE_ACTUALIZADO,
            anioAct,
            ainin_codigo,
            actVerificableDTO.getArere_codigo(),
            responsable
          ));

          // Actualizar la notificación original existente con la resolución y marcarla como leída (sin duplicar notificaciones)
          try {
            String tituloResuelto = "Atendido: Se cambió el estado del verificable a " + estadoVerificable;
            String mensajeAuditor = "Verificable del responsable " + nombreResponsableNotif + " con memorando " + memorando + " Num. Recomendacion: " + numRec + " — Atendido: estado cambiado a " + estadoVerificable + " por el auditor.";
            Integer numRecInt = null;
            try { numRecInt = Integer.parseInt(numRec); } catch (NumberFormatException e) {}
            if (memorando != null && !memorando.isEmpty() && numRecInt != null) {
              int upd = notificacionesRepositorio.actualizarNotificacionesPorArereCodigoConResultado(
                actVerificableDTO.getArere_codigo(), memorando, numRecInt, tituloResuelto, mensajeAuditor, nombreResponsableNotif
              );
              logger.info("[Verificable] Notificación actualizada a atendida: {} filas", upd);

              try {
                List<String> jefes = rolUsuarioRepositorio.getCuentasAuditoresJefe();
                if (jefes != null) {
                  for (String jefe : jefes) {
                    if (jefe != null && !jefe.trim().isEmpty()) {
                      this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + jefe.trim(), "refresh");
                    }
                  }
                }
                String usuarioEvaluador = auditoriaDTO != null ? auditoriaDTO.getUsuario() : null;
                if (usuarioEvaluador != null && !usuarioEvaluador.trim().isEmpty()) {
                  this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + usuarioEvaluador.trim(), "refresh");
                }
              } catch (Exception eWs) {
                logger.warn("[Verificable] No se pudo enviar WebSocket de refresco: {}", eWs.getMessage());
              }
            }
          } catch (Exception e) {
            logger.error("[Verificable] Error al actualizar notificación original a atendida: {}", e.getMessage());
          }
        } else {
          logger.warn("No se encontraron resultados para la consulta de verificable con código: {}", areve_codigo);
        }
      }

      bitacoraServicio.registrarBitacora(respuesta, auditoriaDTO, "CREAR");
      return EstadoRespuestas.respuestaCreacion("Verificable registrado exitosamente");
    } catch (Exception e) {
      logger.error("Error al actualizar verificable: {}", e.getMessage());
      if (conexionEstablecida) {
        try {
          ftpService.desconectarServidorFTP();
        } catch (Exception ex) {
          logger.error("Error al desconectar del servidor FTP: {}", ex.getMessage());
        }
      }
      return EstadoRespuestas.respuestaError("Error al actualizar verificable: " + e.getMessage());
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarVerificableResponsable(Long areve_codigo,
      ActualizarVerificableDTO actVerificableDTO) {
    boolean conexionEstablecida = false;
    try {
      // Obtener el detalle recomendación
      DetalleRecomendacion detalleRecomendacion = detalleServicio
          .obtenerPorRecomendacionResponsablePer(actVerificableDTO.getArere_codigo(),
              actVerificableDTO.getArers_codigo_per());
      
      if (detalleRecomendacion == null) {
        return EstadoRespuestas.respuestaError("No se encontró el detalle de recomendación para el responsable especificado");
      }

      // Validar el tiempo máximo de cumplimiento
      boolean fechaMaximaCumplida = recomendacionServicio.validarFechaMaxima(
          detalleRecomendacion);
      if (fechaMaximaCumplida) {
        return EstadoRespuestas.respuestaError("Fecha máxima cumplida");
      }

      // Verificar que el verificable existe
      if (!verificableRepositorio.existsById(areve_codigo)) {
        return EstadoRespuestas.noEncontrado("Verificable no encontrado");
      }

      // Obtener verificable antiguo
      Verificable verificableOld = verificableRepositorio.findById(areve_codigo).get();

      // Obtener ruta del archivo en BD
      String rutaArchivoBD = verificableOld.getAreve_adjunto();

      // Convertir auditoría
      AuditoriaDTO auditoriaDTO = convertirAuditoriaJSONDTO(actVerificableDTO.getAuditoria());
      if (auditoriaDTO == null) {
        return EstadoRespuestas.respuestaError("Error al procesar la información de auditoría");
      }

      Informe informe = informeServicio.obtenerInformeByRecomendacion(actVerificableDTO.getArere_codigo());
      // Obtener códigos y descripciones adicionales
      Long ainin_codigo = informe.getAinin_codigo();
      String organismoControl = informe.getOrganismoControl().getAinoc_descripcion();

      if (actVerificableDTO.getAreve_adjunto() != null && !actVerificableDTO.getAreve_adjunto().isEmpty()) {
        // Obtener la configuracion ftp
        Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).get();

        // Validar tamaño del archivo antes de conectar al FTP
        if (!ftpService.validarTamanoArchivo(actVerificableDTO.getAreve_adjunto(), configuracion.getAseco_size_file_mb())) {
          double tamanoArchivoMB = ftpService.obtenerTamanoArchivoMB(actVerificableDTO.getAreve_adjunto());
          return EstadoRespuestas.respuestaError(
            String.format("El archivo excede el límite permitido. Tamaño del archivo: %.2f MB, Límite máximo: %d MB", 
              tamanoArchivoMB, configuracion.getAseco_size_file_mb()));
        }

        // Conectar al servidor DTP
        boolean estaConectado = ftpService.conectarServidorFTP(configuracion.getAseco_host(),
            configuracion.getAseco_user(),
            configuracion.getAseco_contrasenia());

        if (!estaConectado) {
          return EstadoRespuestas.respuestaError("Error del servidor externo (FTP/NAS): No se pudo establecer conexión. Consulte con el administrador del sistema.");
        }
        
        conexionEstablecida = true;

        // Obtener la ruta del archivo anterior desde la base de datos
        String rutaRelativaFtp = verificableOld.getAreve_adjunto();

        // Validar que existe un archivo anterior para eliminar
        if (rutaRelativaFtp != null && !rutaRelativaFtp.trim().isEmpty()) {
          // VALIDA SI EL ARCHIVO CONTIENE EL NOMBRE_HOST
          if (rutaRelativaFtp.contains(configuracion.getAseco_nombre_host())) {
            rutaRelativaFtp = rutaRelativaFtp.split(configuracion.getAseco_nombre_host())[1];
          }
          // ELIMINA EL ARCHIVO CON LA RUTA RELATIVA DE LA BASE DE DATOS
          boolean eliminacionExitosa = ftpService.eliminarArchivoDeServidorFTP(rutaRelativaFtp);

          if (!eliminacionExitosa) {
            ftpService.desconectarServidorFTP();
            conexionEstablecida = false;
            return EstadoRespuestas.respuestaError("Error al eliminar el archivo del NAS");
          }
        }
        // Metodo renombrar el archivo
        String nombreArchivo = FileMapper.getFileName(actVerificableDTO.getAreve_adjunto().getOriginalFilename(),
            "verificable");

        // Metodo retorna la ruta relativa
        String rutaRelativa = FileMapper.obtenerRutaArchivoFtp(configuracion.getAseco_directorio(),
            organismoControl, ainin_codigo,
            "Verificables");

        // REGISTRAR EL ARCHIVO Y DEVUELVE SI SE REGISTRO
        Boolean respuesCrearArchivo = ftpService.subirArchivoAlServidorFTP(actVerificableDTO.getAreve_adjunto(),
            rutaRelativa, nombreArchivo);

        if (!respuesCrearArchivo) {
          ftpService.desconectarServidorFTP();
          conexionEstablecida = false;
          return EstadoRespuestas.respuestaError(("Error al crear el archivo"));
        }

        ftpService.desconectarServidorFTP();
        conexionEstablecida = false;

        // CREAR NUEVA RUTA PARA GUARDAR EN LA BASE DE DATOS
        rutaArchivoBD = FileMapper.obtenerRutaArchivoBd(configuracion.getAseco_nombre_host(), rutaRelativa,
            nombreArchivo);
      }
      Verificable verificable = verificableMapper.convertirActualizarResDTOModel(areve_codigo, verificableOld,
          actVerificableDTO,
          rutaArchivoBD, auditoriaDTO);
      Verificable respuesta = verificableRepositorio.save(verificable);
      recomendacionServicio.actualizarEstadoRecomendacion(respuesta.getAreve_codigo());

      String responsableNombre = "Sin asignar";
      if (verificableOld.getDetalleRecomendacion() != null && verificableOld.getDetalleRecomendacion().getResponsable() != null) {
        responsableNombre = verificableOld.getDetalleRecomendacion().getResponsable().getArers_nombre() + " " +
                            verificableOld.getDetalleRecomendacion().getResponsable().getArers_apellido();
      } else {
        responsableNombre = historialServicio.obtenerNombreCompletoPorUsuario(auditoriaDTO.getUsuario());
      }

      // Registrar en el historial de recomendaciones
      historialServicio.registrarEvento(
          actVerificableDTO.getArere_codigo(),
          "ENVIO_VERIFICABLE",
          "Se actualizó el verificable por el responsable: " + responsableNombre + ". Descripción: \"" + respuesta.getAreve_descripcion() + "\".",
          null, responsableNombre, null, null, null, null,
          auditoriaDTO.getUsuario()
      );

      bitacoraServicio.registrarBitacora(respuesta, auditoriaDTO, "ACTUALIZAR");

      // Notificar al auditor que el responsable actualizó su verificable
      try {
        String auditor = verificableRepositorio.findAuditorByRecomendacion(actVerificableDTO.getArere_codigo()).orElse(null);
        if (auditor != null && !auditor.trim().isEmpty()) {
          String memorando = verificableRepositorio.findMemorandoByRecomendacion(actVerificableDTO.getArere_codigo()).orElse("(sin memorando)").trim();
          String numRec = verificableRepositorio.findNumRecByRecomendacion(actVerificableDTO.getArere_codigo()).orElse("?").trim();
          String mensaje = "El responsable ha actualizado su verificable del informe con memorando " + memorando + " Num. Recomendacion: " + numRec;
          simpMessagingTemplate.convertAndSend("/topic/notificacion/" + auditor, "Tiene una nueva notificación");
          Integer anioUpd = anioSeguro(detalleRecomendacion.getRecomendacion().getArere_fecha_inicio_control(), informe.getAinin_otanio());
          notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
              "Verificable actualizado",
              mensaje,
              TipoNotificacionEnum.VERIFICABLE_ACTUALIZADO,
              TipoNotificacionEnum.VERIFICABLE_ACTUALIZADO,
              anioUpd,
              ainin_codigo,
              actVerificableDTO.getArere_codigo(),
              auditor));

          // Enviar correo electrónico a la auditora operativa notificando la actualización
          try {
            String correoAuditor = null;
            if (auditor != null && !auditor.trim().isEmpty()) {
              correoAuditor = rolUsuarioRepositorio.getCorreoByCuenta(auditor.trim());
            }
            if (correoAuditor == null || correoAuditor.trim().isEmpty()) {
              correoAuditor = rolUsuarioRepositorio.getCorreoAuditorResponsable(ainin_codigo);
            }

            if (correoAuditor != null && !correoAuditor.trim().isEmpty()) {
              String nombreAuditor = null;
              if (auditor != null && !auditor.trim().isEmpty()) {
                nombreAuditor = rolUsuarioRepositorio.getNombreByCuenta(auditor.trim());
              }
              if (nombreAuditor == null || nombreAuditor.trim().isEmpty()) {
                nombreAuditor = rolUsuarioRepositorio.getNombreAuditorResponsable(ainin_codigo);
              }

              java.util.Map<String, Object> vars = new java.util.HashMap<>();
              vars.put("fecha", ec.com.eeasa.wssisrec.wssisrec.shared.utils.UtilsFunctions.getDateHour());
              vars.put("auditor", nombreAuditor != null ? nombreAuditor : auditor);
              vars.put("tituloInforme", informe != null && informe.getAinin_titulo() != null ? informe.getAinin_titulo() : "");
              vars.put("memoAprobacion", memorando != null ? memorando : "");
              vars.put("numRecomendacion", numRec != null ? numRec : "");
              vars.put("recDescripcion", detalleRecomendacion.getRecomendacion() != null && detalleRecomendacion.getRecomendacion().getArere_descripcion() != null 
                  ? detalleRecomendacion.getRecomendacion().getArere_descripcion() : "");
              vars.put("nombreResponsable", responsableNombre != null ? responsableNombre : "");
              vars.put("descripcionVerificable", respuesta.getAreve_descripcion() != null ? respuesta.getAreve_descripcion() : "Verificable actualizado");
              String nomArch = actVerificableDTO.getAreve_adjunto() != null && !actVerificableDTO.getAreve_adjunto().isEmpty() 
                  ? actVerificableDTO.getAreve_adjunto().getOriginalFilename() : "Documento actualizado";
              vars.put("nombreArchivo", nomArch);

              emailService.sendCorreoVerificablePendienteAuditor(correoAuditor, vars);
              logger.info("[Verificable] Correo de verificable actualizado enviado al auditor: {}", correoAuditor);
            } else {
              logger.warn("[Verificable] No se encontró correo para el auditor del informe {} (auditor='{}')", ainin_codigo, auditor);
            }
          } catch (Exception eMail) {
            logger.error("[Verificable] Error al enviar correo de verificable actualizado al auditor: {}", eMail.getMessage(), eMail);
          }
        }
      } catch (Exception notifEx) {
        logger.warn("Error al notificar actualización de verificable por responsable: {}", notifEx.getMessage());
      }

      return EstadoRespuestas.respuestaCreacion("Verificable actualizado exitosamente");
    } catch (Exception e) {
      logger.error("Error al actualizar verificable: {}", e.getMessage());
      if (conexionEstablecida) {
        try {
          ftpService.desconectarServidorFTP();
        } catch (Exception ex) {
          logger.error("Error al desconectar del servidor FTP: {}", ex.getMessage());
        }
      }
      return EstadoRespuestas.respuestaError("Error al actualizar verificable: " + e.getMessage());
    }
  }

  @Transactional
  public RecomendacionVerificableDTO obtenerVerificables(FiltroVerificableDTO filtroDTO) {
    try {
      return jdbcTemplate.execute((Connection conn) -> {
        String functionCall = "{?=call ROOTSISREC.FN_VERIFICABLES_FILTROS(?,?)}";
        RecomendacionDTO recomendacion = new RecomendacionDTO();
        RecomendacionVerificableDTO recomendacionVerificableDTO = new RecomendacionVerificableDTO();
        List<VerificableDTO> verificableDTOs = new ArrayList<>();

        try (CallableStatement cs = conn.prepareCall(functionCall)) {
          cs.registerOutParameter(1, Types.REF_CURSOR);
          cs.setLong(2, filtroDTO.getArere_codigo());
          cs.setString(3, filtroDTO.getArers_codigo_per());
          cs.execute();

          try (ResultSet rs = (ResultSet) cs.getObject(1)) {
            BeanPropertyRowMapper<Verificable> mapper = new BeanPropertyRowMapper<>(Verificable.class);
            recomendacion = recomendacionServicio.obtenerPorId(filtroDTO.getArere_codigo());
            String auditorRecomendacion = "";
            try {
              auditorRecomendacion = verificableRepositorio.findAuditorByRecomendacion(filtroDTO.getArere_codigo()).orElse("").trim();
            } catch (Exception ex) {
              logger.warn("No se pudo obtener el auditor asignado a la recomendacion {}: {}", filtroDTO.getArere_codigo(), ex.getMessage());
            }

            while (rs.next()) {
              try {
                Verificable verificable = mapper.mapRow(rs, rs.getRow());
                verificable.setEstadoVer(estadoVerRepositorio.findById(rs.getLong("AREEV_CODIGO")).get());
                DetalleRecomendacion det = detalleServicio.obtenerDetalleById(rs.getLong("AREDR_CODIGO"));
                verificable.setDetalleRecomendacion(det);
                VerificableDTO verificableDTO = verificableMapper.convertirModelDTO(verificable);
                verificableDTO.setAredr_codigo(rs.getLong("AREDR_CODIGO"));
                if (det != null) {
                  Date fechaMaxInd = (det.getAredr_fecha_maxima() != null) 
                    ? det.getAredr_fecha_maxima() 
                    : (det.getRecomendacion() != null ? det.getRecomendacion().getArere_fecha_maxima() : null);
                  if (fechaMaxInd != null) {
                    verificableDTO.setAredr_fecha_maxima(ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper.convertirFechaFormato(fechaMaxInd));
                  }
                }
                verificableDTO.setAreve_fecha_validacion(
                    verificable.getAreve_fecha_validacion() != null 
                    ? verificable.getAreve_fecha_validacion().toString()
                    : null);
                verificableDTO.setResponsable(responsableServicio
                    .obtenerPorCodigo(verificable.getDetalleRecomendacion().getResponsable().getArers_codigo()));

                // Determinar si fue subido por el responsable o por la auditora operativa
                String usuarioCarga = verificable.getAreve_usuario() != null ? verificable.getAreve_usuario().trim() : "";
                String usuarioResp = "";
                if (verificableDTO.getResponsable() != null && verificableDTO.getResponsable().getArers_usuario() != null && !verificableDTO.getResponsable().getArers_usuario().trim().isEmpty()) {
                  usuarioResp = verificableDTO.getResponsable().getArers_usuario().trim();
                } else if (verificable.getDetalleRecomendacion() != null && verificable.getDetalleRecomendacion().getResponsable() != null) {
                  Responsable resp = verificable.getDetalleRecomendacion().getResponsable();
                  if (resp.getArers_usuario_per() != null && !resp.getArers_usuario_per().trim().isEmpty()) {
                    usuarioResp = resp.getArers_usuario_per().trim();
                  } else if (resp.getArers_usuario() != null) {
                    usuarioResp = resp.getArers_usuario().trim();
                  }
                }

                boolean subidoPorResponsable = false;
                if (!usuarioCarga.isEmpty() && !usuarioResp.isEmpty() && usuarioCarga.equalsIgnoreCase(usuarioResp)) {
                  subidoPorResponsable = true;
                }

                boolean subidoPorAuditor = !subidoPorResponsable && !usuarioCarga.isEmpty();
                boolean puedeEditar = subidoPorAuditor;

                verificableDTO.setAreve_usuario(usuarioCarga);
                verificableDTO.setAuditor_recomendacion(auditorRecomendacion);
                verificableDTO.setSubido_por_responsable(subidoPorResponsable);
                verificableDTO.setSubido_por_auditor(subidoPorAuditor);
                verificableDTO.setPuede_editar(puedeEditar);

                verificableDTOs.add(verificableDTO);
              } catch (Exception e) {
                logger.error("Error al obtener verificable: {}", e.getMessage());
              }
            }
            recomendacionVerificableDTO = new RecomendacionVerificableDTO();
            if (recomendacion != null) {
              recomendacionVerificableDTO.setArere_codigo(recomendacion.getArere_codigo());
              recomendacionVerificableDTO.setArere_descripcion(recomendacion.getArere_descripcion());
              recomendacionVerificableDTO.setArere_num_rec(recomendacion.getArere_num_rec());
              recomendacionVerificableDTO.setArere_evidencia_requerida(recomendacion.getArere_evidencia_requerida());
              recomendacionVerificableDTO.setArere_riesgo_verificado(recomendacion.getArere_riesgo_verificado());
              recomendacionVerificableDTO.setEstado_seguimiento(recomendacion.getEstado_seguimiento());
            }
            recomendacionVerificableDTO.setVerificables(verificableDTOs);

            String memorando = recomendacion != null ? recomendacion.getAinin_memorando() : null;
            String titulo = recomendacion != null ? recomendacion.getAinin_titulo() : null;
            if ((memorando == null || memorando.trim().isEmpty()) || (titulo == null || titulo.trim().isEmpty())) {
              try {
                Optional<Object[]> infInfo = verificableRepositorio.findInformeInfoByRecomendacion(filtroDTO.getArere_codigo());
                if (infInfo.isPresent()) {
                  Object[] arr = infInfo.get();
                  if (memorando == null || memorando.trim().isEmpty()) {
                    memorando = arr[0] != null ? arr[0].toString() : "";
                  }
                  if (titulo == null || titulo.trim().isEmpty()) {
                    titulo = arr[1] != null ? arr[1].toString() : "";
                  }
                }
              } catch (Exception ex) {
                logger.warn("No se pudo obtener memorando/titulo para recomendacion {}: {}", filtroDTO.getArere_codigo(), ex.getMessage());
              }
            }
            recomendacionVerificableDTO.setAinin_memorando(memorando);
            recomendacionVerificableDTO.setAinin_titulo(titulo);

          } catch (Exception e) {
            logger.error("Error al obtener verificable: {}", e.getMessage());
          }
        } catch (Exception e) {
          logger.error("Error al obtener verificable: {}", e.getMessage());
        } 
        return recomendacionVerificableDTO;
      });
    } catch (Exception e) {
        logger.error("Error al obtener verificable: {}", e.getMessage());
      return null;
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerVerificable(Long areve_codigo) {
    boolean conexionEstablecida = false;
    try {
      if (!verificableRepositorio.existsById(areve_codigo)) {
        return EstadoRespuestas.noEncontrado("Verificable no encontrado");
      }
      Verificable verificable = verificableRepositorio.findById(areve_codigo).get();
      VerificableDTO verificableDTO = verificableMapper.convertirModelDTO(verificable);
      verificableDTO.setResponsable(responsableServicio
          .obtenerPorCodigo(verificable.getDetalleRecomendacion().getResponsable().getArers_codigo()));
      Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).get();

      boolean conectado = ftpService.conectarServidorFTP(configuracion.getAseco_host(), configuracion.getAseco_user(),
          configuracion.getAseco_contrasenia());
      
      if (conectado) {
        conexionEstablecida = true;
        String rutaRelativaFtp = verificable.getAreve_adjunto();
        if (verificable.getAreve_adjunto() != null && verificable.getAreve_adjunto().contains(configuracion.getAseco_nombre_host())) {
          rutaRelativaFtp = verificable.getAreve_adjunto().split(configuracion.getAseco_nombre_host())[1];
        }
        byte[] archivoAdjunto = ftpService.descargarArchivoFTPNuevo(rutaRelativaFtp);
        verificableDTO.setAreve_archivo(archivoAdjunto);
        ftpService.desconectarServidorFTP();
        conexionEstablecida = false;
      } else {
        logger.warn("No se pudo conectar al servidor FTP/NAS para obtener el adjunto del verificable {}. Se retornarán los datos del formulario.", areve_codigo);
        verificableDTO.setAreve_archivo(null);
        verificableDTO.setAdvertencia("Servidor de archivos (FTP/NAS) no disponible (" + configuracion.getAseco_host() + "). No se pudo cargar el archivo adjunto, pero puede consultar/editar la información.");
      }
      
      return EstadoRespuestas.respuestaObtencion("Verificable obtenido exitosamente", verificableDTO);
    } catch (Exception e) {
      if (conexionEstablecida) {
        try {
          ftpService.desconectarServidorFTP();
        } catch (Exception ex) {
          logger.error("Error al desconectar del servidor FTP: {}", ex.getMessage());
        }
      }
      return EstadoRespuestas.respuestaError("Error al obtener verificable: " + e.getMessage());
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> validarVerificable(Long areve_codigo,
      ValidarVerificableDTO validarVerificableDTO) {
    try {
      // Validar que el motivo del estado no esté vacío
      if (validarVerificableDTO.getAreve_motivo_estado() == null || validarVerificableDTO.getAreve_motivo_estado().trim().isEmpty()) {
        return EstadoRespuestas.respuestaError("El motivo del estado de seguimiento es requerido");
      }

      // Existe verificable
      Verificable verificableOld = verificableRepositorio.findById(areve_codigo).get();

      boolean esAuditorJefe = esUsuarioAuditorJefe();

      // Validar el tiempo maximo de cumplimiento (Solo si NO es auditor jefe)
      if (!esAuditorJefe && recomendacionServicio
          .validarFechaMaxima(verificableOld.getDetalleRecomendacion())) {
        return EstadoRespuestas.respuestaError("Fecha máxima cumplida");
      }

      // Validar ya cumplida RECOMENDACION GENERAL (Solo si NO es auditor jefe)
      if (!esAuditorJefe && recomendacionServicio.validarEstadoSeg(EstadoSegEnum.CUMPLIDA.getCodigo(),
          verificableOld.getDetalleRecomendacion().getRecomendacion().getArere_codigo())) {
        return EstadoRespuestas.respuestaError("Estado de recomendación general cumplido");
      }

      // Obtener el modelo de estado seguimento
      EstadoVerificable estadoSeg = estadoVerRepositorio.findById(validarVerificableDTO.getAreev_codigo()).get();
      // Convertir DTO a modelo
      Verificable verificableNew = verificableMapper.convertirValidarDTOModel(areve_codigo, validarVerificableDTO,
          verificableOld, estadoSeg);

      Verificable respuesta = verificableRepositorio.save(verificableNew);

      String responsableNombre = "Sin asignar";
      if (verificableOld.getDetalleRecomendacion() != null && verificableOld.getDetalleRecomendacion().getResponsable() != null) {
        responsableNombre = verificableOld.getDetalleRecomendacion().getResponsable().getArers_nombre() + " " +
                            verificableOld.getDetalleRecomendacion().getResponsable().getArers_apellido();
      }
      String auditorNombre = historialServicio.obtenerNombreCompletoPorUsuario(validarVerificableDTO.getAuditoria().getUsuario());

      // Registrar en el historial de recomendaciones
      historialServicio.registrarEvento(
          verificableOld.getDetalleRecomendacion().getRecomendacion().getArere_codigo(),
          "VALIDACION_VERIFICABLE",
          "Evaluacion de verificable de " + responsableNombre + " por el auditor: " + auditorNombre + ". Estado: " + obtenerEstadoVerificable(validarVerificableDTO.getAreev_codigo()) + ".",
          null, responsableNombre, null, null, null,
          validarVerificableDTO.getAreve_motivo_estado(),
          validarVerificableDTO.getAuditoria().getUsuario()
      );

      // Recalcular estado automáticamente (cumplida/no cumplida/en proceso)
      recomendacionServicio.actualizarEstadoRecomendacion(areve_codigo);
      //Notificacion de cambio de estado del verificable para el responsable
      String responsable = (verificableOld != null && verificableOld.getDetalleRecomendacion() != null && verificableOld.getDetalleRecomendacion().getResponsable() != null)
          ? verificableOld.getDetalleRecomendacion().getResponsable().getArers_usuario_per()
          : verificableRepositorio.findResponsableByVerificable(areve_codigo).orElse(null);
      if(responsable != null){
        Optional<Object[]> resultOpt = verificableRepositorio.findMemorando_NumRec_ObservacionByVerificable(areve_codigo);
        
        if(resultOpt.isPresent()){
          Object[] result = resultOpt.get();
          
          // Separar los datos en variables individuales desde el Object[] anidado
          String memorando = "";
          String numRec = "";
          
          if(result.length >= 1 && result[0] instanceof Object[]) {
            Object[] dataArray = (Object[]) result[0];
            
            if(dataArray.length >= 1) {
              memorando = dataArray[0] != null ? dataArray[0].toString() : "";
              numRec = dataArray[1] != null ? dataArray[1].toString() : "";
            }
          }
          
          String estadoVerificable = obtenerEstadoVerificable(validarVerificableDTO.getAreev_codigo());
          if (responsableNombre == null || responsableNombre.trim().isEmpty()) {
            responsableNombre = verificableRepositorio.findNombreResponsableByVerificable(areve_codigo).orElse("Responsable").trim();
          }

          this.simpMessagingTemplate.convertAndSend("/topic/notificacion/"+responsable, "Tiene una nueva notificación");
          Integer anioVal = extraerAnio(verificableOld.getDetalleRecomendacion().getRecomendacion().getArere_fecha_inicio_control());
          if (anioVal == null) anioVal = Calendar.getInstance().get(Calendar.YEAR);
          String tituloEspecifico = "Verificable " + estadoVerificable;
          this.notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
            tituloEspecifico,
            "El estado de su verificable (Responsable: " + responsableNombre + ") del informe con memorando " + memorando + ", y num. recomendacion " + numRec + ", ha sido actualizado a " + estadoVerificable + " por el auditor.",
            TipoNotificacionEnum.VERIFICABLE_ACTUALIZADO,
            TipoNotificacionEnum.VERIFICABLE_ACTUALIZADO,
            anioVal,
            null,
            verificableOld.getDetalleRecomendacion().getRecomendacion().getArere_codigo(),
            responsable
          ));

          // Actualizar la notificación original existente con la resolución y marcarla como leída (sin duplicar notificaciones)
          try {
            String tituloResuelto = "Atendido: Se cambió el estado del verificable a " + estadoVerificable;
            String mensajeAuditor = "Verificable del responsable " + responsableNombre + " con memorando " + memorando + " Num. Recomendacion: " + numRec + " — Atendido: estado cambiado a " + estadoVerificable + " por el auditor.";
            Integer numRecInt = null;
            try { numRecInt = Integer.parseInt(numRec); } catch (NumberFormatException e) {}
            if (memorando != null && !memorando.isEmpty() && numRecInt != null) {
              int upd = notificacionesRepositorio.actualizarNotificacionesPorArereCodigoConResultado(
                verificableOld.getDetalleRecomendacion().getRecomendacion().getArere_codigo(), memorando, numRecInt, tituloResuelto, mensajeAuditor, responsableNombre
              );
              logger.info("[Verificable] Notificación actualizada a atendida: {} filas", upd);

              try {
                List<String> jefes = rolUsuarioRepositorio.getCuentasAuditoresJefe();
                if (jefes != null) {
                  for (String jefe : jefes) {
                    if (jefe != null && !jefe.trim().isEmpty()) {
                      this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + jefe.trim(), "refresh");
                    }
                  }
                }
                String usuarioEvaluador = validarVerificableDTO != null && validarVerificableDTO.getAuditoria() != null ? validarVerificableDTO.getAuditoria().getUsuario() : null;
                if (usuarioEvaluador != null && !usuarioEvaluador.trim().isEmpty()) {
                  this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + usuarioEvaluador.trim(), "refresh");
                }
              } catch (Exception eWs) {
                logger.warn("[Verificable] No se pudo enviar WebSocket de refresco: {}", eWs.getMessage());
              }
            }
          } catch (Exception e) {
            logger.error("[Verificable] Error al actualizar notificación original a atendida: {}", e.getMessage());
          }
        } else {
          logger.warn("No se encontraron resultados para la consulta de verificable con código: {}", areve_codigo);
        }
      }

      bitacoraServicio.registrarBitacora(respuesta, validarVerificableDTO.getAuditoria(),
          "VALIDAR");
      return EstadoRespuestas.respuestaCreacion("Verificable validado exitosamente");
    } catch (Exception e) {
      logger.error("Error al validar verificable: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al validar verificable: " + e);
    }

  }

  @Transactional
  private boolean verificarEstadoRecomendacionPorVerificable(Long areve_codigo){
    boolean respuesta = this.verificableRepositorio.verificarEstadoRecomendacionPorVerificable(areve_codigo).get().equals("TRUE") ? true : false;
    return respuesta;
  }

  private String obtenerEstadoVerificable(Long areev_codigo){
    String estado = "";
    if(areev_codigo == 1){
      estado = "PENDIENTE";
    }else if(areev_codigo == 2){
      estado = "JUSTIFICA";
    } else if(areev_codigo == 3){
      estado = "NO JUSTIFICA";
    } else if(areev_codigo == 5){
      estado = "PENDIENTE DE REVISIÓN";
    }
    return estado;
  }

  private boolean esUsuarioAuditorJefe() {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null) {
        boolean hasRole2 = auth.getAuthorities().stream()
            .anyMatch(a -> "2".equals(a.getAuthority()) || "ROLE_2".equals(a.getAuthority()) || (a.getAuthority() != null && a.getAuthority().contains("JEFE")));
        if (hasRole2) return true;
        String usuario = auth.getName();
        if (usuario != null && !usuario.trim().isEmpty()) {
          Integer count = rolUsuarioRepositorio.findByDsgus_cuentaAndAsero_codigo(
              usuario.trim(), 
              RolEnum.AUDITOR_JEFE_INTERNO.getCode()
          );
          return count != null && count > 0;
        }
      }
    } catch (Exception e) {
      logger.warn("Error al verificar rol de Auditor Jefe: {}", e.getMessage());
    }
    return false;
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(EliminarDTO eliminarDTO, Long areve_codigo) {
    try {
      if (!verificableRepositorio.existsById(areve_codigo)) {
        return EstadoRespuestas.noEncontrado("Verificable no encontrado");
      }

      Verificable verificable = verificableRepositorio.findById(areve_codigo).get();
      Recomendacion recomendacion = null;
      if (verificable.getDetalleRecomendacion() != null && verificable.getDetalleRecomendacion().getRecomendacion() != null) {
        recomendacion = verificable.getDetalleRecomendacion().getRecomendacion();
      } else {
        recomendacion = recomendacionServicio.obtenerPorVerificable(areve_codigo);
      }
      Long arereCodigo = recomendacion != null ? recomendacion.getArere_codigo() : null;

      boolean fechaMaximaCumplida = recomendacionServicio.validarFechaMaxima(verificable.getDetalleRecomendacion());
      if (!esUsuarioAuditorJefe() && fechaMaximaCumplida) {
        return EstadoRespuestas.respuestaError("Fecha máxima cumplida");
      }

      String auditor = arereCodigo != null ? verificableRepositorio.findAuditorByRecomendacion(arereCodigo).orElse("").trim() : "";
      String memorando = arereCodigo != null ? verificableRepositorio.findMemorandoByRecomendacion(arereCodigo).orElse("").trim() : "";
      String num_rec = arereCodigo != null ? verificableRepositorio.findNumRecByRecomendacion(arereCodigo).orElse("").trim() : "";
      
      String nombreResponsable = "";
      if (verificable.getDetalleRecomendacion() != null && verificable.getDetalleRecomendacion().getResponsable() != null) {
        String nom = verificable.getDetalleRecomendacion().getResponsable().getArers_nombre();
        String ape = verificable.getDetalleRecomendacion().getResponsable().getArers_apellido();
        nombreResponsable = ((nom != null ? nom : "") + " " + (ape != null ? ape : "")).trim();
      }
      if (nombreResponsable.isEmpty() && verificable.getDetalleRecomendacion() != null && verificable.getDetalleRecomendacion().getResponsable() != null) {
        String arersCodigoPer = verificable.getDetalleRecomendacion().getResponsable().getArers_codigo_per();
        if (arersCodigoPer != null && !arersCodigoPer.trim().isEmpty()) {
          nombreResponsable = verificableRepositorio.findNombreResponsable(arersCodigoPer).orElse("").trim();
        }
      }

      verificableRepositorio.deleteById(areve_codigo);
      bitacoraServicio.registrarBitacora(verificable, eliminarDTO.getAuditoria(),
          "ELIMINAR FISICO");

      // Registrar en el historial de recomendaciones
      try {
        String auditorNombre = historialServicio.obtenerNombreCompletoPorUsuario(
            eliminarDTO.getAuditoria() != null ? eliminarDTO.getAuditoria().getUsuario() : "SISTEMA");
        historialServicio.registrarEvento(
            arereCodigo,
            "ELIMINACION_VERIFICABLE",
            "Se eliminó el verificable \"" + (verificable.getAreve_descripcion() != null ? verificable.getAreve_descripcion() : "Sin descripción") + "\" del informe " + memorando + " Num. Recomendacion: " + num_rec + ". Eliminado por: " + auditorNombre + ".",
            null, nombreResponsable, null, null, auditorNombre, null,
            eliminarDTO.getAuditoria() != null ? eliminarDTO.getAuditoria().getUsuario() : "SISTEMA");
      } catch (Exception eHist) {
        logger.warn("Error al registrar ELIMINACION_VERIFICABLE en historial: {}", eHist.getMessage());
      }

      // Desactivar notificación anterior de subida
      try {
        if (arereCodigo != null) {
          this.notificacionesRepositorio.actualizarEstadoNotificacionesPorArereCodigo(arereCodigo);
          logger.info("[Notificaciones] Notificación de subida previa desactivada para arere_codigo='{}'", arereCodigo);
        }
      } catch (Exception ex) {
        logger.error("[Notificaciones] Error al desactivar notificación de subida previa: {}", ex.getMessage());
      }

      // Comprobar si el usuario logueado es Auditor Operativo
      boolean esAuditorOperativo = false;
      String usuarioLogueado = "";
      try {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
          usuarioLogueado = auth.getName();
          if (usuarioLogueado != null && !usuarioLogueado.trim().isEmpty()) {
            Integer countAO = rolUsuarioRepositorio.findByDsgus_cuentaAndAsero_codigo(
                usuarioLogueado.trim(), 
                RolEnum.AUDITOR_OPERATIVO.getCode()
            );
            esAuditorOperativo = (countAO != null && countAO > 0);
          }
        }
      } catch (Exception ex) {
        logger.error("Error al determinar rol del usuario autenticado: {}", ex.getMessage());
      }

      if (esAuditorOperativo) {
        // Notificar a los jefes de auditoría directamente
        List<String> auditoresJefe = rolUsuarioRepositorio.getCuentasAuditoresJefe();
        if (auditoresJefe != null && !auditoresJefe.isEmpty()) {
          for (String jefe : auditoresJefe) {
            if (jefe != null && !jefe.trim().isEmpty()) {
              String mensajeJefe = "El Auditor " + usuarioLogueado + " eliminó el verificable del informe con memorando " + memorando + " Num. Recomendacion: " + num_rec;
              Integer anioElimJefe = extraerAnio(recomendacion.getArere_fecha_inicio_control());
              if (anioElimJefe == null) anioElimJefe = Calendar.getInstance().get(Calendar.YEAR);
              NuevaNotificacionDTO notiJefe = new NuevaNotificacionDTO(
                  "Verificable eliminado por auditor", 
                  mensajeJefe, 
                  TipoNotificacionEnum.VERIFICABLE_ELIMINADO_AUDITOR,
                  TipoNotificacionEnum.VERIFICABLE_ELIMINADO_AUDITOR,
                  anioElimJefe,
                  null,
                  arereCodigo,
                  jefe.trim()
              );
              notificacionesServicio.registrarNotificacion(notiJefe);

              try {
                simpMessagingTemplate.convertAndSend("/topic/notificacion/" + jefe.trim(), "Un verificable fue eliminado por el auditor.");
              } catch (Exception ex) {
                logger.error("[Notificaciones] Error al enviar WebSocket al jefe: {}", ex.getMessage());
              }
            }
          }
        }
      } else {
        // Lógica para responsable/director (comportamiento habitual)
        if (!auditor.isEmpty()) {
          String mensaje = nombreResponsable + " eliminó el verificable del informe con memorando " + memorando + " Num. Recomendacion: " + num_rec;
          Integer anioElim = extraerAnio(recomendacion.getArere_fecha_inicio_control());
          if (anioElim == null) anioElim = Calendar.getInstance().get(Calendar.YEAR);
          NuevaNotificacionDTO notiEliminacion = new NuevaNotificacionDTO(
              "Verificable eliminado", 
              mensaje, 
              TipoNotificacionEnum.VERIFICABLE_ELIMINADO,
              TipoNotificacionEnum.VERIFICABLE_ELIMINADO,
              anioElim,
              null,
              arereCodigo,
              auditor
          );
          notificacionesServicio.registrarNotificacion(notiEliminacion);

          // Notificación en tiempo real por WebSocket
          try {
            simpMessagingTemplate.convertAndSend("/topic/notificacion/" + auditor, "Tiene un verificable eliminado.");
          } catch (Exception ex) {
            logger.error("[Notificaciones] Error al enviar WebSocket para eliminación: {}", ex.getMessage());
          }
        }
      }

      // Recalcular estado de la recomendación tras eliminar verificable
      try {
        recomendacionServicio.recalcularEstadoRecomendacion(arereCodigo);
      } catch (Exception eRec) {
        logger.warn("Error al recalcular estado de recomendacion tras eliminar verificable: {}", eRec.getMessage());
      }

      return EstadoRespuestas.respuestaEliminacion("Verificable eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar verificable: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar verificable");
    }
  }

  public boolean actualizarDetalle(DetalleRecomendacion detalleRecomendacionOld,
      DetalleRecomendacion detalleRecomendacionNew) {
    try {
      List<Verificable> verificables = verificableRepositorio
          .findByDetalleRecomendacion(detalleRecomendacionOld.getAredr_codigo());
      verificables.stream().filter(verificable -> verificable.getAreve_estado().equals('1'))
          .forEach(verificable -> verificable.setDetalleRecomendacion(detalleRecomendacionNew));
      verificableRepositorio.saveAll(verificables);
      return true;
    } catch (Exception e) {
      logger.error("Error al actualizar detalle : {}", e.getMessage());
      return false;
    }
  }

  private Integer extraerAnio(Date fecha) {
    if (fecha == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(fecha);
    return cal.get(Calendar.YEAR);
  }

  private Integer anioSeguro(Date fechaRecomendacion, Long ainin_otanio) {
    if (ainin_otanio != null) {
      return ainin_otanio.intValue();
    }
    if (fechaRecomendacion != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(fechaRecomendacion);
      return cal.get(Calendar.YEAR);
    }
    return Calendar.getInstance().get(Calendar.YEAR);
  }

  private AuditoriaDTO convertirAuditoriaJSONDTO(String auditoria) {
    ObjectMapper objectMapper = new ObjectMapper();
    AuditoriaDTO auditoriaDTO = null;
    try {
      auditoriaDTO = objectMapper.readValue(auditoria, AuditoriaDTO.class);
    } catch (Exception e) {
      logger.error("Error al convertir auditoria: {}", e.getMessage());
    }
    return auditoriaDTO;
  }

  public ResponseEntity<LinkedHashMap<String, Object>> notificarEnvioVerificables(
      NotificarEnvioVerificablesDTO notificarDTO) {
    try {
      if (notificarDTO == null || notificarDTO.getArere_codigo() == null || notificarDTO.getArers_codigo_per() == null) {
        return EstadoRespuestas.respuestaError("Datos insuficientes para notificar el envío de verificables");
      }

      DetalleRecomendacion detalleRecomendacion = detalleServicio
          .obtenerPorRecomendacionResponsablePer(notificarDTO.getArere_codigo(), notificarDTO.getArers_codigo_per());

      Informe informe = informeServicio.obtenerInformeByRecomendacion(notificarDTO.getArere_codigo());
      Long ainin_codigo = informe != null ? informe.getAinin_codigo() : null;

      String auditor = verificableRepositorio.findAuditorByRecomendacion(notificarDTO.getArere_codigo()).orElse("").trim();
      String nombreResponsableDet = verificableRepositorio.findNombreResponsable(notificarDTO.getArers_codigo_per()).orElse("No asignado").trim();
      String memorando = verificableRepositorio.findMemorandoByRecomendacion(notificarDTO.getArere_codigo()).orElse("").trim();
      String num_rec = verificableRepositorio.findNumRecByRecomendacion(notificarDTO.getArere_codigo()).orElse("").trim();

      int cantidad = (notificarDTO.getCantidadVerificables() != null && notificarDTO.getCantidadVerificables() > 0)
          ? notificarDTO.getCantidadVerificables()
          : (notificarDTO.getNombresArchivos() != null && !notificarDTO.getNombresArchivos().isEmpty()
              ? notificarDTO.getNombresArchivos().size()
              : 1);

      List<String> archivos = notificarDTO.getNombresArchivos();
      String descOriginal = notificarDTO.getDescripcion() != null ? notificarDTO.getDescripcion().trim() : "";

      logger.info("[Verificable] Notificando envío consolidado: {} verificable(s) para REC={}, RESP={}, auditor='{}'",
          cantidad, notificarDTO.getArere_codigo(), notificarDTO.getArers_codigo_per(), auditor);

      // 1. Notificación WebSocket
      if (!auditor.isEmpty()) {
        String msgWs = cantidad == 1
            ? "Tiene 1 verificable pendiente de revisar."
            : "Tiene " + cantidad + " verificables pendientes de revisar.";
        simpMessagingTemplate.convertAndSend("/topic/notificacion/" + auditor, msgWs);
      }

      // 2. Notificación en sistema (in-app)
      String textoCant = cantidad == 1 ? "1 verificable" : cantidad + " verificables";
      String mensajeNotif = "Se enviaron " + textoCant + " del responsable " + nombreResponsableDet 
          + " con memorando " + memorando + " Num. Recomendacion: " + num_rec + " pendientes de revisión.";

      Integer anioVerificable = anioSeguro(
          detalleRecomendacion != null && detalleRecomendacion.getRecomendacion() != null
              ? detalleRecomendacion.getRecomendacion().getArere_fecha_inicio_control() : null,
          informe != null ? informe.getAinin_otanio() : null);

      NuevaNotificacionDTO notificacion = new NuevaNotificacionDTO(
          "Verificable pendiente de revisión.",
          mensajeNotif,
          TipoNotificacionEnum.VERIFICABLE_PENDIENTE,
          TipoNotificacionEnum.VERIFICABLE_PENDIENTE,
          anioVerificable,
          ainin_codigo,
          notificarDTO.getArere_codigo(),
          auditor);
      notificacionesServicio.registrarNotificacion(notificacion);

      // 3. Enviar correo a la auditora operativa
      try {
        String correoAuditor = null;
        if (!auditor.isEmpty()) {
          correoAuditor = rolUsuarioRepositorio.getCorreoByCuenta(auditor);
        }
        if (correoAuditor == null || correoAuditor.trim().isEmpty()) {
          correoAuditor = ainin_codigo != null ? rolUsuarioRepositorio.getCorreoAuditorResponsable(ainin_codigo) : null;
        }

        if (correoAuditor != null && !correoAuditor.trim().isEmpty()) {
          String nombreAuditor = null;
          if (!auditor.isEmpty()) {
            nombreAuditor = rolUsuarioRepositorio.getNombreByCuenta(auditor);
          }
          if (nombreAuditor == null || nombreAuditor.trim().isEmpty()) {
            nombreAuditor = ainin_codigo != null ? rolUsuarioRepositorio.getNombreAuditorResponsable(ainin_codigo) : null;
          }

          java.util.Map<String, Object> vars = new java.util.HashMap<>();
          vars.put("fecha", ec.com.eeasa.wssisrec.wssisrec.shared.utils.UtilsFunctions.getDateHour());
          vars.put("auditor", nombreAuditor != null ? nombreAuditor : auditor);
          vars.put("tituloInforme", informe != null && informe.getAinin_titulo() != null ? informe.getAinin_titulo() : "");
          vars.put("memoAprobacion", memorando != null ? memorando : "");
          vars.put("numRecomendacion", num_rec != null ? num_rec : "");
          vars.put("recDescripcion", detalleRecomendacion != null && detalleRecomendacion.getRecomendacion() != null && detalleRecomendacion.getRecomendacion().getArere_descripcion() != null 
              ? detalleRecomendacion.getRecomendacion().getArere_descripcion() : "");
          vars.put("nombreResponsable", nombreResponsableDet != null ? nombreResponsableDet : "");

          // Detalle Verificable con la cantidad destacada y la descripción ingresada
          StringBuilder sbDetalle = new StringBuilder();
          sbDetalle.append("<div style=\"margin-bottom: 6px;\"><strong style=\"color: #20315F;\">Cantidad de verificables enviados:</strong> ")
                   .append(cantidad).append("</div>");
          if (!descOriginal.isEmpty()) {
            sbDetalle.append("<div>").append(descOriginal.replace("\n", "<br/>")).append("</div>");
          } else {
            sbDetalle.append("<div>Verificables cargados para revisión.</div>");
          }
          vars.put("descripcionVerificable", sbDetalle.toString());

          // Lista de archivos adjuntos estructurada
          StringBuilder sbArchivos = new StringBuilder();
          if (archivos != null && !archivos.isEmpty()) {
            if (archivos.size() == 1) {
              sbArchivos.append(archivos.get(0));
            } else {
              sbArchivos.append("<div style=\"margin-bottom: 4px;\"><strong style=\"color: #20315F;\">")
                         .append(archivos.size()).append(" archivos adjuntos:</strong></div>")
                         .append("<ol style=\"margin: 0; padding-left: 20px; color: #1F2937; line-height: 1.6;\">");
              for (String arch : archivos) {
                sbArchivos.append("<li>").append(arch).append("</li>");
              }
              sbArchivos.append("</ol>");
            }
          } else {
            sbArchivos.append(cantidad).append(" archivo(s) verificable(s)");
          }
          vars.put("nombreArchivo", sbArchivos.toString());

          emailService.sendCorreoVerificablePendienteAuditor(correoAuditor, vars);

          // Actualizar cooldown
          String claveControl = "INF_" + ainin_codigo + "_REC_" + notificarDTO.getArere_codigo() 
              + "_RESP_" + notificarDTO.getArers_codigo_per() + "_AUD_" + correoAuditor.trim().toLowerCase();
          ultimoCorreoEnviadoCooldown.put(claveControl, Instant.now());

          logger.info("[Verificable] Correo de lote ({} verificables) enviado exitosamente al auditor: {}", cantidad, correoAuditor);
        } else {
          logger.warn("[Verificable] No se encontró correo para auditor en lote de informe {}", ainin_codigo);
        }
      } catch (Exception eMail) {
        logger.error("[Verificable] Error al enviar correo de lote al auditor: {}", eMail.getMessage(), eMail);
      }

      // 4. Registrar en historial
      try {
        AuditoriaDTO auditoriaDTO = notificarDTO.getAuditoria() != null 
            ? convertirAuditoriaJSONDTO(notificarDTO.getAuditoria()) 
            : new AuditoriaDTO();
        String usuarioAudit = auditoriaDTO.getUsuario() != null ? auditoriaDTO.getUsuario() : nombreResponsableDet;
        
        historialServicio.registrarEvento(
            notificarDTO.getArere_codigo(),
            "ENVIO_LOTE_VERIFICABLES",
            "Se enviaron a revisión " + cantidad + " archivo(s) verificable(s) por el responsable: " + nombreResponsableDet + ".",
            null, nombreResponsableDet, null, null, null, null,
            usuarioAudit
        );
      } catch (Exception eHist) {
        logger.warn("[Verificable] Error al registrar historial de envío de lote: {}", eHist.getMessage());
      }

      return EstadoRespuestas.respuestaCreacion("Notificación de verificables enviada exitosamente");
    } catch (Exception e) {
      logger.error("[Verificable] Error al notificar envío de verificables: {}", e.getMessage(), e);
      return EstadoRespuestas.respuestaError("Error al notificar envío: " + e.getMessage());
    }
  }

}
