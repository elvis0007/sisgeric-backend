package ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable;

import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteProgresoRecomendacionesResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteRecomendacionesResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteRecomendacionesVencidasResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteVerificablesDTO;

@Repository
public class ReportesResponsableRepository {
    private static final Logger logger = LoggerFactory.getLogger(ReportesResponsableRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public List<ReporteRecomendacionesResponsableDTO> obtenerReporteRecomendacionesResponsable(String fecha_inicial, String fecha_fin, int estado_seguimiento, String usuario) {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporteRecomendacionesEstadoFechaCumplimientoResponsable")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("estado_seguimiento", Types.INTEGER),
                    new SqlParameter("usuario", Types.VARCHAR)).withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fecha_inicial);
            params.put("fecha_fin", fecha_fin);
            params.put("estado_seguimiento", estado_seguimiento);
            params.put("usuario", usuario);

            Map<String, Object> result = simpleJdbcCall.execute(params);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            if(json != null && json.trim().startsWith("{")){
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos en reporteRecomendacionesEstadoFechaCumplimientoResponsable: {}", json);
                    return new ArrayList<>();
                }
                if (json.contains("\"mensaje\"")) {
                    logger.info("Mensaje desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
            }
            return this.objectMapper.readValue(json, new TypeReference<List<ReporteRecomendacionesResponsableDTO>>() {});
        } catch (Exception e) { 
            logger.error("Error al obtener datos para los reportes. (ReportesResponsableRepository): " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<ReporteProgresoRecomendacionesResponsableDTO> obtenerReporteProgresoRecomendacionesResponsable(String fecha_inicial, String fecha_fin, String usuario) {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporteProgresoRecomendacionesResponsable")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("usuario", Types.VARCHAR)).withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fecha_inicial); 
            params.put("fecha_fin", fecha_fin);
            params.put("usuario", usuario);

            Map<String, Object> result = simpleJdbcCall.execute(params);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            
            if(json != null && json.trim().startsWith("{")){
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos en reporteProgresoRecomendacionesResponsable: {}", json);
                    return new ArrayList<>();
                }
                if (json.contains("\"mensaje\"")) {
                    logger.info("Mensaje desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
            }
            List<ReporteProgresoRecomendacionesResponsableDTO> resultado = this.objectMapper.readValue(json, new TypeReference<List<ReporteProgresoRecomendacionesResponsableDTO>>() {});
            logger.info("DTO ReporteProgresoRecomendacionesResponsable: {}", resultado);
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesResponsableRepository): " + e.getMessage(), e);
            return null;
        }
    }

    public List<ReporteVerificablesDTO> obtenerReporteVerificables(String fecha_inicial, String fecha_fin, String usuario) {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporteVerificablesResponsable")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("usuario", Types.VARCHAR)).withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fecha_inicial);
            params.put("fecha_fin", fecha_fin);
            params.put("usuario", usuario);

            Map<String, Object> result = simpleJdbcCall.execute(params);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            
            if(json.trim().startsWith("{") && json.contains("\"mensaje\"")){
                return new ArrayList<>();
            }
            return this.objectMapper.readValue(json, new TypeReference<List<ReporteVerificablesDTO>>() {});
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesResponsableRepository): " + e.getMessage(), e);
            return null;
        }
    }

    public List<ReporteRecomendacionesVencidasResponsableDTO> obtenerReporteRecomendacionesVencidasResponsable(String fecha_inicial, String fecha_fin, int estado_seguimiento, String usuario) {
        try {
            logger.info("Iniciando obtenerReporteRecomendacionesVencidasResponsable con fechas: {} - {}, usuario: {}", fecha_inicial, fecha_fin, usuario);
            
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("REPORTERECOMENDACIONESVENCIDASRESPONSABLE")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("estado_seguimiento", Types.INTEGER),
                    new SqlParameter("usuario", Types.VARCHAR))
                .withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fecha_inicial);
            params.put("fecha_fin", fecha_fin);
            params.put("estado_seguimiento", estado_seguimiento);
            params.put("usuario", usuario);

            logger.info("Ejecutando procedimiento almacenado...");
            Map<String, Object> result = simpleJdbcCall.execute(params);
            logger.info("Procedimiento ejecutado exitosamente");

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            logger.info("JSON obtenido, longitud: {}", json != null ? json.length() : 0);
            
            if(json != null && json.trim().startsWith("{")){
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
                if (json.contains("\"mensaje\"")) {
                    logger.info("Mensaje desde la base de datos: {}", json);
                    return new ArrayList<>();
                }
            }
            
            List<ReporteRecomendacionesVencidasResponsableDTO> resultado = this.objectMapper.readValue(json, new TypeReference<List<ReporteRecomendacionesVencidasResponsableDTO>>() {});
            logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultado != null ? resultado.size() : 0);
            return resultado;
        } catch (Exception e) { 
            logger.error("Error al obtener datos para los reportes. (ReportesResponsableRepository): " + e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String clobToString(Clob clob) throws SQLException {
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
}
