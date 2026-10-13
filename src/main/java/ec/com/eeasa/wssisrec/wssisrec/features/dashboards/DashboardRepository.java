package ec.com.eeasa.wssisrec.wssisrec.features.dashboards;

import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.Dashboard1DTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardAIJS2DTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardAIJS3DTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardAIOS1DTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardAuditadoS1DTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardListaInformesAIJDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardListaInformesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardListaVerificablesResponsableDTO;

/*
 * Autor: Mateo Díaz
 */

@Repository
public class DashboardRepository {
    private static final Logger logger = LoggerFactory.getLogger(DashboardRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public Dashboard1DTO obtenerReporteDptos(){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName("ObtenerReporteJSON")
            .declareParameters(new SqlOutParameter("p_json", Types.CLOB));
    
            Map<String, Object> result = simpleJdbcCall.execute();
    
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            return this.objectMapper.readValue(json, Dashboard1DTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener datos para el dashboard. (DashboardRepository) " + e);
            return null;
        }
    }

    public DashboardAIJS2DTO obtenerReporteRecAnios(){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName("ObtenerDataGraficaPorAnio")
            .declareParameters(new SqlOutParameter("p_json", Types.CLOB));

            Map<String, Object> result =  simpleJdbcCall.execute();

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            
            return this.objectMapper.readValue(json, DashboardAIJS2DTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener datos para el dashboard (Recomendaciones por dptos, filtro anios. DashboardRepository) " + e);
            return null;
        }
    }

    public DashboardAIJS3DTO obtenerReporteAnios(){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName("ObtenerDataGrafica7Anios")
            .declareParameters(new SqlOutParameter("p_json", Types.CLOB));

            Map<String, Object> result =  simpleJdbcCall.execute();

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            
            return this.objectMapper.readValue(json, DashboardAIJS3DTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener datos para el dashboard (Recomendaciones por dptos, 7 ultimos anios. DashboardRepository) " + e);
            return null;
        }
    }
    public DashboardAIOS1DTO obtenerDatosDptosAIO(String arere_usuario) {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName("ReporteDashboardS1AIO").declareParameters(
                    new SqlParameter("arere_usuario", Types.VARCHAR),
                    new SqlOutParameter("p_json", Types.CLOB));
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("arere_usuario", arere_usuario);
            Map<String, Object> result = simpleJdbcCall.execute(params);
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            return this.objectMapper.readValue(json, DashboardAIOS1DTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener los datos para el dashboard auditor operativo S1: ", e);
            return null;
        }
    }

    public DashboardAIJS3DTO obtenerDatos7AniosAIO(String arere_usuario){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName("ObtenerDataGrafica7AniosS2AIO").declareParameters(
                    new SqlParameter("arere_usuario", Types.VARCHAR),
                    new SqlOutParameter("p_json", Types.CLOB));
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("arere_usuario", arere_usuario);
            Map<String, Object> result = simpleJdbcCall.execute(params);
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            return this.objectMapper.readValue(json, DashboardAIJS3DTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener los datos para el dashboard auditor operativo S2: ", e);
            return null;
        }
    }

    public DashboardAIJS2DTO obtenerDatosPorDptoFiltroAIO(String arere_usuario){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName("ObtenerDataGraficaPorAnioAIOS3").declareParameters(
                    new SqlParameter("arere_usuario", Types.VARCHAR),
                    new SqlOutParameter("p_json", Types.CLOB));
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("arere_usuario", arere_usuario);
            Map<String, Object> result = simpleJdbcCall.execute(params);
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            return this.objectMapper.readValue(json, DashboardAIJS2DTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener los datos para el dashboard auditor operativo S3: ", e);
            return null;
        }
    }

    public Dashboard1DTO obtenerDatosDirectorDpto(String nombre_dpto){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName("ObtenerReporteS1Dpto").declareParameters(
                new SqlParameter("nombre_dpto", Types.VARCHAR),
                new SqlOutParameter("p_json", Types.CLOB)
            );
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("nombre_dpto", nombre_dpto);
            Map<String, Object> result = simpleJdbcCall.execute(params);
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            return this.objectMapper.readValue(json, Dashboard1DTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener los datos para el dashboard del director departamental S1: ", e);
            return null;
        }
    }

    public DashboardAIJS3DTO obtenerDatos7aniosS2Dpto(String nombre_dpto){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName("ObtenerDataGrafica7AniosS2DPTO").declareParameters(
                new SqlParameter("nombre_dpto", Types.VARCHAR),
                new SqlOutParameter("p_json", Types.CLOB)
            );
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("nombre_dpto", nombre_dpto);
            Map<String, Object> result = simpleJdbcCall.execute(params);
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            return this.objectMapper.readValue(json, DashboardAIJS3DTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener los datos para el dashboard del director departamental S2: ", e);
            return null;
        }
    }

    public DashboardListaInformesDTO obtenerDatosListaInformesDptoS3(String nombre_dpto){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName("ObtenerListaGraficaPorAnioDptoS3").declareParameters(
                new SqlParameter("nombre_dpto", Types.VARCHAR),
                new SqlOutParameter("p_json", Types.CLOB)
            );
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("nombre_dpto", nombre_dpto);
            Map<String, Object> result = simpleJdbcCall.execute(params);;
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            return this.objectMapper.readValue(json, DashboardListaInformesDTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener los datos para el dashboard del director departamental S3.");
            return null;
        }
    }

    public DashboardAuditadoS1DTO obtenerMetricasAuditadoS1(String arers_usuario_per){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate).withProcedureName("ObtenerMetricasResponsableS1").declareParameters(
                new SqlParameter(arers_usuario_per, Types.VARCHAR),
                new SqlOutParameter("p_json", Types.CLOB)
            );
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("arers_usuario_per", arers_usuario_per);
            Map<String, Object> result = simpleJdbcCall.execute(params);
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            return this.objectMapper.readValue(json, DashboardAuditadoS1DTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener los datos para el dashboard del auditado (responsable) S1: {}", e.getMessage(), e);
            return null;
        }
    }

    public DashboardAIJS3DTO obtenerReporte7AniosRecomendacionesAuditado(String arers_usuario_per){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("ObtenerRecomendaciones7AniosResponsable")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("arers_usuario_per", Types.VARCHAR)
                );
            
            // Crear el mapa con los parámetros
            MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("arers_usuario_per", arers_usuario_per);
            
            Map<String, Object> result = simpleJdbcCall.execute(params);
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            return this.objectMapper.readValue(json, DashboardAIJS3DTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener los datos para el dashboard del auditado (responsable) S2: " + e.getMessage());
            return null;
        }
    }

    public DashboardListaInformesDTO obtenerListaInformesAuditado(String arers_usuario_per){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("ObtenerListaGraficaPorAnioResponsable")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("arers_usuario_per", Types.VARCHAR)
                );
            
            MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("arers_usuario_per", arers_usuario_per);

            Map<String, Object> result = simpleJdbcCall.execute(params);
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);

            return this.objectMapper.readValue(json, DashboardListaInformesDTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener los datos para el dashboard del auditado (responsable) S2: " + e.getMessage());
            return null;
        }
    }

    public DashboardListaInformesAIJDTO obtenerListaInformesAIS4(){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("ObtenerListaInformesPorDptoAIJ")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB)
                );

            Map<String, Object> result = simpleJdbcCall.execute();
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);

            return this.objectMapper.readValue(json, DashboardListaInformesAIJDTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener los datos para el dashboard del auditado (responsable) S2: " + e.getMessage());
            return null;
        }
    }

    public DashboardListaVerificablesResponsableDTO obtenerListaVerificablesResponsable(String usu_per){
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("estadoVerificablesResponsable")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("usu_per", Types.VARCHAR)
                ).withoutProcedureColumnMetaDataAccess();

            MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("usu_per", usu_per);

            Map<String, Object> result = simpleJdbcCall.execute(params);
            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);

            return this.objectMapper.readValue(json, DashboardListaVerificablesResponsableDTO.class);
        } catch (Exception e) {
            logger.error("Error al obtener los datos para el dashboard de verificables (responsable) S4: " + e.getMessage());
            return null;
        }
    }

    private String clobToString(Clob clob) throws SQLException {
        if (clob == null) return "";
        
        try {
            return clob.getSubString(1, (int) clob.length());
        } catch (SQLException e) {
            logger.error("Error al convertir el Clob a String: " + e.getMessage());
            return null;
        } finally {
            // Cerrar explícitamente el Clob para liberar recursos
            if (clob != null) {
                try {
                    clob.free();
                } catch (SQLException e) {
                    logger.error("Error al cerrar el Clob: " + e.getMessage());
                }
            }
        }
    }
}
