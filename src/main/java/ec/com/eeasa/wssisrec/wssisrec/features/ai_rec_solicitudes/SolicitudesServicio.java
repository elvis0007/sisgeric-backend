package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.InformeServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models.Informe;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.NotificacionesServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.NotificacionesRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.dtos.NuevaNotificacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.enums.TipoNotificacionEnum;
import java.util.Date;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.DetalleServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.RecomendacionServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarFechaMaxDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.ResponsableRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models.Responsable;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.AprobarSolicitudDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.DirectorDepartamentalDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.FiltroSolicitudAudDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.FiltroSolicitudDirDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.NuevoSolicitudDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.SolicitudDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.SolicitudDetalleDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.mapper.SolicitudMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.models.Solicitud;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.ConfiguracionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.enums.ConfiguracionEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.models.Configuracion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.enums.RolEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.DirectorInformacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.AuditoriaMapper;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.FileMapper;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.Ftp.FtpService;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_historial.HistorialRecomendacionServicio;

@Service
public class SolicitudesServicio {

  private static final Logger logger = LoggerFactory.getLogger(SolicitudesServicio.class);

  @Autowired
  BitacoraServicio bitacoraServicio;

  @Autowired
  SolicitudesRepositorio solicitudesRepositorio;

  @Autowired
  private ConfiguracionRepositorio configuracionRepositorio;

  @Autowired
  private FtpService ftpService;

  @Autowired
  private InformeServicio informeServicio;

  @Autowired
  private SolicitudMapper solicitudMapper;

  @Autowired
  private DetalleServicio detalleServicio;

  @Autowired
  private RecomendacionServicio recomendacionServicio;

  @Autowired
  private HistorialRecomendacionServicio historialServicio;

  @Autowired
  private ResponsableRepositorio responsableRepositorio;

  @Autowired
  private NotificacionesServicio notificacionesServicio;

  @Autowired
  private NotificacionesRepositorio notificacionesRepositorio;

  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  @Autowired
  private ec.com.eeasa.wssisrec.wssisrec.services.SolicitudPdfService solicitudPdfService;


  @Autowired
  private ec.com.eeasa.wssisrec.wssisrec.services.EmailService emailService;

  @Autowired
  private ec.com.eeasa.wssisrec.wssisrec.services.WsCorreoService wsCorreoService;

  @Autowired
  private ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioRepositorio rolUsuarioRepositorio;

  @Autowired
  private RolUsuarioServicio rolUsuarioServicio;

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> registrarSolicitud(NuevoSolicitudDTO nuevoSolicitudDTO) {
    String rutaArchivoFtp = null;
    boolean conexionFtpActiva = false;
    try {
      if (nuevoSolicitudDTO == null) {
        return EstadoRespuestas.respuestaError("No se recibió la información de la solicitud");
      }

      if (nuevoSolicitudDTO.getArere_codigo() == null || nuevoSolicitudDTO.getArere_codigo() <= 0) {
        return EstadoRespuestas.respuestaError("El código de la recomendación es obligatorio");
      }

      if (nuevoSolicitudDTO.getArers_codigo_per() == null || nuevoSolicitudDTO.getArers_codigo_per().trim().isEmpty()) {
        return EstadoRespuestas.respuestaError("El código personal del responsable es obligatorio");
      }

      // Validar que no exista ya una solicitud de ampliación pendiente y vigente para esta recomendación y este responsable
      Optional<Solicitud> solPendienteOpt = solicitudesRepositorio.findSolicitudPendienteVigenteByRecomendacionYResponsablePer(
          nuevoSolicitudDTO.getArere_codigo(),
          nuevoSolicitudDTO.getArers_codigo_per()
      );
      if (solPendienteOpt.isPresent()) {
        Solicitud solPend = solPendienteOpt.get();
        String fechaPropuesta = solPend.getAreso_fecha_max_nue() != null ? solPend.getAreso_fecha_max_nue().toString() : "";
        return EstadoRespuestas.respuestaError(
            "Ya tiene una solicitud de ampliación de plazo en trámite (Fecha solicitada: " + fechaPropuesta + "). Debe esperar a que sea procesada antes de registrar otra."
        );
      }

      LocalDate fechaMaxAnterior = parsearFechaSolicitud(nuevoSolicitudDTO.getAreso_fecha_max_ant(), "areso_fecha_max_ant");
      LocalDate fechaMaxNueva = parsearFechaSolicitud(nuevoSolicitudDTO.getAreso_fecha_max_nue(), "areso_fecha_max_nue");
      LocalDate hoy = LocalDate.now();

      if (fechaMaxAnterior == null || fechaMaxNueva == null) {
        return EstadoRespuestas.respuestaError("Las fechas deben tener formato YYYY-MM-DD");
      }

      if (fechaMaxNueva.isBefore(fechaMaxAnterior)) {
        return EstadoRespuestas.respuestaError("La nueva fecha debe ser posterior a la fecha máxima actual");
      }

      if (fechaMaxNueva.isBefore(hoy)) {
        return EstadoRespuestas.respuestaError("La nueva fecha debe ser igual o posterior a hoy");
      }

      if (nuevoSolicitudDTO.getAreso_descripcion() == null
          || nuevoSolicitudDTO.getAreso_descripcion().trim().length() < 10) {
        return EstadoRespuestas.respuestaError("La descripción debe tener al menos 10 caracteres");
      }

      if (nuevoSolicitudDTO.getAreso_descripcion().trim().length() > 5000) {
        return EstadoRespuestas.respuestaError("La descripción no puede superar 5000 caracteres");
      }

      if (nuevoSolicitudDTO.getAreso_memorando() != null
          && !nuevoSolicitudDTO.getAreso_memorando().trim().isEmpty()
          && !nuevoSolicitudDTO.getAreso_memorando().trim().matches("^[A-Za-z0-9ÁÉÍÓÚÜÑáéíóúüñ\\-_/ .]+$")) {
        return EstadoRespuestas.respuestaError("El memorando solo puede contener caracteres alfanuméricos y separadores válidos");
      }

      if (nuevoSolicitudDTO.getAreso_memorando() != null && nuevoSolicitudDTO.getAreso_memorando().trim().length() > 50) {
        return EstadoRespuestas.respuestaError("El memorando no puede superar 50 caracteres");
      }

      MultipartFile archivo = nuevoSolicitudDTO.getAreso_adjunto();
      if (archivo == null || archivo.isEmpty()) {
        return EstadoRespuestas.respuestaError("Debe adjuntar un archivo PDF");
      }

      if (!esPdfValido(archivo)) {
        return EstadoRespuestas.respuestaError("Solo se permiten archivos PDF");
      }

      if (existeSolicitudNoAprobada(nuevoSolicitudDTO.getArere_codigo(), nuevoSolicitudDTO.getArers_codigo_per())) {
        return EstadoRespuestas.respuestaError("Existe solicitud no aprobada");
      }

      AuditoriaDTO auditoriaDTO = AuditoriaMapper.convertirAuditoriaJSONDTO(nuevoSolicitudDTO.getAuditoria());
      if (auditoriaDTO == null) {
        return EstadoRespuestas.respuestaError("Error al procesar la información de auditoría");
      }

      DetalleRecomendacion detalleRecomendacion = detalleServicio.obtenerPorRecomendacionResponsablePer(
          nuevoSolicitudDTO.getArere_codigo(), nuevoSolicitudDTO.getArers_codigo_per());
      if (detalleRecomendacion == null) {
        return EstadoRespuestas.respuestaError("No se encontró el detalle de recomendación para el responsable especificado");
      }

      if (detalleRecomendacion.getRecomendacion() != null && detalleRecomendacion.getRecomendacion().getEstadoSeg() != null) {
        if (ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.enums.EstadoSegEnum.CUMPLIDA.getCodigo().equals(detalleRecomendacion.getRecomendacion().getEstadoSeg().getArees_codigo())) {
          return EstadoRespuestas.respuestaError("La recomendación ya se encuentra CUMPLIDA. No se admiten nuevas solicitudes de prórroga.");
        }
      }

      Informe informe = informeServicio.obtenerInformeByRecomendacion(nuevoSolicitudDTO.getArere_codigo());
      if (informe == null || informe.getOrganismoControl() == null) {
        return EstadoRespuestas.respuestaError("No se encontró la recomendación o su informe asociado");
      }

      // Obtener la configuracion ftp
      Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).orElse(null);
      if (configuracion == null) {
        return EstadoRespuestas.respuestaError("No se encontró la configuración FTP");
      }

      // Validar tamaño del archivo antes de conectar al FTP
      if (!ftpService.validarTamanoArchivo(archivo, configuracion.getAseco_size_file_mb())) {
        double tamanoArchivoMB = ftpService.obtenerTamanoArchivoMB(archivo);
        return EstadoRespuestas.respuestaError(
          String.format("El archivo excede el límite permitido. Tamaño del archivo: %.2f MB, Límite máximo: %d MB", 
            tamanoArchivoMB, configuracion.getAseco_size_file_mb()));
      }

      // Conectar al servidor FTP
      conexionFtpActiva = ftpService.conectarServidorFTP(configuracion.getAseco_host(),
          configuracion.getAseco_user(),
          configuracion.getAseco_contrasenia());
      if (!conexionFtpActiva) {
        return EstadoRespuestas.respuestaError("Error del servidor externo (FTP/NAS): No se pudo establecer conexión. Consulte con el administrador del sistema.");
      }

      // Metodo renombrar el archivo
      String nombreArchivo = FileMapper.getFileName(archivo.getOriginalFilename(),
          "solicitud");

      // Obtener códigos y descripciones adicionales
      Long ainin_codigo = informe.getAinin_codigo();
      String organismoControl = informe.getOrganismoControl().getAinoc_descripcion();

      // Metodo retorna la ruta relativa
      String rutaRelativa = FileMapper.obtenerRutaArchivoFtp(configuracion.getAseco_directorio(),
          organismoControl, ainin_codigo,
          "Solicitudes");

      // REGISTRAR EL ARCHIVO Y DEVUELVE SI SE REGISTRO
        Boolean respuesCrearArchivo = ftpService.subirArchivoAlServidorFTP(archivo,
          rutaRelativa, nombreArchivo);

      if (!respuesCrearArchivo) {
        return EstadoRespuestas.respuestaError(("Error al crear el archivo"));
      }

      // Guardar una copia base inmutable para reconstruir el PDF al aprobar sin arrastrar aprobaciones previas.
      String nombreArchivoBase = "base-" + nombreArchivo;
      Boolean respuesCrearArchivoBase = ftpService.subirArchivoAlServidorFTP(archivo,
          rutaRelativa, nombreArchivoBase);

      if (!respuesCrearArchivoBase) {
        logger.warn("No se pudo guardar la copia base del PDF. Se seguirá con el archivo principal: {}", nombreArchivoBase);
      }

        // CREAR NUEVA RUTA PARA GUARDAR EN LA BASE DE DATOS
        String rutaArchivoBd = FileMapper.obtenerRutaArchivoBd(configuracion.getAseco_nombre_host(), rutaRelativa,
          nombreArchivo);
        rutaArchivoFtp = rutaRelativa + nombreArchivo;

        Solicitud solicitud = solicitudMapper.convertirCrearDTOModel(nuevoSolicitudDTO, auditoriaDTO, rutaArchivoBd,
          detalleRecomendacion.getRecomendacion(), detalleRecomendacion.getResponsable());
        Authentication authCreacion = SecurityContextHolder.getContext().getAuthentication();
        if (authCreacion != null && authCreacion.getName() != null && !authCreacion.getName().trim().isEmpty()) {
          solicitud.setAreso_usuario(authCreacion.getName().trim());
        }
        boolean solicitudCreadaPorDirector = esUsuarioDirectorActual();
        solicitud.setAreso_estado_aprob(solicitudCreadaPorDirector ? '2' : '1');
      Solicitud solicitudGuardada = solicitudesRepositorio.save(solicitud);

      String responsable = this.solicitudesRepositorio.findResponsable(detalleRecomendacion.getResponsable().getArers_codigo());
      String auditorUsuario = informe.getAinin_usuario();

      if (solicitudCreadaPorDirector) {
        if (auditorUsuario != null && !auditorUsuario.trim().isEmpty() && solicitudGuardada.getAreso_codigo() != null && solicitudGuardada.getAreso_codigo() > 0) {
          this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + auditorUsuario, "Tiene una nueva notificacion.");
          Integer anioSolPreDir = extraerAnio(detalleRecomendacion.getRecomendacion().getArere_fecha_inicio_control());
          this.notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
              "Solicitud preaprobada",
              String.format("Solicitud de %s enviada directamente por Director Departamental. N. Solicitud %d Rec:%s memo:%s (auditor asignado)(%s)",
                  responsable,
                  solicitudGuardada.getAreso_codigo(),
                  detalleRecomendacion.getRecomendacion() != null && detalleRecomendacion.getRecomendacion().getArere_num_rec() != null
                      ? detalleRecomendacion.getRecomendacion().getArere_num_rec().toString()
                      : "0",
                  informe.getAinin_memorando() != null ? informe.getAinin_memorando() : "(sin memorando)",
                  auditorUsuario),
              TipoNotificacionEnum.SOLICITUD_PREAPROBADA_DIRECTOR,
              TipoNotificacionEnum.SOLICITUD_PREAPROBADA_DIRECTOR,
              anioSolPreDir,
              informe.getAinin_codigo(),
              detalleRecomendacion.getRecomendacion().getArere_codigo(),
              auditorUsuario));
        }
      } else {
        // Notificacion de creación de soliticud para el director del responsable
        String director = this.solicitudesRepositorio.findDirectorByDepartamentoResponsable(detalleRecomendacion.getResponsable().getArers_codigo());
        if(director != null && solicitudGuardada.getAreso_codigo() != null && solicitudGuardada.getAreso_codigo() > 0){
          this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + director, "Tiene una nueva notificacion.");
          Integer anioSolNueva = extraerAnio(detalleRecomendacion.getRecomendacion().getArere_fecha_inicio_control());
          this.notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
            "Nueva solicitud de cambio de fecha N° " + solicitudGuardada.getAreso_codigo(),
            responsable + " ha creado una nueva solicitud de cambio de fecha N° " + solicitudGuardada.getAreso_codigo(),
            TipoNotificacionEnum.SOLICITUD_NUEVA,
            TipoNotificacionEnum.SOLICITUD_NUEVA,
            anioSolNueva,
            informe.getAinin_codigo(),
            detalleRecomendacion.getRecomendacion().getArere_codigo(),
            director));
        }
      }

      bitacoraServicio.registrarBitacora(solicitudGuardada, auditoriaDTO, "CREAR");

      // Registrar evento en el historial de recomendaciones
      try {
        String responsableNombre = "Sin asignar";
        if (solicitudGuardada.getResponsable() != null) {
          responsableNombre = solicitudGuardada.getResponsable().getArers_nombre() + " " + solicitudGuardada.getResponsable().getArers_apellido();
        }
        String creadorNombre = responsableNombre;
        if (solicitudGuardada.getAreso_usuario() != null) {
          String resolvedNombre = historialServicio.obtenerNombreCompletoPorUsuario(solicitudGuardada.getAreso_usuario());
          if (resolvedNombre != null && !resolvedNombre.equals(solicitudGuardada.getAreso_usuario())) {
            creadorNombre = resolvedNombre;
          }
        }
        historialServicio.registrarEvento(
            solicitudGuardada.getRecomendacion().getArere_codigo(),
            "SOLICITUD_PRORROGA",
            "Se creó la solicitud de prórroga para cambio de fecha máxima a: " + 
            DateMapper.convertirFechaFormato(solicitudGuardada.getAreso_fecha_max_nue()) + 
            " (Anterior: " + DateMapper.convertirFechaFormato(solicitudGuardada.getAreso_fecha_max_ant()) + "). Solicitado por el responsable: " + creadorNombre + ". Motivo: " + solicitudGuardada.getAreso_descripcion(),
            null, creadorNombre,
            solicitudGuardada.getAreso_fecha_max_ant(),
            solicitudGuardada.getAreso_fecha_max_nue(),
            null, null,
            solicitudGuardada.getAreso_usuario() != null ? solicitudGuardada.getAreso_usuario() : "RESPONSABLE"
        );
      } catch (Exception ex) {
        logger.error("Error al registrar evento de creación de solicitud en historial: {}", ex.getMessage());
      }

      LinkedHashMap<String, Object> datosCreacion = new LinkedHashMap<>();
      datosCreacion.put("numeroSolicitud", solicitudGuardada.getAreso_codigo());
      datosCreacion.put("aresoCodigo", solicitudGuardada.getAreso_codigo());

      LinkedHashMap<String, Object> response = new LinkedHashMap<>();
      response.put("codigo", 1);
      response.put("mensaje", "Solicitud registrado exitosamente");
      response.put("datos", datosCreacion);
      response.put("status", HttpStatus.CREATED.value());

      return new ResponseEntity<LinkedHashMap<String, Object>>(response, HttpStatus.CREATED);
    } catch (Exception e) {
      logger.error("Error al registrar solicitud: {}", e.getMessage());
      try {
        if (rutaArchivoFtp != null) {
          ftpService.eliminarArchivoDeServidorFTP(rutaArchivoFtp);
        }
      } catch (Exception ex) {
        logger.error("Error al desconectar del servidor FTP: {}", ex.getMessage());
      }
      return EstadoRespuestas.respuestaError("Error al registrar solicitud: " + e.getMessage());
    } finally {
      if (conexionFtpActiva) {
        ftpService.desconectarServidorFTP();
      }
    }
  }

  private LocalDate parsearFechaSolicitud(String fecha, String campo) {
    try {
      if (fecha == null || fecha.trim().isEmpty()) {
        return null;
      }
      return LocalDate.parse(fecha.trim());
    } catch (DateTimeParseException e) {
      logger.error("Error al convertir la fecha {}: {}", campo, e.getMessage());
      return null;
    }
  }

  private Integer extraerAnio(Date fecha) {
    if (fecha == null) {
      return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
    }
    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.setTime(fecha);
    return cal.get(java.util.Calendar.YEAR);
  }

  private boolean esPdfValido(MultipartFile archivo) {
    if (archivo == null || archivo.isEmpty()) {
      return false;
    }

    String nombreOriginal = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename().toLowerCase() : "";
    String contentType = archivo.getContentType() != null ? archivo.getContentType().toLowerCase() : "";

    if (!nombreOriginal.endsWith(".pdf") && !"application/pdf".equals(contentType)) {
      return false;
    }

    try (InputStream inputStream = archivo.getInputStream()) {
      byte[] encabezado = new byte[4];
      int leidos = inputStream.read(encabezado);
      if (leidos < 4) {
        return false;
      }
      String firma = new String(encabezado, StandardCharsets.US_ASCII);
      return "%PDF".equals(firma);
    } catch (Exception e) {
      logger.error("Error al validar el archivo PDF: {}", e.getMessage());
      return false;
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorRecomendacionResponsablePer(Long arere_codigo,
      String codigo_per) {
    try {
      List<Solicitud> solicituds = solicitudesRepositorio.findByRecomendacionResponsablePer(arere_codigo, codigo_per);
      List<SolicitudDTO> solicitudDTOs = solicituds.stream()
          .map(solicitud -> solicitudMapper.convertirModelDTO(solicitud)).collect(Collectors.toList());
      return EstadoRespuestas.respuestaObtencion("Obtección de solicitudes exitoso", solicitudDTOs);
    } catch (Exception e) {
      logger.error("Error al obtener las solicitudes por recomendacion y responsable : {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener las solicitudes por recomendacion y responsable");
    }
  }

  private SolicitudDetalleDTO convertirSolicitudADetalle(Solicitud solicitud) {
    try {
      // Obtener el informe asociado a la recomendación de la solicitud
      Informe informe = informeServicio.obtenerInformeByRecomendacion(solicitud.getRecomendacion().getArere_codigo());
      
      // Extraer valores del informe
      String ainoc_descripcion = informe.getOrganismoControl() != null ? informe.getOrganismoControl().getAinoc_descripcion() : "";
      String ainin_memorando = informe.getAinin_memorando() != null ? informe.getAinin_memorando() : "";
      String ainin_memo_aprobacion = informe.getAinin_memo_aprobacion() != null ? informe.getAinin_memo_aprobacion() : "";
      
      return new SolicitudDetalleDTO(
          solicitud.getAreso_codigo(),
          solicitud.getAreso_codigo(),
          solicitud.getAreso_estado_aprob(),
          solicitud.getAreso_tipo_cambio(),
          solicitud.getAreso_descripcion(),
          DateMapper.convertirFechaFormato(solicitud.getAreso_fecha_max_ant()),
          DateMapper.convertirFechaFormato(solicitud.getAreso_fecha_max_nue()),
          solicitud.getAreso_memorando(),
          solicitud.getAreso_adjunto(),
          DateMapper.convertirFechaFormato(solicitud.getAreso_fecha_carga()),
          solicitudMapper.getResponsableMapper().convertirDTOModel(solicitud.getResponsable()),
          solicitudMapper.getRecomendacionMapper().convertirModelDTO(solicitud.getRecomendacion()),
          informe.getAinin_codigo(),
          informe.getAinin_orden_trabajo(),
          informe.getAinin_titulo(),
          ainin_memorando,
          ainin_memo_aprobacion,
          ainoc_descripcion,
          informe.getAinin_usuario() != null ? informe.getAinin_usuario() : ""
      );
    } catch (Exception e) {
      logger.error("Error al convertir solicitud a detalle: {}", e.getMessage());
      // Generar y sobreescribir el PDF aprobado, luego enviarlo como adjunto al auditor
      return null;
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDinamicoDir(FiltroSolicitudDirDTO filtroDTO) {
    try {
      // Si anio es null, usar el año actual
      Integer anio = filtroDTO.getAnio() != null ? filtroDTO.getAnio() : LocalDate.now().getYear();

      List<Solicitud> solicitudes = solicitudesRepositorio.findByFilterDir(filtroDTO.getDepartamento(),
          filtroDTO.getArers_codigo_per(), anio, filtroDTO.getAreso_estado_aprob());
      List<SolicitudDetalleDTO> solicitudDTOs = solicitudes.stream()
          .filter(solicitud -> solicitud.getAreso_estado().equals('1'))
          .map(solicitud -> convertirSolicitudADetalle(solicitud))
          .collect(Collectors.toList());

      return EstadoRespuestas.respuestaObtencion("Solicitudes obtenidas exitosamente", solicitudDTOs);
    } catch (Exception e) {
      logger.error("Error al obtener las solicitudes por filtro: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener las solicitudes por filtro");
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDinamicoAud(FiltroSolicitudAudDTO filtroDTO) {
    try {
      // Si anio es null, usar el año actual
      Integer anio = filtroDTO.getAnio() != null ? filtroDTO.getAnio() : LocalDate.now().getYear();
      
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String usuario = authentication.getName();

      List<Solicitud> solicitudes = solicitudesRepositorio.findByFilterAud(filtroDTO.getDepartamento(),
          usuario, anio, filtroDTO.getAreso_estado_aprob());

      List<SolicitudDetalleDTO> solicitudDTOs = solicitudes.stream()
          .filter(solicitud -> solicitud.getAreso_estado().equals('1'))
          .map(solicitud -> convertirSolicitudADetalle(solicitud))
          .collect(Collectors.toList());

      return EstadoRespuestas.respuestaObtencion("Solicitudes obtenidas exitosamente", solicitudDTOs);
    } catch (Exception e) {
      logger.error("Error al obtener las solicitudes por filtro: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener las solicitudes por filtro");
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> aprobarSolicitud(AprobarSolicitudDTO aprobarSolicitudDTO,
      Long areso_codigo) {
    try {
      if (!solicitudesRepositorio.existsById(areso_codigo)) {
        return EstadoRespuestas.noEncontrado("Solicitud no encontrada");
      }

      Solicitud solicitudOld = solicitudesRepositorio.findById(areso_codigo).get();
      if (solicitudOld.getRecomendacion() != null && solicitudOld.getRecomendacion().getEstadoSeg() != null) {
        if (ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.enums.EstadoSegEnum.CUMPLIDA.getCodigo().equals(solicitudOld.getRecomendacion().getEstadoSeg().getArees_codigo())) {
          return EstadoRespuestas.respuestaError("No se puede procesar ni aprobar la solicitud porque la recomendación ya se encuentra CUMPLIDA.");
        }
      }
      Date fechaMaxAnt = solicitudOld.getAreso_fecha_max_ant();
      Date fechaMaxNue = solicitudOld.getAreso_fecha_max_nue();

      solicitudMapper.aprobarSolicitud(solicitudOld, aprobarSolicitudDTO);
      solicitudesRepositorio.save(solicitudOld);

      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      List<String> roles = auth.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .collect(Collectors.toList());

      Character estado = solicitudOld.getAreso_estado_aprob();
      String usuarioAprobador = auth != null ? auth.getName() : "SISTEMA";

      if (estado != null && estado == '3') {
        Long arersCodigo = solicitudOld.getResponsable() != null ? solicitudOld.getResponsable().getArers_codigo() : null;
        ActualizarFechaMaxDTO acMaxDTO = new ActualizarFechaMaxDTO(
            DateMapper.convertirFechaFormato(solicitudOld.getAreso_fecha_max_nue()),
            null,
            arersCodigo,
            aprobarSolicitudDTO.getAuditoria()
        );
        ResponseEntity<LinkedHashMap<String, Object>> response = this.recomendacionServicio.actualizarFechaMax(
            solicitudOld.getRecomendacion().getArere_codigo(),
            acMaxDTO,
            true); // esDesdeSolicitud = true

        if (!response.getStatusCode().is2xxSuccessful()) {
          return response;
        }
      }

      // Registrar evento de aprobación/cambio de estado en el historial
      try {
        String responsableNombre = "Sin asignar";
        if (solicitudOld.getResponsable() != null) {
          responsableNombre = solicitudOld.getResponsable().getArers_nombre() + " " + solicitudOld.getResponsable().getArers_apellido();
        } else if (solicitudOld.getAreso_usuario() != null) {
          responsableNombre = historialServicio.obtenerNombreCompletoPorUsuario(solicitudOld.getAreso_usuario());
        }

        String nombreAprobador = historialServicio.obtenerNombreCompletoPorUsuario(usuarioAprobador);

        String accionHistorial = "PRORROGA";
        String detalleHistorial = "";
        
        if (estado != null) {
          if (estado == '2') {
            accionHistorial = "PREAPROBACION_PRORROGA";
            detalleHistorial = "Solicitud de prórroga (solicitada por: " + responsableNombre + ") preaprobada por el Director Departamental: " + 
                nombreAprobador + ". Nueva fecha propuesta: " + DateMapper.convertirFechaFormato(fechaMaxNue) + ".";
          } else if (estado == '3') {
            accionHistorial = "PRORROGA";
            detalleHistorial = "Solicitud de prórroga (solicitada por: " + responsableNombre + ") aprobada por el Auditor: " + 
                nombreAprobador + ". Nueva fecha máxima establecida: " + DateMapper.convertirFechaFormato(fechaMaxNue) + 
                " (Anterior: " + DateMapper.convertirFechaFormato(fechaMaxAnt) + ").";
          } else if (estado == '4') {
            accionHistorial = "RECHAZO_PRORROGA";
            detalleHistorial = "Solicitud de prórroga (solicitada por: " + responsableNombre + ") rechazada por: " + 
                nombreAprobador + ".";
          }
        }
        
        if (!detalleHistorial.isEmpty()) {
          historialServicio.registrarEvento(
              solicitudOld.getRecomendacion().getArere_codigo(),
              accionHistorial,
              detalleHistorial,
              null, responsableNombre,
              fechaMaxAnt,
              fechaMaxNue,
              nombreAprobador,
              null,
              usuarioAprobador
          );
        }
      } catch (Exception ex) {
        logger.error("Error al registrar evento de aprobación de solicitud en historial: {}", ex.getMessage());
      }

      String estadoTexto;
      if (estado != null) {
        switch (estado) {
          case '1':
            estadoTexto = "PENDIENTE";
            break;
          case '2':
            estadoTexto = "APROBADO PARCIALMENTE";
            break;
          case '3':
            estadoTexto = "APROBADO";
            break;
          case '4':
            estadoTexto = "NO APROBADO";
            break;
          default:
            estadoTexto = "DESCONOCIDO";
        }
      } else {
        estadoTexto = "DESCONOCIDO";
      }

      Long arereCodigo = solicitudOld.getRecomendacion() != null ? solicitudOld.getRecomendacion().getArere_codigo() : null;
      Integer arereNumRec = solicitudOld.getRecomendacion() != null ? solicitudOld.getRecomendacion().getArere_num_rec() : null;
      Informe informe = (arereCodigo != null) ? informeServicio.obtenerInformeByRecomendacion(arereCodigo) : null;
      String memoInforme = (informe != null) ? informe.getAinin_memorando() : "(sin memorando)";

      String responsableUsuarioPer = solicitudOld.getResponsable() != null ? solicitudOld.getResponsable().getArers_usuario_per() : null;
      if (responsableUsuarioPer != null && !responsableUsuarioPer.isEmpty()) {
        boolean esRechazo = estado != null && estado == '4';
        String titulo = esRechazo ? "Solicitud rechazada" : "Solicitud actualizada";
        String tipoNotif = esRechazo ? TipoNotificacionEnum.SOLICITUD_RECHAZADA : TipoNotificacionEnum.SOLICITUD_ACTUALIZADA;
        String mensaje = String.format(
            "Solicitud de la recomendacion num: %s, del informe: %s, ha sido %s",
            arereNumRec != null ? arereNumRec.toString() : "?",
            memoInforme,
            esRechazo ? "RECHAZADA" : ("actualizada a : " + estadoTexto));

        this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + responsableUsuarioPer, "Tiene una nueva notificacion.");
        Integer anioSolAct = extraerAnio(solicitudOld.getRecomendacion().getArere_fecha_inicio_control());
        this.notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
            titulo,
            mensaje,
            tipoNotif,
            tipoNotif,
            anioSolAct,
            informe != null ? informe.getAinin_codigo() : null,
            arereCodigo,
            responsableUsuarioPer));

        // Email para rechazo a responsable + director
        if (esRechazo) {
          try {
            String correoResponsable = solicitudOld.getResponsable() != null ? solicitudOld.getResponsable().getArers_correo() : null;
            if (correoResponsable != null && !correoResponsable.trim().isEmpty()) {
              String nombreResp = solicitudOld.getResponsable().getArers_nombre() + " " + solicitudOld.getResponsable().getArers_apellido();
              wsCorreoService.enviarCorreo(correoResponsable.trim(),
                  "[SISREC] Solicitud de prórroga RECHAZADA",
                  "<p>Estimado(a) <b>" + nombreResp + "</b>,</p>"
                  + "<p>Le informamos que la solicitud de prórroga de la Recomendación N° <b>"
                  + (arereNumRec != null ? arereNumRec : "?") + "</b> del informe <b>" + memoInforme + "</b> "
                  + "ha sido <b>RECHAZADA</b>.</p>");
            }
          } catch (Exception eMail) {
            logger.error("Error al enviar correo de rechazo de solicitud: {}", eMail.getMessage());
          }
        }

      }

      // Actualizar la notificación original de solicitud del auditor como ATENDIDA (Aprobada o No aprobada)
      if (estado != null && (estado == '3' || estado == '4') && arereCodigo != null) {
        try {
          String accionResuelta = estado == '3' ? "APROBADA" : "NO APROBADA";
          String tituloResuelto = estado == '3' ? "Atendido: Solicitud aprobada" : "Atendido: Solicitud no aprobada";
          String sufijoAtendido = "Atendido: " + accionResuelta + " por el Auditor.";
          int upd = notificacionesRepositorio.actualizarNotificacionesSolicitudAtendida(
              arereCodigo,
              solicitudOld.getAreso_codigo(),
              tituloResuelto,
              sufijoAtendido
          );
          logger.info("[Solicitudes] Notificación original de solicitud actualizada como atendida: {} filas (titulo={}, solicitud={})",
              upd, tituloResuelto, solicitudOld.getAreso_codigo());

          // Notificar vía WebSocket a auditores jefes y auditor asignado para refrescar su panel en tiempo real
          try {
            List<String> jefes = rolUsuarioRepositorio.getCuentasAuditoresJefe();
            if (jefes != null) {
              for (String jefe : jefes) {
                if (jefe != null && !jefe.trim().isEmpty()) {
                  this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + jefe.trim(), "refresh");
                }
              }
            }
            if (usuarioAprobador != null && !usuarioAprobador.trim().isEmpty()) {
              this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + usuarioAprobador.trim(), "refresh");
            }
          } catch (Exception eWs) {
            logger.warn("[Solicitudes] No se pudo enviar WebSocket a los jefes/auditor: {}", eWs.getMessage());
          }
        } catch (Exception eUpd) {
          logger.warn("[Solicitudes] No se pudo actualizar notificación original de solicitud como atendida: {}", eUpd.getMessage());
        }
      }

      // Notificar al Director cuando el Auditor aprueba (estado=3) o rechaza (estado=4)
      if (estado != null && (estado == '3' || estado == '4')) {
        String directorUsuario = null;
        if (solicitudOld.getResponsable() != null && solicitudOld.getResponsable().getArers_codigo() != null) {
          try {
            directorUsuario = this.solicitudesRepositorio.findDirectorByDepartamentoResponsable(solicitudOld.getResponsable().getArers_codigo());
          } catch (Exception eDir) {
            logger.warn("No se pudo obtener director para la solicitud {}: {}", areso_codigo, eDir.getMessage());
          }
        }
        if (directorUsuario != null && !directorUsuario.trim().isEmpty() &&
            !directorUsuario.equals(usuarioAprobador)) {
          String accion = estado == '3' ? "APROBADA" : "RECHAZADA";
          String tipoNotifDir = estado == '3' ? TipoNotificacionEnum.SOLICITUD_ACTUALIZADA : TipoNotificacionEnum.SOLICITUD_RECHAZADA;
          String mensajeDir = String.format(
              "Solicitud N° %d de prórroga (Rec:%d, memo:%s) ha sido %s por el Auditor.",
              solicitudOld.getAreso_codigo(),
              arereNumRec != null ? arereNumRec : 0,
              memoInforme,
              accion);

          this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + directorUsuario, "Tiene una nueva notificacion.");
          Integer anioDir = extraerAnio(solicitudOld.getRecomendacion().getArere_fecha_inicio_control());
          this.notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
              "Solicitud N° " + solicitudOld.getAreso_codigo() + " " + accion.toLowerCase(),
              mensajeDir,
              tipoNotifDir,
              tipoNotifDir,
              anioDir,
              informe != null ? informe.getAinin_codigo() : null,
              arereCodigo,
              directorUsuario));

          try {
            String correoDirector = null;
            if (directorUsuario != null && !directorUsuario.trim().isEmpty()) {
              java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models.RolUsuario> rolesDir = rolUsuarioRepositorio.findByDsgus_cuenta(directorUsuario.trim());
              if (rolesDir != null && !rolesDir.isEmpty() && rolesDir.get(0).getAseru_correo() != null && !rolesDir.get(0).getAseru_correo().trim().isEmpty()) {
                correoDirector = rolesDir.get(0).getAseru_correo().trim();
              }
            }
            if (correoDirector == null && informe != null && informe.getAinin_codigo() != null) {
              java.util.List<String> correos = rolUsuarioRepositorio.getCorreosDirectoresByInforme(informe.getAinin_codigo());
              if (correos != null && !correos.isEmpty()) {
                correoDirector = correos.get(0);
              }
            }
            if (correoDirector != null && !correoDirector.isEmpty()) {
              wsCorreoService.enviarCorreo(correoDirector,
                  "[SISREC] Solicitud de prórroga " + accion + " por Auditor",
                  "<p>Le informamos que la solicitud de prórroga de la Recomendación N° <b>"
                  + (arereNumRec != null ? arereNumRec : 0) + "</b> del informe <b>" + memoInforme + "</b> "
                  + "ha sido <b>" + accion + "</b> por el Auditor.</p>");
            }
          } catch (Exception eMail) {
            logger.error("Error al enviar correo al Director sobre solicitud {}: {}", accion, eMail.getMessage());
          }
        }
      }

      if (estado != null && estado == '2' && informe != null && informe.getAinin_usuario() != null && !informe.getAinin_usuario().isEmpty()) {
        String auditorUsuario = informe.getAinin_usuario();
        String solicitanteNombre = (solicitudOld.getResponsable() != null)
            ? String.format("%s %s",
                solicitudOld.getResponsable().getArers_nombre() != null ? solicitudOld.getResponsable().getArers_nombre() : "",
                solicitudOld.getResponsable().getArers_apellido() != null ? solicitudOld.getResponsable().getArers_apellido() : "").trim()
            : "el responsable";

        String tituloAud = "Solicitud preaprobada";
        String memoInformeAud = (informe != null) ? informe.getAinin_memorando() : "(sin memorando)";
        Integer arereNumRecAud = solicitudOld.getRecomendacion() != null ? solicitudOld.getRecomendacion().getArere_num_rec() : null;
        String mensajeAud = String.format(
            "Solicitud de %s preaprobada por Director. N. Solicitud %d Rec:%d memo:%s (auditor asignado)(%s)",
            solicitanteNombre.isEmpty() ? "el responsable" : solicitanteNombre,
            solicitudOld.getAreso_codigo(),
            arereNumRecAud != null ? arereNumRecAud : 0,
            memoInformeAud,
            auditorUsuario);

        this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + auditorUsuario, "Tiene una nueva notificacion.");
        Integer anioSolPreAud = extraerAnio(solicitudOld.getRecomendacion().getArere_fecha_inicio_control());
        this.notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
            tituloAud,
            mensajeAud,
            TipoNotificacionEnum.SOLICITUD_PREAPROBADA,
            TipoNotificacionEnum.SOLICITUD_PREAPROBADA,
            anioSolPreAud,
            informe.getAinin_codigo(),
            arereCodigo,
            auditorUsuario));

        try {
          Configuracion configuracionFTP = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).orElse(null);
          byte[] nuevoPdf = construirPdfAprobacionDesdeFtp(solicitudOld, configuracionFTP, false);
          if (nuevoPdf != null && configuracionFTP != null) {
            String rutaRelativa = obtenerRutaRelativaAdjunto(solicitudOld.getAreso_adjunto(), configuracionFTP);
            if (rutaRelativa != null) {
              int idx = rutaRelativa.lastIndexOf('/');
              String rutaDir = idx >= 0 ? rutaRelativa.substring(0, idx + 1) : "";
              String nombreArchivo = idx >= 0 ? rutaRelativa.substring(idx + 1) : rutaRelativa;
              boolean subido = ftpService.subirArchivoBytes(nuevoPdf, rutaDir, nombreArchivo);
              if (subido) {
                logger.info("PDF regenerado y sobreescrito correctamente en FTP: {}{}", rutaDir, nombreArchivo);
              } else {
                logger.warn("No se pudo subir el PDF regenerado al FTP");
              }

              try {
                String correoAuditor = rolUsuarioRepositorio.getCorreoAuditorResponsable(informe.getAinin_codigo());
                if (correoAuditor != null && !correoAuditor.isEmpty()) {
                  java.util.Map<String, Object> vars = new java.util.HashMap<>();
                  vars.put("fecha", ec.com.eeasa.wssisrec.wssisrec.shared.utils.UtilsFunctions.getDateHour());
                  vars.put("auditor", rolUsuarioRepositorio.getNombreAuditorResponsable(informe.getAinin_codigo()));
                  vars.put("tituloInforme", informe.getAinin_titulo() != null ? informe.getAinin_titulo() : "");
                  vars.put("memoAprobacion", informe.getAinin_memorando() != null ? informe.getAinin_memorando() : "");
                  vars.put("nombreSolicitante", solicitanteNombre);
                  vars.put("nombreArchivo", nombreArchivo);
                  vars.put("numRecomendacion", arereNumRecAud != null ? arereNumRecAud : 0);
                  boolean enviado = emailService.sendTemplateEmailWithAttachment(
                      correoAuditor,
                      "Solicitud preaprobada y archivo actualizado",
                      "correo-auditor-solicitud",
                      vars,
                      nuevoPdf,
                      nombreArchivo);
                  if (enviado) {
                    logger.info("Correo enviado al auditor: {} con adjunto {}", correoAuditor, nombreArchivo);
                  } else {
                    logger.warn("No se pudo enviar correo al auditor: {}", correoAuditor);
                  }
                } else {
                  logger.warn("No se encontró correo del auditor para informe {}", informe.getAinin_codigo());
                }
              } catch (Exception eMail) {
                logger.error("Error al enviar correo al auditor con adjunto: {}", eMail.getMessage(), eMail);
              }
            }
          }
        } catch (Exception ex) {
          logger.error("Error al regenerar o enviar PDF en aprobacion: {}", ex.getMessage(), ex);
        } finally {
          ftpService.desconectarServidorFTP();
        }
      }

      if (estado != null && estado == '3' && informe != null) {
        try {
          Configuracion configuracionFTP = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).orElse(null);
          byte[] nuevoPdf = construirPdfAprobacionDesdeFtp(solicitudOld, configuracionFTP, true);
          if (nuevoPdf != null && configuracionFTP != null) {
            String rutaRelativa = obtenerRutaRelativaAdjunto(solicitudOld.getAreso_adjunto(), configuracionFTP);
            if (rutaRelativa != null) {
              int idx = rutaRelativa.lastIndexOf('/');
              String rutaDir = idx >= 0 ? rutaRelativa.substring(0, idx + 1) : "";
              String nombreArchivo = idx >= 0 ? rutaRelativa.substring(idx + 1) : rutaRelativa;
              boolean subido = ftpService.subirArchivoBytes(nuevoPdf, rutaDir, nombreArchivo);
              if (subido) {
                logger.info("PDF actualizado con aprobación de auditor en FTP: {}{}", rutaDir, nombreArchivo);
              } else {
                logger.warn("No se pudo subir el PDF actualizado con aprobación de auditor");
              }
            }
          }
        } catch (Exception ex) {
          logger.error("Error al actualizar PDF con aprobación de auditor: {}", ex.getMessage(), ex);
        } finally {
          ftpService.desconectarServidorFTP();
        }
      }

      bitacoraServicio.registrarBitacora(solicitudOld, aprobarSolicitudDTO.getAuditoria(), "APROBAR");
      return EstadoRespuestas.respuestaCreacion("Aprobación de solicitud exitosa");
    } catch (Exception e) {
      logger.error("Error al aprobar solicitud : {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al aprobar solicitud : " + e);
    }
  }

  private boolean existeSolicitudNoAprobada(Long arere_codigo, String codigo_per) {
    List<Solicitud> solicituds = solicitudesRepositorio.findByRecomendacionResponsablePer(arere_codigo, codigo_per);
    long nNoAprobados = solicituds.stream()
        .filter(solicitud -> solicitud.getAreso_estado_aprob().equals('1') || solicitud.getAreso_estado_aprob().equals('2'))
        .count();
        return nNoAprobados > 0;
  }

  private String obtenerRutaRelativaAdjunto(String rutaAdjunto, Configuracion configuracionFTP) {
    if (rutaAdjunto == null || rutaAdjunto.trim().isEmpty()) {
      return null;
    }

    String rutaRelativa = rutaAdjunto;
    if (configuracionFTP != null && rutaAdjunto.contains(configuracionFTP.getAseco_nombre_host())) {
      rutaRelativa = rutaAdjunto.split(configuracionFTP.getAseco_nombre_host())[1];
    }
    return rutaRelativa;
  }

  private boolean esUsuarioDirectorActual() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return false;
    }

    String director = RolEnum.DIRECTOR_DEPARTAMENTAL.getCode().toString();
    String auditorJefe = RolEnum.AUDITOR_JEFE_INTERNO.getCode().toString();

    return auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(autoridad -> director.equals(autoridad) || auditorJefe.equals(autoridad));
  }

  private byte[] construirPdfAprobacionDesdeFtp(Solicitud solicitudOld, Configuracion configuracionFTP, boolean esAuditor) {
    boolean conectado = false;
    try {
      if (configuracionFTP == null || solicitudOld == null || solicitudOld.getAreso_adjunto() == null || solicitudOld.getAreso_adjunto().isEmpty()) {
        return null;
      }
      conectado = ftpService.conectarServidorFTP(
          configuracionFTP.getAseco_host(),
          configuracionFTP.getAseco_user(),
          configuracionFTP.getAseco_contrasenia());
      if (!conectado) {
        return null;
      }

      String rutaRelativa = obtenerRutaRelativaAdjunto(solicitudOld.getAreso_adjunto(), configuracionFTP);
      if (rutaRelativa == null) {
        return null;
      }

      String rutaBaseRelativa = null;
      int idxNombreOriginal = rutaRelativa.lastIndexOf('/');
      if (idxNombreOriginal >= 0) {
        rutaBaseRelativa = rutaRelativa.substring(0, idxNombreOriginal + 1) + "base-" + rutaRelativa.substring(idxNombreOriginal + 1);
      }

      byte[] originalPdf = null;
      if (esAuditor) {
        originalPdf = ftpService.descargarArchivoFTPNuevo(rutaRelativa);
      } else {
        if (rutaBaseRelativa != null) {
          originalPdf = ftpService.descargarArchivoFTPNuevo(rutaBaseRelativa);
        }
        if (originalPdf == null) {
          originalPdf = ftpService.descargarArchivoFTPNuevo(rutaRelativa);
        }
      }

      Authentication authAprob = SecurityContextHolder.getContext().getAuthentication();
      String usuarioAprob = authAprob != null ? authAprob.getName() : null;
      String aprobadorNombre = usuarioAprob;
      String aprobadorCargo = "";
      String aprobadorDepartamento = "";
      try {
        if (usuarioAprob != null) {
          java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models.RolUsuario> rolesUsu = rolUsuarioRepositorio.findByDsgus_cuenta(usuarioAprob);
          if (rolesUsu != null && !rolesUsu.isEmpty()) {
            ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models.RolUsuario r = rolesUsu.get(0);
            aprobadorNombre = (r.getAseru_nombre() != null ? r.getAseru_nombre() + " " : "") + (r.getAseru_apellido() != null ? r.getAseru_apellido() : "");
            aprobadorDepartamento = r.getAseru_departamento() != null ? r.getAseru_departamento() : "";

            String correoAprobador = r.getAseru_correo();
            if (correoAprobador != null && !correoAprobador.trim().isEmpty()) {
              DirectorInformacionDTO infoDirector = rolUsuarioServicio.obtenerInfoDirector(correoAprobador.trim());
              if (infoDirector != null) {
                if (infoDirector.getDirector() != null && !infoDirector.getDirector().trim().isEmpty()) {
                  aprobadorNombre = infoDirector.getDirector().trim();
                }
                if (infoDirector.getDepartamento() != null && !infoDirector.getDepartamento().trim().isEmpty()) {
                  aprobadorDepartamento = infoDirector.getDepartamento().trim();
                }
                if (infoDirector.getCargo() != null && !infoDirector.getCargo().trim().isEmpty()) {
                  aprobadorCargo = infoDirector.getCargo().trim();
                }
                if ((aprobadorCargo == null || aprobadorCargo.isEmpty()) && r.getAseru_codigo_per() != null) {
                  try {
                    String cargoFallback = solicitudesRepositorio.findCargoByCodigoPer(r.getAseru_codigo_per()).orElse(null);
                    if (cargoFallback != null && !cargoFallback.trim().isEmpty()) {
                      aprobadorCargo = cargoFallback.trim();
                    }
                  } catch (Exception qe) {
                  }
                }
              }
            }

            if ((aprobadorCargo == null || aprobadorCargo.isEmpty()) && r.getRol() != null && r.getRol().getAsero_descripcion() != null) {
              aprobadorCargo = r.getRol().getAsero_descripcion();
            }
          }
        }
      } catch (Exception ex) {
        logger.warn("No se pudo obtener información detallada del aprobador: {}", ex.getMessage());
      }

      // Determinar si el creador de la solicitud era Director Departamental o su equivalente funcional (AIJ)
      boolean creadorEsDirector = false;
      try {
        String usuarioCreador = solicitudOld.getAreso_usuario();
        if (usuarioCreador != null && !usuarioCreador.trim().isEmpty()) {
          java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models.RolUsuario> rolesCreador = rolUsuarioRepositorio.findByDsgus_cuenta(usuarioCreador);
          if (rolesCreador != null && !rolesCreador.isEmpty()) {
            for (ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models.RolUsuario ru : rolesCreador) {
              if (ru != null && ru.getRol() != null && ru.getRol().getAsero_codigo() != null) {
                if (ru.getRol().getAsero_codigo().equals(ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.enums.RolEnum.DIRECTOR_DEPARTAMENTAL.getCode())
                    || ru.getRol().getAsero_codigo().equals(ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.enums.RolEnum.AUDITOR_JEFE_INTERNO.getCode())) {
                  creadorEsDirector = true;
                  break;
                }
              }
            }
          }
        }
      } catch (Exception ex) {
        logger.warn("No se pudo determinar rol del creador: {}", ex.getMessage());
      }

      boolean colocarDebajoDirector = esAuditor && !creadorEsDirector;
      return solicitudPdfService.generarPdfAprobacion(originalPdf, aprobadorNombre, aprobadorCargo, aprobadorDepartamento, esAuditor, colocarDebajoDirector);
    } catch (Exception ex) {
      logger.error("Error construyendo PDF de aprobación: {}", ex.getMessage(), ex);
      return null;
    }
  }
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarSolicitud(Long areso_codigo) {
    try {
      if (!solicitudesRepositorio.existsById(areso_codigo)) {
        return EstadoRespuestas.noEncontrado("Solicitud no encontrada");
      }

      Solicitud solicitud = solicitudesRepositorio.findById(areso_codigo).get();

      // Permitir eliminar si está pendiente ('1'), o si está en Aprobado Parcial ('2') y el usuario es Director Departamental o Auditor Jefe
      Character estadoAprob = solicitud.getAreso_estado_aprob();
      if (estadoAprob != null && estadoAprob != '1') {
        if (estadoAprob == '2') {
          boolean esDirectorOJefe = esUsuarioDirectorActual();
          if (!esDirectorOJefe) {
            try {
              Authentication auth = SecurityContextHolder.getContext().getAuthentication();
              if (auth != null && auth.getName() != null) {
                String usuarioActual = auth.getName().trim();
                Integer countDir = rolUsuarioRepositorio.findByDsgus_cuentaAndAsero_codigo(
                    usuarioActual, 
                    RolEnum.DIRECTOR_DEPARTAMENTAL.getCode()
                );
                Integer countJefe = rolUsuarioRepositorio.findByDsgus_cuentaAndAsero_codigo(
                    usuarioActual, 
                    RolEnum.AUDITOR_JEFE_INTERNO.getCode()
                );
                if ((countDir != null && countDir > 0) || (countJefe != null && countJefe > 0)) {
                  esDirectorOJefe = true;
                }
              }
            } catch (Exception ex) {
              logger.error("Error al validar rol de Director/Jefe en eliminarSolicitud: {}", ex.getMessage());
            }
          }

          if (!esDirectorOJefe) {
            return EstadoRespuestas.respuestaError("Solo un Director Departamental o Auditor Jefe puede eliminar solicitudes en estado APROBADO PARCIAL");
          }
        } else {
          return EstadoRespuestas.respuestaError("Solo se pueden eliminar solicitudes en estado PENDIENTE o APROBADO PARCIAL");
        }
      }

      Recomendacion recomendacion = solicitud.getRecomendacion();
      Long arereCodigo = recomendacion.getArere_codigo();

      String auditor = solicitudesRepositorio.findAuditorUsuarioByRecomendacion(arereCodigo).orElse("").trim();
      String memorando = solicitudesRepositorio.findMemorandoByRecomendacion(arereCodigo).orElse("").trim();
      String num_rec = solicitudesRepositorio.findNumRecByRecomendacion(arereCodigo).orElse("").trim();
      String nombreResponsable = solicitud.getResponsable() != null 
          ? (solicitud.getResponsable().getArers_nombre() + " " + solicitud.getResponsable().getArers_apellido()).trim()
          : "";

      solicitud.setAreso_estado('0');
      solicitudesRepositorio.save(solicitud);

      AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
      auditoriaDTO.setHost("SISREC");
      auditoriaDTO.setUsuario(solicitud.getAreso_usuario());

      bitacoraServicio.registrarBitacora(solicitud, auditoriaDTO, "ELIMINAR");

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
        // Si elimina el Auditor Operativo, se notifica directamente a los Jefes de Auditoría
        List<String> auditoresJefe = rolUsuarioRepositorio.getCuentasAuditoresJefe();
        if (auditoresJefe != null && !auditoresJefe.isEmpty()) {
          for (String jefe : auditoresJefe) {
            if (jefe != null && !jefe.trim().isEmpty()) {
              String mensajeJefe = "El Auditor " + usuarioLogueado + " eliminó la solicitud de prórroga del informe con memorando " + memorando + " Num. Recomendacion: " + num_rec;
              Integer anioElimSolAud = extraerAnio(recomendacion.getArere_fecha_inicio_control());
              NuevaNotificacionDTO notiJefe = new NuevaNotificacionDTO(
                  "Solicitud de prórroga eliminada por auditor", 
                  mensajeJefe, 
                  TipoNotificacionEnum.SOLICITUD_ELIMINADA_AUDITOR,
                  TipoNotificacionEnum.SOLICITUD_ELIMINADA_AUDITOR,
                  anioElimSolAud,
                  informeServicio.obtenerInformeByRecomendacion(arereCodigo).getAinin_codigo(),
                  arereCodigo,
                  jefe.trim()
              );
              notificacionesServicio.registrarNotificacion(notiJefe);

              try {
                simpMessagingTemplate.convertAndSend("/topic/notificacion/" + jefe.trim(), "Una solicitud de prórroga fue eliminada por el auditor.");
              } catch (Exception ex) {
                logger.error("[Notificaciones] Error al enviar WebSocket al jefe para prórroga: {}", ex.getMessage());
              }
            }
          }
        }
      } else {
        // Si elimina el responsable o director, se notifica al Auditor Operativo (se replica automáticamente al Jefe)
        if (!auditor.isEmpty()) {
          String mensaje = nombreResponsable + " eliminó la solicitud de prórroga del informe con memorando " + memorando + " Num. Recomendacion: " + num_rec;
          Integer anioElimSol = extraerAnio(recomendacion.getArere_fecha_inicio_control());
          NuevaNotificacionDTO notiEliminacion = new NuevaNotificacionDTO(
              "Solicitud de prórroga eliminada", 
              mensaje, 
              TipoNotificacionEnum.SOLICITUD_ELIMINADA,
              TipoNotificacionEnum.SOLICITUD_ELIMINADA,
              anioElimSol,
              informeServicio.obtenerInformeByRecomendacion(arereCodigo).getAinin_codigo(),
              arereCodigo,
              auditor
          );
          notificacionesServicio.registrarNotificacion(notiEliminacion);

          // Notificación en tiempo real por WebSocket
          try {
            simpMessagingTemplate.convertAndSend("/topic/notificacion/" + auditor, "Tiene una solicitud de prórroga eliminada.");
          } catch (Exception ex) {
            logger.error("[Notificaciones] Error al enviar WebSocket para prórroga eliminada: {}", ex.getMessage());
          }
        }
      }

      return EstadoRespuestas.respuestaCreacion("Solicitud eliminada correctamente");
    } catch (Exception e) {
      logger.error("Error al eliminar solicitud : {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar solicitud : " + e.getMessage());
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDetalleSolicitud(Long areso_codigo) {
    try {
      Solicitud solicitud = solicitudesRepositorio.findById(areso_codigo).get();
      
      SolicitudDetalleDTO solicitudDetalleDTO = convertirSolicitudADetalle(solicitud);
      
      return EstadoRespuestas.respuestaObtencion("Detalle de solicitud obtenido exitosamente", solicitudDetalleDTO);
    } catch (Exception e) {
      logger.error("Error al obtener detalle de solicitud: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener detalle de solicitud: " + e.getMessage());
    }
  }

  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDirectorDepartamental(String codigo_per) {
    try {
      // Obtener responsable por código personal
      Responsable responsable = null;
      String departamento = null;
      String cargoResponsable = null;
      
      try {
        responsable = responsableRepositorio.findTopByCodigoPer(codigo_per).orElse(null);
        if (responsable != null) {
          departamento = responsable.getArers_departamento();
          cargoResponsable = responsable.getArers_cargo();
        }
      } catch (Exception qe) {
      }

      // Si no se encontró cargo, intentar buscarlo directamente
      if ((cargoResponsable == null || cargoResponsable.isEmpty()) && codigo_per != null) {
        try {
          cargoResponsable = solicitudesRepositorio.findCargoByCodigoPer(codigo_per).orElse(null);
        } catch (Exception qe) {
        }
      }

      // Si no existe responsable en BD o no tiene departamento, buscar en AI_SEG_ROL_USU
      if (departamento == null || departamento.isEmpty()) {
        try {
          departamento = solicitudesRepositorio.findDepartamentoByCodigoPer(codigo_per).orElse(null);
        } catch (Exception qe) {
        }
      }

      // Si se encontró departamento (de cualquier origen), buscar director
      if (departamento != null && !departamento.isEmpty()) {
        String nombreDirector = solicitudesRepositorio.findDirectorByDepartamento(departamento).orElse(null);
        
        if (nombreDirector != null && !nombreDirector.isEmpty()) {
          Long numeroSolicitud = solicitudesRepositorio.obtenerProximoNumeroSolicitud();
          DirectorDepartamentalDTO directorDTO = new DirectorDepartamentalDTO();
          directorDTO.setNumeroSolicitud(numeroSolicitud);
          directorDTO.setNombreDirector(nombreDirector);
          directorDTO.setDepartamento(departamento);
          directorDTO.setCorreoDirector("");
          directorDTO.setCargoResponsable(cargoResponsable != null ? cargoResponsable.trim() : "");
          
          return EstadoRespuestas.respuestaObtencion(
              "Director departamental y número de solicitud obtenidos correctamente.", directorDTO);
        } else {
          return EstadoRespuestas.noEncontrado("Director no encontrado para el departamento: " + departamento);
        }
      }

      // Si no se encontró departamento en ningún lado, usar fallback del usuario loguado
      ResponseEntity<LinkedHashMap<String, Object>> fallbackResponse = obtenerDirectorDelUsuarioActual();
      return fallbackResponse;
      
    } catch (Exception e) {
      return EstadoRespuestas.respuestaError("Error al obtener director departamental: " + e.getMessage());
    }
  }

  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDirectorDelUsuarioActual() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String usuarioLoguado = authentication.getName();

      // Obtener próximo número de solicitud
      Long numeroSolicitud = solicitudesRepositorio.obtenerProximoNumeroSolicitud();

      // Si el usuario es director, retorna su propio nombre como director
      // (caso donde es director y responsable a la vez)
      DirectorDepartamentalDTO directorDTO = new DirectorDepartamentalDTO();
      directorDTO.setNumeroSolicitud(numeroSolicitud);
      directorDTO.setNombreDirector(usuarioLoguado); // El director es el mismo usuario
      directorDTO.setDepartamento(""); // Sin departamento específico, es del usuario loguado
      directorDTO.setCorreoDirector("");

      return EstadoRespuestas.respuestaObtencion(
          "Director departamental y número de solicitud obtenidos correctamente (usuario es director).", directorDTO);
    } catch (Exception e) {
      return EstadoRespuestas.respuestaError("Error al obtener director: " + e.getMessage());
    }
  }

  public ResponseEntity<LinkedHashMap<String, Object>> obtenerAuditorAsignado(Long arere_codigo) {
    try {
      // Buscar auditor asignado a la recomendación
      String nombreAuditor = solicitudesRepositorio.findAuditorByRecomendacion(arere_codigo)
          .map(String::trim)
          .orElse(null);

      String cargoResponsable = null;
      try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usuarioLogueado = authentication != null ? authentication.getName() : null;
        if (usuarioLogueado != null && !usuarioLogueado.trim().isEmpty()) {
          List<ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models.RolUsuario> usuariosResponsable = rolUsuarioRepositorio.findByDsgus_cuenta(usuarioLogueado.trim());
          if (usuariosResponsable != null && !usuariosResponsable.isEmpty()) {
            ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models.RolUsuario usuarioResponsable = usuariosResponsable.get(0);
            if (usuarioResponsable.getAseru_correo() != null && !usuarioResponsable.getAseru_correo().trim().isEmpty()) {
              DirectorInformacionDTO infoResponsable = rolUsuarioServicio.obtenerInfoDirector(usuarioResponsable.getAseru_correo().trim());
              if (infoResponsable != null && infoResponsable.getCargo() != null && !infoResponsable.getCargo().trim().isEmpty()) {
                cargoResponsable = infoResponsable.getCargo().trim();
              }
            }

            if ((cargoResponsable == null || cargoResponsable.isEmpty()) && usuarioResponsable.getAseru_codigo_per() != null) {
              cargoResponsable = solicitudesRepositorio.findCargoByCodigoPer(usuarioResponsable.getAseru_codigo_per()).orElse(null);
            }
          }
        }
      } catch (Exception exCargo) {
      }

      if (nombreAuditor == null || nombreAuditor.isEmpty()) {
        return EstadoRespuestas.noEncontrado("Auditor no encontrado para la recomendación: " + arere_codigo);
      }

      // Obtener próximo número de solicitud
      Long numeroSolicitud = solicitudesRepositorio.obtenerProximoNumeroSolicitud();

      DirectorDepartamentalDTO auditorDTO = new DirectorDepartamentalDTO();
      auditorDTO.setNumeroSolicitud(numeroSolicitud);
      auditorDTO.setNombreDirector(nombreAuditor); // Se reutiliza el campo para auditor
      auditorDTO.setDepartamento(""); // No aplica para auditor
      auditorDTO.setCorreoDirector("");
      auditorDTO.setCargoResponsable(cargoResponsable != null ? cargoResponsable.trim() : "");

      return EstadoRespuestas.respuestaObtencion(
          "Auditor y número de solicitud obtenidos correctamente.", auditorDTO);
    } catch (Exception e) {
      return EstadoRespuestas.respuestaError("Error al obtener auditor: " + e.getMessage());
    }
  }

  @Transactional
  public void depurarSolicitudesCaducadas() {
    try {
      List<Solicitud> solicitudesCaducadas = solicitudesRepositorio.findSolicitudesPendientesCaducadas();
      if (solicitudesCaducadas == null || solicitudesCaducadas.isEmpty()) {
        return;
      }
      logger.info("[DEPURACION SOLICITUDES] Se encontraron {} solicitudes pendientes caducadas para procesar", solicitudesCaducadas.size());

      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

      for (Solicitud sol : solicitudesCaducadas) {
        if (sol == null || sol.getRecomendacion() == null) {
          continue;
        }

        Long arereCodigo = sol.getRecomendacion().getArere_codigo();
        Long aresoCodigo = sol.getAreso_codigo();
        Recomendacion rec = sol.getRecomendacion();

        // Validación defensiva: asegurar que la fecha propuesta sea estrictamente anterior a hoy
        if (sol.getAreso_fecha_max_nue() != null) {
          LocalDate fechaPropuesta = new java.sql.Date(sol.getAreso_fecha_max_nue().getTime()).toLocalDate();
          if (!fechaPropuesta.isBefore(LocalDate.now())) {
            logger.info("[DEPURACION SOLICITUDES] Solicitud N° {} omitida: fecha propuesta ({}) es hoy o futura, aún tiene vigencia para atención",
                aresoCodigo, fechaPropuesta);
            continue;
          }
        }

        // 1. Obtener estado de aprobación previo y responsable
        Character estadoAprobPrevio = sol.getAreso_estado_aprob();

        String responsableNombre = "Sin asignar";
        if (sol.getResponsable() != null) {
          responsableNombre = sol.getResponsable().getArers_nombre() + " " + sol.getResponsable().getArers_apellido();
        } else if (sol.getAreso_usuario() != null) {
          responsableNombre = historialServicio.obtenerNombreCompletoPorUsuario(sol.getAreso_usuario());
        }

        String rolNoAtendio = (estadoAprobPrevio != null && estadoAprobPrevio == '2') 
            ? "Auditor Operativo" 
            : "Director Departamental";

        // 2. Marcar la solicitud como NO APROBADO ('4')
        sol.setAreso_estado_aprob('4');
        solicitudesRepositorio.save(sol);

        // 3. Determinar escenario de descarte
        boolean esCumplida = (rec.getEstadoSeg() != null && rec.getEstadoSeg().getArees_codigo() != null && rec.getEstadoSeg().getArees_codigo() == 2);
        long posterioresVigentes = solicitudesRepositorio.countSolicitudesPosterioresVigentes(arereCodigo, aresoCodigo);

        long verificablesJustificados = 0;
        if (sol.getResponsable() != null && sol.getResponsable().getArers_codigo() != null) {
          verificablesJustificados = solicitudesRepositorio.countVerificablesJustificadosPorResponsable(arereCodigo, sol.getResponsable().getArers_codigo());
        }

        String fechaAntStr = sol.getAreso_fecha_max_ant() != null ? sdf.format(sol.getAreso_fecha_max_ant()) : "N/A";
        String fechaNueStr = sol.getAreso_fecha_max_nue() != null ? sdf.format(sol.getAreso_fecha_max_nue()) : "N/A";

        String motivoDescarte;
        boolean enviarNotificacion = false;

        if (esCumplida) {
          motivoDescarte = "Recomendación CUMPLIDA";
        } else if (posterioresVigentes > 0) {
          motivoDescarte = "Existe solicitud posterior registrada";
        } else if (verificablesJustificados > 0) {
          motivoDescarte = "Responsable ya cuenta con verificables en estado JUSTIFICA";
        } else {
          motivoDescarte = "Fecha solicitada transcurrida sin atención oportuna";
          enviarNotificacion = true;
        }

        String detalleHistorial = "Descarte automático por el sistema: Solicitud N° " + aresoCodigo + 
            " (Solicitada por: " + responsableNombre + ", fecha solicitada: " + fechaNueStr + ", anterior: " + fechaAntStr + ")" +
            " no fue atendida en su momento por el " + rolNoAtendio + ". Motivo de descarte: " + motivoDescarte + ".";

        // 4. Registrar en AI_REC_HISTORIAL
        try {
          historialServicio.registrarEvento(
              arereCodigo,
              "CADUCIDAD_SOLICITUD",
              detalleHistorial,
              null,
              responsableNombre,
              sol.getAreso_fecha_max_ant(),
              sol.getAreso_fecha_max_nue(),
              "SISTEMA",
              detalleHistorial,
              "SISTEMA"
          );
        } catch (Exception eHist) {
          logger.error("[DEPURACION SOLICITUDES] Error al registrar historial para la solicitud {}: {}", aresoCodigo, eHist.getMessage());
        }

        // 4. Enviar notificación por correo únicamente en el caso C
        if (enviarNotificacion) {
          try {
            if (sol.getResponsable() != null && sol.getResponsable().getArers_correo() != null && !sol.getResponsable().getArers_correo().trim().isEmpty()) {
              String correoDestino = sol.getResponsable().getArers_correo().trim();
              String asunto = "[SISREC] Notificación: Solicitud de ampliación de plazo rechazada por caducidad";
              String numRec = rec.getArere_num_rec() != null ? String.valueOf(rec.getArere_num_rec()) : String.valueOf(arereCodigo);
              
              String cuerpoHtml = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;\">"
                  + "<h2 style=\"color: #1e3a8a;\">EMPRESA ELÉCTRICA AMBATO REGIONAL CENTRO SUR S.A.</h2>"
                  + "<h3 style=\"color: #3b82f6;\">Sistema de Seguimiento de Recomendaciones (SISREC)</h3>"
                  + "<hr/>"
                  + "<p>Estimado(a) <b>" + sol.getResponsable().getArers_nombre() + " " + sol.getResponsable().getArers_apellido() + "</b>,</p>"
                  + "<p>Le informamos que la solicitud de ampliación de plazo N° <b>" + aresoCodigo + "</b> asignada a la Recomendación N° <b>" + numRec + "</b> "
                  + "ha sido <b>RECHAZADA AUTOMÁTICAMENTE</b> por el sistema debido a que la fecha propuesta (<b>" + fechaNueStr + "</b>) ha transcurrido.</p>"
                  + "<div style=\"background-color: #f1f5f9; padding: 12px; border-left: 4px solid #ef4444; margin: 16px 0;\">"
                  + "<p style=\"margin: 0;\"><b>Motivo:</b> La fecha solicitada (" + fechaNueStr + ") es anterior a la fecha actual sin atención por parte del " + rolNoAtendio + ".</p>"
                  + "</div>"
                  + "<p>Si la recomendación aún se encuentra en proceso de implementación y requiere una ampliación de plazo, por favor genere una <b>NUEVA SOLICITUD DE PRÓRROGA</b> en el sistema especificando una fecha compromiso futura.</p>"
                  + "<hr/>"
                  + "<p style=\"font-size: 12px; color: #64748b;\">Este es un mensaje automático generado por SISREC. Por favor no responda a este correo.</p>"
                  + "</div>";

              wsCorreoService.enviarCorreo(correoDestino, asunto, cuerpoHtml);
            }

            // Notificación en el sistema para el responsable
            if (sol.getResponsable() != null && sol.getResponsable().getArers_usuario_per() != null
                && !sol.getResponsable().getArers_usuario_per().trim().isEmpty()) {
              String respUsuario = sol.getResponsable().getArers_usuario_per().trim();
              String numRecStr = rec.getArere_num_rec() != null ? String.valueOf(rec.getArere_num_rec()) : String.valueOf(arereCodigo);
              String memo = "";
              try { Informe infTemp = informeServicio.obtenerInformeByRecomendacion(arereCodigo);
                memo = infTemp != null && infTemp.getAinin_memorando() != null ? infTemp.getAinin_memorando() : "(sin memorando)";
              } catch (Exception eMemo) {}
              Integer anioCad = extraerAnio(rec.getArere_fecha_inicio_control());
              this.notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
                  "Solicitud caducada",
                  "Su solicitud de prórroga N° " + aresoCodigo + " (Rec: " + numRecStr + ", memo: " + memo + ") ha sido rechazada automáticamente por caducidad.",
                  TipoNotificacionEnum.SOLICITUD_CADUCADA,
                  TipoNotificacionEnum.SOLICITUD_CADUCADA,
                  anioCad,
                  null,
                  arereCodigo,
                  respUsuario));
            }

            // Notificación en el sistema para quien debió atender (Director o Auditor)
            try {
              String usuarioNoAtendio = null;
              if (sol.getResponsable() != null && sol.getResponsable().getArers_codigo() != null) {
                if (estadoAprobPrevio != null && estadoAprobPrevio == '2') {
                  Informe informeCad = informeServicio.obtenerInformeByRecomendacion(arereCodigo);
                  if (informeCad != null) {
                    usuarioNoAtendio = informeCad.getAinin_usuario();
                  }
                } else {
                  usuarioNoAtendio = solicitudesRepositorio.findDirectorByDepartamentoResponsable(sol.getResponsable().getArers_codigo());
                }
              }
              if (usuarioNoAtendio != null && !usuarioNoAtendio.trim().isEmpty()) {
                String numRecStr = rec.getArere_num_rec() != null ? String.valueOf(rec.getArere_num_rec()) : String.valueOf(arereCodigo);
                String memo = "";
                try { Informe infTemp = informeServicio.obtenerInformeByRecomendacion(arereCodigo);
                  memo = infTemp != null && infTemp.getAinin_memorando() != null ? infTemp.getAinin_memorando() : "(sin memorando)";
                } catch (Exception eMemo) {}
                Integer anioCad = extraerAnio(rec.getArere_fecha_inicio_control());
                simpMessagingTemplate.convertAndSend("/topic/notificacion/" + usuarioNoAtendio, "Tiene una nueva notificacion.");
                this.notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
                    "Solicitud caducada",
                    "La solicitud de prórroga N° " + aresoCodigo + " (Rec: " + numRecStr + ", memo: " + memo + ") ha caducado sin su atención.",
                    TipoNotificacionEnum.SOLICITUD_CADUCADA,
                    TipoNotificacionEnum.SOLICITUD_CADUCADA,
                    anioCad,
                    null,
                    arereCodigo,
                    usuarioNoAtendio));
              }
            } catch (Exception eNotif2) {
              logger.warn("[DEPURACION SOLICITUDES] No se pudo enviar notificación al responsable de atender la solicitud {}: {}", aresoCodigo, eNotif2.getMessage());
            }
          } catch (Exception eMail) {
            logger.error("[DEPURACION SOLICITUDES] Error al enviar notificación por correo para la solicitud {}: {}", aresoCodigo, eMail.getMessage());
          }
        }
      }

    } catch (Exception e) {
      logger.error("[DEPURACION SOLICITUDES] Error general en la depuración automática de solicitudes caducadas: {}", e.getMessage(), e);
    }
  }
}

