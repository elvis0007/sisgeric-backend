package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.models.EvidenciaRequerida;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.repositorios.EvidenciaRequeridaRepositorio;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.InformeServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models.Informe;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.CorteEvaluacionServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.models.OrganismoControl;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.dtos.NuevaNotificacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.NotificacionesServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.enums.TipoNotificacionEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.ConclusionServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.models.Conclusion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.DetalleServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.dtos.NuevoDetalleDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_historial.HistorialRecomendacionServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.EstadoSegRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.EstadoSegDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.enums.EstadoSegEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.models.EstadoSeg;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarFechaMaxDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarRiesgoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.AsignarResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ConclusionRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.DetallePlantillaCorreoRecordatorioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.DetalleRecomendacionesPorInformeDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.EliminarResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.InformeFiltroDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.InformeRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.InformeRecomendacionesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.NuevaRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.RecomendacionCambioFechaDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.RecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.RecomendacionFiltroDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.RecomendacionFiltroRDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.UsuarioRecordatorioNotificacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ValidarRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.mapper.RecomendacionMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.ResponsableServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.NuevoResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.enums.RolEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioServicio;
import ec.com.eeasa.wssisrec.wssisrec.services.EmailService;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.UtilsFunctions;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.ResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models.Responsable;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.ResponsableRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.VerificableServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.VerificableRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.models.Verificable;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.DetalleRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.enums.RolEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@Service
public class RecomendacionServicio {

  private static final Logger logger = LoggerFactory.getLogger(RecomendacionServicio.class);
  @Autowired
  private BitacoraServicio bitacoraServicio;

  @Autowired
  private RecomendacionRepositorio recomendacionRepositorio;

  @Autowired
  private RecomendacionMapper recomendacionMapper;

  @Autowired
  private EstadoSegRepositorio estadoSegRepositorio;

  @Autowired
  @Lazy
  private ConclusionServicio conclusionServicio;

  @Autowired
  private ResponsableServicio responsableServicio;

  @Autowired
  private DetalleServicio detalleServicio;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private InformeServicio informeServicio;

  @Autowired
  @Lazy
  private CorteEvaluacionServicio corteEvaluacionServicio;

  @Autowired
  private VerificableServicio verificableServicio;

  @Autowired
  private ResponsableRepositorio responsableRepositorio;

  @Autowired
  private VerificableRepositorio verificableRepositorio;

  @Autowired
  private DetalleRepositorio detalleRepositorio;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private HistorialRecomendacionServicio historialServicio;

  @Autowired
  private EvidenciaRequeridaServicio evidenciaRequeridaServicio;

  @Autowired
  private ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.SolicitudesRepositorio solicitudesRepositorio;

  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  @Autowired
  private NotificacionesServicio notificacionesServicio;

  @Autowired
  private RolUsuarioRepositorio rolUsuarioRepositorio;

  @Autowired
  private RolUsuarioServicio rolUsuarioServicio;

  @Autowired
  @Lazy
  private EmailService emailService;

  @Autowired
  private EvidenciaRequeridaRepositorio evidenciaRequeridaRepositorio;

  private final Map<Long, EstadoSeg> estadoSegCache = new ConcurrentHashMap<>();

  private EstadoSeg getEstadoSegCached(Long codigo) {
    return estadoSegCache.computeIfAbsent(codigo, c ->
      estadoSegRepositorio.findById(c).orElse(null)
    );
  }

  private Map<Long, Informe> batchObtenerInformesPorRecomendacion(List<Long> recCodigos) {
    if (recCodigos == null || recCodigos.isEmpty()) return new HashMap<>();
    String sql = "SELECT I.*, OC.AINOC_DESCRIPCION AS ORG_DESC, R.ARERE_CODIGO AS REC_COD " +
      "FROM AI_INF_INFORMES I " +
      "LEFT JOIN AI_INF_ORG_CONT OC ON I.AINOC_CODIGO = OC.AINOC_CODIGO " +
      "JOIN AI_REC_HALLAZGOS H ON I.AININ_CODIGO = H.AININ_CODIGO " +
      "JOIN AI_REC_CONCLUSIONES C ON H.AREHA_CODIGO = C.AREHA_CODIGO " +
      "JOIN AI_REC_RECOMENDACIONES R ON C.ARECO_CODIGO = R.ARECO_CODIGO " +
      "WHERE R.ARERE_CODIGO IN (" +
      recCodigos.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
    try {
      return jdbcTemplate.query(sql, rs -> {
        Map<Long, Informe> result = new HashMap<>();
        while (rs.next()) {
          Informe inf = new Informe();
          inf.setAinin_codigo(rs.getLong("AININ_CODIGO"));
          inf.setAinin_orden_trabajo(rs.getString("AININ_ORDEN_TRABAJO"));
          inf.setAinin_titulo(rs.getString("AININ_TITULO"));
          inf.setAinin_memorando(rs.getString("AININ_MEMORANDO"));
          inf.setAinin_memo_aprobacion(rs.getString("AININ_MEMO_APROBACION"));
          inf.setAinin_usuario(rs.getString("AININ_USUARIO"));
          inf.setAinin_fecha_aprobacion(rs.getTimestamp("AININ_FECHA_APROBACION"));
          inf.setAinin_fecha(rs.getTimestamp("AININ_FECHA"));
          String orgDesc = rs.getString("ORG_DESC");
          if (orgDesc != null) {
            OrganismoControl oc = new OrganismoControl();
            oc.setAinoc_descripcion(orgDesc);
            inf.setOrganismoControl(oc);
          }
          Long recCod = rs.getLong("REC_COD");
          result.put(recCod, inf);
        }
        return result;
      });
    } catch (Exception e) {
      logger.error("Error en batchObtenerInformes: {}", e.getMessage());
      return new HashMap<>();
    }
  }

  private Map<Long, Integer> batchContarResponsablesActivos(List<Long> recCodigos) {
    if (recCodigos == null || recCodigos.isEmpty()) return new HashMap<>();
    String sql = "SELECT DR.ARERE_CODIGO, COUNT(DISTINCT DR.ARERS_CODIGO) AS CANT " +
      "FROM AI_REC_DETALLE_RECOMENDACION DR " +
      "WHERE DR.ARERE_CODIGO IN (" +
      recCodigos.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") " +
      "AND DR.AREDR_ESTADO = 1 GROUP BY DR.ARERE_CODIGO";
    try {
      return jdbcTemplate.query(sql, rs -> {
        Map<Long, Integer> result = new HashMap<>();
        while (rs.next()) {
          result.put(rs.getLong("ARERE_CODIGO"), rs.getInt("CANT"));
        }
        return result;
      });
    } catch (Exception e) {
      logger.error("Error en batchContarResponsables: {}", e.getMessage());
      return new HashMap<>();
    }
  }

  private Map<Long, Conclusion> batchObtenerConclusionesPorRecomendacion(List<Long> recCodigos) {
    if (recCodigos == null || recCodigos.isEmpty()) return new HashMap<>();
    String sql = "SELECT C.*, R.ARERE_CODIGO AS REC_COD " +
      "FROM AI_REC_CONCLUSIONES C, AI_REC_RECOMENDACIONES R " +
      "WHERE C.ARECO_CODIGO = R.ARECO_CODIGO AND R.ARERE_CODIGO IN (" +
      recCodigos.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
    try {
      return jdbcTemplate.query(sql, rs -> {
        Map<Long, Conclusion> result = new HashMap<>();
        while (rs.next()) {
          Conclusion c = new Conclusion();
          c.setAreco_codigo(rs.getLong("ARECO_CODIGO"));
          c.setAreco_conclusion(rs.getString("ARECO_CONCLUSION"));
          result.put(rs.getLong("REC_COD"), c);
        }
        return result;
      });
    } catch (Exception e) {
      logger.error("Error en batchObtenerConclusiones: {}", e.getMessage());
      return new HashMap<>();
    }
  }

  private Map<Long, Recomendacion> batchObtenerRecomendaciones(List<Long> recCodigos) {
    if (recCodigos == null || recCodigos.isEmpty()) return new HashMap<>();
    String sql = "SELECT * FROM AI_REC_RECOMENDACIONES WHERE ARERE_CODIGO IN (" +
      recCodigos.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
    try {
      Map<Long, Recomendacion> map = jdbcTemplate.query(sql, rs -> {
        Map<Long, Recomendacion> result = new HashMap<>();
        while (rs.next()) {
          Recomendacion r = new Recomendacion();
          r.setArere_codigo(rs.getLong("ARERE_CODIGO"));
          r.setArere_descripcion(rs.getString("ARERE_DESCRIPCION"));
          r.setArere_num_rec(rs.getInt("ARERE_NUM_REC"));
          r.setArere_fecha_maxima(rs.getTimestamp("ARERE_FECHA_MAXIMA"));
          r.setArere_fecha_inicio_control(rs.getTimestamp("ARERE_FECHA_INICIO_CONTROL"));
          r.setArere_fecha_cumplida(rs.getTimestamp("ARERE_FECHA_CUMPLIDA"));
          r.setArere_estado(rs.getString("ARERE_ESTADO") != null ? rs.getString("ARERE_ESTADO").charAt(0) : null);
          r.setArere_evidencia_requerida(rs.getString("ARERE_EVIDENCIA_REQUERIDA"));
          r.setArere_riesgo_verificado(rs.getString("ARERE_RIESGO_VERIFICADO"));
          String estadoSegStr = rs.getString("AREES_CODIGO");
          if (estadoSegStr != null) {
            r.setEstadoSeg(getEstadoSegCached(rs.getLong("AREES_CODIGO")));
          }
          result.put(r.getArere_codigo(), r);
        }
        return result;
      });

      if (map != null && !map.isEmpty()) {
        List<Long> sinEvidencia = map.values().stream()
            .filter(r -> r.getArere_evidencia_requerida() == null || r.getArere_evidencia_requerida().trim().isEmpty())
            .map(Recomendacion::getArere_codigo)
            .collect(Collectors.toList());

        if (!sinEvidencia.isEmpty()) {
          String inSinEvid = sinEvidencia.stream().map(String::valueOf).collect(Collectors.joining(","));
          String sqlEvid = "SELECT ARERE_CODIGO, ARER_DESCRIPCION FROM ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS " +
              "WHERE ARERE_CODIGO IN (" + inSinEvid + ") AND ARER_ESTADO = 'A' ORDER BY ARERE_CODIGO, ARER_CODIGO ASC";
          try {
            jdbcTemplate.query(sqlEvid, rs -> {
              Map<Long, List<String>> agrupar = new HashMap<>();
              while (rs.next()) {
                agrupar.computeIfAbsent(rs.getLong("ARERE_CODIGO"), k -> new ArrayList<>())
                       .add(rs.getString("ARER_DESCRIPCION"));
              }
              for (Map.Entry<Long, List<String>> entry : agrupar.entrySet()) {
                Recomendacion r = map.get(entry.getKey());
                if (r != null && (r.getArere_evidencia_requerida() == null || r.getArere_evidencia_requerida().trim().isEmpty())) {
                  List<String> items = entry.getValue();
                  StringBuilder sb = new StringBuilder();
                  for (int i = 0; i < items.size(); i++) {
                    if (i > 0) sb.append("\n");
                    sb.append(i + 1).append(". ").append(items.get(i).trim());
                  }
                  r.setArere_evidencia_requerida(sb.toString());
                }
              }
              return null;
            });
          } catch (Exception e) {
            logger.warn("No se pudo enriquecer evidencias requeridas de AI_REC_EVIDENCIAS_REQUERIDAS: {}", e.getMessage());
          }
        }
      }

      return map != null ? map : new HashMap<>();
    } catch (Exception e) {
      logger.error("Error en batchObtenerRecomendaciones: {}", e.getMessage());
      return new HashMap<>();
    }
  }

  private Map<Long, String> batchObtenerResponsablesStr(List<Long> recCodigos) {
    if (recCodigos == null || recCodigos.isEmpty()) return new HashMap<>();
    String inClause = recCodigos.stream().map(String::valueOf).collect(Collectors.joining(","));
    String sql = "SELECT DR.ARERE_CODIGO, " +
      "LISTAGG(RES.ARERS_NOMBRE || ' ' || RES.ARERS_APELLIDO || ' (' || COALESCE(EV.AREEV_DESCRIPCION, 'VERIFICABLE NO CREADO') || ')', ', ') " +
      "WITHIN GROUP (ORDER BY RES.ARERS_NOMBRE, RES.ARERS_APELLIDO) AS RESPONSABLES " +
      "FROM AI_REC_RESPONSABLES RES " +
      "JOIN AI_REC_DETALLE_RECOMENDACION DR ON RES.ARERS_CODIGO = DR.ARERS_CODIGO " +
      "JOIN AI_REC_RECOMENDACIONES REC ON DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
      "LEFT JOIN (" +
      "  SELECT AREDR_CODIGO, AREEV_CODIGO, ROW_NUMBER() OVER (" +
      "    PARTITION BY AREDR_CODIGO " +
      "    ORDER BY " +
      "      CASE " +
      "        WHEN AREEV_CODIGO = 2 THEN 1 " +
      "        WHEN AREEV_CODIGO = 1 THEN 2 " +
      "        WHEN AREEV_CODIGO = 3 THEN 3 " +
      "        ELSE 4 " +
      "      END ASC, " +
      "      NVL(AREVE_FECHA_CARGA, AREVE_FECHA) DESC NULLS LAST, " +
      "      AREVE_CODIGO DESC" +
      "  ) AS rn " +
      "  FROM AI_REC_VERIFICABLES " +
      "  WHERE TO_CHAR(AREVE_ESTADO) = '1'" +
      ") ULT ON DR.AREDR_CODIGO = ULT.AREDR_CODIGO AND ULT.rn = 1 " +
      "LEFT JOIN AI_REC_ESTADO_VERIFICABLE EV ON ULT.AREEV_CODIGO = EV.AREEV_CODIGO " +
      "WHERE RES.ARERS_ESTADO = 1 AND DR.AREDR_ESTADO = 1 AND REC.ARERE_ESTADO = 1 " +
      "AND REC.ARERE_CODIGO IN (" + inClause + ") " +
      "GROUP BY DR.ARERE_CODIGO";
    try {
      return jdbcTemplate.query(sql, rs -> {
        Map<Long, String> result = new HashMap<>();
        while (rs.next()) {
          result.put(rs.getLong("ARERE_CODIGO"), rs.getString("RESPONSABLES"));
        }
        return result;
      });
    } catch (Exception e) {
      logger.error("Error en batchObtenerResponsablesStr: {}", e.getMessage());
      return new HashMap<>();
    }
  }

  private static class SolicitudRespInfo {
    String estadoAprob;
    Date fechaMaxNueva;
  }

  private Map<Long, SolicitudRespInfo> batchObtenerSolicitudesPorRecomendacion(List<Long> recCodigos, String codigoPer) {
    if (recCodigos == null || recCodigos.isEmpty()) return new HashMap<>();
    String ids = recCodigos.stream().map(String::valueOf).collect(Collectors.joining(","));
    String sql = "SELECT S.ARERE_CODIGO, S.ARESO_ESTADO_APROB, S.ARESO_FECHA_MAX_NUE FROM AI_REC_SOLICITUDES S " +
      "JOIN AI_REC_RESPONSABLES RS ON RS.ARERS_CODIGO = S.ARERS_CODIGO " +
      "WHERE S.ARERE_CODIGO IN (" + ids + ") " +
      "AND S.ARESO_ESTADO = '1' " +
      "AND RS.ARERS_CODIGO_PER = ? " +
      "ORDER BY S.ARESO_CODIGO DESC";
    try {
      return jdbcTemplate.query(sql, ps -> ps.setString(1, codigoPer), rs -> {
        Map<Long, SolicitudRespInfo> result = new HashMap<>();
        while (rs.next()) {
          long recCod = rs.getLong("ARERE_CODIGO");
          String estadoAprob = rs.getString("ARESO_ESTADO_APROB");
          Date fechaNue = rs.getDate("ARESO_FECHA_MAX_NUE");
          if (estadoAprob != null) {
            estadoAprob = estadoAprob.trim();
            SolicitudRespInfo existente = result.get(recCod);
            if (existente == null) {
              existente = new SolicitudRespInfo();
              result.put(recCod, existente);
            }
            // Priorizar estado de solicitud activa en trámite ('1' o '2') para alertas visuales
            if (existente.estadoAprob == null || "1".equals(estadoAprob) || "2".equals(estadoAprob)) {
              existente.estadoAprob = estadoAprob;
            }
            // Solo solicitudes formalmente APROBADAS por auditoría ('3') modifican la fecha máxima de cumplimiento
            boolean esAprobada = "3".equals(estadoAprob);
            if (esAprobada && fechaNue != null) {
              if (existente.fechaMaxNueva == null || fechaNue.after(existente.fechaMaxNueva)) {
                existente.fechaMaxNueva = fechaNue;
              }
            }
          }
        }
        return result;
      });
    } catch (Exception e) {
      logger.error("Error en batchObtenerSolicitudesPorRecomendacion: {}", e.getMessage());
      return new HashMap<>();
    }
  }

  private Map<Long, String> batchObtenerEstadoSolicitudPorRecomendacion(List<Long> recCodigos, String codigoPer) {
    Map<Long, SolicitudRespInfo> map = batchObtenerSolicitudesPorRecomendacion(recCodigos, codigoPer);
    Map<Long, String> result = new HashMap<>();
    for (Map.Entry<Long, SolicitudRespInfo> entry : map.entrySet()) {
      if (entry.getValue() != null && entry.getValue().estadoAprob != null) {
        result.put(entry.getKey(), entry.getValue().estadoAprob);
      }
    }
    return result;
  }

  private Map<Long, String> batchObtenerEstadoVerificablePorRecomendacion(List<Long> recCodigos) {
    if (recCodigos == null || recCodigos.isEmpty()) return new HashMap<>();
    String ids = recCodigos.stream().map(String::valueOf).collect(Collectors.joining(","));
    String sql = "SELECT DR.AREDR_CODIGO, " +
      "(SELECT EV.AREEV_DESCRIPCION FROM (" +
      "  SELECT AREDR_CODIGO, AREEV_CODIGO FROM (" +
      "    SELECT AREDR_CODIGO, AREEV_CODIGO, ROW_NUMBER() OVER (" +
      "      PARTITION BY AREDR_CODIGO " +
      "      ORDER BY " +
      "        CASE " +
      "          WHEN AREEV_CODIGO = 2 THEN 1 " +
      "          WHEN AREEV_CODIGO = 1 THEN 2 " +
      "          WHEN AREEV_CODIGO = 3 THEN 3 " +
      "          ELSE 4 " +
      "        END ASC, " +
      "        NVL(AREVE_FECHA_CARGA, AREVE_FECHA) DESC NULLS LAST, " +
      "        AREVE_CODIGO DESC" +
      "    ) AS rn " +
      "    FROM AI_REC_VERIFICABLES WHERE TO_CHAR(AREVE_ESTADO) = '1' " +
      "  ) WHERE rn = 1 " +
      ") V JOIN AI_REC_ESTADO_VERIFICABLE EV ON V.AREEV_CODIGO = EV.AREEV_CODIGO " +
      "WHERE V.AREDR_CODIGO = DR.AREDR_CODIGO AND EV.AREEV_ESTADO = 1) AS ESTADO " +
      "FROM AI_REC_DETALLE_RECOMENDACION DR " +
      "WHERE DR.ARERE_CODIGO IN (" + ids + ") AND DR.AREDR_ESTADO = 1";
    try {
      return jdbcTemplate.query(sql, rs -> {
        Map<Long, String> result = new HashMap<>();
        while (rs.next()) {
          long aredrCodigo = rs.getLong("AREDR_CODIGO");
          String estado = rs.getString("ESTADO");
          result.put(aredrCodigo, estado != null ? estado : "VERIFICABLE NO CREADO");
        }
        return result;
      });
    } catch (Exception e) {
      logger.error("Error en batchObtenerEstadoVerificablePorRecomendacion: {}", e.getMessage());
      return new HashMap<>();
    }
  }

  @Transactional
  public RecomendacionDTO obtenerPorId(Long arere_codigo) {
    try {
      Recomendacion recomendacion = recomendacionRepositorio.findById(arere_codigo).get();
      RecomendacionDTO dto = recomendacionMapper.convertirModelDTO(recomendacion);
      if (dto != null) {
        batchEnriquecerRecomendacionesConResponsables(java.util.Collections.singletonList(dto));
      }
      return dto;
    } catch (Exception e) {
      logger.error("Error al obtener la recomendaci?n por id: {}", e.getMessage());
      return null;
    }
  }

  @Transactional
  public Recomendacion obtenerPorRecomendacion(Long arere_codigo) {
    try {
      Recomendacion recomendacion = recomendacionRepositorio.findById(arere_codigo).get();
      return recomendacion;
    } catch (Exception e) {
      logger.error("Error al obtener la recomendaci?n por recomendaci?n: {}", e.getMessage());
      return null;
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDinamicoAuditor(RecomendacionFiltroDTO filtroDTO) {
    long t0 = System.currentTimeMillis();
    return jdbcTemplate.execute((Connection conn) -> {
      String functionCall = "{? = call ROOTSISREC.FN_RECOMENDACIONES_FILTROS(?,?,?)}";

      List<InformeRecomendacionDTO> recomendacionDTOs = new ArrayList<>();

      try (CallableStatement cs = conn.prepareCall(functionCall)) {
        cs.registerOutParameter(1, Types.REF_CURSOR);
        cs.setLong(2, filtroDTO.getArees_estado_seg());
        cs.setString(3, filtroDTO.getCurrent_year());
        cs.setString(4, filtroDTO.getArers_departamento());

        cs.execute();

        try (ResultSet rs = (ResultSet) cs.getObject(1)) {
          BeanPropertyRowMapper<Recomendacion> mapper = new BeanPropertyRowMapper<>(Recomendacion.class);
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          String usuario = auth.getName();
          List<String> roles = auth.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.toList());

          List<Recomendacion> recs = new ArrayList<>();
          List<Long> recCodigos = new ArrayList<>();
          while (rs.next()) {
            Recomendacion rec = mapper.mapRow(rs, rs.getRow());
            rec.setEstadoSeg(getEstadoSegCached(rs.getLong("AREES_CODIGO")));
            recs.add(rec);
            recCodigos.add(rec.getArere_codigo());
          }

          String auditorFilter = filtroDTO.getAuditor();
          boolean filtrarAuditor = auditorFilter != null && !auditorFilter.equals("0") && !auditorFilter.trim().isEmpty();

          CompletableFuture<Map<Long, Informe>> futureInformes = CompletableFuture.supplyAsync(() -> batchObtenerInformesPorRecomendacion(recCodigos));
          CompletableFuture<Map<Long, String>> futureRespStr = CompletableFuture.supplyAsync(() -> batchObtenerResponsablesStr(recCodigos));
          CompletableFuture<Map<Long, ResumenResponsablesRec>> futureResumen = CompletableFuture.supplyAsync(() -> batchObtenerResumenResponsablesPorRecomendacion(recCodigos));

          CompletableFuture.allOf(futureInformes, futureRespStr, futureResumen).join();

          Map<Long, Informe> informeMap = futureInformes.join();
          Map<Long, String> respStrMap = futureRespStr.join();
          Map<Long, ResumenResponsablesRec> resumenRespBatch = futureResumen.join();

          for (Recomendacion recomendacion : recs) {
            Informe informe = informeMap.get(recomendacion.getArere_codigo());
            if (informe == null) continue;

            if (filtrarAuditor && !auditorFilter.equals(informe.getAinin_usuario())) {
              continue;
            }

            boolean esAuditorJefeInterno = roles.contains(RolEnum.AUDITOR_JEFE_INTERNO.getCode().toString());
            boolean esAuditorOperativoSolo = roles.contains(RolEnum.AUDITOR_OPERATIVO.getCode().toString())
                && !esAuditorJefeInterno;

            if (esAuditorOperativoSolo && !informe.getAinin_usuario().equals(usuario)) {
              continue;
            }

            RecomendacionDTO recomendacionDTO = recomendacionMapper.convertirModelDTO(recomendacion);
            ResumenResponsablesRec resumenRec = resumenRespBatch.get(recomendacion.getArere_codigo());
            int numResp = (resumenRec != null) ? resumenRec.total : 0;
            recomendacionDTO.setResponsables(numResp);
            String respStr = respStrMap.getOrDefault(recomendacion.getArere_codigo(), "");
            String enteControl = (informe.getOrganismoControl() != null) ? informe.getOrganismoControl().getAinoc_descripcion() : "Sin Ente de Control";
            InformeRecomendacionDTO dto = new InformeRecomendacionDTO(
                informe.getAinin_codigo(), informe.getAinin_orden_trabajo(), informe.getAinin_titulo(),
                enteControl, recomendacionDTO, informe.getAinin_memorando(), "",
                informe.getAinin_usuario(), respStr, null);
            recomendacionDTOs.add(dto);
          }

          List<RecomendacionDTO> recDtoList = recomendacionDTOs.stream()
              .map(InformeRecomendacionDTO::getRecomendaciones)
              .filter(java.util.Objects::nonNull)
              .collect(Collectors.toList());
          batchEnriquecerRecomendacionesConResponsables(recDtoList, resumenRespBatch);

          if (filtroDTO.getArees_estado_seg() != null && filtroDTO.getArees_estado_seg() != 0) {
            long requestedState = filtroDTO.getArees_estado_seg();
            recomendacionDTOs = recomendacionDTOs.stream()
                .filter(d -> d.getRecomendaciones() != null &&
                    d.getRecomendaciones().getEstado_seguimiento() != null &&
                    d.getRecomendaciones().getEstado_seguimiento().getArees_codigo() != null &&
                    d.getRecomendaciones().getEstado_seguimiento().getArees_codigo().equals(requestedState))
                .collect(Collectors.toList());
          }

          logger.info("obtenerDinamicoAuditor: {} recs en {} ms", recCodigos.size(), System.currentTimeMillis() - t0);

        } catch (Exception e) {
          logger.error("Error al obtener las recomendaciones: {}", e.getMessage());
        }

      } catch (Exception e) {
        logger.error("Error al obtener las recomendaciones: {}", e.getMessage());
      }
      return EstadoRespuestas.respuestaObtencion("Recomendaciones obtenidas exitosamente", recomendacionDTOs);
    });
  }

  private static class ResumenResponsablesRec {
    int total = 0;
    int cumplidos = 0;
    int vencidos = 0;
    int enPlazo = 0;
    Date maxFechaEfectiva = null;
    Date maxFechaPendiente = null;
    Long recEstadoSegBase = null;
    Set<Long> estadosIndividuales = new HashSet<>();
  }

  private Map<Long, ResumenResponsablesRec> batchObtenerResumenResponsablesPorRecomendacion(List<Long> recCodigos) {
    if (recCodigos == null || recCodigos.isEmpty()) return new HashMap<>();

    Map<Long, ResumenResponsablesRec> resumenMap = new HashMap<>();
    int batchSize = 500;
    for (int i = 0; i < recCodigos.size(); i += batchSize) {
      List<Long> subList = recCodigos.subList(i, Math.min(i + batchSize, recCodigos.size()));
      String ids = subList.stream().map(String::valueOf).collect(Collectors.joining(","));

      String sql = "SELECT D.ARERE_CODIGO, D.AREDR_CODIGO, D.ARERS_CODIGO, D.AREDR_FECHA_MAXIMA, " +
          "R.ARERE_FECHA_MAXIMA AS REC_FECHA_MAX, R.AREES_CODIGO AS REC_ESTADO_SEG, " +
          "(SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM ROOTSISREC.AI_REC_SOLICITUDES S " +
          " WHERE S.ARERE_CODIGO = D.ARERE_CODIGO AND S.ARERS_CODIGO = D.ARERS_CODIGO AND S.ARESO_ESTADO = '1' " +
          " AND S.ARESO_ESTADO_APROB = '3' " +
          " AND S.ARESO_FECHA_MAX_NUE IS NOT NULL) AS FECHA_PRORROGA, " +
          "(SELECT COUNT(1) FROM ROOTSISREC.AI_REC_VERIFICABLES V " +
          " WHERE V.AREDR_CODIGO = D.AREDR_CODIGO AND V.AREVE_ESTADO = '1' AND V.AREEV_CODIGO = 2) AS TIENE_JUSTIFICADO " +
          "FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION D " +
          "JOIN ROOTSISREC.AI_REC_RECOMENDACIONES R ON R.ARERE_CODIGO = D.ARERE_CODIGO " +
          "JOIN ROOTSISREC.AI_REC_RESPONSABLES RES ON RES.ARERS_CODIGO = D.ARERS_CODIGO " +
          "WHERE D.ARERE_CODIGO IN (" + ids + ") " +
          "AND D.AREDR_ESTADO = 1 AND R.ARERE_ESTADO = 1 AND RES.ARERS_ESTADO = 1";

      try {
        jdbcTemplate.query(sql, rs -> {
          long recCod = rs.getLong("ARERE_CODIGO");
          ResumenResponsablesRec resumen = resumenMap.computeIfAbsent(recCod, k -> new ResumenResponsablesRec());
          resumen.total++;

          long recEstadoSeg = rs.getLong("REC_ESTADO_SEG");
          resumen.recEstadoSegBase = recEstadoSeg;

          Date fechaProrroga = rs.getDate("FECHA_PRORROGA");
          Date fechaDetalle = rs.getDate("AREDR_FECHA_MAXIMA");
          Date fechaRec = rs.getDate("REC_FECHA_MAX");

          Date fechaEfectiva = fechaDetalle;
          if (fechaRec != null && (fechaEfectiva == null || fechaRec.after(fechaEfectiva))) {
            fechaEfectiva = fechaRec;
          }
          if (fechaProrroga != null && (fechaEfectiva == null || fechaProrroga.after(fechaEfectiva))) {
            fechaEfectiva = fechaProrroga;
          }
          if (fechaEfectiva != null) {
            if (resumen.maxFechaEfectiva == null || fechaEfectiva.after(resumen.maxFechaEfectiva)) {
              resumen.maxFechaEfectiva = fechaEfectiva;
            }
          }

          int tieneJustificado = rs.getInt("TIENE_JUSTIFICADO");
          Long estadoInd;
          boolean esCumplido = (tieneJustificado > 0 || recEstadoSeg == 2L);
          if (esCumplido) {
            resumen.cumplidos++;
            estadoInd = 2L;
          } else if (recEstadoSeg == 4L) {
            estadoInd = 4L;
          } else if (fechaEfectiva != null) {
            if (resumen.maxFechaPendiente == null || fechaEfectiva.after(resumen.maxFechaPendiente)) {
              resumen.maxFechaPendiente = fechaEfectiva;
            }
            int p = DateMapper.calcularDias(fechaEfectiva);
            if (p < 0) {
              resumen.vencidos++;
              estadoInd = 3L;
            } else {
              resumen.enPlazo++;
              estadoInd = 1L;
            }
          } else {
            resumen.enPlazo++;
            estadoInd = 1L;
          }
          resumen.estadosIndividuales.add(estadoInd);
        });
      } catch (Exception e) {
        logger.error("Error en batchObtenerResumenResponsablesPorRecomendacion: {}", e.getMessage(), e);
      }
    }
    return resumenMap;
  }

  private void batchEnriquecerRecomendacionesConResponsables(List<RecomendacionDTO> dtos) {
    batchEnriquecerRecomendacionesConResponsables(dtos, null);
  }

  private void batchEnriquecerRecomendacionesConResponsables(List<RecomendacionDTO> dtos, Map<Long, ResumenResponsablesRec> preloadedResumenMap) {
    if (dtos == null || dtos.isEmpty()) return;

    List<Long> recCodigos = dtos.stream()
        .map(RecomendacionDTO::getArere_codigo)
        .filter(java.util.Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());

    if (recCodigos.isEmpty()) return;

    Map<Long, ResumenResponsablesRec> resumenMap = (preloadedResumenMap != null)
        ? preloadedResumenMap
        : batchObtenerResumenResponsablesPorRecomendacion(recCodigos);

    for (RecomendacionDTO dto : dtos) {
      if (dto == null || dto.getArere_codigo() == null) continue;
      ResumenResponsablesRec resumen = resumenMap.get(dto.getArere_codigo());
      if (resumen != null && resumen.total > 0) {
        dto.setTotal_responsables(resumen.total);
        dto.setResponsables_cumplidos(resumen.cumplidos);
        dto.setResponsables_vencidos(resumen.vencidos);
        dto.setResponsables(resumen.total);

        // Regla: la fecha global de cumplimiento se rige por los responsables pendientes.
        // Si todos están cumplidos, toma la fecha máxima del último cumplimiento.
        Date fBase = DateMapper.convertirFechaFormato(dto.getArere_fecha_maxima());
        Date fFinal;
        if (resumen.cumplidos == resumen.total && resumen.total > 0) {
          fFinal = (resumen.maxFechaEfectiva != null) ? resumen.maxFechaEfectiva : fBase;
        } else if (resumen.maxFechaPendiente != null) {
          fFinal = resumen.maxFechaPendiente;
        } else {
          fFinal = (resumen.maxFechaEfectiva != null) ? resumen.maxFechaEfectiva : fBase;
        }

        if (fFinal != null) {
          dto.setArere_fecha_maxima(DateMapper.convertirFechaFormato(fFinal));
          int plazo = DateMapper.calcularDias(fFinal);
          dto.setPlazo(plazo);
        }

        // Estado global de la recomendación alineado a las reglas del negocio
        Long estadoFinalCod;
        if (resumen.recEstadoSegBase != null && resumen.recEstadoSegBase == 4L) {
          estadoFinalCod = 4L; // NO APLICA
        } else if (resumen.cumplidos == resumen.total && resumen.total > 0) {
          estadoFinalCod = 2L; // CUMPLIDA
        } else if (resumen.vencidos > 0) {
          estadoFinalCod = 3L; // NO CUMPLIDA (Vencida)
        } else if (resumen.enPlazo > 0 || dto.getPlazo() >= 0) {
          estadoFinalCod = 1L; // EN PROCESO
        } else {
          estadoFinalCod = 3L; // NO CUMPLIDA (Vencida)
        }

        EstadoSeg estadoSeg = getEstadoSegCached(estadoFinalCod);
        if (estadoSeg != null) {
          dto.setEstado_seguimiento(new EstadoSegDTO(estadoSeg.getArees_codigo(), estadoSeg.getArees_descripcion()));
        }
      } else {
        Date fBase = DateMapper.convertirFechaFormato(dto.getArere_fecha_maxima());
        if (fBase != null) {
          dto.setPlazo(DateMapper.calcularDias(fBase));
        }
      }
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDinamicoResponsable(RecomendacionFiltroRDTO filtroDTO) {
    long t0 = System.currentTimeMillis();
    return jdbcTemplate.execute((Connection conn) -> {
      String functionCall = "{? = call ROOTSISREC.FN_RECOMENDACIONES_FILTROS_R(?,?,?)}";

      List<InformeRecomendacionDTO> informeRecomendacionDTOs = new ArrayList<>();

      try (CallableStatement cs = conn.prepareCall(functionCall)) {
        cs.registerOutParameter(1, Types.REF_CURSOR);
        cs.setInt(2, filtroDTO.getArere_anio_inicio_control());
        cs.setLong(3, filtroDTO.getArees_estado_seg());
        cs.setString(4, filtroDTO.getArers_codigo_per());
        cs.execute();

        try (ResultSet rs = (ResultSet) cs.getObject(1)) {
          BeanPropertyRowMapper<DetalleRecomendacion> mapper = new BeanPropertyRowMapper<>(DetalleRecomendacion.class);
          List<DetalleRecomendacion> detalles = new ArrayList<>();
          List<Long> recCodigos = new ArrayList<>();
          while (rs.next()) {
            Long recCod = rs.getLong("ARERE_CODIGO");
            DetalleRecomendacion detalle = mapper.mapRow(rs, rs.getRow());
            detalles.add(detalle);
            recCodigos.add(recCod);
          }

          Map<Long, Informe> informeMap = batchObtenerInformesPorRecomendacion(recCodigos);
          Map<Long, Conclusion> conclusionMap = batchObtenerConclusionesPorRecomendacion(recCodigos);
          Map<Long, String> respStrMap = batchObtenerResponsablesStr(recCodigos);
          Map<Long, Recomendacion> recMap = batchObtenerRecomendaciones(recCodigos);
          Map<Long, SolicitudRespInfo> solicitudesBatch = batchObtenerSolicitudesPorRecomendacion(recCodigos, filtroDTO.getArers_codigo_per());
          Map<Long, String> estadoVerificableBatch = batchObtenerEstadoVerificablePorRecomendacion(recCodigos);
          Map<Long, ResumenResponsablesRec> resumenRespBatch = batchObtenerResumenResponsablesPorRecomendacion(recCodigos);

          for (int i = 0; i < detalles.size(); i++) {
            try {
              DetalleRecomendacion detalle = detalles.get(i);
              Long recCod = recCodigos.get(i);
              Recomendacion rec = recMap.get(recCod);
              if (rec == null) continue;
              detalle.setRecomendacion(rec);

              Informe informe = informeMap.get(recCod);
              Conclusion conclusion = conclusionMap.get(recCod);
              if (informe == null || conclusion == null) continue;

              RecomendacionDTO recomendacionDTO = recomendacionMapper.convertirDetalleResponsableModelDTO(detalle);
              if (recomendacionDTO != null) {
                if (recomendacionDTO.getAredr_codigo() == null && detalle.getAredr_codigo() != null) {
                  recomendacionDTO.setAredr_codigo(detalle.getAredr_codigo());
                }
                if (recomendacionDTO.getArers_codigo_per() == null && filtroDTO.getArers_codigo_per() != null) {
                  recomendacionDTO.setArers_codigo_per(filtroDTO.getArers_codigo_per());
                }
              }
              
              SolicitudRespInfo solInfo = solicitudesBatch.get(recCod);
              String estadoSolicitud = null;
              if (solInfo != null && solInfo.estadoAprob != null) {
                  if ("1".equals(solInfo.estadoAprob)) estadoSolicitud = "PENDIENTE";
                  else if ("2".equals(solInfo.estadoAprob)) estadoSolicitud = "APROBADO_PARCIAL";
                  else if ("3".equals(solInfo.estadoAprob)) estadoSolicitud = "APROBADO";
              }
              recomendacionDTO.setEstado_solicitud(estadoSolicitud);

              String estadoVerificable = estadoVerificableBatch.getOrDefault(detalle.getAredr_codigo(), "VERIFICABLE NO CREADO");
              recomendacionDTO.setEstado_verificable(estadoVerificable);

              // 1. Estado General Institucional y Consensuado + Progreso Global
              Long estadoGlobalOriginal = (rec.getEstadoSeg() != null) ? rec.getEstadoSeg().getArees_codigo() : 1L;
              ResumenResponsablesRec resumenRec = resumenRespBatch.get(recCod);
              Long estadoGeneralCod = estadoGlobalOriginal;

              int totalResp = (resumenRec != null) ? resumenRec.total : 0;
              int cumplidosResp = (resumenRec != null) ? resumenRec.cumplidos : 0;

              if (Long.valueOf(2L).equals(estadoGlobalOriginal)) {
                estadoGeneralCod = 2L;
              } else if (Long.valueOf(4L).equals(estadoGlobalOriginal)) {
                estadoGeneralCod = 4L;
              } else if (resumenRec != null && resumenRec.estadosIndividuales.size() == 1) {
                // Todos los responsables asignados comparten unánimemente el mismo estado individual
                estadoGeneralCod = resumenRec.estadosIndividuales.iterator().next();
              }

              EstadoSeg estadoGeneralModel = getEstadoSegCached(estadoGeneralCod);
              String descGeneral = (estadoGeneralModel != null) ? estadoGeneralModel.getArees_descripcion() : "EN PROCESO";
              EstadoSegDTO estadoGeneralDTO = new EstadoSegDTO(estadoGeneralCod, descGeneral);
              recomendacionDTO.setEstado_general(estadoGeneralDTO);

              // Progreso global de la recomendación
              float progreso = 0.0f;
              if (Long.valueOf(2L).equals(estadoGeneralCod) || Long.valueOf(2L).equals(estadoGlobalOriginal)) {
                progreso = 100.0f;
                if (totalResp == 0) totalResp = 1;
                cumplidosResp = totalResp;
              } else if (totalResp > 0) {
                progreso = Math.round((cumplidosResp * 100.0f) / totalResp);
              }
              recomendacionDTO.setArere_progreso(progreso);
              recomendacionDTO.setTotal_responsables(totalResp);
              recomendacionDTO.setResponsables_cumplidos(cumplidosResp);
              if (rec.getArere_fecha_maxima() != null) {
                recomendacionDTO.setFecha_maxima_general(DateMapper.convertirFechaFormato(rec.getArere_fecha_maxima()));
              }

              // 2. Mi Estado (Seguimiento Individual para este responsable)
              Date fechaEfectivaIndividual = null;
              if (solInfo != null && solInfo.fechaMaxNueva != null) {
                fechaEfectivaIndividual = solInfo.fechaMaxNueva;
              } else if (detalle.getAredr_fecha_maxima() != null) {
                fechaEfectivaIndividual = detalle.getAredr_fecha_maxima();
              } else if (rec.getArere_fecha_maxima() != null) {
                fechaEfectivaIndividual = rec.getArere_fecha_maxima();
              }

              boolean tieneVerificableJustificado = estadoVerificable != null && (
                  "JUSTIFICA".equalsIgnoreCase(estadoVerificable.trim()) ||
                  "JUSTIFICADO".equalsIgnoreCase(estadoVerificable.trim()) ||
                  "VERIFICABLE JUSTIFICADO".equalsIgnoreCase(estadoVerificable.trim())
              );

              Long estadoIndCod;
              int plazoInd = 0;
              if (Long.valueOf(4L).equals(estadoGlobalOriginal)) {
                estadoIndCod = 4L;
              } else if (tieneVerificableJustificado) {
                estadoIndCod = 2L;
                plazoInd = 0;
              } else if (fechaEfectivaIndividual != null) {
                plazoInd = DateMapper.calcularDias(fechaEfectivaIndividual);
                if (plazoInd < 0) {
                  estadoIndCod = 3L;
                } else {
                  estadoIndCod = 1L;
                }
              } else {
                estadoIndCod = 1L;
              }

              EstadoSeg estadoIndModel = getEstadoSegCached(estadoIndCod);
              String descInd = (estadoIndModel != null) ? estadoIndModel.getArees_descripcion() : "EN PROCESO";
              EstadoSegDTO estadoIndDTO = new EstadoSegDTO(estadoIndCod, descInd);
              recomendacionDTO.setEstado_individual(estadoIndDTO);
              recomendacionDTO.setPlazo_individual(plazoInd);
              if (fechaEfectivaIndividual != null) {
                recomendacionDTO.setFecha_maxima_individual(DateMapper.convertirFechaFormato(fechaEfectivaIndividual));
              }

              // Asignar campos de compatibilidad hacia atrás
              recomendacionDTO.setEstado_seguimiento(estadoGeneralDTO);
              recomendacionDTO.setPlazo(plazoInd);
              recomendacionDTO.setArere_fecha_maxima(
                  recomendacionDTO.getFecha_maxima_individual() != null ? 
                  recomendacionDTO.getFecha_maxima_individual() : recomendacionDTO.getFecha_maxima_general()
              );

              String responsables = respStrMap.getOrDefault(recCod, "");
              InformeRecomendacionDTO dto = new InformeRecomendacionDTO(
                  informe.getAinin_codigo(),
                  informe.getAinin_orden_trabajo(), informe.getAinin_titulo(),
                  conclusion.getAreco_conclusion(), recomendacionDTO, informe.getAinin_memorando(),
                  informe.getAinin_memo_aprobacion(), informe.getAinin_usuario(), responsables, null);
              informeRecomendacionDTOs.add(dto);
            } catch (Exception e) {
              logger.error("Error al procesar recomendación individual: {}", e.getMessage(), e);
            }
          }
          if (filtroDTO.getArees_estado_seg() != null && filtroDTO.getArees_estado_seg() != 0) {
            long requestedState = filtroDTO.getArees_estado_seg();
            informeRecomendacionDTOs = informeRecomendacionDTOs.stream()
                .filter(d -> {
                  if (d.getRecomendaciones() == null) return false;
                  Long estadoAComparar = (d.getRecomendaciones().getEstado_individual() != null && d.getRecomendaciones().getEstado_individual().getArees_codigo() != null)
                      ? d.getRecomendaciones().getEstado_individual().getArees_codigo()
                      : (d.getRecomendaciones().getEstado_seguimiento() != null ? d.getRecomendaciones().getEstado_seguimiento().getArees_codigo() : null);
                  return Long.valueOf(requestedState).equals(estadoAComparar);
                })
                .collect(Collectors.toList());
          }
          logger.info("obtenerDinamicoResponsable: {} detalles en {} ms", detalles.size(), System.currentTimeMillis() - t0);
        } catch (Exception e) {
          logger.error("Error al obtener las recomendaciones: {}", e.getMessage());
        }

      } catch (Exception e) {
        logger.error("Error al obtener las recomendaciones: {}", e.getMessage());
      }
      return EstadoRespuestas.respuestaObtencion("Recomendaciones responsables obtenidas exitosamente",
          informeRecomendacionDTOs);
    });
  }

  private Integer extraerAnio(Date fecha) {
    if (fecha == null) {
      return Calendar.getInstance().get(Calendar.YEAR);
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(fecha);
    return cal.get(Calendar.YEAR);
  }

  /**
   * Servicio para asignar un nuevo responsable a la recomendaci?n *
   * 
   * @param AsignarResponsableDTO con parametros para el registro y asignaci?n del
   *                              nuevo responsable.
   * @return el resultado es un JSON.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> asignarResponsable(AsignarResponsableDTO dto,
      Long arere_codigo) {
    try {
      Recomendacion recomendacion = recomendacionRepositorio.findById(arere_codigo).get();
      if (recomendacion.getEstadoSeg() != null && EstadoSegEnum.CUMPLIDA.getCodigo().equals(recomendacion.getEstadoSeg().getArees_codigo())) {
        return EstadoRespuestas.respuestaError("No se pueden asignar responsables a una recomendación que se encuentra CUMPLIDA");
      }
      
      // Asegurar que todos los nuevos responsables tengan rol de RESPONSABLE (código 4)
      for (NuevoResponsableDTO responsableDTO : dto.getResponsables()) {
        rolUsuarioServicio.asegurarRolResponsable(responsableDTO, dto.getAuditoria());
      }

      for (NuevoResponsableDTO responsableDTO : dto.getResponsables()) {
        // Validar si el responsable ya se encuentra asignado y activo en esta recomendación
        List<DetalleRecomendacion> detallesExistentes = detalleRepositorio.findByRecomendacion(arere_codigo);
        boolean yaAsignado = detallesExistentes != null && detallesExistentes.stream()
            .anyMatch(d -> d.getAredr_estado() != null 
                && (d.getAredr_estado() == '1' || "1".equals(String.valueOf(d.getAredr_estado())))
                && d.getResponsable() != null 
                && responsableDTO.getArers_codigo_per().equals(d.getResponsable().getArers_codigo_per()));
        if (yaAsignado) {
          logger.warn("[ASIGNACIÓN] El responsable {} ya se encuentra activo en la recomendación {}. Omitiendo duplicado.",
              responsableDTO.getArers_codigo_per(), arere_codigo);
          continue;
        }

        responsableDTO.setAuditoria(dto.getAuditoria());
        Responsable responsable = responsableServicio.registrarResponsable(responsableDTO);
        // Registrar detalle
        detalleServicio.registar(new NuevoDetalleDTO(dto.getAuditoria()), recomendacion, responsable);
        
        // Registrar en el historial de recomendaciones
        String respNueName = responsable.getArers_nombre() + " " + responsable.getArers_apellido();
        historialServicio.registrarEvento(
            recomendacion.getArere_codigo(),
            "ASIGNACION_RESPONSABLE",
            "Se asignó como responsable a: " + respNueName + " (" + (responsable.getArers_cargo() != null ? responsable.getArers_cargo() : "Sin cargo") + ").",
            null,
            respNueName,
            null, null, null, null,
            dto.getAuditoria().getUsuario()
        );

        // Notificacion de asignacion de responsable para el director del responsable
        this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + responsable.getArers_usuario_per(),
            "Tiene una nueva notificacion.");
        Integer anioRecAsig = extraerAnio(recomendacion.getArere_fecha_inicio_control());
        String memoInforme = this.recomendacionRepositorio.obtenerMemoInforme(recomendacion.getArere_codigo());
        this.notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO("Nueva recomendaci\u00f3n",
            "Se le asign\u00f3 como responsable de cumplimiento de la recomendacion del informe con memorando "
                + memoInforme + " Num. Recomendacion: " + recomendacion.getArere_num_rec(),
            TipoNotificacionEnum.RECOMENDACION_ASIGNADA,
            TipoNotificacionEnum.RECOMENDACION_ASIGNADA,
            anioRecAsig,
            informeServicio.obtenerInformeByRecomendacion(recomendacion.getArere_codigo()).getAinin_codigo(),
            recomendacion.getArere_codigo(),
            responsable.getArers_usuario_per()));

        // Notificación para formulación y registro obligatorio del Plan de Acción (plazo máximo 48 horas)
        try {
          String descNumRec = recomendacion.getArere_num_rec() != null ? String.valueOf(recomendacion.getArere_num_rec()) : "";
          Informe infRecPlan = informeServicio.obtenerInformeByRecomendacion(recomendacion.getArere_codigo());
          String memoFinalPlan = (memoInforme != null && !memoInforme.trim().isEmpty())
              ? memoInforme.trim()
              : (infRecPlan != null && infRecPlan.getAinin_memorando() != null && !infRecPlan.getAinin_memorando().trim().isEmpty()
                  ? infRecPlan.getAinin_memorando().trim()
                  : (infRecPlan != null && infRecPlan.getAinin_memo_aprobacion() != null ? infRecPlan.getAinin_memo_aprobacion().trim() : ""));
          String mensajePlan = "Dispone de un plazo de hasta 48 horas posteriores a esta notificación para formular y registrar su Plan de Acción en el sistema SISREC para la recomendación "
              + descNumRec + (memoFinalPlan != null && !memoFinalPlan.isEmpty() ? (" del informe con memorando " + memoFinalPlan) : "") + ".";
          NuevaNotificacionDTO notifPlan = new NuevaNotificacionDTO(
              "Registro de Plan de Acción",
              mensajePlan,
              "/inicio/plan-accion-responsables",
              TipoNotificacionEnum.PLAN_ACCION_REQUERIDO,
              anioRecAsig,
              informeServicio.obtenerInformeByRecomendacion(recomendacion.getArere_codigo()).getAinin_codigo(),
              recomendacion.getArere_codigo(),
              responsable.getArers_usuario_per()
          );
          this.notificacionesServicio.registrarNotificacion(notifPlan);
          this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + responsable.getArers_usuario_per(),
              "Tiene una nueva notificación: Registro de Plan de Acción.");
        } catch (Exception ePlan) {
          logger.error("Error al registrar notificación de Plan de Acción al asignar responsable: {}", ePlan.getMessage());
        }

        // Enviar correo de asignación
        try {
          Informe informe = informeServicio.obtenerInformeByRecomendacion(recomendacion.getArere_codigo());
          String memoAuditoria = (memoInforme != null && !memoInforme.isEmpty()) 
              ? memoInforme 
              : (informe != null && informe.getAinin_memorando() != null ? informe.getAinin_memorando() : "");
          String memoPresidencia = (informe != null && informe.getAinin_memo_aprobacion() != null) 
              ? informe.getAinin_memo_aprobacion() 
              : "";
          Map<String, Object> variables = new HashMap<>();
          variables.put("fecha", UtilsFunctions.getDateHour());
          variables.put("nombres", responsable.getArers_nombre());
          variables.put("apellidos", responsable.getArers_apellido());
          variables.put("titulo_informe", informe != null ? informe.getAinin_titulo() : "");
          variables.put("tituloInforme", informe != null ? informe.getAinin_titulo() : "");
          variables.put("memorando", memoAuditoria);
          variables.put("memoaprobacion", memoPresidencia);
          variables.put("memoAprobacion", memoPresidencia);
          variables.put("memorando_aprobacion", memoPresidencia);
          variables.put("recomendacion", recomendacion.getArere_descripcion());
          variables.put("fecha_max_cumplimiento",
              recomendacion.getArere_fecha_maxima() != null
                  ? DateMapper.convertirFechaFormato(recomendacion.getArere_fecha_maxima())
                  : "");
          variables.put("fechaMaxCumplimiento",
              recomendacion.getArere_fecha_maxima() != null
                  ? DateMapper.convertirFechaFormato(recomendacion.getArere_fecha_maxima())
                  : "");

          String correoResponsable = responsableDTO.getArers_correo();
          if (correoResponsable != null && !correoResponsable.isEmpty()) {
            emailService.sendCorreoAsignacion(correoResponsable, variables);
            logger.info("[ASIGNACIÓN] Correo de asignación enviado a: {}", correoResponsable);
          } else {
            logger.warn("[ASIGNACIÓN] No se pudo enviar correo: el responsable no tiene correo registrado.");
          }
        } catch (Exception e) {
          logger.error("[ASIGNACIÓN] Error al enviar correo de asignación: {}", e.getMessage());
        }
      }
      return EstadoRespuestas.respuestaCreacion("Responsable asignado exitosamente");
    } catch (Exception e) {
      logger.error("Error al asignar responsable: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al asignar responsable");
    }
  }

  private void sincronizarEvidenciasRequeridas(Recomendacion recomendacion, String textoEvidencias) {
    if (recomendacion == null || textoEvidencias == null || textoEvidencias.trim().isEmpty()) {
      return;
    }
    try {
      Long arereCodigo = recomendacion.getArere_codigo();
      if (arereCodigo == null) return;

      String[] parts = textoEvidencias.split("[\\r\\n;•]+|(?<=[\\.\\?!])\\s+(?=[A-ZÁÉÍÓÚÑ0-9])|(?<=\\s|^)(?:[0-9]+[\\.\\)]|[a-zA-Z][\\.\\)])\\s+");
      Set<String> descs = new LinkedHashSet<>();
      for (String part : parts) {
        String clean = part.replaceAll("^[0-9]+\\s*[\\.\\-\\)]\\s*", "")
                           .replaceAll("^[•\\-*\\s]+", "")
                           .replaceAll("[;\\.]+\\s*$", "")
                           .trim();
        if (!clean.isEmpty()) {
          descs.add(clean);
        }
      }

      List<EvidenciaRequerida> existentes = evidenciaRequeridaRepositorio.findByRecomendacion(arereCodigo);
      Map<String, EvidenciaRequerida> mapExistentes = new HashMap<>();
      for (EvidenciaRequerida er : existentes) {
        if (er.getArer_descripcion() != null) {
          mapExistentes.put(er.getArer_descripcion().trim().toLowerCase(), er);
        }
      }

      for (String desc : descs) {
        String key = desc.toLowerCase();
        if (!mapExistentes.containsKey(key)) {
          EvidenciaRequerida nueva = new EvidenciaRequerida();
          nueva.setRecomendacion(recomendacion);
          nueva.setArer_descripcion(desc.length() > 500 ? desc.substring(0, 500) : desc);
          nueva.setArer_estado('A');
          evidenciaRequeridaRepositorio.save(nueva);
        }
      }
      evidenciaRequeridaRepositorio.flush();
    } catch (Exception e) {
      logger.warn("Error al sincronizar evidencias en AI_REC_EVIDENCIAS_REQUERIDAS: {}", e.getMessage());
    }
  }

  /**
   * Servicio para registrar una nueva recomendaci?n
   * 
   * @param NuevaRecomendacionDTO DTO con parametros de actualizaci?n.
   * @return el resultado es un JSON.
   */
  @Transactional
  public Recomendacion registrarRecomendacion(NuevaRecomendacionDTO dto,
      Long areco_codigo) {
    try {
      java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.EvidenciaRequeridaDTO> evidencias = evidenciaRequeridaServicio.preparar(dto.getArere_evidencia_requerida(), dto.getArere_evidencias_requeridas());
      Conclusion conclusion = conclusionServicio.obtenerConclusion(areco_codigo);
      // El estado de seguimiento cuando se crea la recomendacion se envia por defecto
      // En Proceso.
      EstadoSeg estadoSeg = estadoSegRepositorio.findById(EstadoSegEnum.EN_PROCESO.getCodigo()).get();
      // Convertir DTO a modelo
      Recomendacion recomendacion = recomendacionMapper.convertirCrearDTOModel(dto, estadoSeg,
          conclusion);
      // Log de fechas asignadas a la recomendación
      logger.info("=== Registrando nueva recomendación ===");
      logger.info("  Conclusión código: {}", areco_codigo);
      logger.info("  Fecha inicio control (DTO): {}", dto.getArere_fecha_inicio_control());
      logger.info("  Fecha máxima (DTO): {}", dto.getArere_fecha_maxima());
      logger.info("  Fecha inicio control (Modelo): {}", recomendacion.getArere_fecha_inicio_control());
      logger.info("  Fecha máxima (Modelo): {}", recomendacion.getArere_fecha_maxima());
      logger.info("  Estado seguimiento: {}", estadoSeg.getArees_descripcion());
      // Registrar la recomendacion
      Recomendacion respuesta = recomendacionRepositorio.saveAndFlush(recomendacion);
      evidenciaRequeridaServicio.guardar(respuesta.getArere_codigo(), evidencias);
      logger.info("  Recomendación guardada con código: {}", respuesta.getArere_codigo());
      
      // Sincronizar directamente las evidencias requeridas en la tabla relacional AI_REC_EVIDENCIAS_REQUERIDAS
      sincronizarEvidenciasRequeridas(respuesta, dto.getArere_evidencia_requerida());
      
      // Registrar en el historial de recomendaciones
      historialServicio.registrarEvento(
          respuesta.getArere_codigo(),
          "CREACION",
          "Creacion e inicio de control de la recomendacion N " + respuesta.getArere_num_rec() + ".",
          null, null, null, null, null, null,
          dto.getAuditoria().getUsuario()
      );

      if (dto.getResponsables() != null && !dto.getResponsables().isEmpty()) {
        AsignarResponsableDTO asignacion = new AsignarResponsableDTO();
        asignacion.setResponsables(dto.getResponsables());
        asignacion.setAuditoria(dto.getAuditoria());
        ResponseEntity<LinkedHashMap<String, Object>> resultadoAsignacion =
            asignarResponsable(asignacion, respuesta.getArere_codigo());
        if (!resultadoAsignacion.getStatusCode().is2xxSuccessful()) {
          throw new IllegalStateException("No se pudieron asignar los responsables de la recomendacion");
        }
      }

      // Registrar en la bitacora
      try {
        bitacoraServicio.registrarBitacora(respuesta, dto.getAuditoria(), "CREAR");
      } catch (Exception e) {
        logger.error("Error al registrar bitacora para recomendacion: {}", e.getMessage());
        // No propagar el error de bitacora para evitar rollback de la transaccion principal
      }

      // Sincronizar automáticamente con periodos de evaluación activos (ej. Cierre Anual)
      try {
        corteEvaluacionServicio.sincronizarPeriodosActivos();
      } catch (Exception e) {
        logger.warn("Error al sincronizar periodos de evaluación al registrar recomendación: {}", e.getMessage());
      }

      return respuesta;
    } catch (Exception e) {
      logger.error("Error al registrar la recomendaci?n: {}", e.getMessage());
      org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return null;
    }

  }

  /**
   * Servicio para actualizar la recomendaci?n
   * 
   * @param dto          DTO con parametros de actualizaci?n.
   * @param arere_codigo codigo de la recomendaci?n
   * @return el resultado es un JSON.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarRecomendacion(ActualizarRecomendacionDTO dto,
      Long id) {
    String riesgo = dto.getArere_riesgo_verificado();
    if (riesgo != null && !("ALTO".equals(riesgo) || "MODERADO".equals(riesgo) || "BAJO".equals(riesgo))) {
      return EstadoRespuestas.respuestaError("Seleccione un nivel de riesgo valido: ALTO, MODERADO o BAJO.");
    }
    String evidencia = dto.getArere_evidencia_requerida();
    if (evidencia != null && (evidencia.trim().isEmpty() || evidencia.length() > 2000)) {
      return EstadoRespuestas.respuestaError("Ingrese la evidencia requerida (maximo 2000 caracteres).");
    }
    try {
      java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.EvidenciaRequeridaDTO> evidencias = evidenciaRequeridaServicio.preparar(evidencia, dto.getArere_evidencias_requeridas());
      if (evidencias != null) evidencia = evidenciaRequeridaServicio.serializar(evidencias);
      Recomendacion recomendacionOld = recomendacionRepositorio.findById(id).get();
      if (recomendacionOld.getEstadoSeg() != null && EstadoSegEnum.CUMPLIDA.getCodigo().equals(recomendacionOld.getEstadoSeg().getArees_codigo())) {
        return EstadoRespuestas.respuestaError("No se puede modificar una recomendación que se encuentra CUMPLIDA");
      }
      Date fechaMaxAnt = recomendacionOld.getArere_fecha_maxima();
      Date nuevaFechaMax = dto.getArere_fecha_maxima() != null ? DateMapper.convertirFechaFormato(dto.getArere_fecha_maxima()) : null;

      if (nuevaFechaMax != null && fechaMaxAnt != null) {
        if (DateMapper.toLocalDate(nuevaFechaMax).isBefore(DateMapper.toLocalDate(fechaMaxAnt))) {
          return EstadoRespuestas.respuestaError(
              "La nueva fecha no puede ser anterior a la fecha global de la recomendación ("
                  + DateMapper.convertirFechaFormato(fechaMaxAnt) + "). Debe ser posterior.");
        }
      }

      // 1. Actualizar campos base de la recomendación
      if (riesgo != null) {
        recomendacionOld.setArere_riesgo_verificado(riesgo);
      }
      if (evidencia != null) {
        recomendacionOld.setArere_evidencia_requerida(evidencia.trim());
        sincronizarEvidenciasRequeridas(recomendacionOld, evidencia);
      }
      recomendacionOld.setArere_descripcion(dto.getArere_descripcion());
      recomendacionOld.setArere_num_rec(dto.getArere_num_rec());
      recomendacionOld.setArere_usuario(dto.getAuditoria().getUsuario());
      recomendacionOld.setArere_fecha(new Date());

      // 2. Manejo de fecha individual por responsable o global
      String responsableNombre = "Sin asignar";
      boolean fechaCambiada = false;

      if (dto.getArers_codigo() != null) {
        DetalleRecomendacion targetDetail = detalleRepositorio.findByRecomendacionResponsable(id, dto.getArers_codigo());
        if (targetDetail != null) {
          Date targetFechaAnt = targetDetail.getAredr_fecha_maxima() != null ? targetDetail.getAredr_fecha_maxima() : fechaMaxAnt;
          if (nuevaFechaMax != null && !nuevaFechaMax.equals(targetFechaAnt)) {
            targetDetail.setAredr_fecha_maxima(nuevaFechaMax);
            detalleRepositorio.saveAndFlush(targetDetail);
            try {
              jdbcTemplate.update(
                  "UPDATE ROOTSISREC.AI_REC_SOLICITUDES S SET S.ARESO_FECHA_MAX_NUE = ? " +
                  "WHERE S.ARERE_CODIGO = ? AND S.ARERS_CODIGO = ? AND S.ARESO_ESTADO = '1' " +
                  "AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_FECHA_MAX_NUE < ?",
                  nuevaFechaMax, id, dto.getArers_codigo(), nuevaFechaMax);
            } catch (Exception e) {
              logger.warn("Error al sincronizar prórroga en actualizarRecomendacion: {}", e.getMessage());
            }
            fechaCambiada = true;
            fechaMaxAnt = targetFechaAnt;
            if (targetDetail.getResponsable() != null) {
              responsableNombre = targetDetail.getResponsable().getArers_nombre() + " " + targetDetail.getResponsable().getArers_apellido();
            }
          }
        }
        // Sincronizar fecha global de la recomendación con la fecha máxima de sus detalles activos
        List<DetalleRecomendacion> todosDetalles = detalleRepositorio.findByRecomendacion(id);
        if (todosDetalles != null) {
          Date maxFecha = null;
          for (DetalleRecomendacion d : todosDetalles) {
            if (d.getAredr_estado() != null && (d.getAredr_estado() == '1' || "1".equals(String.valueOf(d.getAredr_estado())))) {
              Date dFecha = d.getAredr_fecha_maxima() != null ? d.getAredr_fecha_maxima() : recomendacionOld.getArere_fecha_maxima();
              if (dFecha != null && (maxFecha == null || dFecha.after(maxFecha))) {
                maxFecha = dFecha;
              }
            }
          }
          if (maxFecha != null) {
            recomendacionOld.setArere_fecha_maxima(maxFecha);
          }
        }
      } else if (nuevaFechaMax != null && !nuevaFechaMax.equals(fechaMaxAnt)) {
        // No se especificó responsable: actualizar todos los detalles activos (o la recomendación base si no hay detalles)
        List<DetalleRecomendacion> detalles = detalleRepositorio.findByRecomendacion(id);
        if (detalles != null && !detalles.isEmpty()) {
          for (DetalleRecomendacion det : detalles) {
            if (det.getAredr_estado() != null && (det.getAredr_estado() == '1' || "1".equals(String.valueOf(det.getAredr_estado())))) {
              det.setAredr_fecha_maxima(nuevaFechaMax);
              detalleRepositorio.save(det);
              if ("Sin asignar".equals(responsableNombre) && det.getResponsable() != null) {
                responsableNombre = det.getResponsable().getArers_nombre() + " " + det.getResponsable().getArers_apellido();
              }
            }
          }
          detalleRepositorio.flush();
          try {
            jdbcTemplate.update(
                "UPDATE ROOTSISREC.AI_REC_SOLICITUDES S SET S.ARESO_FECHA_MAX_NUE = ? " +
                "WHERE S.ARERE_CODIGO = ? AND S.ARESO_ESTADO = '1' " +
                "AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_FECHA_MAX_NUE < ?",
                nuevaFechaMax, id, nuevaFechaMax);
          } catch (Exception e) {
            logger.warn("Error al sincronizar prórrogas globales en actualizarRecomendacion: {}", e.getMessage());
          }
          if ("Sin asignar".equals(responsableNombre)) {
            responsableNombre = "Todos los responsables";
          }
        }
        recomendacionOld.setArere_fecha_maxima(nuevaFechaMax);
        fechaCambiada = true;
      }

      recomendacionRepositorio.saveAndFlush(recomendacionOld);
      evidenciaRequeridaServicio.guardar(id, evidencias);
      recalcularEstadoRecomendacion(id);
      Recomendacion respuesta = recomendacionRepositorio.findById(id).get();

      if (fechaCambiada && nuevaFechaMax != null) {
        String detailText = "Modificación de la fecha máxima de cumplimiento. Nueva: " 
            + ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper.convertirFechaFormato(nuevaFechaMax)
            + " (Anterior: " + ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper.convertirFechaFormato(fechaMaxAnt) + "). Responsable afectado: " + responsableNombre + ".";
        
        historialServicio.registrarEvento(
            respuesta.getArere_codigo(),
            "ACTUALIZACION_FECHA",
            detailText,
            null, null,
            fechaMaxAnt, nuevaFechaMax,
            null, null,
            dto.getAuditoria().getUsuario()
        );
      }

      try {
        bitacoraServicio.registrarBitacora(respuesta, dto.getAuditoria(), "ACTUALIZAR");
      } catch (Exception e) {
        logger.error("Error al registrar bitacora para actualizacion de recomendacion: {}", e.getMessage());
        // No propagar el error de bitacora para evitar rollback de la transaccion principal
      }
      return EstadoRespuestas.respuestaCreacion("Recomendaci?n actualizada exitosamente");
    } catch (Exception e) {
      org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      logger.error("Error al actualizar la recomendaci?n: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar la recomendaci?n");
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarRiesgo(ActualizarRiesgoDTO dto, Long id) {
    String riesgo = dto.getArere_riesgo_verificado();
    String evidencia = dto.getArere_evidencia_requerida();
    if ((riesgo == null && evidencia == null && dto.getArere_evidencias_requeridas() == null) ||
        (riesgo != null && !("ALTO".equals(riesgo) || "MODERADO".equals(riesgo) || "BAJO".equals(riesgo))) ||
        (evidencia != null && (evidencia.trim().isEmpty() || evidencia.length() > 2000)) ||
        dto.getAuditoria() == null || dto.getAuditoria().getUsuario() == null) {
      return EstadoRespuestas.respuestaError("Seleccione un riesgo valido e ingrese la evidencia requerida (maximo 2000 caracteres).");
    }
    java.util.List<ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.EvidenciaRequeridaDTO> evidencias;
    try {
      evidencias = evidenciaRequeridaServicio.preparar(evidencia, dto.getArere_evidencias_requeridas());
      if (evidencias != null) evidencia = evidenciaRequeridaServicio.serializar(evidencias);
    } catch (IllegalArgumentException e) {
      return EstadoRespuestas.respuestaError(e.getMessage());
    }
    Recomendacion recomendacion = recomendacionRepositorio.findById(id).orElse(null);
    if (recomendacion == null || Character.valueOf('0').equals(recomendacion.getArere_estado())) {
      return EstadoRespuestas.respuestaError("La recomendacion no esta disponible.");
    }
    if (recomendacion.getEstadoSeg() != null &&
        EstadoSegEnum.CUMPLIDA.getCodigo().equals(recomendacion.getEstadoSeg().getArees_codigo()) &&
        ((riesgo != null) ||
         (evidencias != null && !evidenciaRequeridaServicio.listar(id).isEmpty()))) {
      return EstadoRespuestas.respuestaError("Una recomendacion CUMPLIDA tiene riesgo BAJO automatico; solo se puede completar la evidencia sin registrar.");
    }

    if (riesgo != null) recomendacion.setArere_riesgo_verificado(riesgo);
    if (evidencia != null) {
      recomendacion.setArere_evidencia_requerida(evidencia.trim());
      sincronizarEvidenciasRequeridas(recomendacion, evidencia);
    }
    recomendacion.setArere_usuario(dto.getAuditoria().getUsuario());
    recomendacion.setArere_fecha(new Date());
    Recomendacion respuesta = recomendacionRepositorio.saveAndFlush(recomendacion);
    try {
      evidenciaRequeridaServicio.guardar(id, evidencias);
    } catch (Exception e) {
      org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return EstadoRespuestas.respuestaError("No se pudo guardar la evidencia requerida: " + e.getMessage());
    }
    try {
      bitacoraServicio.registrarBitacora(respuesta, dto.getAuditoria(), "ACTUALIZAR");
    } catch (Exception e) {
      logger.error("Error al registrar bitacora del riesgo de recomendacion {}", id, e);
    }
    return EstadoRespuestas.respuestaCreacion("Riesgo y evidencia requerida actualizados exitosamente");
  }

  /**
   * Servicio para eliminar la recomendaci?n en base a codigo de la recomendaci?n
   * 
   * @param eliminar     DTO con parametros de auditoria.
   * @param arere_codigo codigo de la recomendaci?n
   * @return el resultado es un JSON.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(EliminarDTO eliminarDTO, Long arere_codigo) {
    try {
      Recomendacion recomendacion = recomendacionRepositorio.findById(arere_codigo).get();
      if (recomendacion.getEstadoSeg() != null && EstadoSegEnum.CUMPLIDA.getCodigo().equals(recomendacion.getEstadoSeg().getArees_codigo())) {
        return EstadoRespuestas.respuestaError("No se puede eliminar una recomendación que se encuentra CUMPLIDA");
      }

      recomendacion.setArere_estado('0');

      recomendacionRepositorio.save(recomendacion);

      try {
        bitacoraServicio.registrarBitacora(recomendacion, eliminarDTO.getAuditoria(), "ELIMINACION LOGICA");
      } catch (Exception e) {
        logger.error("Error al registrar bit?cora para eliminaci?n l?gica de recomendaci?n: {}", e.getMessage());
        // No propagar el error de bit?cora para evitar rollback de la transacci?n
        // principal
      }
      // reordenarNumRecPorInforme(ainin_codigo);

      return EstadoRespuestas.respuestaEliminacion("Recomendacion eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar la recomendaci?n: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar la recomendacion");
    }
  }

  /**
   * Servicio para eliminar la recomendaci?n en base a codigo de la recomendaci?n
   * 
   * @param eliminar     DTO con parametros de auditoria.
   * @param arere_codigo codigo de la recomendaci?n
   * @return el resultado es un JSON.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(EliminarDTO eliminar, Long arere_codigo) {
    try {
      if (!recomendacionRepositorio.existsById(arere_codigo)) {
        return EstadoRespuestas.noEncontrado("Recomendacion no encontrada");
      }
      Recomendacion recomendacion = recomendacionRepositorio.findById(arere_codigo).get();
      if (recomendacion.getEstadoSeg() != null && EstadoSegEnum.CUMPLIDA.getCodigo().equals(recomendacion.getEstadoSeg().getArees_codigo())) {
        return EstadoRespuestas.respuestaError("No se puede eliminar una recomendación que se encuentra CUMPLIDA");
      }
      recomendacionRepositorio.deleteById(arere_codigo);
      try {
        bitacoraServicio.registrarBitacora(recomendacion, eliminar.getAuditoria(),
            "ELIMINAR FISICA");
      } catch (Exception e) {
        logger.error("Error al registrar bit?cora para eliminaci?n f?sica de recomendaci?n: {}", e.getMessage());
        // No propagar el error de bit?cora para evitar rollback de la transacci?n
        // principal
      }
      return EstadoRespuestas.respuestaEliminacion("Eliminacion de recomendaci?n exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar la recomendaci?n: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar la recomendaci?n");
    }
  }

  /**
   * Servicio para obtener el mayor numero de recomendaciones (numero secuencial)
   * a nivel del informe
   * 
   * @param ainin_codigo codigo del informe
   * @return el resultado es un JSON.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerMaxNumRec(Long ainin_codigo) {
    try {
      Integer numMaxRecomendacion = recomendacionRepositorio.findMaxNumRecPorInforme(ainin_codigo);
      return EstadoRespuestas.respuestaObtencion("Numero de recomendacion obtenido", numMaxRecomendacion);
    } catch (Exception e) {
      logger.error("Error al obtener numero de recomendaci?n: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener numero de recomendacino");
    }
  }

  @Transactional
  public void reordenarNumRecPorInforme(Long ainin_codigo) {
    List<Recomendacion> recomendaciones = recomendacionRepositorio.findRecomendacionesByInformeId(ainin_codigo);

    for (int i = 1; i < recomendaciones.size(); i++) {
      recomendaciones.get(i).setArere_num_rec(i);
    }
    recomendacionRepositorio.saveAll(recomendaciones);
  }

  /**
   * M?todo para obtener las recomendaciones en base al codigo de la conclusi?n
   * 
   * @param areco_codigo codigo de la conclusi?n
   * @return el resultado es un JSON.
   */
  @Transactional
  public ConclusionRecomendacionDTO obtenerPorConclusion(Long areco_codigo) {
    try {
      logger.info("=== Obteniendo recomendaciones por conclusión (estado de seguimiento) ===");
      logger.info("  Conclusión código: {}", areco_codigo);
      Conclusion conclusion = conclusionServicio.obtenerConclusion(areco_codigo);
      List<Recomendacion> recomendaciones = recomendacionRepositorio.findByConclusion(areco_codigo);
      logger.info("  Total recomendaciones encontradas: {}", recomendaciones.size());
      
      List<RecomendacionDTO> recomendacionDTOs = recomendaciones.stream()
          .filter((recomendacion) -> recomendacion.getArere_estado().equals('1'))
          .map((recomendacion) -> recomendacionMapper.convertirModelDTO(recomendacion))
          .collect(Collectors.toList());

      List<Long> recCodigos = recomendacionDTOs.stream()
          .map(RecomendacionDTO::getArere_codigo).collect(Collectors.toList());
      Map<Long, Integer> respCountMap = batchContarResponsablesActivos(recCodigos);
      recomendacionDTOs.forEach(dto ->
          dto.setResponsables(respCountMap.getOrDefault(dto.getArere_codigo(), 0))
      );
      batchEnriquecerRecomendacionesConResponsables(recomendacionDTOs);

      Informe informe = informeServicio.obtenerInformeByConclusion(areco_codigo);

      String fechaAprobacion = "";
      if (informe.getAinin_fecha_aprobacion() != null) {
        fechaAprobacion = DateMapper.convertirFechaFormato(informe.getAinin_fecha_aprobacion());
      }
      ConclusionRecomendacionDTO conclusionRecomendacionDTO = new ConclusionRecomendacionDTO(
          conclusion.getAreco_codigo(), conclusion.getAreco_conclusion(),
          informe.getAinin_orden_trabajo(),
          fechaAprobacion,
          informe.getAinin_titulo(),
          recomendacionDTOs);

      return conclusionRecomendacionDTO;
    } catch (Exception e) {
      logger.error("Error al obtener la conclusi?n: {}", e.getMessage());
      return null;
    }
  }

  /**
   * M?todo para obtener la informaci?n de las recomendaciones apartir del codigo
   * del informe y opcionalmente filtrado por departamento
   * 
   * @param filtroDTO contiene ainin_codigo (codigo del informe) y departamento
   *                  (opcional)
   * @return el resultado es un JSON con las recomendaciones del informe.
   */
  @Transactional
  public InformeRecomendacionesDTO obtenerRecomendacionesByInformes(
      InformeFiltroDTO filtroDTO) {
    long t0 = System.currentTimeMillis();
    try {
      return jdbcTemplate.execute((Connection conn) -> {
        String functionCall = "{?=call ROOTSISREC.FN_RECOMENDACIONES_INFORME(?,?)}";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<String> roles = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        InformeRecomendacionesDTO informeRecomendacionDTO = new InformeRecomendacionesDTO();
        Informe informe = informeServicio.obtenerInforme(filtroDTO.getAinin_codigo());
        try (CallableStatement cs = conn.prepareCall(functionCall)) {
          cs.registerOutParameter(1, Types.REF_CURSOR);
          cs.setLong(2, filtroDTO.getAinin_codigo());
          String depa = (filtroDTO.getDepartamento() == null || filtroDTO.getDepartamento().trim().isEmpty())
              ? "0"
              : filtroDTO.getDepartamento();
          cs.setString(3, depa);

          cs.execute();

          try (ResultSet rs = (ResultSet) cs.getObject(1)) {
            List<Recomendacion> recs = new ArrayList<>();
            List<Long> recCodigos = new ArrayList<>();
            Map<Long, Float> progresoMap = new HashMap<>();
            Map<Long, Integer> totalRespMap = new HashMap<>();
            Map<Long, Integer> respCumplidosMap = new HashMap<>();
            Map<Long, Integer> respVencidosMap = new HashMap<>();
            BeanPropertyRowMapper<Recomendacion> mapper = new BeanPropertyRowMapper<>(Recomendacion.class);
            while (rs.next()) {
              Recomendacion rec = mapper.mapRow(rs, rs.getRow());
              rec.setEstadoSeg(getEstadoSegCached(rs.getLong("AREES_CODIGO")));
              recs.add(rec);
              recCodigos.add(rec.getArere_codigo());
              try { progresoMap.put(rec.getArere_codigo(), rs.getFloat("PORCENTAJE")); } catch (Exception ignored) {}
              try { totalRespMap.put(rec.getArere_codigo(), rs.getInt("TOTAL_RESPONSABLES")); } catch (Exception ignored) {}
              try { respCumplidosMap.put(rec.getArere_codigo(), rs.getInt("RESPONSABLES_CUMPLIDOS")); } catch (Exception ignored) {}
              try { respVencidosMap.put(rec.getArere_codigo(), rs.getInt("RESPONSABLES_VENCIDOS")); } catch (Exception ignored) {}
            }

            Map<Long, Integer> respCountMap = batchContarResponsablesActivos(recCodigos);

            List<RecomendacionDTO> recomendacionDTOs = new ArrayList<>();
            boolean puedeVer = roles.contains("2") || roles.contains("3")
                || roles.contains(RolEnum.DIRECTOR_DEPARTAMENTAL.getCode().toString());

            if (puedeVer) {
              for (Recomendacion rec : recs) {
                RecomendacionDTO dto = recomendacionMapper.convertirModelDTO(rec);
                dto.setArere_progreso(progresoMap.getOrDefault(rec.getArere_codigo(), 0f));
                dto.setResponsables(respCountMap.getOrDefault(rec.getArere_codigo(), 0));
                dto.setTotal_responsables(totalRespMap.getOrDefault(rec.getArere_codigo(), 0));
                dto.setResponsables_cumplidos(respCumplidosMap.getOrDefault(rec.getArere_codigo(), 0));
                dto.setResponsables_vencidos(respVencidosMap.getOrDefault(rec.getArere_codigo(), 0));
                recomendacionDTOs.add(dto);
              }
              batchEnriquecerRecomendacionesConResponsables(recomendacionDTOs);
            }
            informeRecomendacionDTO = new InformeRecomendacionesDTO(informe.getAinin_codigo(),
                informe.getAinin_orden_trabajo(), informe.getAinin_titulo(),
                recomendacionDTOs);
            logger.info("obtenerRecomendacionesByInformes: {} recs en {} ms", recCodigos.size(), System.currentTimeMillis() - t0);

          } catch (Exception e) {
            logger.error("Error procesando ResultSet: {}", e.getMessage());
          }

        } catch (Exception e) {
          logger.error("Error ejecutando funci?n de base de datos: {}", e.getMessage());
        }
        return informeRecomendacionDTO;

      });

    } catch (Exception e) {
      logger.error("Error al obtener recomendaciones por informe: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Valida si ya se paso la fecha maxima de cumplimiento de la recomendacion
   * true: Fecha pasado
   * false: Fecha vigente
   * 
   * @param arere_codigo Codigo de la recomendaci?n.
   * @return el resultado booleano.
   */
  @Transactional
  public boolean validarFechaMaxima(Long arere_codigo) {
    Recomendacion recomendacion = recomendacionRepositorio.findById(arere_codigo).orElse(null);
    if (recomendacion == null) {
      return false;
    }
    Date fechaEval = recomendacion.getArere_fecha_maxima();
    try {
      String sqlProrroga = "SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM ROOTSISREC.AI_REC_SOLICITUDES S " +
          "WHERE S.ARERE_CODIGO = ? AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'";
      Date fechaProrroga = jdbcTemplate.queryForObject(sqlProrroga, Date.class, arere_codigo);
      if (fechaProrroga != null && (fechaEval == null || fechaProrroga.after(fechaEval))) {
        fechaEval = fechaProrroga;
      }
    } catch (Exception ex) {
      logger.warn("Error al consultar prórroga para validarFechaMaxima(Long): {}", ex.getMessage());
    }
    if (fechaEval == null) {
      return false;
    }
    Date ahora = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(fechaEval);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 999);
    Date fechaMaximaFinDeDia = cal.getTime();

    return !fechaMaximaFinDeDia.after(ahora);
  }

  @Transactional
  public boolean validarFechaMaxima(DetalleRecomendacion det) {
    if (det == null) {
      return false;
    }
    // Debe tomar SIEMPRE la fecha individual del responsable
    Date fechaEval = det.getAredr_fecha_maxima();

    // Si tiene una prórroga aprobada específica para este responsable, tomar la fecha de prórroga si es más reciente
    if (det.getRecomendacion() != null && det.getRecomendacion().getArere_codigo() != null && det.getResponsable() != null) {
      try {
        String sqlProrroga = "SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM ROOTSISREC.AI_REC_SOLICITUDES S " +
            "WHERE S.ARERE_CODIGO = ? AND S.ARERS_CODIGO = ? AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'";
        Date fechaProrroga = jdbcTemplate.queryForObject(sqlProrroga, Date.class,
            det.getRecomendacion().getArere_codigo(), det.getResponsable().getArers_codigo());
        if (fechaProrroga != null && (fechaEval == null || fechaProrroga.after(fechaEval))) {
          fechaEval = fechaProrroga;
        }
      } catch (Exception ex) {
        logger.warn("Error al consultar prórroga para validarFechaMaxima(Detalle): {}", ex.getMessage());
      }
    }

    // Fallback: si el detalle no tiene fecha individual configurada (datos históricos), toma la de la recomendación
    if (fechaEval == null && det.getRecomendacion() != null) {
      fechaEval = det.getRecomendacion().getArere_fecha_maxima();
    }

    if (fechaEval == null) {
      return false;
    }
    Date ahora = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(fechaEval);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 999);
    return !cal.getTime().after(ahora);
  }

  @Transactional
  public boolean validarFechaMaxima(Long arere_codigo, Long aredr_codigo) {
    if (aredr_codigo != null) {
      DetalleRecomendacion det = detalleRepositorio.findById(aredr_codigo).orElse(null);
      if (det != null) {
        return validarFechaMaxima(det);
      }
    }
    return validarFechaMaxima(arere_codigo);
  }

  /**
   * M?todo para actualizar fecha maxima de cumplimiento realizado por el auditor.
   * 
   * @param arere_codigo Codigo de la recomendaci?n.
   * @param acMaxDTO     Parametros como la fecha nueva.
   * @return el resultado es un JSON.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarFechaMax(Long arere_codigo,
      ActualizarFechaMaxDTO acMaxDTO) {
    return actualizarFechaMax(arere_codigo, acMaxDTO, false);
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarFechaMax(Long arere_codigo,
      ActualizarFechaMaxDTO acMaxDTO, boolean esDesdeSolicitud) {
    try {
      // Validar si existe recomendacion
      if (!recomendacionRepositorio.existsById(arere_codigo)) {
        return EstadoRespuestas.noEncontrado("Recomendación no encontrada");
      }

      if (validarEstadoSeg(EstadoSegEnum.CUMPLIDA.getCodigo(), arere_codigo)) {
        return EstadoRespuestas
            .respuestaError("Recomendación cumplida, no puede cambiar la fecha máxima de cumplimiento");
      }
      // Obtener recomendacion
      Recomendacion recomendacionOld = recomendacionRepositorio.findById(arere_codigo).get();
      Date fechaMaxAnt = recomendacionOld.getArere_fecha_maxima();
      Date nuevaFechaMax = DateMapper.convertirFechaFormato(acMaxDTO.getArere_fecha_maxima());

      String responsableNombre = "Sin asignar";
      DetalleRecomendacion targetDetail = null;
      if (acMaxDTO.getAredr_codigo() != null) {
        targetDetail = detalleRepositorio.findById(acMaxDTO.getAredr_codigo()).orElse(null);
      } else if (acMaxDTO.getArers_codigo() != null) {
        targetDetail = detalleRepositorio.findByRecomendacionResponsable(arere_codigo, acMaxDTO.getArers_codigo());
      }

      if (targetDetail != null && targetDetail.getAredr_fecha_maxima() != null) {
        fechaMaxAnt = targetDetail.getAredr_fecha_maxima();
      }

      if (nuevaFechaMax != null && fechaMaxAnt != null) {
        if (DateMapper.toLocalDate(nuevaFechaMax).isBefore(DateMapper.toLocalDate(fechaMaxAnt))) {
          return EstadoRespuestas.respuestaError(
              "La nueva fecha no puede ser anterior a la fecha máxima actual ("
                  + DateMapper.convertirFechaFormato(fechaMaxAnt) + "). Debe ser posterior.");
        }
      }

      if (targetDetail != null) {
        targetDetail.setAredr_fecha_maxima(nuevaFechaMax);
        detalleRepositorio.saveAndFlush(targetDetail);
        if (targetDetail.getResponsable() != null) {
          responsableNombre = targetDetail.getResponsable().getArers_nombre() + " " + targetDetail.getResponsable().getArers_apellido();
          try {
            jdbcTemplate.update(
                "UPDATE ROOTSISREC.AI_REC_SOLICITUDES S SET S.ARESO_FECHA_MAX_NUE = ? " +
                "WHERE S.ARERE_CODIGO = ? AND S.ARERS_CODIGO = ? AND S.ARESO_ESTADO = '1' " +
                "AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_FECHA_MAX_NUE < ?",
                nuevaFechaMax, arere_codigo, targetDetail.getResponsable().getArers_codigo(), nuevaFechaMax);
          } catch (Exception e) {
            logger.warn("Error al sincronizar prórroga en actualizarFechaMax: {}", e.getMessage());
          }
        }
        // Recalcular la fecha máxima global de la recomendación como el MAX de todos sus detalles activos
        Date fechaMaxGlobal = nuevaFechaMax;
        List<DetalleRecomendacion> todosDetalles = detalleRepositorio.findByRecomendacion(arere_codigo);
        if (todosDetalles != null) {
          for (DetalleRecomendacion d : todosDetalles) {
            if (d.getAredr_estado() != null && (d.getAredr_estado() == '1' || "1".equals(String.valueOf(d.getAredr_estado())))) {
              Date fDet = d.getAredr_fecha_maxima();
              if (fDet != null && (fechaMaxGlobal == null || fDet.after(fechaMaxGlobal))) {
                fechaMaxGlobal = fDet;
              }
            }
          }
        }
        recomendacionOld.setArere_fecha_maxima(fechaMaxGlobal);
        recomendacionRepositorio.saveAndFlush(recomendacionOld);
      } else {
        List<DetalleRecomendacion> detalles = detalleRepositorio.findByRecomendacion(arere_codigo);
        if (detalles != null) {
          for (DetalleRecomendacion det : detalles) {
            if (det.getAredr_estado() != null && (det.getAredr_estado() == '1' || "1".equals(String.valueOf(det.getAredr_estado())))) {
              det.setAredr_fecha_maxima(nuevaFechaMax);
              detalleRepositorio.save(det);
              if (det.getResponsable() != null && "Sin asignar".equals(responsableNombre)) {
                responsableNombre = det.getResponsable().getArers_nombre() + " " + det.getResponsable().getArers_apellido();
              }
            }
          }
          detalleRepositorio.flush();
          try {
            jdbcTemplate.update(
                "UPDATE ROOTSISREC.AI_REC_SOLICITUDES S SET S.ARESO_FECHA_MAX_NUE = ? " +
                "WHERE S.ARERE_CODIGO = ? AND S.ARESO_ESTADO = '1' " +
                "AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_FECHA_MAX_NUE < ?",
                nuevaFechaMax, arere_codigo, nuevaFechaMax);
          } catch (Exception e) {
            logger.warn("Error al sincronizar prórrogas globales en actualizarFechaMax: {}", e.getMessage());
          }
        }
        recomendacionOld.setArere_fecha_maxima(nuevaFechaMax);
        recomendacionRepositorio.saveAndFlush(recomendacionOld);
      }

      recalcularEstadoRecomendacion(arere_codigo);
      Recomendacion respuesta = recomendacionRepositorio.findById(arere_codigo).get();

      if (!esDesdeSolicitud) {
        if ("Sin asignar".equals(responsableNombre)) {
          try {
            List<DetalleRecomendacion> detalles = detalleRepositorio.findByRecomendacion(arere_codigo);
            if (detalles != null) {
              DetalleRecomendacion activeDetail = detalles.stream()
                  .filter(d -> d.getAredr_estado() != null && (d.getAredr_estado() == '1' || "1".equals(String.valueOf(d.getAredr_estado()))))
                  .findFirst()
                  .orElse(null);
              if (activeDetail != null && activeDetail.getResponsable() != null) {
                responsableNombre = activeDetail.getResponsable().getArers_nombre() + " " + activeDetail.getResponsable().getArers_apellido();
              }
            }
          } catch (Exception ex) {
            logger.warn("Error al buscar responsable activo para el historial: {}", ex.getMessage());
          }
        }

        String auditorUsuario = acMaxDTO.getAuditoria() != null ? acMaxDTO.getAuditoria().getUsuario() : "AUDITOR";
        String auditorNombre = historialServicio.obtenerNombreCompletoPorUsuario(auditorUsuario);

        historialServicio.registrarEvento(
            arere_codigo,
            "PRORROGA",
            "Fecha cambiada manualmente por la auditora: " + auditorNombre + ". Nueva fecha: " + 
            DateMapper.convertirFechaFormato(nuevaFechaMax) + 
            " (Anterior: " + DateMapper.convertirFechaFormato(fechaMaxAnt) + "). Responsable afectado: " + responsableNombre + ".",
            null, responsableNombre,
            fechaMaxAnt,
            nuevaFechaMax,
            auditorNombre, null,
            auditorUsuario
        );

        Integer anioFechaCamb = extraerAnio(recomendacionOld.getArere_fecha_inicio_control());
        if (anioFechaCamb == null) anioFechaCamb = Calendar.getInstance().get(Calendar.YEAR);
        Informe informeFecha = informeServicio.obtenerInformeByRecomendacion(arere_codigo);
        if (informeFecha != null) {
          // Notificar a los Responsables
          try {
            List<DetalleRecomendacion> detallesNotif = detalleRepositorio.findByRecomendacion(arere_codigo);
            if (detallesNotif != null) {
              for (DetalleRecomendacion det : detallesNotif) {
                if (det.getAredr_estado() != null && (det.getAredr_estado() == '1') && det.getResponsable() != null) {
                  // Si se especificó un responsable puntual (targetDetail), solo notificar a ese responsable
                  if (targetDetail != null && !targetDetail.getAredr_codigo().equals(det.getAredr_codigo())) {
                    continue;
                  }
                  // Si el responsable ya justificó (CUMPLIDA), no necesita notificación de cambio de fecha
                  try {
                    Integer tieneJustificado = jdbcTemplate.queryForObject(
                        "SELECT COUNT(1) FROM AI_REC_VERIFICABLES WHERE AREDR_CODIGO = ? AND AREVE_ESTADO = '1' AND AREEV_CODIGO = 2",
                        Integer.class, det.getAredr_codigo());
                    if (tieneJustificado != null && tieneJustificado > 0) {
                      continue;
                    }
                  } catch (Exception eJust) {
                    // ignorar
                  }
                  String respUsuarioPer = det.getResponsable().getArers_usuario_per();
                  if (respUsuarioPer != null && !respUsuarioPer.trim().isEmpty()) {
                    notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
                        "Fecha de cumplimiento modificada",
                        "El auditor " + auditorNombre + " ha modificado la fecha de cumplimiento de la recomendaci\u00f3n " +
                        recomendacionOld.getArere_num_rec() + " del informe " + informeFecha.getAinin_memorando(),
                        TipoNotificacionEnum.RECOMENDACION_FECHA_CAMBIADA,
                        TipoNotificacionEnum.RECOMENDACION_FECHA_CAMBIADA,
                        anioFechaCamb,
                        informeFecha.getAinin_codigo(),
                        arere_codigo,
                        respUsuarioPer.trim()));
                  }
                }
              }
            }
          } catch (Exception eResp) {
            logger.warn("No se pudo notificar a responsables sobre cambio de fecha: {}", eResp.getMessage());
          }

          // Notificar a los Auditores Jefe
          List<String> auditoresJefeFecha = rolUsuarioRepositorio.getCuentasAuditoresJefe();
          if (auditoresJefeFecha != null) {
            for (String aj : auditoresJefeFecha) {
              if (aj == null || aj.trim().isEmpty()) continue;
              notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
                  "Fecha de cumplimiento modificada",
                  "El auditor " + auditorNombre + " ha modificado la fecha de cumplimiento de la recomendaci\u00f3n " +
                  recomendacionOld.getArere_num_rec() + " del informe " + informeFecha.getAinin_memorando(),
                  TipoNotificacionEnum.RECOMENDACION_FECHA_CAMBIADA,
                  TipoNotificacionEnum.RECOMENDACION_FECHA_CAMBIADA,
                  anioFechaCamb,
                  informeFecha.getAinin_codigo(),
                  arere_codigo,
                  aj.trim()));
            }
          }
        }
      }

      // registrar bitacora
      bitacoraServicio.registrarBitacora(respuesta, acMaxDTO.getAuditoria(),
          "ACTUALIZAR FECHA MAXIMA");
      return EstadoRespuestas.respuestaCreacion("Fecha máxima de cumplimiento actualizada exitosamente");
    } catch (Exception e) {
      logger.error("Error al actualizar fecha máxima de cumplimiento: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar fecha máxima de cumplimiento : " + e);
    }
  }

  private boolean estaFechaMaximaVencida(Date fechaMaxima) {
    if (fechaMaxima == null) {
      return false;
    }
    Calendar hoyInicio = Calendar.getInstance();
    hoyInicio.set(Calendar.HOUR_OF_DAY, 0);
    hoyInicio.set(Calendar.MINUTE, 0);
    hoyInicio.set(Calendar.SECOND, 0);
    hoyInicio.set(Calendar.MILLISECOND, 0);

    Calendar fechaMax = Calendar.getInstance();
    fechaMax.setTime(fechaMaxima);
    fechaMax.set(Calendar.HOUR_OF_DAY, 0);
    fechaMax.set(Calendar.MINUTE, 0);
    fechaMax.set(Calendar.SECOND, 0);
    fechaMax.set(Calendar.MILLISECOND, 0);

    // Regla de vencido: fecha_maxima < hoy (misma l?gica que TRUNC(fecha) <
    // TRUNC(SYSDATE))
    return fechaMax.getTime().before(hoyInicio.getTime());
  }

  /**
   * M?todo para obtener detalle de la recomendacion en este caso informaci?n del
   * informe y de la recomendaci?n.
   * 
   * @param arere_codigo Codigo de la recomendaci?n.
   * @return el resultado es un JSON.
   */
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDetalle(Long arere_codigo) {
    try {
      Recomendacion recomendacion = recomendacionRepositorio.findById(arere_codigo).get();

      Informe informe = informeServicio.obtenerInformeByRecomendacion(arere_codigo);
      if (informe == null) {
        try {
          String sqlInfId = "SELECT I.AININ_CODIGO FROM AI_INF_INFORMES I " +
              "JOIN AI_REC_HALLAZGOS H ON I.AININ_CODIGO = H.AININ_CODIGO " +
              "JOIN AI_REC_CONCLUSIONES C ON H.AREHA_CODIGO = C.AREHA_CODIGO " +
              "JOIN AI_REC_RECOMENDACIONES R ON C.ARECO_CODIGO = R.ARECO_CODIGO " +
              "WHERE R.ARERE_CODIGO = ?";
          Long aininCodigoDirecto = jdbcTemplate.queryForObject(sqlInfId, Long.class, arere_codigo);
          if (aininCodigoDirecto != null) {
            informe = informeServicio.obtenerInforme(aininCodigoDirecto);
          }
        } catch (Exception ex) {
          logger.warn("No se pudo obtener informe por consulta directa para recomendacion {}: {}", arere_codigo, ex.getMessage());
        }
      }

      Conclusion conclusion = conclusionServicio.obtenerConclusionByRecomendacion(arere_codigo);

      // Extraer valores del informe
      String ainoc_descripcion = (informe != null && informe.getOrganismoControl() != null)
          ? informe.getOrganismoControl().getAinoc_descripcion()
          : "";
      String ainin_memorando = (informe != null && informe.getAinin_memorando() != null) ? informe.getAinin_memorando() : "";
      String ainin_memo_aprobacion = (informe != null && informe.getAinin_memo_aprobacion() != null) ? informe.getAinin_memo_aprobacion()
          : "";
      String ainin_titulo = (informe != null && informe.getAinin_titulo() != null) ? informe.getAinin_titulo() : "";
      String ainin_orden_trabajo = (informe != null && informe.getAinin_orden_trabajo() != null) ? informe.getAinin_orden_trabajo() : "";
      Long ainin_codigo = (informe != null) ? informe.getAinin_codigo() : null;

      if (ainin_memorando.isEmpty()) {
        try {
          String sqlDirectInf = "SELECT I.AININ_CODIGO, I.AININ_MEMORANDO, I.AININ_TITULO, I.AININ_MEMO_APROBACION, I.AININ_ORDEN_TRABAJO " +
              "FROM AI_INF_INFORMES I " +
              "JOIN AI_REC_HALLAZGOS H ON I.AININ_CODIGO = H.AININ_CODIGO " +
              "JOIN AI_REC_CONCLUSIONES C ON H.AREHA_CODIGO = C.AREHA_CODIGO " +
              "JOIN AI_REC_RECOMENDACIONES R ON C.ARECO_CODIGO = R.ARECO_CODIGO " +
              "WHERE R.ARERE_CODIGO = ?";
          final String[] arr = new String[4];
          final Long[] arrId = new Long[1];
          jdbcTemplate.query(sqlDirectInf, rs -> {
            arrId[0] = rs.getLong("AININ_CODIGO");
            arr[0] = rs.getString("AININ_MEMORANDO");
            arr[1] = rs.getString("AININ_TITULO");
            arr[2] = rs.getString("AININ_MEMO_APROBACION");
            arr[3] = rs.getString("AININ_ORDEN_TRABAJO");
          }, arere_codigo);
          if (arr[0] != null && !arr[0].isEmpty()) {
            ainin_memorando = arr[0];
            if (ainin_titulo.isEmpty() && arr[1] != null) ainin_titulo = arr[1];
            if (ainin_memo_aprobacion.isEmpty() && arr[2] != null) ainin_memo_aprobacion = arr[2];
            if (ainin_orden_trabajo.isEmpty() && arr[3] != null) ainin_orden_trabajo = arr[3];
            if (ainin_codigo == null) ainin_codigo = arrId[0];
          }
        } catch (Exception ex) {
          logger.warn("No se pudo obtener datos de informe por consulta fallback para arere_codigo {}: {}", arere_codigo, ex.getMessage());
        }
      }

      String responsablesStr = recomendacionRepositorio.obtenerResponsablesPorRecomendacion(arere_codigo);
      if (responsablesStr == null) responsablesStr = "";

      // Calcular progreso de la recomendación y del informe (SQL directo, evita stored procedure que recorre todos los registros)
      float recProgress = 0.0f;
      double infProgress = 0.0;
      
      try {
        String sqlRecProg = "SELECT " +
            "  COALESCE(" +
            "    (SUM(CASE WHEN V.AREEV_CODIGO = 2 THEN 1 ELSE 0 END) * 1.0) / NULLIF(COUNT(DR.AREDR_CODIGO), 0)," +
            "    0" +
            "  ) AS PROGRESO " +
            "FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION DR " +
            "JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON DR.ARERS_CODIGO = R.ARERS_CODIGO " +
            "LEFT JOIN (" +
            "  SELECT AREDR_CODIGO, AREEV_CODIGO FROM (" +
            "    SELECT AREDR_CODIGO, AREEV_CODIGO, ROW_NUMBER() OVER (" +
            "      PARTITION BY AREDR_CODIGO " +
            "      ORDER BY " +
            "        CASE " +
            "          WHEN AREEV_CODIGO = 2 THEN 1 " +
            "          WHEN AREEV_CODIGO = 1 THEN 2 " +
            "          WHEN AREEV_CODIGO = 3 THEN 3 " +
            "          ELSE 4 " +
            "        END ASC, " +
            "        NVL(AREVE_FECHA_CARGA, AREVE_FECHA) DESC NULLS LAST, " +
            "        AREVE_CODIGO DESC" +
            "    ) as rn " +
            "    FROM ROOTSISREC.AI_REC_VERIFICABLES " +
            "    WHERE AREVE_ESTADO = '1' " +
            "  ) WHERE rn = 1 " +
            ") V ON DR.AREDR_CODIGO = V.AREDR_CODIGO " +
            "WHERE DR.ARERE_CODIGO = ? " +
            "  AND DR.AREDR_ESTADO = 1 " +
            "  AND R.ARERS_ESTADO = 1";
        Double res = jdbcTemplate.queryForObject(sqlRecProg, Double.class, arere_codigo);
        if (res != null) {
          recProgress = res.floatValue();
        }
      } catch (Exception e) {
        logger.error("Error calculando progreso recomendación: {}", e.getMessage());
      }
      
      if (ainin_codigo != null) {
        try {
          String sqlInfProg = "SELECT " +
              "  COALESCE(" +
              "    (SUM(CASE WHEN REC.AREES_CODIGO = 2 THEN 1 ELSE 0 END) * 1.0) / NULLIF(COUNT(DISTINCT REC.ARERE_CODIGO), 0)," +
              "    0" +
              "  ) AS PROGRESO_INF " +
              "FROM ROOTSISREC.AI_REC_RECOMENDACIONES REC " +
              "JOIN ROOTSISREC.AI_REC_CONCLUSIONES CON ON REC.ARECO_CODIGO = CON.ARECO_CODIGO " +
              "JOIN ROOTSISREC.AI_REC_HALLAZGOS HALL ON CON.AREHA_CODIGO = HALL.AREHA_CODIGO " +
              "WHERE HALL.AININ_CODIGO = ? " +
              "  AND REC.ARERE_ESTADO = 1";
          Double res = jdbcTemplate.queryForObject(sqlInfProg, Double.class, ainin_codigo);
          if (res != null) {
            infProgress = res;
          }
        } catch (Exception e) {
          logger.error("Error calculando progreso informe: {}", e.getMessage());
        }
      }

      RecomendacionDTO recomendacionDTO = recomendacionMapper.convertirModelDTO(recomendacion);
      if (recomendacionDTO == null) {
        throw new IllegalStateException("No se pudo construir el detalle de la recomendacion " + arere_codigo);
      }
      // Obtener datos actuales para el historial desde su fuente de almacenamiento.
      recomendacionDTO.setArere_riesgo_verificado(recomendacion.getArere_riesgo_verificado());
      recomendacionDTO.setArere_evidencias_requeridas(evidenciaRequeridaServicio.listar(arere_codigo));
      recomendacionDTO.setArere_evidencia_requerida(
          recomendacionDTO.getArere_evidencias_requeridas().isEmpty() ? null
              : evidenciaRequeridaServicio.serializar(recomendacionDTO.getArere_evidencias_requeridas()));
      if (recomendacionDTO != null) {
        try {
          String sqlProrroga = "SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM ROOTSISREC.AI_REC_SOLICITUDES S " +
              "WHERE S.ARERE_CODIGO = ? AND S.ARESO_ESTADO_APROB = '3' AND S.ARESO_ESTADO = '1'";
          Date fechaProrroga = jdbcTemplate.queryForObject(sqlProrroga, Date.class, arere_codigo);
          Date fechaRec = recomendacion.getArere_fecha_maxima();
          if (fechaProrroga != null && (fechaRec == null || fechaProrroga.after(fechaRec))) {
            recomendacionDTO.setArere_fecha_maxima(ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper.convertirFechaFormato(fechaProrroga));
            recomendacionDTO.setPlazo(ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper.calcularDias(fechaProrroga));
          }
        } catch (Exception ex) {
          logger.warn("Error al consultar prórroga para detalle: {}", ex.getMessage());
        }
      }
      if (recomendacionDTO != null && (recomendacionDTO.getArere_fecha_inicio_control() == null || recomendacionDTO.getArere_fecha_inicio_control().trim().isEmpty())) {
        Date fechaAprob = informe != null && informe.getAinin_fecha_aprobacion() != null ? informe.getAinin_fecha_aprobacion() : (informe != null ? informe.getAinin_fecha() : null);
        if (fechaAprob != null) {
          recomendacionDTO.setArere_fecha_inicio_control(ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper.convertirFechaFormato(fechaAprob));
        }
      }
      recomendacionDTO.setArere_progreso(recProgress);
      double progresoInfVal = infProgress;

      InformeRecomendacionDTO informeRecomendacionDTO = new InformeRecomendacionDTO(
          ainin_codigo,
          ainin_orden_trabajo,
          ainin_titulo,
          ainoc_descripcion,
          recomendacionDTO,
          ainin_memorando,
          ainin_memo_aprobacion,
          (informe != null && informe.getAinin_usuario() != null) ? informe.getAinin_usuario() : "",
          responsablesStr,
          progresoInfVal);

      return EstadoRespuestas.respuestaObtencion("Detalle recomendaci?n obtenido exitosamente",
          informeRecomendacionDTO);
    } catch (Exception e) {
      logger.error("Error al obtener detalle recomendacion: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener detalle recomendacion : " + e.getMessage());
    }

  }

  /**
   * M?todo para obtener las recomendaciones con fecha de cumplimiento vencido
   * 
   * @return el resultado es un lista de recomendaciones vencidas.
   */
  public List<Recomendacion> obtenerRecomendacionesVencidas() {
    try {
      List<Recomendacion> recomendacionesVencidas = recomendacionRepositorio.findRecomendacionesVencidas(
          DateMapper.convertirFechaFormato(new Date()), EstadoSegEnum.NO_CUMPLIDA.getCodigo(),
          EstadoSegEnum.CUMPLIDA.getCodigo());
      return recomendacionesVencidas;
    } catch (Exception e) {
      logger.error("Error al obtener las recomendaciones vencidas: {}", e.getMessage());
      return null;
    }
  }

  /**
   * M?todo para actualizar el estado de seguimiento de la recomendaci?n
   * 
   * @param ingresa la recomendaci?n
   * @return el resultado es un valor booleano
   */
  public boolean actualizarEstadoVencido(Recomendacion recomendacion) {
    try {
      recomendacion.setEstadoSeg(estadoSegRepositorio.findById(EstadoSegEnum.NO_CUMPLIDA.getCodigo()).get());
      recomendacionRepositorio.save(recomendacion);
      return true;
    } catch (Exception e) {
      logger.error("Error al actualizar estado vencido: {}", e.getMessage());
      return false;
    }
  }

  public boolean validarEstadoSeg(Long arees_codigo, Long arere_codigo) {
    Recomendacion recomendacion = recomendacionRepositorio.findById(arere_codigo).get();
    EstadoSeg estadoSeg = estadoSegRepositorio.findById(arees_codigo).get();
    if (!estadoSeg.equals(recomendacion.getEstadoSeg())) {
      return false;
    }
    return true;
  }

  public Recomendacion obtenerPorVerificable(Long areve_codigo) {
    Recomendacion recomendacion = recomendacionRepositorio.findByVerificable(areve_codigo);
    return recomendacion;
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarResponsable(
      ActualizarResponsableDTO actualizarResponsableDTO) {
    try {

      // 1. Asegurar que el nuevo responsable tenga rol de RESPONSABLE (código 4)
      rolUsuarioServicio.asegurarRolResponsable(actualizarResponsableDTO.getResponsable(), actualizarResponsableDTO.getAuditoria());

      // 2. Cambiar estado de responsable anterior
      responsableServicio.reemplazar(
          new EliminarDTO(actualizarResponsableDTO.getAuditoria()), actualizarResponsableDTO.getArers_codigo(),
          actualizarResponsableDTO.getResponsable());

      // Obtener detalle recomendación
      DetalleRecomendacion detalleOld = detalleServicio.obtenerPorResponsableRecomendacion(
          actualizarResponsableDTO.getArere_codigo(), actualizarResponsableDTO.getArers_codigo());

      // Cambiar estado de detalle recomendación responsable
      detalleServicio.eliminarLogico(new EliminarDTO(actualizarResponsableDTO.getAuditoria()),
          detalleOld.getAredr_codigo());

      // Obtener Recomendación
      Recomendacion recomendacion = recomendacionRepositorio.findById(actualizarResponsableDTO.getArere_codigo()).get();
      if (recomendacion.getEstadoSeg() != null && EstadoSegEnum.CUMPLIDA.getCodigo().equals(recomendacion.getEstadoSeg().getArees_codigo())) {
        return EstadoRespuestas.respuestaError("No se pueden reasignar responsables a una recomendación que se encuentra CUMPLIDA");
      }

      NuevoResponsableDTO responsableDTO = actualizarResponsableDTO.getResponsable();
      responsableDTO.setAuditoria(actualizarResponsableDTO.getAuditoria());
      // Registrar Responsable
      Responsable responsableNuevo = responsableServicio
          .registrarResponsable(responsableDTO);

      // Crear detalle recomendación
      DetalleRecomendacion detalleNuevo = detalleServicio.registar(
          new NuevoDetalleDTO(actualizarResponsableDTO.getAuditoria()), recomendacion, responsableNuevo);

      // Registrar en el historial de recomendaciones
      String respAntName = detalleOld.getResponsable() != null 
          ? detalleOld.getResponsable().getArers_nombre() + " " + detalleOld.getResponsable().getArers_apellido() 
          : "Sin asignar";
      String respNueName = responsableNuevo.getArers_nombre() + " " + responsableNuevo.getArers_apellido();
      String detalleHistorial = "Se reasignó el responsable. Nuevo: " + respNueName + " (Anterior: " + respAntName + ").";
      
      historialServicio.registrarEvento(
          recomendacion.getArere_codigo(),
          "ASIGNACION_RESPONSABLE",
          detalleHistorial,
          respAntName,
          respNueName,
          null, null, null, null,
          actualizarResponsableDTO.getAuditoria().getUsuario()
      );

      // Actualizar verificables si existen del anterior responsable
      verificableServicio.actualizarDetalle(detalleOld, detalleNuevo);

      // 3. Notificación en sistema (WebSocket + BD) al nuevo responsable
      try {
        String memoInforme = this.recomendacionRepositorio.obtenerMemoInforme(recomendacion.getArere_codigo());
        this.simpMessagingTemplate.convertAndSend(
            "/topic/notificacion/" + responsableNuevo.getArers_usuario_per(),
            "Tiene una nueva notificación.");
        Integer anioRecReasig = extraerAnio(recomendacion.getArere_fecha_inicio_control());
        this.notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
            "Reasignación de recomendación",
            "Se le ha reasignado como responsable de cumplimiento de la recomendación del informe con memorando "
                + memoInforme + " Num. Recomendacion: " + recomendacion.getArere_num_rec(),
            TipoNotificacionEnum.RECOMENDACION_REASIGNADA,
            TipoNotificacionEnum.RECOMENDACION_REASIGNADA,
            anioRecReasig,
            informeServicio.obtenerInformeByRecomendacion(recomendacion.getArere_codigo()).getAinin_codigo(),
            recomendacion.getArere_codigo(),
            responsableNuevo.getArers_usuario_per()));
        logger.info("[REASIGNACIÓN] Notificación enviada al nuevo responsable: {}", responsableNuevo.getArers_usuario_per());

        // Notificación para formulación y registro obligatorio del Plan de Acción (plazo máximo 48 horas)
        try {
          String descNumRec = recomendacion.getArere_num_rec() != null ? String.valueOf(recomendacion.getArere_num_rec()) : "";
          Informe infRecPlan = informeServicio.obtenerInformeByRecomendacion(recomendacion.getArere_codigo());
          String memoFinalPlan = (memoInforme != null && !memoInforme.trim().isEmpty())
              ? memoInforme.trim()
              : (infRecPlan != null && infRecPlan.getAinin_memorando() != null && !infRecPlan.getAinin_memorando().trim().isEmpty()
                  ? infRecPlan.getAinin_memorando().trim()
                  : (infRecPlan != null && infRecPlan.getAinin_memo_aprobacion() != null ? infRecPlan.getAinin_memo_aprobacion().trim() : ""));
          String mensajePlan = "Dispone de un plazo de hasta 48 horas posteriores a esta notificación para formular y registrar su Plan de Acción en el sistema SISREC para la recomendación "
              + descNumRec + (memoFinalPlan != null && !memoFinalPlan.isEmpty() ? (" del informe con memorando " + memoFinalPlan) : "") + ".";
          NuevaNotificacionDTO notifPlan = new NuevaNotificacionDTO(
              "Registro de Plan de Acción",
              mensajePlan,
              "/inicio/plan-accion-responsables",
              TipoNotificacionEnum.PLAN_ACCION_REQUERIDO,
              anioRecReasig,
              informeServicio.obtenerInformeByRecomendacion(recomendacion.getArere_codigo()).getAinin_codigo(),
              recomendacion.getArere_codigo(),
              responsableNuevo.getArers_usuario_per()
          );
          this.notificacionesServicio.registrarNotificacion(notifPlan);
          this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + responsableNuevo.getArers_usuario_per(),
              "Tiene una nueva notificación: Registro de Plan de Acción.");
        } catch (Exception ePlan) {
          logger.error("[REASIGNACIÓN] Error al registrar notificación de Plan de Acción: {}", ePlan.getMessage());
        }
      } catch (Exception e) {
        logger.error("[REASIGNACIÓN] Error al enviar notificación al nuevo responsable: {}", e.getMessage());
      }

      // 4. Envío de correo al nuevo responsable
      try {
        Informe informe = informeServicio.obtenerInformeByRecomendacion(recomendacion.getArere_codigo());
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("fecha", UtilsFunctions.getDateHour());
        variables.put("nombres", responsableNuevo.getArers_nombre());
        variables.put("apellidos", responsableNuevo.getArers_apellido());
        variables.put("titulo_informe", informe != null ? informe.getAinin_titulo() : "");
        variables.put("tituloInforme", informe != null ? informe.getAinin_titulo() : "");
        variables.put("memorando", informe != null && informe.getAinin_memorando() != null ? informe.getAinin_memorando() : "");
        variables.put("memoaprobacion", informe != null && informe.getAinin_memo_aprobacion() != null ? informe.getAinin_memo_aprobacion() : "");
        variables.put("memoAprobacion", informe != null && informe.getAinin_memo_aprobacion() != null ? informe.getAinin_memo_aprobacion() : "");
        variables.put("memorando_aprobacion", informe != null && informe.getAinin_memo_aprobacion() != null ? informe.getAinin_memo_aprobacion() : "");
        variables.put("recomendacion", recomendacion.getArere_descripcion());
        variables.put("fecha_max_cumplimiento",
            recomendacion.getArere_fecha_maxima() != null
                ? DateMapper.convertirFechaFormato(recomendacion.getArere_fecha_maxima())
                : "");
        variables.put("fechaMaxCumplimiento",
            recomendacion.getArere_fecha_maxima() != null
                ? DateMapper.convertirFechaFormato(recomendacion.getArere_fecha_maxima())
                : "");

        String correoNuevoResponsable = responsableDTO.getArers_correo();
        if (correoNuevoResponsable != null && !correoNuevoResponsable.isEmpty()) {
          emailService.sendCorreoReasignacion(correoNuevoResponsable, variables);
          logger.info("[REASIGNACIÓN] Correo de reasignación enviado a: {}", correoNuevoResponsable);
        } else {
          logger.warn("[REASIGNACIÓN] No se pudo enviar correo: el nuevo responsable no tiene correo registrado.");
        }
      } catch (Exception e) {
        logger.error("[REASIGNACIÓN] Error al enviar correo de reasignación: {}", e.getMessage());
      }

      return EstadoRespuestas.respuestaCreacion("Reasignación de responsable exitosa");

    } catch (Exception e) {
      logger.error("Error al actualizar responsable: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar responsable : " + e);
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarResponsable(
      EliminarResponsableDTO eliminarResponsableDTO, Long arere_codigo, Long arers_codigo) {
    try {
      Recomendacion recomendacion = recomendacionRepositorio.findById(arere_codigo).orElse(null);
      if (recomendacion != null && recomendacion.getEstadoSeg() != null && EstadoSegEnum.CUMPLIDA.getCodigo().equals(recomendacion.getEstadoSeg().getArees_codigo())) {
        return EstadoRespuestas.respuestaError("No se pueden eliminar responsables de una recomendación que se encuentra CUMPLIDA");
      }

      // Verificar que el responsable existe
      if (!responsableRepositorio.existsById(arers_codigo)) {
        return EstadoRespuestas.noEncontrado("Responsable no encontrado");
      }

      // Obtener el detalle de recomendaci?n para este responsable y recomendaci?n
      DetalleRecomendacion detalleRecomendacion = detalleServicio.obtenerPorResponsableRecomendacion(
          arere_codigo,
          arers_codigo);

      if (detalleRecomendacion == null) {
        return EstadoRespuestas
            .noEncontrado("No se encontr? el detalle de recomendaci?n para el responsable especificado");
      }

      // Verificar si existen verificables asociados a este detalle de recomendaci?n
      List<Verificable> verificablesAsociados = verificableRepositorio
          .findByDetalleRecomendacion(detalleRecomendacion.getAredr_codigo());

      if (!verificablesAsociados.isEmpty()) {
        return EstadoRespuestas.respuestaError(
            "No se puede eliminar el responsable con " + verificablesAsociados.size() + " verificables asociados.");
      }

      // Si no hay verificables, proceder con la eliminaci?n
      // 1. Eliminar el detalle de recomendaci?n
      detalleRepositorio.deleteById(detalleRecomendacion.getAredr_codigo());

      // 2. Eliminar el responsable
      responsableRepositorio.deleteById(arers_codigo);

      // 3. Registrar en bitácora
      try {
        bitacoraServicio.registrarBitacora(
            "Eliminación de responsable ID: " + arers_codigo +
                " y detalle de recomendación ID: " + detalleRecomendacion.getAredr_codigo(),
            eliminarResponsableDTO != null ? eliminarResponsableDTO.getAuditoria() : null,
            "ELIMINAR RESPONSABLE Y DETALLE");
      } catch (Exception bitacoraEx) {
        logger.error("Error al registrar bitácora para eliminación de responsable: {}", bitacoraEx.getMessage());
      }

      return EstadoRespuestas.respuestaEliminacion("Eliminación de responsable exitosa");
    } catch (Exception e) {
      logger.error("Error al eliminar responsable: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar responsable : " + e.getMessage());
    }
  }

  // Obtener y mapear al DTO de detalle de recomendaciones por codigo de informe

  @Transactional
  public List<DetalleRecomendacionesPorInformeDTO> obtRecomendacionesPorInformeDTO(Long ainin_codigo) {
    List<Object> rawResults = this.recomendacionRepositorio.obtenerDetalleRecomendacionesPorInformeDTO(ainin_codigo);
    return rawResults.stream()
        .map(obj -> {
          try {
            String json;
            if (obj instanceof java.sql.Clob) {
              java.sql.Clob clob = (java.sql.Clob) obj;
              json = clob.getSubString(1, (int) clob.length());
            } else {
              json = obj != null ? obj.toString() : null;
            }
            if (json == null)
              return null;
            return objectMapper.readValue(json, DetalleRecomendacionesPorInformeDTO.class);
          } catch (Exception e) {
            logger.error("Error al convertir JSON a DTO: {}", e.getMessage());
            return null;
          }
        })
        .collect(Collectors.toList());
  }

  // MAPEO DE DATOS DEL CAMBIO DE FECHA MAX DE LAS RECOMENDACIONES
  @Transactional
  public List<RecomendacionCambioFechaDTO> obtenerDetalleCambioFechaMaxCumplimientoProcesado(Long arere_codigo, Long arers_codigo) {
    List<Tuple> data = this.recomendacionRepositorio.obtenerDetalleCambioFechaMaxCumplimiento(arere_codigo, arers_codigo);
    try {
      return data.stream().map(tuple -> new RecomendacionCambioFechaDTO(
          tuple.get("nombreApellidoRS", String.class),
          tuple.get("responsableUsuarioPer", String.class),
          tuple.get("responsableCorreo", String.class),
          tuple.get("infTitulo", String.class),
          tuple.get("infMemoAprobacion", String.class),
          tuple.get("infMemorando", String.class),
          tuple.get("recFechaMaxCumplimiento", String.class),
          tuple.get("recDescripcion", String.class))).collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error al mapear datos del repositorio: {}", e.getMessage());
      return null;
    }
  }

  @Transactional
  public List<RecomendacionCambioFechaDTO> obtenerDetalleCambioFechaMaxCumplimientoProcesado(Long arere_codigo) {
    return obtenerDetalleCambioFechaMaxCumplimientoProcesado(arere_codigo, null);
  }

  // MAPEO DE DATOS DE CORREOS Y USUARIOS DE LOS RESPONSABLES CON RECOMENDACIONES
  // VENCIDAS
  @Transactional
  public List<UsuarioRecordatorioNotificacionDTO> obtenerCorreoUsuarioResponsablesRecomendacionesVencidas() {
    List<Tuple> data = this.recomendacionRepositorio.obtenerResponsablesConRecomendacionesVencidas();
    try {
      return data.stream().map(tuple -> new UsuarioRecordatorioNotificacionDTO(
          tuple.get("correo", String.class),
          tuple.get("usuario", String.class))).collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error al mapear datos del repositorio (Recomendaciones Vencidas): {}", e.getMessage());
      return null;
    }
  }

  @Transactional
  public List<DetallePlantillaCorreoRecordatorioDTO> obtenerDetalleRecomendacionesVencidas(String usuario) {
    List<Tuple> data = this.recomendacionRepositorio.obtenerRecomendacionesVencidas(usuario);
    try {
      return data.stream().map(tuple -> new DetallePlantillaCorreoRecordatorioDTO(
          tuple.get("nombres", String.class),
          tuple.get("apellidos", String.class),
          tuple.get("tituloInforme", String.class),
          tuple.get("conteoRecomendacionesPendientes", BigDecimal.class).intValue())).collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error al mapear datos del repositorio (Detalle recomendaciones vencidas): {}", e.getMessage());
      return null;
    }
  }

  public int actualizarEstadoRecVencidas() {
    int actualizadasCumplidas = this.recomendacionRepositorio.actualizarEstadoRecCumplidasAutomatico();
    this.recomendacionRepositorio.normalizarRiesgoCumplidas();
    int actualizadasVencidas = this.recomendacionRepositorio.actualizarEstadoRecVencidas();
    return actualizadasCumplidas + actualizadasVencidas;
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
  public ResponseEntity<LinkedHashMap<String, Object>> validarRecomendacion(Long arere_codigo,
      ValidarRecomendacionDTO validarRecomendacionDTO) {
    try {
      if (!recomendacionRepositorio.existsById(arere_codigo)) {
        return EstadoRespuestas.noEncontrado("Recomendacio?n no encontrado");
      }
      Recomendacion recomendacionOld = recomendacionRepositorio.findById(arere_codigo).get();
      boolean esAuditorJefe = esUsuarioAuditorJefe();
      if (!esAuditorJefe && recomendacionOld.getEstadoSeg() != null && EstadoSegEnum.CUMPLIDA.getCodigo().equals(recomendacionOld.getEstadoSeg().getArees_codigo())) {
        return EstadoRespuestas.respuestaError("No se puede modificar el estado de una recomendación que ya se encuentra CUMPLIDA");
      }
      EstadoSeg estadoSeg = estadoSegRepositorio.findById(validarRecomendacionDTO.getArees_codigo()).get();
      Recomendacion recomendacion = recomendacionMapper.convertirValidarRecomendacionDTOModel(recomendacionOld,
          estadoSeg, validarRecomendacionDTO);
      Recomendacion respuesta = recomendacionRepositorio.save(recomendacion);
      bitacoraServicio.registrarBitacora(respuesta, validarRecomendacionDTO.getAuditoria(), "VALIDAR");

      // Registrar en el historial de la recomendación
      try {
        String usuario = (validarRecomendacionDTO.getAuditoria() != null && validarRecomendacionDTO.getAuditoria().getUsuario() != null)
            ? validarRecomendacionDTO.getAuditoria().getUsuario() : "AUDITOR";
        String nombreUsuario = historialServicio.obtenerNombreCompletoPorUsuario(usuario);
        String estadoAnterior = recomendacionOld.getEstadoSeg() != null ? recomendacionOld.getEstadoSeg().getArees_descripcion() : "Sin estado";
        historialServicio.registrarEvento(
            arere_codigo,
            "CAMBIO_ESTADO_MANUAL",
            "Estado de recomendacion cambiado manualmente de \"" + estadoAnterior + "\" a \"" + estadoSeg.getArees_descripcion() + "\" por " + nombreUsuario + ".",
            null, null, null, null, nombreUsuario,
            validarRecomendacionDTO.getArere_motivo_estado(),
            usuario
        );
      } catch (Exception ex) {
        logger.warn("Error al registrar historial de cambio de estado de recomendacion: {}", ex.getMessage());
      }

      return EstadoRespuestas.respuestaCreacion("Recomendación validada exitosamente");
    } catch (Exception e) {
      logger.error("Error al actualizar estado: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar estado : " + e);
    }
  }

  @Transactional
  public void actualizarEstadoRecomendacion(Long areve_codigo) {
    try {
      Recomendacion recomendacion = recomendacionRepositorio.findByVerificable(areve_codigo);
      if (recomendacion == null) {
        logger.warn("No se encontr? recomendaci?n asociada al verificable {}", areve_codigo);
        return;
      }
      recalcularEstadoRecomendacion(recomendacion.getArere_codigo());
    } catch (Exception e) {
      logger.error("Error al actualizar estado de recomendaci?n: {}", e.getMessage());
    }
  }

  @Transactional
  public void recalcularEstadoRecomendacion(Long arere_codigo) {
    try {
      Recomendacion recomendacion = recomendacionRepositorio.findById(arere_codigo).orElse(null);
      if (recomendacion == null || recomendacion.getArere_estado() == null || recomendacion.getArere_estado() != '1') {
        return;
      }

      if (recomendacion.getEstadoSeg() != null
          && EstadoSegEnum.NO_APLICA.getCodigo().equals(recomendacion.getEstadoSeg().getArees_codigo())) {
        return;
      }

      // Sincronizar primero la fecha máxima global con el MAX de los detalles activos
      Date maxFechaDetalles = null;
      List<DetalleRecomendacion> detallesActivos = detalleRepositorio.findByRecomendacion(arere_codigo);
      if (detallesActivos != null) {
        for (DetalleRecomendacion d : detallesActivos) {
          if (d.getAredr_estado() != null && (d.getAredr_estado() == '1' || "1".equals(String.valueOf(d.getAredr_estado())))) {
            Date fDet = d.getAredr_fecha_maxima();
            if (fDet != null && (maxFechaDetalles == null || fDet.after(maxFechaDetalles))) {
              maxFechaDetalles = fDet;
            }
          }
        }
      }
      // Sincronizar la fecha máxima global de la recomendación con el MAX de los detalles activos
      if (maxFechaDetalles != null && (recomendacion.getArere_fecha_maxima() == null || maxFechaDetalles.after(recomendacion.getArere_fecha_maxima()))) {
        recomendacion.setArere_fecha_maxima(maxFechaDetalles);
        recomendacionRepositorio.saveAndFlush(recomendacion);
      }
      // Solo si algún detalle activo aún no tiene fecha individual asignada (null), inicializarlo con la fecha base
      if (recomendacion.getArere_fecha_maxima() != null && detallesActivos != null) {
        boolean detalleActualizado = false;
        for (DetalleRecomendacion d : detallesActivos) {
          if (d.getAredr_fecha_maxima() == null) {
            d.setAredr_fecha_maxima(recomendacion.getArere_fecha_maxima());
            detalleRepositorio.save(d);
            detalleActualizado = true;
          }
        }
        if (detalleActualizado) {
          detalleRepositorio.flush();
        }
      }

      int totalDetallesActivos = recomendacionRepositorio.contarDetallesActivosPorRecomendacion(arere_codigo);
      int totalDetallesJustificados = recomendacionRepositorio
          .contarDetallesActivosConUltimoVerificableJustifica(arere_codigo);

      Long nuevoEstadoCodigo;
      if (totalDetallesActivos > 0 && totalDetallesActivos == totalDetallesJustificados) {
        nuevoEstadoCodigo = EstadoSegEnum.CUMPLIDA.getCodigo();
      } else {
        // Verificar si la fecha global está vigente (>= hoy). Si es así, la recomendación macro está EN PROCESO
        boolean fechaGlobalVigente = recomendacion.getArere_fecha_maxima() != null
            && !estaFechaMaximaVencida(recomendacion.getArere_fecha_maxima());

        if (fechaGlobalVigente) {
          nuevoEstadoCodigo = EstadoSegEnum.EN_PROCESO.getCodigo();
        } else {
          // Solo si la fecha global más lejana ya venció y aún no todos justificaron, pasa a NO CUMPLIDA
          nuevoEstadoCodigo = EstadoSegEnum.NO_CUMPLIDA.getCodigo();
        }
      }

      Long estadoActualCodigo = recomendacion.getEstadoSeg() != null ? recomendacion.getEstadoSeg().getArees_codigo()
          : null;
      if (estadoActualCodigo == null || !estadoActualCodigo.equals(nuevoEstadoCodigo)) {
        String estadoAnteriorDesc = recomendacion.getEstadoSeg() != null ? recomendacion.getEstadoSeg().getArees_descripcion() : "Sin estado";
        EstadoSeg nuevoEstado = estadoSegRepositorio.findById(nuevoEstadoCodigo).get();
        recomendacion.setEstadoSeg(nuevoEstado);
        recomendacionRepositorio.save(recomendacion);
        // Registrar cambio de estado en el historial
        try {
          String auditorNombre = historialServicio.obtenerNombreAuditor(arere_codigo);
          historialServicio.registrarEvento(
            arere_codigo,
            "CAMBIO_ESTADO",
            "Estado de la recomendacion cambiado de \"" + estadoAnteriorDesc + "\" a \"" + nuevoEstado.getArees_descripcion() + "\".",
            null, null, null, null, auditorNombre, null, "SISTEMA"
          );

          // Notificar cuando la recomendación pasa a CUMPLIDA
          if (EstadoSegEnum.CUMPLIDA.getCodigo().equals(nuevoEstadoCodigo)) {
            try {
              Informe informe = informeServicio.obtenerInformeByRecomendacion(arere_codigo);
              if (informe != null && informe.getAinin_usuario() != null && !informe.getAinin_usuario().trim().isEmpty()) {
                String auditorUsuario = informe.getAinin_usuario().trim();
                String memo = informe.getAinin_memorando() != null ? informe.getAinin_memorando() : "(sin memorando)";
                Integer numRec = recomendacion.getArere_num_rec();

                simpMessagingTemplate.convertAndSend("/topic/notificacion/" + auditorUsuario, "Tiene una nueva notificacion.");
                Integer anio = recomendacion.getArere_fecha_inicio_control() != null
                    ? Calendar.getInstance().get(Calendar.YEAR) : Calendar.getInstance().get(Calendar.YEAR);
                try {
                  Calendar cal = Calendar.getInstance();
                  cal.setTime(recomendacion.getArere_fecha_inicio_control());
                  anio = cal.get(Calendar.YEAR);
                } catch (Exception e2) {}

                notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
                    "Recomendación cumplida",
                    "La recomendación " + (numRec != null ? "N° " + numRec : "") + " del informe " + memo + " ha sido marcada como CUMPLIDA.",
                    TipoNotificacionEnum.RECOMENDACION_CUMPLIDA,
                    TipoNotificacionEnum.RECOMENDACION_CUMPLIDA,
                    anio,
                    informe.getAinin_codigo(),
                    arere_codigo,
                    auditorUsuario));

                // Notificar formalmente a los responsables asignados a la recomendación
                try {
                  List<String> responsables = recomendacionRepositorio.findResponsablesByRecomendacion(arere_codigo);
                  if (responsables != null) {
                    for (String resp : responsables) {
                      if (resp != null && !resp.trim().isEmpty() && !resp.trim().equalsIgnoreCase(auditorUsuario)) {
                        simpMessagingTemplate.convertAndSend("/topic/notificacion/" + resp.trim(), "Su recomendación ha sido CUMPLIDA.");
                        notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
                            "Recomendación cumplida",
                            "La recomendación " + (numRec != null ? "N° " + numRec : "") + " del informe " + memo + " ha sido marcada como CUMPLIDA.",
                            TipoNotificacionEnum.RECOMENDACION_CUMPLIDA,
                            TipoNotificacionEnum.RECOMENDACION_CUMPLIDA,
                            anio,
                            informe.getAinin_codigo(),
                            arere_codigo,
                            resp.trim()));
                      }
                    }
                  }
                } catch (Exception eResp) {
                  logger.warn("No se pudo notificar a responsables sobre recomendación cumplida {}: {}", arere_codigo, eResp.getMessage());
                }
              }
            } catch (Exception notifEx) {
              logger.warn("No se pudo enviar notificación de recomendación cumplida para rec {}: {}", arere_codigo, notifEx.getMessage());
            }
          }
        } catch (Exception ex) {
          logger.warn("No se pudo registrar CAMBIO_ESTADO en historial para rec {}: {}", arere_codigo, ex.getMessage());
        }
      }
    } catch (Exception e) {
      logger.error("Error al recalcular estado de recomendaci?n {}: {}", arere_codigo, e.getMessage());
    }
  }
}
