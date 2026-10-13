package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe;

import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte1AIJDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte2PoRecomendacionesAIJDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte3RecomendacionesDirectorioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte4ConteoRecomendacionesEstadoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte5ConteoRecomendacionesPorDepartamentoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteHallazgos;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReportePorTipoInformeDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteRecomendacionesFechaCorteDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte6RecomendacionesVencidasDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte7AuditoresDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteGeneralAIJDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteProrrogasDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteProrrogasRealizadasDTO;

@Repository
public class ReportesAuditorInternoJefeRepository {
    private static final Logger logger = LoggerFactory.getLogger(ReportesAuditorInternoJefeRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public List<Reporte1AIJDTO> reporte1aijdto(String fechaInicio, String fechaFin, int organismoControl, int estadoSeguimiento) {
        try {
            logger.info("Iniciando reporte1aijdto con fechas: {} - {}, organismoControl: {}, estadoSeguimiento: {}", fechaInicio, fechaFin, organismoControl, estadoSeguimiento);
            
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporte1AIJ")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("organismo_control", Types.INTEGER),
                    new SqlParameter("estado_seguimiento", Types.INTEGER)
                )
                .withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fechaInicio);
            params.put("fecha_fin", fechaFin);
            params.put("organismo_control", organismoControl);
            params.put("estado_seguimiento", estadoSeguimiento);
    
            logger.info("Ejecutando procedimiento almacenado reporte1AIJ...");
            Map<String, Object> result = simpleJdbcCall.execute(params);
            logger.info("Procedimiento ejecutado exitosamente");
    
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            logger.info("JSON obtenido, longitud: {}", json != null ? json.length() : 0);

            if (json != null && json.trim().startsWith("{")) {
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
                if (json.contains("\"mensaje\"")) {
                    logger.info("Mensaje desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
            }

            if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
                logger.info("No se encontraron datos para el reporte 1");
                return new ArrayList<>();
            }

            List<Reporte1AIJDTO> resultado = this.objectMapper.readValue(json, new TypeReference<List<Reporte1AIJDTO>>() {});
            logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultado != null ? resultado.size() : 0);
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoJefeRepository): " + e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Mantiene el procedimiento existente; complementa solo riesgos de coincidencias unicas.
    private void completarRiesgosReporteSeguimiento(List<Reporte2PoRecomendacionesAIJDTO> reportes) {
        if (reportes == null || reportes.isEmpty()) return;
        String sql = "SELECT i.AININ_TITULO, r.ARERE_DESCRIPCION, i.AININ_MEMORANDO, "
            + "i.AININ_MEMO_APROBACION, r.ARERE_RIESGO_VERIFICADO "
            + "FROM ROOTSISREC.AI_REC_RECOMENDACIONES r "
            + "JOIN ROOTSISREC.AI_REC_CONCLUSIONES c ON c.ARECO_CODIGO = r.ARECO_CODIGO "
            + "JOIN ROOTSISREC.AI_REC_HALLAZGOS h ON h.AREHA_CODIGO = c.AREHA_CODIGO "
            + "JOIN ROOTSISREC.AI_INF_INFORMES i ON i.AININ_CODIGO = h.AININ_CODIGO "
            + "WHERE r.ARERE_ESTADO = '1' AND c.ARECO_ESTADO = '1' "
            + "AND h.AREHA_ESTADO = '1' AND i.AININ_ESTADO = '1'";
        Map<List<String>, String> riesgos = new HashMap<>();
        java.util.Set<List<String>> ambiguas = new java.util.HashSet<>();
        jdbcTemplate.query(sql, (org.springframework.jdbc.core.RowCallbackHandler) rs -> {
            List<String> clave = java.util.Arrays.asList(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
            if (riesgos.containsKey(clave)) ambiguas.add(clave);
            riesgos.put(clave, rs.getString(5));
        });
        for (Reporte2PoRecomendacionesAIJDTO reporte : reportes) {
            if (reporte.getRiesgoVerificado() != null) continue;
            List<String> clave = java.util.Arrays.asList(reporte.getTituloInforme(), reporte.getRecomendacion(),
                reporte.getMemorando(), reporte.getMemoAprobacion());
            if (!ambiguas.contains(clave)) reporte.setRiesgoVerificado(riesgos.get(clave));
        }
    }

    public List<Reporte2PoRecomendacionesAIJDTO> reporte2PoRecomendacionesAIJ(String fechaInicio, String fechaFin, String dpto, int estadoSeguimiento, String auditor_responsable) {
        try {
            logger.info("Iniciando reporte2PoRecomendacionesAIJ con fechas: {} - {}, dpto: {}, estadoSeguimiento: {}, auditor_responsable: {}", fechaInicio, fechaFin, dpto, estadoSeguimiento, auditor_responsable);
            
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporte2PoRecomendacionesAIJ")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("estado_seguimiento", Types.INTEGER),
                    new SqlParameter("auditor_responsable", Types.VARCHAR)
                )
                .withoutProcedureColumnMetaDataAccess();
    
            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fechaInicio);
            params.put("fecha_fin", fechaFin);
            params.put("dpto", dpto);
            params.put("estado_seguimiento", estadoSeguimiento);
            params.put("auditor_responsable", auditor_responsable);
    
            logger.info("Ejecutando procedimiento almacenado reporte2PoRecomendacionesAIJ...");
            Map<String, Object> result = simpleJdbcCall.execute(params);
            logger.info("Procedimiento ejecutado exitosamente");
            
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            logger.info("JSON obtenido, longitud: {}", json != null ? json.length() : 0);

            if (json != null && json.trim().startsWith("{")) {
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
                if (json.contains("\"mensaje\"")) {
                    logger.info("Mensaje desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
            }

            if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
                logger.info("No se encontraron datos para el reporte 2");
                return new ArrayList<>();
            }
    
            List<Reporte2PoRecomendacionesAIJDTO> resultado = this.objectMapper.readValue(json, new TypeReference<List<Reporte2PoRecomendacionesAIJDTO>>() {});
            completarRiesgosReporteSeguimiento(resultado);
            logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultado != null ? resultado.size() : 0);
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoJefeRepository): " + e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Reporte6RecomendacionesVencidasDTO> reporteRecomendacionesVencidasAIJ(String fechaInicio, String fechaFin, int estadoSeguimiento) {
        try {
            logger.info("Iniciando reporteRecomendacionesVencidasAIJ con fechas: {} - {}", fechaInicio, fechaFin);
            
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("REPORTERECOMENDACIONESVENCIDASAIJ")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("estado_seguimiento", Types.INTEGER)
                )
                .withoutProcedureColumnMetaDataAccess();
    
            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fechaInicio);
            params.put("fecha_fin", fechaFin);
            params.put("estado_seguimiento", estadoSeguimiento);
    
            logger.info("Ejecutando procedimiento almacenado...");
            Map<String, Object> result = simpleJdbcCall.execute(params);
            logger.info("Procedimiento ejecutado exitosamente");
            
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            logger.info("JSON obtenido, longitud: {}", json != null ? json.length() : 0);

            if(json != null && json.trim().startsWith("{")) {
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
                if (json.contains("\"mensaje\"")) {
                    logger.info("Mensaje desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
            }
    
            List<Reporte6RecomendacionesVencidasDTO> resultado = this.objectMapper.readValue(json, new TypeReference<List<Reporte6RecomendacionesVencidasDTO>>() {});
            logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultado != null ? resultado.size() : 0);
            if (resultado != null && !resultado.isEmpty()) {
                enriquecerFechasVencidasAIJ(resultado);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoJefeRepository): " + e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<Reporte3RecomendacionesDirectorioDTO> reporte3RecomendacionesDirectorio(String fecha_inicial, String fecha_fin, String dpto, int org_control, int estado_seguimiento, String auditor){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("obtenerRecomendacionesPorDepartamentoEnBaseEstadoSeguimiento")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("dpto", Types.VARCHAR),
                    new SqlParameter("org_control", Types.INTEGER),
                    new SqlParameter("estado_seguimiento", Types.INTEGER),
                    new SqlParameter("auditor", Types.VARCHAR)
                )
                .withoutProcedureColumnMetaDataAccess();
    
            Map<String, Object> params = new HashMap<>();
            params.put("p_json", null);
            params.put("fecha_inicial", fecha_inicial);
            params.put("fecha_fin", fecha_fin);
            params.put("dpto", dpto);
            params.put("org_control", org_control);
            params.put("estado_seguimiento", estado_seguimiento);
            params.put("auditor", auditor);
    
            Map<String, Object> result = simpleJdbcCall.execute(params);    
            
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);

            if(json.trim().startsWith("{") && json.contains("\"mensaje\"")){
                return new ArrayList<>();
            }
    
            List<Reporte3RecomendacionesDirectorioDTO> resultado = this.objectMapper.readValue(json, new TypeReference<List<Reporte3RecomendacionesDirectorioDTO>>() {});
            logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultado != null ? resultado.size() : 0);
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoJefeRepository): " + e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Reporte4ConteoRecomendacionesEstadoDTO> reporte4ConteoRecomendacionesEstado(String fecha_inicial, String fecha_fin){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporteRecomendacionesEstadoSeguimiento")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR)
                    
                ).withoutProcedureColumnMetaDataAccess();
            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fecha_inicial);
            params.put("fecha_fin", fecha_fin);

            Map<String, Object> result = simpleJdbcCall.execute(params);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob); 
            
            if(json.trim().startsWith("{") && json.contains("\"mensaje\"")){
                return new ArrayList<>();
            }

            List<Reporte4ConteoRecomendacionesEstadoDTO> resultado = this.objectMapper.readValue(json, new TypeReference<List<Reporte4ConteoRecomendacionesEstadoDTO>>() {});
            logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultado != null ? resultado.size() : 0);
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoJefeRepository): " + e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<Reporte5ConteoRecomendacionesPorDepartamentoDTO> reporte5ConteoRecomendacionesPorDepartamento(String fecha_inicial, String fecha_fin, String dpto, int org_control){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporteConteoRecomendacionesPorDpto")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("dpto", Types.VARCHAR),
                    new SqlParameter("org_control", Types.INTEGER)
                ).withoutProcedureColumnMetaDataAccess();

                Map<String, Object> params = new HashMap<>();
                params.put("fecha_inicial", fecha_inicial);
                params.put("fecha_fin", fecha_fin);
                params.put("dpto", dpto);
                params.put("org_control", org_control);

                Map<String, Object> result = simpleJdbcCall.execute(params);    

                Clob clob = (Clob) result.get("p_json");
                String json = clobToString(clob); 
                
                if(json.trim().startsWith("{") && json.contains("\"mensaje\"")){
                return new ArrayList<>();
            }

            List<Reporte5ConteoRecomendacionesPorDepartamentoDTO> resultado = this.objectMapper.readValue(json, new TypeReference<List<Reporte5ConteoRecomendacionesPorDepartamentoDTO>>() {});
            logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultado != null ? resultado.size() : 0);
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoJefeRepository): " + e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<ReporteGeneralAIJDTO> reporteGeneralAIJ(String fecha_inicial, String fecha_fin, int organismo_control,
            int estado_seguimiento, String auditor, String dpto) {
        try {
            logger.info("Iniciando reporteGeneralAIJ con fechas: {} - {}, organismo_control: {}, estado_seguimiento: {}, auditor: {}, dpto: {}",
                    fecha_inicial, fecha_fin, organismo_control, estado_seguimiento, auditor, dpto);

            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                    .withProcedureName("reporteGeneralAIJ")
                    .declareParameters(
                            new SqlOutParameter("p_json", Types.CLOB),
                            new SqlParameter("fecha_inicial", Types.VARCHAR),
                            new SqlParameter("fecha_fin", Types.VARCHAR),
                            new SqlParameter("organismo_control", Types.INTEGER),
                            new SqlParameter("estado_seguimiento", Types.INTEGER),
                            new SqlParameter("auditor", Types.VARCHAR),
                            new SqlParameter("dpto", Types.VARCHAR))
                    .withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fecha_inicial);
            params.put("fecha_fin", fecha_fin);
            params.put("organismo_control", organismo_control);
            params.put("estado_seguimiento", estado_seguimiento);
            params.put("auditor", (auditor != null && !auditor.trim().isEmpty()) ? auditor : "0");
            params.put("dpto", (dpto != null && !dpto.trim().isEmpty()) ? dpto : "0");

            Map<String, Object> result = simpleJdbcCall.execute(params);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);

            if (json != null && json.trim().startsWith("{")) {
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos (reporteGeneralAIJ): {}", json);
                    return new ArrayList<>();
                }
                if (json.contains("\"mensaje\"")) {
                    logger.info("Mensaje desde la base de datos (reporteGeneralAIJ): {}", json);
                    return new ArrayList<>();
                }
            }

            if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
                logger.info("No se encontraron datos para reporteGeneralAIJ");
                return new ArrayList<>();
            }

            List<ReporteGeneralAIJDTO> resultado = this.objectMapper.readValue(json,
                    new TypeReference<List<ReporteGeneralAIJDTO>>() {});
            logger.info("reporteGeneralAIJ procesado exitosamente, {} registros obtenidos",
                    resultado != null ? resultado.size() : 0);
            if (resultado != null) {
                enriquecerSeguimientoIndividual(resultado);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para reporteGeneralAIJ: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<ReporteGeneralAIJDTO> reportePlanesYEvidenciasAIJ(String fecha_inicial, String fecha_fin, int organismo_control,
            int estado_seguimiento, String auditor, String dpto) {
        try {
            logger.info("Iniciando reportePlanesYEvidenciasAIJ con fechas: {} - {}, organismo_control: {}, estado_seguimiento: {}, auditor: {}, dpto: {}",
                    fecha_inicial, fecha_fin, organismo_control, estado_seguimiento, auditor, dpto);

            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                    .withProcedureName("reportePlanesYEvidenciasAIJ")
                    .declareParameters(
                            new SqlOutParameter("p_json", Types.CLOB),
                            new SqlParameter("fecha_inicial", Types.VARCHAR),
                            new SqlParameter("fecha_fin", Types.VARCHAR),
                            new SqlParameter("organismo_control", Types.INTEGER),
                            new SqlParameter("estado_seguimiento", Types.INTEGER),
                            new SqlParameter("auditor_responsable", Types.VARCHAR),
                            new SqlParameter("dpto", Types.VARCHAR),
                            new SqlParameter("responsable", Types.VARCHAR))
                    .withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fecha_inicial);
            params.put("fecha_fin", fecha_fin);
            params.put("organismo_control", organismo_control);
            params.put("estado_seguimiento", estado_seguimiento);
            params.put("auditor_responsable", (auditor != null && !auditor.trim().isEmpty()) ? auditor : "0");
            params.put("dpto", (dpto != null && !dpto.trim().isEmpty()) ? dpto : "0");
            params.put("responsable", "0");

            Map<String, Object> result = simpleJdbcCall.execute(params);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);

            if (json != null && json.trim().startsWith("{")) {
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos (reportePlanesYEvidenciasAIJ): {}", json);
                    return new ArrayList<>();
                }
                if (json.contains("\"mensaje\"")) {
                    logger.info("Mensaje desde la base de datos (reportePlanesYEvidenciasAIJ): {}", json);
                    return new ArrayList<>();
                }
            }

            if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
                logger.info("No se encontraron datos para reportePlanesYEvidenciasAIJ");
                return new ArrayList<>();
            }

            List<ReporteGeneralAIJDTO> resultado = this.objectMapper.readValue(json,
                    new TypeReference<List<ReporteGeneralAIJDTO>>() {});
            logger.info("reportePlanesYEvidenciasAIJ procesado exitosamente, {} registros obtenidos",
                    resultado != null ? resultado.size() : 0);
            if (resultado != null) {
                enriquecerSeguimientoIndividual(resultado);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para reportePlanesYEvidenciasAIJ: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<ReportePorTipoInformeDTO> reportePorTipoInforme(String fecha_inicial, String fecha_fin, int tipo_informe){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reportePorTipoInforme")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("tipo_informe", Types.INTEGER)).withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fecha_inicial);
            params.put("fecha_fin", fecha_fin);
            params.put("tipo_informe", tipo_informe);

            Map<String, Object> result = simpleJdbcCall.execute(params);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);

            if(json.trim().startsWith("{") && json.contains("\"mensaje\"")){
                return new ArrayList<>();
            }
            List<ReportePorTipoInformeDTO> resultado = this.objectMapper.readValue(json, new TypeReference<List<ReportePorTipoInformeDTO>>() {});
            logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultado != null ? resultado.size() : 0);
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoJefeRepository): " + e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<ReporteHallazgos> reporteHallazgos(String fechaInicio, String fechaFin, int organismoControl, String auditor){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporteHallazgosAuditorJefe")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("organismo_control", Types.INTEGER),
                    new SqlParameter("auditor", Types.VARCHAR)
                ).withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fechaInicio);   
            params.put("fecha_fin", fechaFin);
            params.put("organismo_control", organismoControl);
            params.put("auditor", auditor);

            Map<String, Object> result = simpleJdbcCall.execute(params);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);

            if(json.trim().startsWith("{") && json.contains("\"mensaje\"")){
                return new ArrayList<>();
            }

            List<ReporteHallazgos> resultado = this.objectMapper.readValue(json, new TypeReference<List<ReporteHallazgos>>() {});
            logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultado != null ? resultado.size() : 0);
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoJefeRepository): " + e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String clobToString(Clob clob) throws SQLException, IOException {
        if (clob == null) return "";
        
        try {
            return clob.getSubString(1, (int) clob.length());
        } finally {
            // Cerrar explícitamente el Clob para liberar recursos
            if (clob != null) {
                try {
                    clob.free();
                } catch (SQLException e) {
                    logger.warn("Error al cerrar el Clob: " + e.getMessage());
                }
            }
        }
    }

    public List<ReporteRecomendacionesFechaCorteDTO> reporteRecomendacionesFechaCorte(String fecha_corte, String dtpo, int org_control){
        return reporteRecomendacionesFechaCorte(fecha_corte, fecha_corte, dtpo, org_control);
    }

    public List<ReporteRecomendacionesFechaCorteDTO> reporteRecomendacionesFechaCorte(String fecha_corte_emision, String fecha_corte_estado, String dtpo, int org_control){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporteFechaCorteRecomendaciones")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_corte_emision", Types.VARCHAR),
                    new SqlParameter("fecha_corte_estado", Types.VARCHAR),
                    new SqlParameter("dtpo", Types.VARCHAR),    
                    new SqlParameter("org_control", Types.INTEGER)
                ).withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_corte_emision", fecha_corte_emision);
            params.put("fecha_corte_estado", fecha_corte_estado);
            params.put("dtpo", dtpo);
            params.put("org_control", org_control); 

            Map<String, Object> result = simpleJdbcCall.execute(params);
            logger.info("storedprocedure: reporteFechaCorteRecomendaciones ejecutado con fecha_corte_emision: {}, fecha_corte_estado: {}, dtpo: {}, org_control: {}", fecha_corte_emision, fecha_corte_estado, dtpo, org_control);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);   

            if (json != null && json.trim().startsWith("{")) {
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
                if (json.contains("\"mensaje\"")) {
                    logger.info("Mensaje desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
            }

            if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
                logger.info("No se encontraron datos para el reporte de fecha de corte");
                return new ArrayList<>();
            }

            List<ReporteRecomendacionesFechaCorteDTO> resultado = this.objectMapper.readValue(json, new TypeReference<List<ReporteRecomendacionesFechaCorteDTO>>() {});
            logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultado != null ? resultado.size() : 0);
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoJefeRepository): " + e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Reporte7AuditoresDTO> reporte7Auditores(String fechaInicio, String fechaFin, String auditor) {
        try {
            logger.info("Iniciando reporte7Auditores con fechas: {} - {}, auditor: {}", fechaInicio, fechaFin, auditor);
            
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporte7Auditores")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("auditor", Types.VARCHAR)
                )
                .withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fechaInicio);
            params.put("fecha_fin", fechaFin);
            params.put("auditor", auditor);

            logger.info("Ejecutando procedimiento almacenado reporte7Auditores...");
            Map<String, Object> result = simpleJdbcCall.execute(params);
            logger.info("Procedimiento ejecutado exitosamente");

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            logger.info("JSON obtenido, longitud: {}", json != null ? json.length() : 0);

            if (json != null && json.trim().startsWith("{")) {
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
                if (json.contains("\"mensaje\"")) {
                    logger.info("Mensaje desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
            }

            if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
                logger.info("No se encontraron datos para el reporte 7 de auditores");
                return new ArrayList<>();
            }
            
            com.fasterxml.jackson.databind.JsonNode rootNode = this.objectMapper.readTree(json);
            
            if (rootNode.isArray()) {
                java.util.List<Reporte7AuditoresDTO> resultList = new java.util.ArrayList<>();
                
                for (com.fasterxml.jackson.databind.JsonNode node : rootNode) {
                    if (node.has("responsables") && node.get("responsables").isTextual()) {
                        String responsablesStr = node.get("responsables").asText();
                        try {
                            com.fasterxml.jackson.databind.JsonNode responsablesArray = this.objectMapper.readTree(responsablesStr);
                            ((com.fasterxml.jackson.databind.node.ObjectNode) node).set("responsables", responsablesArray);
                        } catch (Exception e) {
                            logger.warn("Error al parsear responsables como JSON: {}", e.getMessage());
                            ((com.fasterxml.jackson.databind.node.ObjectNode) node).putArray("responsables");
                        }
                    }
                    
                    Reporte7AuditoresDTO dto = this.objectMapper.treeToValue(node, Reporte7AuditoresDTO.class);
                    resultList.add(dto);
                }
                
                logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultList.size());
                return resultList;
            }
            
            List<Reporte7AuditoresDTO> resultado = this.objectMapper.readValue(json, new TypeReference<List<Reporte7AuditoresDTO>>() {});
            logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultado != null ? resultado.size() : 0);
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoJefeRepository): " + e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ReporteProrrogasDTO reporteProrrogasAIJ() {
        try {
            logger.info("Iniciando reporteProrrogasAIJ...");
            
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporteprorrogasaij")
                .declareParameters(new SqlOutParameter("p_json", Types.CLOB))
                .withoutProcedureColumnMetaDataAccess();

            Map<String, Object> result = simpleJdbcCall.execute();
            logger.info("Procedimiento reporteprorrogasaij ejecutado exitosamente");
            
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            logger.info("JSON obtenido de reporteprorrogasaij, longitud: {}", json != null ? json.length() : 0);

            if (json != null && json.trim().startsWith("{")) {
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos (reporteprorrogasaij): {}", json);
                    return new ReporteProrrogasDTO();
                }
            }

            if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
                logger.info("No se encontraron datos para el reporte de prórrogas");
                return new ReporteProrrogasDTO();
            }

            ReporteProrrogasDTO resultado = this.objectMapper.readValue(json, ReporteProrrogasDTO.class);
            logger.info("Reporte de prórrogas procesado exitosamente, {} registros obtenidos", 
                (resultado != null && resultado.getDatos() != null) ? resultado.getDatos().size() : 0);
            if (resultado != null && resultado.getDatos() != null) {
                for (ReporteProrrogasDTO.DetalleReporteProrrogaDTO dto : resultado.getDatos()) {
                    logger.info("ProrrogasReport - Rec: {}, FechaMaxima: {}, EstadoRec: {}", 
                        dto.getNumeroRecomendacion(), dto.getFechaMaxima(), dto.getEstadoRecomendacion());
                    Date date = parseFecha(dto.getFechaMaxima());
                    if (date != null) {
                        int plazo = DateMapper.calcularDias(date);
                        logger.info("ProrrogasReport - Rec: {}, Calculated Plazo: {}", dto.getNumeroRecomendacion(), plazo);
                        String estRec = dto.getEstadoRecomendacion() != null ? dto.getEstadoRecomendacion().toUpperCase().trim() : "";
                        if (estRec.equals("CUMPLIDA") || estRec.equals("NO APLICA")) {
                            dto.setEstadoPlazo("CUMPLIDA");
                            dto.setDiasVencidos(0);
                        } else if (plazo < 0) {
                            dto.setEstadoPlazo("VENCIDO");
                            dto.setDiasVencidos(Math.abs(plazo));
                            if (!estRec.equals("CUMPLIDA") && !estRec.equals("NO APLICA")) {
                                dto.setEstadoRecomendacion("NO CUMPLIDA");
                            }
                        } else {
                            dto.setEstadoPlazo("EN PLAZO");
                            dto.setDiasVencidos(plazo);
                            if (estRec.equals("NO CUMPLIDA") || estRec.equals("VENCIDO") || estRec.isEmpty()) {
                                dto.setEstadoRecomendacion("EN PROCESO");
                            }
                        }
                        logger.info("ProrrogasReport - Rec: {}, Final DiasVencidos: {}, EstadoPlazo: {}", 
                            dto.getNumeroRecomendacion(), dto.getDiasVencidos(), dto.getEstadoPlazo());
                    } else {
                        logger.warn("ProrrogasReport - Failed to parse date: {}", dto.getFechaMaxima());
                    }
                }
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para reporteProrrogasAIJ: " + e.getMessage(), e);
            return new ReporteProrrogasDTO();
        }
    }

    public ReporteProrrogasRealizadasDTO reporteProrrogasRealizadas() {
        try {
            logger.info("Iniciando reporteProrrogasRealizadas...");
            
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reportecambiosfechamanual")
                .declareParameters(new SqlOutParameter("p_json", Types.CLOB))
                .withoutProcedureColumnMetaDataAccess();

            Map<String, Object> result = simpleJdbcCall.execute();
            logger.info("Procedimiento reportecambiosfechamanual ejecutado exitosamente");
            
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            logger.info("JSON obtenido de reportecambiosfechamanual, longitud: {}", json != null ? json.length() : 0);

            if (json != null && json.trim().startsWith("{")) {
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos (reportecambiosfechamanual): {}", json);
                    return new ReporteProrrogasRealizadasDTO();
                }
            }

            if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
                logger.info("No se encontraron datos para el reporte de prórrogas realizadas");
                return new ReporteProrrogasRealizadasDTO();
            }

            ReporteProrrogasRealizadasDTO resultado = this.objectMapper.readValue(json, ReporteProrrogasRealizadasDTO.class);
            logger.info("Reporte de prórrogas realizadas procesado exitosamente, {} registros obtenidos", 
                (resultado != null && resultado.getDatos() != null) ? resultado.getDatos().size() : 0);
            
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para reporteProrrogasRealizadas: " + e.getMessage(), e);
            return new ReporteProrrogasRealizadasDTO();
        }
    }

    private Date parseFecha(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        dateStr = dateStr.trim();
        if (dateStr.length() > 10) {
            dateStr = dateStr.substring(0, 10);
        }
        String format = "yyyy-MM-dd";
        if (dateStr.contains("/")) {
            format = "dd/MM/yyyy";
        }
        try {
            return new java.text.SimpleDateFormat(format).parse(dateStr);
        } catch (Exception e) {
            logger.error("Error parsing date: " + dateStr + " using format: " + format, e);
            return null;
        }
    }

    private void enriquecerSeguimientoIndividual(List<ReporteGeneralAIJDTO> resultado) {
        if (resultado == null || resultado.isEmpty()) {
            return;
        }

        List<Long> recCodigos = resultado.stream()
                .map(ReporteGeneralAIJDTO::getCodigoRecomendacion)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (recCodigos.isEmpty()) {
            return;
        }

        Map<String, ResponsableInfoIndividual> mapaDetalles = new HashMap<>();
        int batchSize = 500;
        for (int i = 0; i < recCodigos.size(); i += batchSize) {
            List<Long> subList = recCodigos.subList(i, Math.min(i + batchSize, recCodigos.size()));
            String inClause = subList.stream().map(String::valueOf).collect(Collectors.joining(","));

            String sql = 
                "SELECT D.AREDR_CODIGO, D.ARERE_CODIGO, D.ARERS_CODIGO, " +
                "       R.ARERS_CODIGO_PER, " +
                "       TRIM(UPPER(R.ARERS_NOMBRE || ' ' || R.ARERS_APELLIDO)) AS NOMBRE_COMPLETO_1, " +
                "       TRIM(UPPER(R.ARERS_APELLIDO || ' ' || R.ARERS_NOMBRE)) AS NOMBRE_COMPLETO_2, " +
                "       D.AREDR_FECHA_MAXIMA, " +
                "       REC.ARERE_FECHA_MAXIMA AS REC_FECHA_MAXIMA, " +
                "       REC.ARERE_RIESGO_VERIFICADO AS RIESGO, " +
                "       COALESCE( " +
                "           (SELECT LISTAGG(EV.ARER_DESCRIPCION, ' | ') WITHIN GROUP (ORDER BY EV.ARER_CODIGO) " +
                "            FROM ROOTSISREC.AI_REC_EVIDENCIAS_REQUERIDAS EV " +
                "            WHERE EV.ARERE_CODIGO = D.ARERE_CODIGO AND EV.ARER_ESTADO = 'A'), " +
                "           REC.ARERE_EVIDENCIA_REQUERIDA " +
                "       ) AS EVIDENCIAS_REQUERIDAS, " +
                "       ( " +
                "           SELECT COUNT(1) " +
                "           FROM ROOTSISREC.AI_REC_PLAN_ACCION PA " +
                "           WHERE PA.ARERE_CODIGO = D.ARERE_CODIGO " +
                "             AND PA.AREDR_CODIGO = D.AREDR_CODIGO " +
                "             AND PA.ARPA_ESTADO = 'A' " +
                "       ) AS TIENE_PLAN_ACCION, " +
                "       ( " +
                "           SELECT COUNT(1) " +
                "           FROM ROOTSISREC.AI_REC_PLAN_ACTIVIDADES ACT " +
                "           JOIN ROOTSISREC.AI_REC_PLAN_ACCION PA ON PA.ARPA_CODIGO = ACT.ARPA_CODIGO " +
                "           WHERE PA.ARERE_CODIGO = D.ARERE_CODIGO " +
                "             AND PA.AREDR_CODIGO = D.AREDR_CODIGO " +
                "             AND PA.ARPA_ESTADO = 'A' " +
                "             AND ACT.ARPAC_ESTADO = 'A' " +
                "       ) AS CANT_ACTIVIDADES_PLAN, " +
                "       ( " +
                "           SELECT MAX(S.ARESO_FECHA_MAX_NUE) " +
                "           FROM ROOTSISREC.AI_REC_SOLICITUDES S " +
                "           WHERE S.ARERE_CODIGO = D.ARERE_CODIGO " +
                "             AND S.ARERS_CODIGO = D.ARERS_CODIGO " +
                "             AND S.ARESO_ESTADO = '1' " +
                "             AND S.ARESO_ESTADO_APROB = '3' " +
                "             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL " +
                "       ) AS FECHA_PRORROGA, " +
                "       ( " +
                "           SELECT COUNT(1) " +
                "           FROM ROOTSISREC.AI_REC_VERIFICABLES V " +
                "           WHERE V.AREDR_CODIGO = D.AREDR_CODIGO " +
                "             AND V.AREVE_ESTADO = '1' " +
                "             AND V.AREEV_CODIGO = 2 " +
                "       ) AS TIENE_JUSTIFICADO, " +
                "       ( " +
                "           SELECT MAX(V.AREVE_FECHA_VALIDACION) " +
                "           FROM ROOTSISREC.AI_REC_VERIFICABLES V " +
                "           WHERE V.AREDR_CODIGO = D.AREDR_CODIGO " +
                "             AND V.AREVE_ESTADO = '1' " +
                "             AND (V.AREEV_CODIGO = 2 OR V.AREVE_FECHA_VALIDACION IS NOT NULL) " +
                "       ) AS FECHA_VALIDACION_VERIF, " +
                "       ( " +
                "           SELECT MAX(V.AREVE_FECHA_CARGA) " +
                "           FROM ROOTSISREC.AI_REC_VERIFICABLES V " +
                "           WHERE V.AREDR_CODIGO = D.AREDR_CODIGO " +
                "             AND V.AREVE_ESTADO = '1' " +
                "       ) AS FECHA_CARGA_VERIF, " +
                "       ( " +
                "           SELECT MAX(V.AREVE_OBSERVACION) KEEP (DENSE_RANK LAST ORDER BY CASE WHEN V.AREEV_CODIGO = 2 THEN 2 ELSE 1 END ASC, V.AREVE_FECHA_CARGA ASC, V.AREVE_CODIGO ASC) " +
                "           FROM ROOTSISREC.AI_REC_VERIFICABLES V " +
                "           WHERE V.AREDR_CODIGO = D.AREDR_CODIGO " +
                "             AND V.AREVE_ESTADO = '1' " +
                "       ) AS OBSERVACION_VERIFICABLE, " +
                "       ( " +
                "           SELECT MAX(E.AREEV_DESCRIPCION) KEEP (DENSE_RANK LAST ORDER BY CASE WHEN V.AREEV_CODIGO = 2 THEN 2 ELSE 1 END ASC, V.AREVE_FECHA_CARGA ASC, V.AREVE_CODIGO ASC) " +
                "           FROM ROOTSISREC.AI_REC_VERIFICABLES V " +
                "           LEFT JOIN ROOTSISREC.AI_REC_ESTADO_VERIFICABLE E ON E.AREEV_CODIGO = V.AREEV_CODIGO " +
                "           WHERE V.AREDR_CODIGO = D.AREDR_CODIGO " +
                "             AND V.AREVE_ESTADO = '1' " +
                "       ) AS ESTADO_VERIFICABLE_IND " +
                "FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION D " +
                "JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARERE_CODIGO = D.ARERE_CODIGO " +
                "JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON R.ARERS_CODIGO = D.ARERS_CODIGO " +
                "WHERE D.AREDR_ESTADO = 1 AND D.ARERE_CODIGO IN (" + inClause + ")";

            try {
                jdbcTemplate.query(sql, rs -> {
                    long arere = rs.getLong("ARERE_CODIGO");
                    String n1 = rs.getString("NOMBRE_COMPLETO_1");
                    String n2 = rs.getString("NOMBRE_COMPLETO_2");
                    Date fInd = rs.getDate("AREDR_FECHA_MAXIMA");
                    Date fRec = rs.getDate("REC_FECHA_MAXIMA");
                    Date fProrroga = rs.getDate("FECHA_PRORROGA");
                    int justificado = rs.getInt("TIENE_JUSTIFICADO");

                    Date fEfectiva = fProrroga != null ? fProrroga : (fInd != null ? fInd : fRec);
                    boolean tieneCumplido = justificado > 0;
                    String riesgo = rs.getString("RIESGO");
                    String evidencias = rs.getString("EVIDENCIAS_REQUERIDAS");
                    boolean tienePlan = rs.getInt("TIENE_PLAN_ACCION") > 0;
                    int cantActividades = rs.getInt("CANT_ACTIVIDADES_PLAN");
                    Date fechaVal = rs.getTimestamp("FECHA_VALIDACION_VERIF");
                    Date fechaCarga = rs.getTimestamp("FECHA_CARGA_VERIF");
                    String obsVerif = rs.getString("OBSERVACION_VERIFICABLE");
                    String estVerif = rs.getString("ESTADO_VERIFICABLE_IND");
                    Long aredr = rs.getLong("AREDR_CODIGO");
                    Long arers = rs.getLong("ARERS_CODIGO");
                    String arersPer = rs.getString("ARERS_CODIGO_PER");

                    ResponsableInfoIndividual info = new ResponsableInfoIndividual(
                        fEfectiva, tieneCumplido, riesgo, evidencias, tienePlan, cantActividades,
                        fechaVal, fechaCarga, obsVerif, estVerif, aredr, arers, arersPer
                    );
                    if (n1 != null) mapaDetalles.put(arere + "___" + normalizarTexto(n1), info);
                    if (n2 != null) mapaDetalles.put(arere + "___" + normalizarTexto(n2), info);
                });
            } catch (Exception ex) {
                logger.error("Error al consultar enriquecimiento individual por responsable en AIJ: {}", ex.getMessage(), ex);
            }
        }

        for (ReporteGeneralAIJDTO dto : resultado) {
            Long recId = dto.getCodigoRecomendacion();
            String resp = dto.getResponsable();
            if (recId == null || resp == null) continue;

            String key = recId + "___" + normalizarTexto(resp);
            ResponsableInfoIndividual info = mapaDetalles.get(key);

            if (info == null) {
                String normResp = normalizarTexto(resp);
                for (Map.Entry<String, ResponsableInfoIndividual> entry : mapaDetalles.entrySet()) {
                    if (entry.getKey().startsWith(recId + "___")) {
                        String keyName = entry.getKey().substring((recId + "___").length());
                        if (keyName.contains(normResp) || normResp.contains(keyName)) {
                            info = entry.getValue();
                            break;
                        }
                    }
                }
            }

            if (info != null) {
                if (info.riesgo != null && !info.riesgo.trim().isEmpty()) {
                    dto.setRiesgo(info.riesgo);
                }
                if (info.evidenciaRequerida != null && !info.evidenciaRequerida.trim().isEmpty()) {
                    dto.setEvidenciaRequerida(info.evidenciaRequerida);
                }
                dto.setTienePlanAccion(info.tienePlan ? "SÍ" : "NO");
                dto.setCantidadActividadesPlan(info.cantidadActividades);
                if (info.fechaValidacion != null) {
                    dto.setFechaValidacionVerificable(DateMapper.convertirFechaFormato(info.fechaValidacion));
                }
                if (info.fechaCarga != null) {
                    dto.setFechaCargaVerificable(DateMapper.convertirFechaFormato(info.fechaCarga));
                }
                if (info.observacionVerificable != null && !info.observacionVerificable.trim().isEmpty()) {
                    dto.setObservacionAuditor(info.observacionVerificable);
                }
                if (info.estadoVerificable != null && !info.estadoVerificable.trim().isEmpty()) {
                    dto.setEstadoVerificable(info.estadoVerificable);
                } else if (dto.getEstadoVerificable() == null || dto.getEstadoVerificable().trim().isEmpty()) {
                    dto.setEstadoVerificable("SIN VERIFICABLE");
                }
                dto.setAredrCodigo(info.aredrCodigo);
                dto.setArersCodigo(info.arersCodigo);
                dto.setArersCodigoPer(info.arersCodigoPer);

                if (info.tieneCumplido) {
                    dto.setEstadoRecomendacion("CUMPLIDA");
                    dto.setEstadoPlazo("CUMPLIDA");
                    dto.setDiasVencidos(0);
                    if (info.fechaEfectiva != null) {
                        dto.setFechaVencimiento(DateMapper.convertirFechaFormato(info.fechaEfectiva));
                    }
                } else {
                    Date fechaEval = info.fechaEfectiva != null ? info.fechaEfectiva : parseFecha(dto.getFechaVencimiento());
                    if (fechaEval != null) {
                        dto.setFechaVencimiento(DateMapper.convertirFechaFormato(fechaEval));
                        int plazo = DateMapper.calcularDias(fechaEval);
                        if (plazo >= 0) {
                            dto.setEstadoRecomendacion("EN PROCESO");
                            dto.setEstadoPlazo("EN PLAZO");
                            dto.setDiasVencidos(plazo);
                        } else {
                            dto.setEstadoRecomendacion("NO CUMPLIDA");
                            dto.setEstadoPlazo("VENCIDO");
                            dto.setDiasVencidos(Math.abs(plazo));
                        }
                    }
                }
            } else {
                if (dto.getTienePlanAccion() == null) {
                    dto.setTienePlanAccion("NO");
                    dto.setCantidadActividadesPlan(0);
                }
                if (dto.getEstadoVerificable() == null || dto.getEstadoVerificable().trim().isEmpty()) {
                    dto.setEstadoVerificable("SIN VERIFICABLE");
                }
                Date date = parseFecha(dto.getFechaVencimiento());
                if (date != null) {
                    int plazo = DateMapper.calcularDias(date);
                    String estRec = dto.getEstadoRecomendacion() != null ? dto.getEstadoRecomendacion().toUpperCase().trim() : "";
                    if (estRec.equals("CUMPLIDA") || estRec.equals("NO APLICA")) {
                        dto.setEstadoPlazo("CUMPLIDA");
                        dto.setDiasVencidos(0);
                    } else if (plazo < 0) {
                        dto.setEstadoPlazo("VENCIDO");
                        dto.setDiasVencidos(Math.abs(plazo));
                        dto.setEstadoRecomendacion("NO CUMPLIDA");
                    } else {
                        dto.setEstadoPlazo("EN PLAZO");
                        dto.setDiasVencidos(plazo);
                        dto.setEstadoRecomendacion("EN PROCESO");
                    }
                }
            }
        }
    }

    private String normalizarTexto(String s) {
        if (s == null) return "";
        return s.toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    private void enriquecerFechasVencidasAIJ(List<Reporte6RecomendacionesVencidasDTO> resultado) {
        if (resultado == null || resultado.isEmpty()) {
            return;
        }

        List<Long> recCodigos = resultado.stream()
                .map(Reporte6RecomendacionesVencidasDTO::getArereCodigo)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (recCodigos.isEmpty()) {
            return;
        }

        Map<Long, Date> mapaFechasEfectivas = new HashMap<>();
        int batchSize = 500;
        for (int i = 0; i < recCodigos.size(); i += batchSize) {
            List<Long> subList = recCodigos.subList(i, Math.min(i + batchSize, recCodigos.size()));
            String inClause = subList.stream().map(String::valueOf).collect(Collectors.joining(","));

            String sql = 
                "SELECT D.ARERE_CODIGO, " +
                "       D.AREDR_FECHA_MAXIMA, " +
                "       REC.ARERE_FECHA_MAXIMA AS REC_FECHA_MAXIMA, " +
                "       ( " +
                "           SELECT MAX(S.ARESO_FECHA_MAX_NUE) " +
                "           FROM ROOTSISREC.AI_REC_SOLICITUDES S " +
                "           WHERE S.ARERE_CODIGO = D.ARERE_CODIGO " +
                "             AND S.ARERS_CODIGO = D.ARERS_CODIGO " +
                "             AND S.ARESO_ESTADO = '1' " +
                "             AND S.ARESO_ESTADO_APROB = '3' " +
                "             AND S.ARESO_FECHA_MAX_NUE IS NOT NULL " +
                "       ) AS FECHA_PRORROGA " +
                "FROM ROOTSISREC.AI_REC_DETALLE_RECOMENDACION D " +
                "JOIN ROOTSISREC.AI_REC_RECOMENDACIONES REC ON REC.ARERE_CODIGO = D.ARERE_CODIGO " +
                "JOIN ROOTSISREC.AI_REC_RESPONSABLES R ON R.ARERS_CODIGO = D.ARERS_CODIGO " +
                "WHERE D.AREDR_ESTADO = 1 AND R.ARERS_ESTADO = 1 AND D.ARERE_CODIGO IN (" + inClause + ")";

            try {
                jdbcTemplate.query(sql, rs -> {
                    long arere = rs.getLong("ARERE_CODIGO");
                    Date fInd = rs.getDate("AREDR_FECHA_MAXIMA");
                    Date fRec = rs.getDate("REC_FECHA_MAXIMA");
                    Date fProrroga = rs.getDate("FECHA_PRORROGA");

                    Date fEfectiva = fProrroga != null ? fProrroga : (fInd != null ? fInd : fRec);
                    if (fEfectiva != null) {
                        Date actualMax = mapaFechasEfectivas.get(arere);
                        if (actualMax == null || fEfectiva.after(actualMax)) {
                            mapaFechasEfectivas.put(arere, fEfectiva);
                        }
                    }
                });
            } catch (Exception ex) {
                logger.error("Error al consultar enriquecimiento de fechas vencidas en AIJ: {}", ex.getMessage(), ex);
            }
        }

        for (Reporte6RecomendacionesVencidasDTO dto : resultado) {
            Long arere = dto.getArereCodigo();
            Date fechaEfectiva = (arere != null) ? mapaFechasEfectivas.get(arere) : null;
            if (fechaEfectiva == null) {
                fechaEfectiva = parseFecha(dto.getFechaMaxima());
            }

            if (fechaEfectiva != null) {
                dto.setFechaMaxima(DateMapper.convertirFechaFormato(fechaEfectiva));
                int plazo = DateMapper.calcularDias(fechaEfectiva);
                dto.setPlazo(plazo);
            }
        }
    }

    private static class ResponsableInfoIndividual {
        final Date fechaEfectiva;
        final boolean tieneCumplido;
        final String riesgo;
        final String evidenciaRequerida;
        final boolean tienePlan;
        final int cantidadActividades;
        final Date fechaValidacion;
        final Date fechaCarga;
        final String observacionVerificable;
        final String estadoVerificable;
        final Long aredrCodigo;
        final Long arersCodigo;
        final String arersCodigoPer;

        ResponsableInfoIndividual(Date fechaEfectiva, boolean tieneCumplido, String riesgo,
                                 String evidenciaRequerida, boolean tienePlan, int cantidadActividades,
                                 Date fechaValidacion, Date fechaCarga, String observacionVerificable,
                                 String estadoVerificable,
                                 Long aredrCodigo, Long arersCodigo, String arersCodigoPer) {
            this.fechaEfectiva = fechaEfectiva;
            this.tieneCumplido = tieneCumplido;
            this.riesgo = riesgo;
            this.evidenciaRequerida = evidenciaRequerida;
            this.tienePlan = tienePlan;
            this.cantidadActividades = cantidadActividades;
            this.fechaValidacion = fechaValidacion;
            this.fechaCarga = fechaCarga;
            this.observacionVerificable = observacionVerificable;
            this.estadoVerificable = estadoVerificable;
            this.aredrCodigo = aredrCodigo;
            this.arersCodigo = arersCodigo;
            this.arersCodigoPer = arersCodigoPer;
        }
    }
}
