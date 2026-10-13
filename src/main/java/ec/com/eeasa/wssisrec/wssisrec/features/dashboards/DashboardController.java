package ec.com.eeasa.wssisrec.wssisrec.features.dashboards;

import org.springframework.web.bind.annotation.RestController;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.Dashboard1DTO;
import ec.com.eeasa.wssisrec.wssisrec.features.dashboards.dtos.DashboardAIOS1DTO;

/*
 * Autor: Mateo Díaz
 * aij (Auditor-Interno-Jefe)
 *      dashboard-data-s1 -> grafico dona y 2 métricas
 *      dashboard-data-s2 -> grafico de barras horizontales cant. recomendaciones por dpto (filtro anio en front)
 *      dashboard-data-s3 -> grafico de barras horizontales cant. recomendaciones por anio en general (7 anios)
 *      dashboard-data-s4 -> lista de informes en general.
 * aio (Auditor-Interno-Operativo. Datos filtrados en base a lo que haya cargador el auditor al sistema) 
 *      dashboard-data-s1 -> grafico dona y 2 métricas
 *      dashboard-data-s2 -> grafico de barras horizontales cant. recomendaciones por anio en general (7 anios)
 *      dashboard-data-s3 -> grafico de barras horizontales cant. recomendaciones por dpto (filtro anio en front)
 * d-departamental (Director departamental. Datos filtrados en base al campo Departamento )
 *      dashboard-data-s1 -> grafico dona y 2 metricas
 *      dashboard-data-s2 -> grafico de barras horizontales cant. recomendaciones por anio en general del dpto (7 anios)
 *      dashboard-data-s3 -> lista de informes con detalle de recomendaciones del dpto del director
 * auditado (Auditado - responsable)
 *      dashboard-data-s1 -> 3 metricas auditado
 */

@RestController
@RequestMapping("api/dashboards")
public class DashboardController {
    //VAR
    @Autowired
    DashboardService dashboardService;

    @GetMapping("/ai-jefe/dashboard-data-s1")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS1() {
        return EstadoRespuestas.respuestaObtencion("Datos para el dashboard 1 obtenidos correctamente.", this.dashboardService.obtenerDetalleRecomendacionesDashboard());
    }

    @GetMapping("/ai-jefe/dashboard-data-s2")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS2() {
        return EstadoRespuestas.respuestaObtencion("Datos para la grafica de barras en base a los dptos obtenidos correctamente.", this.dashboardService.obtenerReporteRecAnios());
    }

    @GetMapping("/ai-jefe/dashboard-data-s3")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS3() {
        return EstadoRespuestas.respuestaObtencion("Datos para la grafica de barras en base a los ultimos 7 años obtenidos correctamente.", this.dashboardService.obtenerReporteAnios());
    }

    @GetMapping("/ai-jefe/dashboard-data-s4")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS4() {
        return EstadoRespuestas.respuestaObtencion("Datos para la lista de informes obtenidos correctamente.", this.dashboardService.obtenerListaInformesAIS4());
    }
    
    @GetMapping("/ai-operativo/dashboard-data-s1/{arere_usuario}")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS1(@PathVariable String arere_usuario) {
        return EstadoRespuestas.respuestaObtencion("Datos para dashboard auditor interno operativo (seccion 1) obtenidos correctamente.", this.dashboardService.obtenerDatosPorDptoAIO(arere_usuario));
    }

    @GetMapping("/ai-operativo/dashboard-data-s2/{arere_usuario}")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS2(@PathVariable String arere_usuario) {
        return EstadoRespuestas.respuestaObtencion("Datos para dashboard auditor interno operativo (seccion 2) obtenidos correctamente.", this.dashboardService.obtenerDatos7AniosAIO(arere_usuario));
    }

    @GetMapping("/ai-operativo/dashboard-data-s3/{arere_usuario}")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS3(@PathVariable String arere_usuario) {
        return EstadoRespuestas.respuestaObtencion("Datos para dashboard auditor interno operativo (seccion 3) obtenidos correctamente.", this.dashboardService.obtenerDatosPorDptoAnioAIO(arere_usuario));
    }
    
    @GetMapping("/d-departamental/dashboard-data-s1/{nombre_dpto}")
    public ResponseEntity<LinkedHashMap<String, Object>> getDatDashboardS1(@PathVariable String nombre_dpto) {
        return EstadoRespuestas.respuestaObtencion("Datos para dashboard director departamental (seccion 1) obtenidos correctamente.", this.dashboardService.obtenerDatosDirectoDptoS1(nombre_dpto));
    }
    
    @GetMapping("/d-departamental/dashboard-data-s2/{nombre_dpto}")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS2Dpto(@PathVariable String nombre_dpto) {
        return EstadoRespuestas.respuestaObtencion("Datos para dashboard director departamental (seccion 2) obtenidos correctamente.", this.dashboardService.obtenerDatos7aniosS2Dpto(nombre_dpto));
    }
    
    @GetMapping("/d-departamental/dashboard-data-s3/{nombre_dpto}")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS3Dpto(@PathVariable String nombre_dpto) {
        return EstadoRespuestas.respuestaObtencion("Datos para dashboard director departamental (seccion 3) obtenidos correctamente.", this.dashboardService.obteneDashboardListaInformesDptoS3(nombre_dpto));
    }
    
    @GetMapping("/auditado/dashboard-data-s1/{arers_usuario_per}")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS1Auditado(@PathVariable String arers_usuario_per) {
        return EstadoRespuestas.respuestaObtencion("Datos para dashboard de auditado (seccion 1 - metricas) obtenidos correctamente.", this.dashboardService.obtenerMetricasAuditadoS1(arers_usuario_per));
    }
    
    @GetMapping("/auditado/dashboard-data-s2/{arers_usuario_per}")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS2Auditado(@PathVariable String arers_usuario_per) {
        return EstadoRespuestas.respuestaObtencion("Datos para dashboard de auditado (seccion 2 - grafico barras) obtenidos correctamente.", this.dashboardService.obtenerReporte7AniosRecomendacionesAuditado(arers_usuario_per));
    }

    @GetMapping("/auditado/dashboard-data-s3/{arers_usuario_per}")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS3Auditado(@PathVariable String arers_usuario_per) {
        return EstadoRespuestas.respuestaObtencion("Datos para dashboard de auditado (seccion 3 - lista informes) obtenidos correctamente.", this.dashboardService.obtenerListaInformesAuditado(arers_usuario_per));
    }

    @GetMapping("/auditado/dashboard-data-s4/{arers_usuario_per}")
    public ResponseEntity<LinkedHashMap<String, Object>> getDataDashboardS4Auditado(@PathVariable String arers_usuario_per) {
        return EstadoRespuestas.respuestaObtencion("Datos para dashboard de auditado (seccion 4 - lista verificables) obtenidos correctamente.", this.dashboardService.obtenerListaVerificablesResponsable(arers_usuario_per));
    }

}
