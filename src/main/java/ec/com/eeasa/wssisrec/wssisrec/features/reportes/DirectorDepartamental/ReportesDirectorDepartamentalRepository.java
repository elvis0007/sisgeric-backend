package ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental;


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

import ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental.dto.ReporteRecomendacionesPorDepartamentoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental.dto.ReporteRecomendacionesPorDptoBody;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental.dto.ReporteDepartamentoFechasVencidasDTO;

@Repository
public class ReportesDirectorDepartamentalRepository {
    private static final Logger logger = LoggerFactory.getLogger(ReportesDirectorDepartamentalRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public List<ReporteRecomendacionesPorDepartamentoDTO> obtenerReporteRecomendacionesPorDepartamento(ReporteRecomendacionesPorDptoBody reporteRecomendacionesPorDptoBody) {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporteRecomendacionesPorDirectorDepartamental")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("organismo_control", Types.INTEGER),
                    new SqlParameter("estado_seguimiento", Types.INTEGER),
                    new SqlParameter("auditor", Types.VARCHAR),
                    new SqlParameter("dpto", Types.VARCHAR)
                ).withoutProcedureColumnMetaDataAccess();


            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", reporteRecomendacionesPorDptoBody.getFecha_inicial()); 
            params.put("fecha_fin", reporteRecomendacionesPorDptoBody.getFecha_fin());
            params.put("organismo_control", reporteRecomendacionesPorDptoBody.getOrganismo_control());
            params.put("estado_seguimiento", reporteRecomendacionesPorDptoBody.getEstado_seguimiento());
            params.put("auditor", reporteRecomendacionesPorDptoBody.getAuditor());
            params.put("dpto", reporteRecomendacionesPorDptoBody.getDpto()); 

            Map<String, Object> result = simpleJdbcCall.execute(params);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            
            if(json.trim().startsWith("{") && json.contains("\"mensaje\"")){    
                return new ArrayList<>();
            }
            return this.objectMapper.readValue(json, new TypeReference<List<ReporteRecomendacionesPorDepartamentoDTO>>() {});
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesDirectorDepartamentalRepository): " + e.getMessage(), e);
            return null;
        }
    }

    public List<ReporteDepartamentoFechasVencidasDTO> obtenerReporteDepartamentoFechasVencidas(ReporteRecomendacionesPorDptoBody reporteRecomendacionesPorDptoBody) {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reporteDepartamentoFechasVencidasDirector")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("organismo_control", Types.INTEGER),
                    new SqlParameter("estado_seguimiento", Types.INTEGER),
                    new SqlParameter("auditor", Types.VARCHAR),
                    new SqlParameter("dpto", Types.VARCHAR)
                ).withoutProcedureColumnMetaDataAccess();


            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", reporteRecomendacionesPorDptoBody.getFecha_inicial()); 
            params.put("fecha_fin", reporteRecomendacionesPorDptoBody.getFecha_fin());
            params.put("organismo_control", reporteRecomendacionesPorDptoBody.getOrganismo_control());
            params.put("estado_seguimiento", reporteRecomendacionesPorDptoBody.getEstado_seguimiento());
            params.put("auditor", reporteRecomendacionesPorDptoBody.getAuditor());
            params.put("dpto", reporteRecomendacionesPorDptoBody.getDpto()); 

            Map<String, Object> result = simpleJdbcCall.execute(params);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            
            if(json.trim().startsWith("{") && json.contains("\"mensaje\"")){    
                return new ArrayList<>();
            }
            return this.objectMapper.readValue(json, new TypeReference<List<ReporteDepartamentoFechasVencidasDTO>>() {});
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesDirectorDepartamentalRepository): " + e.getMessage(), e);
            return null;
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
