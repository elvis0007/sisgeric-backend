package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto.ActualizarInformeDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto.ActualizarUsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto.InformeDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto.InformeFiltroDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto.NuevoInformeDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.mapper.InformeMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models.Informe;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.OrganismosControlServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.models.OrganismoControl;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.TipoInfServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.models.TipoInforme;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.ConfiguracionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.enums.ConfiguracionEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.models.Configuracion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.enums.RolEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.AuditoriaMapper;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.FileMapper;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.Ftp.FtpService;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.HallazgoRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.models.Hallazgo;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.CorteEvaluacionServicio;

@Service
public class InformeServicio {
  private final String TITULO = "Informe";
  private static final Logger logger = LoggerFactory.getLogger(InformeServicio.class);

  @Autowired
  private InformeRepositorio informeRepositorio;

  @Autowired
  @Lazy
  private TipoInfServicio tipoInfServicio;

  @Autowired
  private BitacoraServicio bitacoraServicio;

  @Autowired
  private OrganismosControlServicio organismoServicio;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private InformeMapper informeMapper;

  @Autowired
  private FtpService ftpService;

  @Autowired
  private ConfiguracionRepositorio configuracionRepositorio;

  @Autowired
  private RolUsuarioServicio rolUsuarioServicio;

  @Autowired
  private HallazgoRepositorio hallazgoRepositorio;

  @Autowired
  @Lazy
  private CorteEvaluacionServicio corteEvaluacionServicio;

  /**
   * Obtiene informes de manera dinámica basado en los filtros especificados.
   *
   * @param informeFiltroDTO DTO con los criterios de filtrado
   * @return ResponseEntity con la lista de informes obtenidos
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> getInformesAuditorOperativo(InformeFiltroDTO informeFiltroDTO) {
    logger.info("===== [filtro-auditor-operativo] Parámetros recibidos =====");
    logger.info("  organismo_control: {}", informeFiltroDTO.getAinoc_organismo_control());
    logger.info("  tipo_inf: {}", informeFiltroDTO.getAinti_tipo_inf());
    logger.info("  fecha_inicio: '{}'", informeFiltroDTO.getAinin_fecha_inicio());
    logger.info("  fecha_fin: '{}'", informeFiltroDTO.getAinin_fecha_fin());
    logger.info("  usuario_filtro: '{}'", informeFiltroDTO.getAinin_usuario());
    logger.info("================================================");
    return jdbcTemplate.execute((Connection conn) -> {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String usuario = auth.getName();
      List<String> roles = obtenerRoles(auth);

      // Solo obtener informes del auditor logueado
      String functionCall = determinarFunctionCall(roles);
      List<InformeDTO> informeDtos = new ArrayList<>();

      try {
        informeDtos = ejecutarConsultaInformes(conn, functionCall, informeFiltroDTO, roles, usuario, true);
        return EstadoRespuestas.respuestaObtencion("Informes obtenidos exitosamente", informeDtos);
      } catch (Exception e) {
        logger.error("Error al obtener los informes del auditor operativo: {}", e.getMessage());
        return EstadoRespuestas.noEncontrado("Error al obtener los informes: " + e);
      } finally {
        if (!conn.isClosed()) {
          conn.close();
        }
      }
    });
  }

  /**
   * Obtiene informes de manera dinámica basado en los filtros especificados.
   *
   * @param informeFiltroDTO DTO con los criterios de filtrado
   * @return ResponseEntity con la lista de informes obtenidos
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> getInformesAuditorGeneral(InformeFiltroDTO informeFiltroDTO) {
    return jdbcTemplate.execute((Connection conn) -> {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String usuario = auth.getName();
      List<String> roles = obtenerRoles(auth);

      // Obtener todos los informes de todos los auditores
      String functionCall = determinarFunctionCall(roles);
      List<InformeDTO> informeDtos = new ArrayList<>();

      try {
        informeDtos = ejecutarConsultaInformes(conn, functionCall, informeFiltroDTO, roles, usuario, false);
        return EstadoRespuestas.respuestaObtencion("Informes obtenidos exitosamente", informeDtos);
      } catch (Exception e) {
        logger.error("Error al obtener los informes del auditor general: {}", e.getMessage());
        return EstadoRespuestas.noEncontrado("Error al obtener los informes: " + e);
      }
      finally {
        if (!conn.isClosed()) {
          conn.close();
        }
      }
    });
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> getInformesDirector(InformeFiltroDTO informeFiltroDTO) {
    return jdbcTemplate.execute((Connection conn) -> {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String usuario = auth.getName();
      List<String> roles = obtenerRoles(auth);

      String functionCall = determinarFunctionCall(roles);
      List<InformeDTO> informeDtos = new ArrayList<>();

      try {
        informeDtos = ejecutarConsultaInformes(conn, functionCall, informeFiltroDTO, roles, usuario, false);
        return EstadoRespuestas.respuestaObtencion("Informes obtenidos exitosamente", informeDtos);
      } catch (Exception e) {
        logger.error("Error al obtener los informes del director: {}", e.getMessage());
        return EstadoRespuestas.noEncontrado("Error al obtener los informes: " + e);
      } finally {
        if (!conn.isClosed()) {
          conn.close();
        }
      }
    });
  }

  /**
   * Obtiene los roles del usuario autenticado
   */
  private List<String> obtenerRoles(Authentication auth) {
    return auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
  }

  /**
   * Determina qué función de base de datos llamar según el rol del usuario
   */
  private String determinarFunctionCall(List<String> roles) {
    if (roles.contains(RolEnum.AUDITOR_JEFE_INTERNO.getCode().toString())
        || roles.contains(RolEnum.AUDITOR_OPERATIVO.getCode().toString())) {
      return "{? = call ROOTSISREC.FN_INFORMES_FILTROS(?,?,?,?,?)}";
    } else if (roles.contains(RolEnum.DIRECTOR_DEPARTAMENTAL.getCode().toString())) {
      return "{? = call ROOTSISREC.FN_INFORMES_FILTROS_D(?,?,?,?,?)}";
    }
    return "";
  }

  /**
   * Ejecuta la consulta a la base de datos y procesa los resultados
   */
  private List<InformeDTO> ejecutarConsultaInformes(
      Connection conn,
      String functionCall,
      InformeFiltroDTO informeFiltroDTO,
      List<String> roles,
      String usuario,
      boolean soloUsuarioActual) throws SQLException {

    List<InformeDTO> informeDtos = new ArrayList<>();

    try (CallableStatement cs = conn.prepareCall(functionCall)) {
      configurarParametrosConsulta(cs, informeFiltroDTO, roles, usuario);
      cs.execute();

      try (ResultSet rs = (ResultSet) cs.getObject(1)) {
        informeDtos = procesarResultados(
            rs,
            roles,
            usuario,
            soloUsuarioActual,
            informeFiltroDTO != null ? informeFiltroDTO.getAinin_usuario() : null);
      } catch (Exception e) {
        logger.error("Error al procesar los resultados de la consulta: {}", e.getMessage());
      } finally {
        if (!cs.isClosed()) {
          cs.close();
        }
      }
    } finally {
      if (!conn.isClosed()) {
        conn.close();
      }
    }

    return informeDtos;
  }

  /**
   * Configura los parámetros para la consulta a la base de datos
   */
  private void configurarParametrosConsulta(
      CallableStatement cs,
      InformeFiltroDTO informeFiltroDTO,
      List<String> roles,
      String usuario) throws SQLException {

    logger.info("===== [configurarParametrosConsulta] Enviando a BD =====");
    logger.info("  Param 2 (organismo): {}", informeFiltroDTO.getAinoc_organismo_control());
    logger.info("  Param 3 (tipo_inf): {}", informeFiltroDTO.getAinti_tipo_inf());
    logger.info("  Param 4 (fecha_inicio): '{}'", informeFiltroDTO.getAinin_fecha_inicio());
    logger.info("  Param 5 (fecha_fin): '{}'", informeFiltroDTO.getAinin_fecha_fin());
    logger.info("  Roles del usuario: {}", roles);

    cs.registerOutParameter(1, Types.REF_CURSOR);
    cs.setLong(2, informeFiltroDTO.getAinoc_organismo_control());
    cs.setLong(3, informeFiltroDTO.getAinti_tipo_inf());
    cs.setString(4, informeFiltroDTO.getAinin_fecha_inicio());
    cs.setString(5, informeFiltroDTO.getAinin_fecha_fin());

    if (roles.contains(RolEnum.AUDITOR_JEFE_INTERNO.getCode().toString())
        || roles.contains(RolEnum.AUDITOR_OPERATIVO.getCode().toString())) {
      logger.info("  Param 6 (usuario filtro - auditor): '{}'", informeFiltroDTO.getAinin_usuario());
      cs.setString(6, informeFiltroDTO.getAinin_usuario());
    }
    if (roles.contains(RolEnum.RESPONSABLE.getCode().toString())) {
      logger.info("  Param 6 (usuario - responsable): '{}'", usuario);
      cs.setString(6, usuario);
    }
    if (roles.contains(RolEnum.DIRECTOR_DEPARTAMENTAL.getCode().toString())) {
      logger.info("  Param 6 (departamento - director): '{}'", informeFiltroDTO.getDepartamento());
      cs.setString(6, informeFiltroDTO.getDepartamento());
    }
    logger.info("================================================");
  }

  /**
   * Procesa los resultados de la consulta y los convierte a DTOs
   */
  private List<InformeDTO> procesarResultados(
      ResultSet rs,
      List<String> roles,
      String usuario,
      boolean soloUsuarioActual,
      String filtroAuditor) throws SQLException {

    List<InformeDTO> informeDtos = new ArrayList<>();
    BeanPropertyRowMapper<Informe> mapper = new BeanPropertyRowMapper<>(Informe.class);

    String filtroAuditorNormalizado = filtroAuditor != null ? filtroAuditor.trim() : "";
    boolean hayFiltroAuditor = !filtroAuditorNormalizado.isEmpty()
        && !"0".equals(filtroAuditorNormalizado);

    while (rs.next()) {
      Informe informe = mapper.mapRow(rs, rs.getRow());
      informe.setOrganismoControl(organismoServicio.obtenerOrganismo(rs.getLong("AINOC_CODIGO")));
      informe.setTipoInforme(tipoInfServicio.obtenerTipoInforme(rs.getLong("AINTI_CODIGO")));

      // Log detallado solo a nivel DEBUG para no afectar rendimiento en producción
      if (logger.isDebugEnabled()) {
        logger.debug("===== [procesarResultados] Informe código: {} =====", informe.getAinin_codigo());
        logger.debug("  Título: {}", informe.getAinin_titulo());
        logger.debug("  Memorando: {}", informe.getAinin_memorando());
        logger.debug("  Fecha aprobación: {}", informe.getAinin_fecha_aprobacion());
        logger.debug("  Fecha memo: {}", informe.getAinin_fecha_memo());
        logger.debug("  Fecha registro: {}", informe.getAinin_fecha());
        logger.debug("  Usuario informe: {}", rs.getString("AININ_USUARIO"));
        logger.debug("  Notificado: {}", informe.getAinin_notificado());
      }

      String usuarioInforme = rs.getString("AININ_USUARIO");
      boolean coincideAuditorFiltro = !hayFiltroAuditor
          || (usuarioInforme != null
              && usuarioInforme.trim().equalsIgnoreCase(filtroAuditorNormalizado));

      // Para rol AUDITOR_OPERATIVO (código "3")
      if (roles.contains("3") || roles.contains("2")) {
        // Para auditor operativo: solo sus informes cuando soloUsuarioActual = true.
        // Para auditor general: soloUsuarioActual = false, pero se respeta el filtroAuditor.
        boolean coincideUsuarioActual = !soloUsuarioActual
            || (usuarioInforme != null && usuarioInforme.equals(usuario));

        if (coincideUsuarioActual && coincideAuditorFiltro) {
          InformeDTO informeDTO = crearInformeDTO(informe, rs);
          informeDtos.add(informeDTO);
        }
      }

      // Para AUDITOR_JEFE_INTERNO (código "2") o DIRECTOR_DEPARTAMENTAL
      if (roles.contains(RolEnum.DIRECTOR_DEPARTAMENTAL.getCode().toString())) {
        if (coincideAuditorFiltro) {
          InformeDTO informeDTO = crearInformeDTO(informe, rs);
          informeDtos.add(informeDTO);
        }
      }
    }

    return informeDtos;
  }

  /**
   * Crea un objeto InformeDTO a partir de un objeto Informe y un ResultSet
   */
  private InformeDTO crearInformeDTO(Informe informe, ResultSet rs) throws SQLException {
    InformeDTO informeDTO = informeMapper.convertirModelDTO(informe);
    
    // Fallback: Si el valor es null (por ejemplo, si la función de BD no tiene mapeada la columna),
    // consultamos al repositorio para obtener el valor real.
    if (informeDTO.getAinin_notificado() == null) {
      try {
        informeRepositorio.findById(informe.getAinin_codigo()).ifPresent(dbInf -> {
          informeDTO.setAinin_notificado(dbInf.getAinin_notificado());
        });
      } catch (Exception e) {
        logger.error("Error al consultar ainin_notificado desde el repositorio: {}", e.getMessage());
      }
    }

    // Establece el valor de progreso fuera del método convertirModelDTO
    informeDTO.setProgreso(rs.getFloat("PORCENTAJE"));
    // Establece el total de recomendaciones, manejando valores NULL
    try {
      Integer totalRecomendaciones = rs.getInt("TOTAL_RECOMENDACIONES");
      if (rs.wasNull()) {
        totalRecomendaciones = 0;
      }
      informeDTO.setTotalRecomendaciones(totalRecomendaciones);
    } catch (SQLException e) {
      logger.error("Error al obtener el total de recomendaciones: {}", e.getMessage());
      // Si el campo no existe, establecer valor por defecto
      informeDTO.setTotalRecomendaciones(0);
    }
    return informeDTO;
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> registrar(NuevoInformeDTO nuevoInformeDTO, String auditoria) {
    try {
      // Verificar si ya existe un informe con el mismo memorando
      if (existeInformeConMemorando(nuevoInformeDTO.getAinin_memorando())) {
        return EstadoRespuestas.respuestaError(String.format("Ya existe un informe con el memorando %s", nuevoInformeDTO.getAinin_memorando()));
      }

      Long maxValue = Optional.ofNullable(informeRepositorio.maxValue()).orElse(1L);

      // Convertir Auditoria a DTO
      AuditoriaDTO auditoriaDTO = AuditoriaMapper.convertirAuditoriaJSONDTO(auditoria);

      // Obtener organismo
      OrganismoControl organismoControl = organismoServicio
          .obtenerOrganismo(nuevoInformeDTO.getAinoc_organismoControl());

      // Obtener tipo de informe
      TipoInforme tipoInforme = tipoInfServicio.obtenerTipoInforme(nuevoInformeDTO.getAinti_tipoInf());

      // OBTENER LA CONFIGURACION DEL NAS
      Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).get();

      // Validar tamaño del archivo antes de conectar al FTP
      if (!ftpService.validarTamanoArchivo(nuevoInformeDTO.getAinin_infAdjunto(), configuracion.getAseco_size_file_mb())) {
        double tamanoArchivoMB = ftpService.obtenerTamanoArchivoMB(nuevoInformeDTO.getAinin_infAdjunto());
        return EstadoRespuestas.respuestaError(
          String.format("El archivo excede el límite permitido. Tamaño del archivo: %.2f MB, Límite máximo: %d MB", 
            tamanoArchivoMB, configuracion.getAseco_size_file_mb()));
      }

      // CONECTAR AL SERVIDOR FTP
      boolean estaConectado = ftpService.conectarServidorFTP(configuracion.getAseco_host(), configuracion.getAseco_user(),
          configuracion.getAseco_contrasenia());
      if (!estaConectado) {
        return EstadoRespuestas.respuestaError("Error del servidor externo (FTP/NAS): No se pudo establecer conexión. Consulte con el administrador del sistema.");
      }

      // Metodo renombra el archivo. Ejm: Memorando-202410100101.pdf
      String nombreArchivo = FileMapper.getFileName(nuevoInformeDTO.getAinin_infAdjunto().getOriginalFilename(),
          nuevoInformeDTO.getAinin_memorando());

      // Metodo retorna la ruta relativa. Ejm : /SISREC/ORGANISMO CONTROL/2024/
      String rutaRelativa = FileMapper.obtenerRutaArchivoFtp(configuracion.getAseco_directorio(),
          organismoControl.getAinoc_descripcion(), (maxValue + 1), TITULO);

      // REGISTRAR EL ARCHIVO Y DEVUELVE SI SE REGISTRO TRUE:FALSE
      Boolean respuestaCrearArchivo = ftpService.subirArchivoAlServidorFTP(nuevoInformeDTO.getAinin_infAdjunto(),
          rutaRelativa,
          nombreArchivo);

      // VALIDA SI EL ARCHIVO HA SIDO CREADO EXITOSAMENTE
      if (!respuestaCrearArchivo) {
        ftpService.desconectarServidorFTP();
        
        return EstadoRespuestas.respuestaError("Error al crear el archivo");
      }

      // DESCONECTA DEL SERVIDOR NAS.
      ftpService.desconectarServidorFTP();

      // CREAR NUEVA RUTA PARA GUARDAR EN LA BASE DE DATOS
      String rutaArchivo = FileMapper.obtenerRutaArchivoBd(configuracion.getAseco_nombre_host(), rutaRelativa,
          nombreArchivo);

      // CONVERTIR EL INFORME DTO A MODELO
      Informe informeConvertido = informeMapper.convertirCrearDTOModel(
          nuevoInformeDTO,
          auditoriaDTO,
          organismoControl,
          rutaArchivo,
          tipoInforme);

      // GUARDA EL INFORME CONVERTIDO
      Informe respuesta = informeRepositorio.save(informeConvertido);
      // Registrar Bitacora
      bitacoraServicio.registrarBitacora(respuesta,
          auditoriaDTO,
          "CREAR");

      // Sincronizar automáticamente el informe emitido con los periodos de evaluación activos
      try {
        corteEvaluacionServicio.sincronizarPeriodosActivos();
      } catch (Exception e) {
        logger.warn("Error al sincronizar periodos de evaluación al registrar informe: {}", e.getMessage());
      }

      return EstadoRespuestas.respuestaCreacion("Informe creado exitosamente");

    } catch (Exception e) {
      logger.error("Error al crear el informe: {}", e.getMessage());
      ftpService.desconectarServidorFTP();
      return EstadoRespuestas.respuestaError("Error al crear el informe: " + e.getMessage());
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorId(Long id) {
    try {
      if (!existeInforme(id)) {
        return EstadoRespuestas.respuestaError("Informe no existe");
      }

      Informe informe = informeRepositorio.findById(id).get();

      Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).get();

      InformeDTO informeDTO = informeMapper.convertirModelDTO(informe);

      boolean estaConectado = ftpService.conectarServidorFTP(configuracion.getAseco_host(), configuracion.getAseco_user(),
          configuracion.getAseco_contrasenia());
      if (estaConectado) {
        String rutaRelativaFtp = informe.getAinin_inf_adjunto();
        if (informe.getAinin_inf_adjunto() != null && informe.getAinin_inf_adjunto().contains(configuracion.getAseco_nombre_host())) {
          rutaRelativaFtp = informe.getAinin_inf_adjunto().split(configuracion.getAseco_nombre_host())[1];
        }
        byte[] archivoAdjunto = ftpService.descargarArchivoFTPNuevo(rutaRelativaFtp);
        informeDTO.setAinin_infAdjunto(archivoAdjunto);
        ftpService.desconectarServidorFTP();
      } else {
        logger.warn("No se pudo conectar al servidor FTP/NAS para obtener el adjunto del informe {}. Se retornarán solo los datos.", id);
        informeDTO.setAinin_infAdjunto(null);
        informeDTO.setAdvertencia("Servidor de archivos (FTP/NAS) no disponible (" + configuracion.getAseco_host() + "). No se pudo cargar el archivo adjunto del informe.");
      }
      return EstadoRespuestas.respuestaObtencion("Informe obtenido con exito", informeDTO);
    } catch (Exception e) {
      ftpService.desconectarServidorFTP();
      logger.error("Error al obtener el informe: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener el informe");
    }
  }

  /**
   * Actualiza un informe existente.
   *
   * @param actualizarInformeDTO Datos del informe a actualizar.
   * @param auditoria            Información de auditoría.
   * @param id                   Identificador del informe.
   * @return ResponseEntity indicando el resultado de la operación.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizar(ActualizarInformeDTO actualizarInformeDTO,
      String auditoria, Long id) {
    try {
      // Validar si existe
      if (!existeInforme(id)) {
        return EstadoRespuestas.respuestaError("El informe no existe");
      }
      
      // Obtener el informe actual
      Informe informeAnterior = informeRepositorio.findById(id).get();
      
      // Verificar si ya existe otro informe con el mismo memorando (distinto al actual)
      Informe informeExistente = informeRepositorio.findByMemorando(actualizarInformeDTO.getAinin_memorando());
      if (informeExistente != null && !informeExistente.getAinin_codigo().equals(id)) {
        return EstadoRespuestas.respuestaError("Ya existe un informe con el memorando " + actualizarInformeDTO.getAinin_memorando());
      }
      
      // Convertir Auditoria a DTO
      AuditoriaDTO auditoriaDTO = AuditoriaMapper.convertirAuditoriaJSONDTO(auditoria);
      if (auditoriaDTO == null) {
        return EstadoRespuestas.respuestaError("Error al procesar la información de auditoría");
      }

      // Obtener organismo
      OrganismoControl organismoControl = organismoServicio
          .obtenerOrganismo(actualizarInformeDTO.getAinoc_organismoControl());

      // Obtener tipo de informe
      TipoInforme tipoInforme = tipoInfServicio.obtenerTipoInforme(actualizarInformeDTO.getAinti_tipoInf());
      String rutaArchivoBD = informeAnterior.getAinin_inf_adjunto();

      if (actualizarInformeDTO.getAinin_infAdjunto() != null && !actualizarInformeDTO.getAinin_infAdjunto().isEmpty()) {
        // Obtiene la configuración del NAS
        Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).orElse(null);
        if (configuracion == null) {
          return EstadoRespuestas.respuestaError("No se encontró la configuración FTP");
        }

        // Validar tamaño del archivo antes de conectar al FTP
        if (!ftpService.validarTamanoArchivo(actualizarInformeDTO.getAinin_infAdjunto(), configuracion.getAseco_size_file_mb())) {
          double tamanoArchivoMB = ftpService.obtenerTamanoArchivoMB(actualizarInformeDTO.getAinin_infAdjunto());
          return EstadoRespuestas.respuestaError(
            String.format("El archivo excede el límite permitido. Tamaño del archivo: %.2f MB, Límite máximo: %d MB", 
              tamanoArchivoMB, configuracion.getAseco_size_file_mb()));
        }

        // CONECTA EL SERVIDOR FTP
        boolean conectado = ftpService.conectarServidorFTP(configuracion.getAseco_host(), configuracion.getAseco_user(),
            configuracion.getAseco_contrasenia());
        if (!conectado) {
          return EstadoRespuestas.respuestaError("Error del servidor externo (FTP/NAS): No se pudo establecer conexión. Consulte con el administrador del sistema.");
        }

        // Obtener la ruta del archivo anterior desde la base de datos
        String rutaRelativaFtp = informeAnterior.getAinin_inf_adjunto();

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
            return EstadoRespuestas.respuestaError("Error al eliminar el archivo del NAS");
          }
        }

        // Metodo renombra el archivo. Ejm: Memorando-202410100101.pdf
        String nombreArchivo = FileMapper.getFileName(actualizarInformeDTO.getAinin_infAdjunto().getOriginalFilename(),
            actualizarInformeDTO.getAinin_memorando());

        String rutaRelativa = FileMapper.obtenerRutaArchivoFtp(configuracion.getAseco_directorio(),
            organismoControl.getAinoc_descripcion(), id, TITULO);

        // Registra el archivo en en NAS en ese ruta.
        Boolean respuestaCrearArchivo = ftpService.subirArchivoAlServidorFTP(actualizarInformeDTO.getAinin_infAdjunto(),
            rutaRelativa,
            nombreArchivo);

        // VALIDA SI SI REGISTRO EL ARCHIVO
        if (!respuestaCrearArchivo) {
          ftpService.desconectarServidorFTP();
          return EstadoRespuestas.respuestaError("Error al crear el archivo");
        }
        ftpService.desconectarServidorFTP();

        // CREAR NUEVA RUTA PARA GUARDAR EN LA BASE DE DATOS
        rutaArchivoBD = FileMapper.obtenerRutaArchivoBd(configuracion.getAseco_nombre_host(), rutaRelativa,
            nombreArchivo);
      }
      // CONVIERTE EL INFORMEDTO A INFORMEMODEL.
      Informe informe = informeMapper.convertirActualizarDTOModel(
          actualizarInformeDTO,
          auditoriaDTO,
          id,
          informeAnterior,
          organismoControl,
          rutaArchivoBD,
          tipoInforme);
      // Actualiza informe
      Informe resultado = informeRepositorio.save(informe);
      // Registrar en bitacora
      bitacoraServicio.registrarBitacora(resultado, auditoriaDTO, "ACTUALIZAR");

      // Sincronizar automáticamente el informe actualizado con los periodos de evaluación activos
      try {
        corteEvaluacionServicio.sincronizarPeriodosActivos();
      } catch (Exception e) {
        logger.warn("Error al sincronizar periodos de evaluación al actualizar informe: {}", e.getMessage());
      }

      return EstadoRespuestas.respuestaCreacion("Informe actualizado correctamente");
    } catch (Exception e) {
      ftpService.desconectarServidorFTP();
      logger.error("Error al actualizar el informe: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar el informe: " + e);
    }
  }

  /**
   * Verifica si un informe existe en la base de datos.
   *
   * @param id Identificador del informe.
   * @return true si el informe existe, false en caso contrario.
   */
  private Boolean existeInforme(Long id) {
    return informeRepositorio.existsById(id);
  }

  /**
   * Verifica si existe un informe con el memorando especificado.
   *
   * @param memorando Número de memorando a verificar.
   * @return true si existe un informe con ese memorando, false en caso contrario.
   */
  private Boolean existeInformeConMemorando(String memorando) {
    return informeRepositorio.findByMemorando(memorando) != null;
  }

  /**
   * Realiza una eliminación lógica de un informe.
   *
   * @param auditoria Datos de auditoría.
   * @param id        Identificador del informe.
   * @return ResponseEntity indicando el resultado de la operación.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogica(EliminarDTO auditoria, Long id) {
    try {
      if (!existeInforme(id)) {
        return EstadoRespuestas.respuestaError("El informe no existe");
      }

      Informe informe = informeRepositorio.findById(id).get();
      
      // Verificar si existen hallazgos activos para este informe
      List<Hallazgo> hallazgos = hallazgoRepositorio.findByInforme(informe);
      boolean tieneHallazgosActivos = hallazgos.stream()
          .anyMatch(hallazgo -> hallazgo.getAreha_estado() == '1');
      
      if (tieneHallazgosActivos) {
        return EstadoRespuestas.respuestaError("No se puede eliminar el informe porque tiene hallazgos registrados");
      }
      
      informe.setAinin_estado('0');
      informeRepositorio.save(informe);
      bitacoraServicio.registrarBitacora(informe, auditoria.getAuditoria(), "ELIMINACION LOGICA");

      // Eliminar de los periodos de evaluación automáticos
      try {
        jdbcTemplate.update("DELETE FROM AI_CORTE_EVALUACION_INFORME WHERE ainin_codigo = ? AND arcei_es_manual = 0", id);
      } catch (Exception e) {
        logger.warn("Error al desvincular informe eliminado de periodos de evaluación: {}", e.getMessage());
      }

      return EstadoRespuestas.respuestaEliminacion("Informe eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar el informe: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar el informe");
    }

  }

  /**
   * Realiza una eliminación física de un informe.
   *
   * @param auditoria Datos de auditoría.
   * @param id        Identificador del informe.
   * @return ResponseEntity indicando el resultado de la operación.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(EliminarDTO auditoria, Long id) {
    try {
      if (!existeInforme(id)) {
        return EstadoRespuestas.respuestaError("El informe no existe");
      }
      Informe informe = informeRepositorio.findById(id).get();
      informeRepositorio.deleteById(id);
      bitacoraServicio.registrarBitacora(informe, auditoria.getAuditoria(), "ELIMINACIÓN FISICA");
      return EstadoRespuestas.respuestaEliminacion("Informe eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar el informe: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar el informe");
    }
  }

  /**
   * Actualiza los usuarios asignados a informes específicos.
   *
   * @param actualizarUsuarioDTO Datos de los usuarios e informes a actualizar.
   * @return ResponseEntity indicando el resultado de la operación.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarUsuarios(ActualizarUsuarioDTO actualizarUsuarioDTO) {
    try {
      // Validar codigo informes:
      boolean todosExisten = true;
      for (Long id : actualizarUsuarioDTO.getAinin_codigos()) {
        if (!informeRepositorio.existsById(id)) {
          todosExisten = false;
          break;
        }
      }
      if (!todosExisten) {
        return EstadoRespuestas.noEncontrado("Algun ID no existe");
      }

      if (rolUsuarioServicio.existeUsuarioByCuenta(actualizarUsuarioDTO.getAseru_cuenta())) {
        return EstadoRespuestas.noEncontrado("Usuario no asignado a ningun rol");
      }

      List<Informe> informes = informeRepositorio.findAllById(actualizarUsuarioDTO.getAinin_codigos());

      informes.stream()
          .filter(informe -> informe.getAinin_estado().equals('1'))
          .forEach(informe -> informe.setAinin_usuario(actualizarUsuarioDTO.getAseru_cuenta()));

      informeRepositorio.saveAll(informes);
      bitacoraServicio.registrarBitacora(informes, actualizarUsuarioDTO.getAuditoria(), "ACTUALIZAR USUARIOS");
      return EstadoRespuestas.respuestaCreacion("Actualización de usuarios - informes exitoso");
    } catch (Exception e) {
      logger.error("Error al actualizar usuarios - informes: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar usuarios - informes :" + e);
    }
  }

  /**
   * Obtiene un informe por su código.
   *
   * @param ainin_codigo Código del informe.
   * @return Informe encontrado o null si no existe.
   */
  @Transactional
  public Informe obtenerInforme(Long ainin_codigo) {
    try {
      Informe informe = informeRepositorio.findById(ainin_codigo).get();
      return informe == null ? null : informe;
    } catch (Exception e) {
      logger.error("Error al obtener el informe: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Obtiene un informe basado en una recomendación.
   *
   * @param arere_codigo Código de la recomendación.
   * @return Informe asociado a la recomendación o null si no existe.
   */
  @Transactional
  public Informe obtenerInformeByRecomendacion(Long arere_codigo) {
    try {
      Informe informe = informeRepositorio.findByRecomendacion(arere_codigo);
      return informe;
    } catch (Exception e) {
      logger.error("Error al obtener el informe por recomendacion: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Obtiene un informe basado en una conclusión.
   *
   * @param areco_codigo Código de la conclusión.
   * @return Informe asociado a la conclusión o null si no existe.
   */
  @Transactional
  public Informe obtenerInformeByConclusion(Long areco_codigo) {
    try {
      Informe informe = informeRepositorio.findByConclusion(areco_codigo);
      return informe;
    } catch (Exception e) {
      logger.error("Error al obtener el informe por conclusión: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Obtiene un informe basado en un hallazgo.
   *
   * @param areha_codigo Código del hallazgo.
   * @return Informe asociado al hallazgo o null si no existe.
   */
  @Transactional
  public Informe obtenerInformeByHallazgo(Long areha_codigo) {
    try {
      Informe informe = informeRepositorio.findByHallazgo(areha_codigo);
      return informe;
    } catch (Exception e) {
      logger.error("Error al obtener el informe por hallazgo: {}", e.getMessage());
      return null;
    }
  }
}

