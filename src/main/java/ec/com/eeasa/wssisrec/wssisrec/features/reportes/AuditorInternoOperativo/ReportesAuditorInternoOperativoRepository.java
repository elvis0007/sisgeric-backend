package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo;

import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Reader;
import java.io.IOException;

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

import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto.ReportePorDepartamentoEstadoSeguimientoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto.ReporteRecomendacionesProgresoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto.ReporteRecomendacionesVencidasOperativoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteGeneralAIJDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class ReportesAuditorInternoOperativoRepository {
    private static final Logger logger = LoggerFactory.getLogger(ReportesAuditorInternoOperativoRepository.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public List<ReportePorDepartamentoEstadoSeguimientoDTO> obtenerReportePorDepartamentoEstadoSeguimiento(String fecha_inicial, String fecha_fin, String dpto, int estado_seguimiento, String auditor) {
        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("reportePorDepartamentoEstadoSeguimiento")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("dpto", Types.VARCHAR),
                    new SqlParameter("estado_seguimiento", Types.INTEGER),
                    new SqlParameter("auditor_responsable", Types.VARCHAR)).withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fecha_inicial);
            params.put("fecha_fin", fecha_fin);
            params.put("dpto", dpto);
            params.put("estado_seguimiento", estado_seguimiento);
            params.put("auditor_responsable", auditor);
            Map<String, Object> result = simpleJdbcCall.execute(params);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);
            
            if(json.trim().startsWith("{")){
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos: " + json);
                }
                if (json.contains("\"mensaje\"")) {
                    return new ArrayList<>();
                }
            }
            return this.objectMapper.readValue(json, new TypeReference<List<ReportePorDepartamentoEstadoSeguimientoDTO>>() {});
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoOperativoRepository): " + e.getMessage(), e);
            return null;
        }
    }

    public List<ReporteRecomendacionesProgresoDTO> obtenerReporteRecomendacionesProgreso(String fecha_inicial, String fecha_fin, String auditor, int org_control) {
    try {
        SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
            .withProcedureName("reporteRecomendacionesProgreso")
            .declareParameters(
                new SqlOutParameter("p_json", Types.CLOB),
                new SqlParameter("fecha_inicial", Types.VARCHAR),
                new SqlParameter("fecha_fin", Types.VARCHAR),
                new SqlParameter("auditor_responsable", Types.VARCHAR),
                new SqlParameter("org_control", Types.INTEGER)
            )
            .withoutProcedureColumnMetaDataAccess();

        Map<String, Object> params = new HashMap<>();
        params.put("fecha_inicial", fecha_inicial);
        params.put("fecha_fin", fecha_fin);
        params.put("auditor_responsable", auditor);
        params.put("org_control", org_control);   

        Map<String, Object> result = simpleJdbcCall.execute(params);

        Clob clob = (Clob) result.get("p_json");
        String json = clobToString(clob);

        if (json.trim().startsWith("{")) {
            if (json.contains("\"error\"")) {
                logger.error("Error desde la base de datos: " + json);
            }
            if (json.contains("\"mensaje\"")) {
                return new ArrayList<>();
            }
        }

        return this.objectMapper.readValue(json, new TypeReference<List<ReporteRecomendacionesProgresoDTO>>() {});
    } catch (Exception e) {
        logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoOperativoRepository): " + e.getMessage(), e);
        return null;
    }
}

    public List<ReporteRecomendacionesVencidasOperativoDTO> obtenerReporteRecomendacionesVencidasOperativo(String fecha_inicial, String fecha_fin, int estado_seguimiento, String auditor) {
        try {
            logger.info("Iniciando obtenerReporteRecomendacionesVencidasOperativo con fechas: {} - {}, auditor: {}", fecha_inicial, fecha_fin, auditor);
            
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("REPORTERECOMENDACIONESVENCIDASOPERATIVO")
                .declareParameters(
                    new SqlOutParameter("p_json", Types.CLOB),
                    new SqlParameter("fecha_inicial", Types.VARCHAR),
                    new SqlParameter("fecha_fin", Types.VARCHAR),
                    new SqlParameter("estado_seguimiento", Types.INTEGER),
                    new SqlParameter("auditor_responsable", Types.VARCHAR))
                .withoutProcedureColumnMetaDataAccess();

            Map<String, Object> params = new HashMap<>();
            params.put("fecha_inicial", fecha_inicial);
            params.put("fecha_fin", fecha_fin);
            params.put("estado_seguimiento", estado_seguimiento);
            params.put("auditor_responsable", auditor);
            
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
            
            List<ReporteRecomendacionesVencidasOperativoDTO> resultado = this.objectMapper.readValue(json, new TypeReference<List<ReporteRecomendacionesVencidasOperativoDTO>>() {});
            logger.info("Reporte procesado exitosamente, {} registros obtenidos", resultado != null ? resultado.size() : 0);
            if (resultado != null) {
                for (ReporteRecomendacionesVencidasOperativoDTO dto : resultado) {
                    Date date = parseFecha(dto.getFechaMaxima());
                    if (date != null) {
                        int plazo = DateMapper.calcularDias(date);
                        dto.setPlazo(plazo);
                    }
                }
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para los reportes. (ReportesAuditorInternoOperativoRepository): " + e.getMessage(), e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<ReporteGeneralAIJDTO> obtenerReporteSeguimientoResponsable(String fecha_inicial, String fecha_fin, int organismo_control, int estado_seguimiento, String auditor, String dpto) {
        try {
            logger.info("Iniciando obtenerReporteSeguimientoResponsable con fechas: {} - {}, organismo_control: {}, estado_seguimiento: {}, auditor: {}, dpto: {}",
                    fecha_inicial, fecha_fin, organismo_control, estado_seguimiento, auditor, dpto);

            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                    .withProcedureName("reporteSeguimientoResponsableOperativo")
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
                    logger.error("Error desde la base de datos (reporteSeguimientoResponsableOperativo): {}", json);
                    return new ArrayList<>();
                }
                if (json.contains("\"mensaje\"")) {
                    logger.info("Mensaje desde la base de datos (reporteSeguimientoResponsableOperativo): {}", json);
                    return new ArrayList<>();
                }
            }

            if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
                logger.info("No se encontraron datos para reporteSeguimientoResponsableOperativo");
                return new ArrayList<>();
            }

            List<ReporteGeneralAIJDTO> resultado = this.objectMapper.readValue(json,
                    new TypeReference<List<ReporteGeneralAIJDTO>>() {});
            logger.info("reporteSeguimientoResponsableOperativo procesado exitosamente para Auditor Operativo, {} registros obtenidos",
                    resultado != null ? resultado.size() : 0);
            if (resultado != null && !resultado.isEmpty()) {
                enriquecerSeguimientoIndividual(resultado);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para reporteSeguimientoResponsableOperativo: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<ReporteGeneralAIJDTO> reportePlanesYEvidenciasAO(String fecha_inicial, String fecha_fin, int organismo_control, int estado_seguimiento, String auditor, String dpto) {
        try {
            logger.info("Iniciando reportePlanesYEvidenciasAO con fechas: {} - {}, organismo_control: {}, estado_seguimiento: {}, auditor: {}, dpto: {}",
                    fecha_inicial, fecha_fin, organismo_control, estado_seguimiento, auditor, dpto);

            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                    .withProcedureName("reportePlanesYEvidenciasAO")
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
            params.put("auditor_responsable", (auditor != null && !auditor.trim().isEmpty()) ? auditor : "");
            params.put("dpto", (dpto != null && !dpto.trim().isEmpty()) ? dpto : "0");
            params.put("responsable", "0");

            Map<String, Object> result = simpleJdbcCall.execute(params);

            Clob clob = (Clob) result.get("p_json");
            String json = clobToString(clob);

            if (json != null && json.trim().startsWith("{")) {
                if (json.contains("\"error\"")) {
                    logger.error("Error desde la base de datos (reportePlanesYEvidenciasAO): {}", json);
                    return new ArrayList<>();
                }
                if (json.contains("\"mensaje\"")) {
                    logger.info("Mensaje desde la base de datos (reportePlanesYEvidenciasAO): {}", json);
                    return new ArrayList<>();
                }
            }

            if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
                logger.info("No se encontraron datos para reportePlanesYEvidenciasAO");
                return new ArrayList<>();
            }

            List<ReporteGeneralAIJDTO> resultado = this.objectMapper.readValue(json,
                    new TypeReference<List<ReporteGeneralAIJDTO>>() {});
            logger.info("reportePlanesYEvidenciasAO procesado exitosamente para Auditor Operativo, {} registros obtenidos",
                    resultado != null ? resultado.size() : 0);
            if (resultado != null && !resultado.isEmpty()) {
                enriquecerSeguimientoIndividual(resultado);
            }
            return resultado;
        } catch (Exception e) {
            logger.error("Error al obtener datos para reportePlanesYEvidenciasAO: " + e.getMessage(), e);
            return new ArrayList<>();
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
                logger.error("Error al consultar enriquecimiento individual por responsable en AO: {}", ex.getMessage(), ex);
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
