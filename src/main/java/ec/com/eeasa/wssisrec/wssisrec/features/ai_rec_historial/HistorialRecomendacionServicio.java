package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_historial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_historial.models.HistorialRecomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.RecomendacionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.SolicitudesRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.models.Solicitud;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@Service
public class HistorialRecomendacionServicio {

  private static final Logger logger = LoggerFactory.getLogger(HistorialRecomendacionServicio.class);

  @Autowired
  private HistorialRecomendacionRepositorio historialRepositorio;

  @Autowired
  private RecomendacionRepositorio recomendacionRepositorio;

  @Autowired
  private RolUsuarioRepositorio rolUsuarioRepositorio;

  @Autowired
  private SolicitudesRepositorio solicitudesRepositorio;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Transactional
  public void registrarEvento(Long arereCodigo, String accion, String detalle, 
                             String respAnt, String respNue, 
                             Date fechaMaxAnt, Date fechaMaxNue, 
                             String auditor, String observacion, String usuario) {
    try {
      Recomendacion rec = recomendacionRepositorio.findById(arereCodigo).orElse(null);
      if (rec == null) {
        logger.error("No se pudo registrar evento de historial: recomendacion {} no encontrada", arereCodigo);
        return;
      }

      // Resolver auditor asignado si no se proporciona
      if (auditor == null) {
        auditor = obtenerNombreAuditor(arereCodigo);
      }

      HistorialRecomendacion historial = new HistorialRecomendacion();
      historial.setRecomendacion(rec);
      historial.setArehi_fecha(new Date());
      historial.setArehi_accion(accion);
      historial.setArehi_detalle(detalle);
      historial.setArehi_responsable_ant(respAnt);
      historial.setArehi_responsable_nue(respNue);
      historial.setArehi_fecha_max_ant(fechaMaxAnt);
      historial.setArehi_fecha_max_nue(fechaMaxNue);
      historial.setArehi_auditor(auditor);
      historial.setArehi_observacion(observacion);
      historial.setArehi_usuario(usuario != null ? usuario : "SYSTEM");

      historialRepositorio.save(historial);
      logger.info("Evento de historial registrado exitosamente para la recomendación {}: {}", arereCodigo, accion);
    } catch (Exception e) {
      logger.error("Error al registrar evento en el historial: {}", e.getMessage(), e);
    }
  }

  @Transactional(readOnly = true)
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerHistorialPorRecomendacion(Long arereCodigo) {
    try {
      List<HistorialRecomendacion> historial = historialRepositorio.findByRecomendacionCodigo(arereCodigo);
      List<Solicitud> solicitudes = solicitudesRepositorio.findByRecomendacion(arereCodigo);

      LinkedHashMap<String, Object> data = new LinkedHashMap<>();
      data.put("historial", historial);
      data.put("solicitudes", solicitudes);

      return EstadoRespuestas.respuestaObtencion("Historial obtenido exitosamente", data);
    } catch (Exception e) {
      logger.error("Error al obtener historial para la recomendación {}: {}", arereCodigo, e.getMessage(), e);
      return EstadoRespuestas.respuestaError("Error al obtener el historial de la recomendación");
    }
  }

  @Transactional(readOnly = true)
  public String obtenerNombreCompletoPorUsuario(String usuario) {
    if (usuario == null || usuario.trim().isEmpty() || "SYSTEM".equalsIgnoreCase(usuario) || "SISTEMA".equalsIgnoreCase(usuario) || "AUDITOR".equalsIgnoreCase(usuario) || "RESPONSABLE".equalsIgnoreCase(usuario)) {
      return usuario;
    }
    try {
      String sql = "SELECT MAX(RU.ASERU_NOMBRE || ' ' || RU.ASERU_APELLIDO) " +
                   "FROM ROOTSISREC.AI_SEG_ROL_USU RU " +
                   "WHERE UPPER(TRIM(RU.ASERU_CUENTA)) = UPPER(TRIM(?))";
      String nombre = jdbcTemplate.queryForObject(sql, String.class, usuario.trim());
      if (nombre != null && !nombre.trim().isEmpty()) {
        return nombre.trim();
      }
    } catch (Exception e) {
      logger.error("Error al obtener nombre completo para usuario {}: {}", usuario, e.getMessage());
    }
    return usuario;
  }


  /**
   * Obtiene el nombre completo del auditor responsable a partir de la recomendación.
   */
  public String obtenerNombreAuditor(Long arereCodigo) {
    try {
      String sql = "SELECT INF.AININ_USUARIO FROM ROOTSISREC.AI_REC_RECOMENDACIONES REC " +
                   "JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
                   "JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
                   "JOIN ROOTSISREC.AI_INF_INFORMES INF ON HALL.AININ_CODIGO = INF.AININ_CODIGO " +
                   "WHERE REC.ARERE_CODIGO = ?";
      String auditorUsuario = jdbcTemplate.queryForObject(sql, String.class, arereCodigo);
      if (auditorUsuario != null && !auditorUsuario.trim().isEmpty()) {
        return auditorUsuario.trim();
      }
    } catch (Exception e) {
      logger.error("Error al obtener nombre de auditor para recomendación {}: {}", arereCodigo, e.getMessage());
    }
    return "No Asignado";
  }
}
