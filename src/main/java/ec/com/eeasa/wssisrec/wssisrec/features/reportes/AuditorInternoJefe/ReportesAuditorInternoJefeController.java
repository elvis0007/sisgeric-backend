package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte1Body;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte2Body;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte3Body;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.Reporte7Body;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteHallazgosBody;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteRangoFechaBody;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteRecomendacionesFechaCorteBody;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteGeneralBody;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteTipoInformeBody;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteProrrogasDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteProrrogasRealizadasDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@RestController
@RequestMapping("api/reportes/auditor-interno-jefe")
public class ReportesAuditorInternoJefeController {
    
    //VAR
    @Autowired
    private ReportesAuditorInternoJefeServicio reportesAuditorInternoJefeServicio;

    @PostMapping("/reporte1-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reporte1AIJefe(@RequestBody Reporte1Body reporte1Body){
        return EstadoRespuestas.respuestaObtencion("Reporte 1 informes por departamento. Obtenido correctamente.", this.reportesAuditorInternoJefeServicio.reporte1aijdto(reporte1Body.getFechaInicio(), reporte1Body.getFechaFin(), reporte1Body.getOrganismoControl(), reporte1Body.getEstadoSeguimiento()));
    }

    @PostMapping("/reporte2-directorio-recomendaciones-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reporte2PoRecomendacionesAIJ(@RequestBody Reporte2Body reporte2Body) {
        return EstadoRespuestas.respuestaObtencion("Reporte 2 directorio de recomendaciones. Obtenido correctamente.", 
            this.reportesAuditorInternoJefeServicio.reporte2PoRecomendacionesAIJ(
                reporte2Body.getFechaInicio(),
                reporte2Body.getFechaFin(),
                reporte2Body.getDpto(),
                reporte2Body.getEstadoSeguimiento(),
                reporte2Body.getAuditor_responsable()
            )
        );
    }

    @PostMapping("/reporte3-recomendaciones-directorio-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reporte3RecomendacionesDirectorio(@RequestBody Reporte3Body reporte3Body) {
        return EstadoRespuestas.respuestaObtencion("Reporte 3 directorio de recomendaciones. Obtenido correctamente.", this.reportesAuditorInternoJefeServicio.reporte3RecomendacionesDirectorio(reporte3Body.getFecha_inicial(), reporte3Body.getFecha_fin(), reporte3Body.getDpto(), reporte3Body.getOrg_control(), reporte3Body.getEstado_seguimiento(), reporte3Body.getAuditor()));
    }

    @PostMapping("/reporte4-conteo-recomendaciones-estado-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reporte4ConteoRecomendacionesEstado(@RequestBody ReporteRangoFechaBody reporteRangoFechaBody) {
        return EstadoRespuestas.respuestaObtencion("Reporte 4 conteo de recomendaciones por estado. Obtenido correctamente.", this.reportesAuditorInternoJefeServicio.reporte4ConteoRecomendacionesEstado(reporteRangoFechaBody.getFecha_inicial(), reporteRangoFechaBody.getFecha_fin()));
    }

    @PostMapping("/reporte5-conteo-recomendaciones-departamento-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reporte5ConteoRecomendacionesDepartamento(@RequestBody ReporteRangoFechaBody reporteRangoFechaBody) {
        return EstadoRespuestas.respuestaObtencion("Reporte 5 conteo de recomendaciones por departamento. Obtenido correctamente.", this.reportesAuditorInternoJefeServicio.reporte5ConteoRecomendacionesPorDepartamento(reporteRangoFechaBody.getFecha_inicial(), reporteRangoFechaBody.getFecha_fin(), reporteRangoFechaBody.getDpto(), reporteRangoFechaBody.getOrg_control()));
    }  

    @PostMapping("/reporte-por-tipo-informe-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reportePorTipoInforme(@RequestBody ReporteTipoInformeBody reporteTipoInformeBody) {
        return EstadoRespuestas.respuestaObtencion("Reporte por tipo de informe. Obtenido correctamente.", this.reportesAuditorInternoJefeServicio.reportePorTipoInforme(reporteTipoInformeBody.getFecha_inicial(), reporteTipoInformeBody.getFecha_fin(), reporteTipoInformeBody.getTipo_informe()));
    }

    /**
     * Genera reporte de hallazgos para el Auditor Interno Jefe
     * @param reporteHallazgosBody Contiene fecha_inicial, fecha_fin, organismo_control (0=todos), auditor ("0"=todos)
     * @return Lista de hallazgos con título del informe, organismo de control, memorando, etc.
     */
    @PostMapping("/reporte-hallazgos-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reporteHallazgos(@RequestBody ReporteHallazgosBody reporteHallazgosBody) {
        return EstadoRespuestas.respuestaObtencion("Reporte de hallazgos obtenido correctamente.", 
            this.reportesAuditorInternoJefeServicio.reporteHallazgos(
                reporteHallazgosBody.getFecha_inicial(), 
                reporteHallazgosBody.getFecha_fin(), 
                reporteHallazgosBody.getOrganismo_control(), 
                reporteHallazgosBody.getAuditor()
            )
        );
    }   

    @PostMapping("/reporte-recomendaciones-fecha-corte-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reporteRecomendacionesFechaCorte(@RequestBody ReporteRecomendacionesFechaCorteBody reporteRecomendacionesFechaCorteBody) {
        return EstadoRespuestas.respuestaObtencion("Reporte de recomendaciones por fecha de corte. Obtenido correctamente.", this.reportesAuditorInternoJefeServicio.reporteRecomendacionesFechaCorte(reporteRecomendacionesFechaCorteBody.getFecha_corte_emision(), reporteRecomendacionesFechaCorteBody.getFecha_corte_estado(), reporteRecomendacionesFechaCorteBody.getDpto(), reporteRecomendacionesFechaCorteBody.getOrg_control()));
    }

    @PostMapping("/reporte-recomendaciones-vencidas-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reporteRecomendacionesVencidasAIJ(@RequestBody Reporte2Body reporte2Body) {
        return EstadoRespuestas.respuestaObtencion("Reporte de recomendaciones vencidas. Obtenido correctamente.", this.reportesAuditorInternoJefeServicio.reporteRecomendacionesVencidasAIJ(reporte2Body.getFechaInicio(), reporte2Body.getFechaFin(), reporte2Body.getEstadoSeguimiento()));
    }

    @PostMapping("/reporte7-auditores-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reporte7Auditores(@RequestBody Reporte7Body reporte7Body) {
        return EstadoRespuestas.respuestaObtencion("Reporte 7 por auditor. Obtenido correctamente.", this.reportesAuditorInternoJefeServicio.reporte7Auditores(reporte7Body.getFechaInicio(), reporte7Body.getFechaFin(), reporte7Body.getAuditor()));
    }

    @PostMapping("/reporte-general-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reporteGeneralAIJ(@RequestBody ReporteGeneralBody body) {
        return EstadoRespuestas.respuestaObtencion(
            "Reporte general AIJ obtenido correctamente.",
            this.reportesAuditorInternoJefeServicio.reporteGeneralAIJ(
                body.getFecha_inicial(),
                body.getFecha_fin(),
                body.getOrganismo_control(),
                body.getEstado_seguimiento(),
                body.getAuditor(),
                body.getDpto()
            )
        );
    }

    @PostMapping("/reporte-planes-evidencias-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reportePlanesEvidenciasAIJ(@RequestBody ReporteGeneralBody body) {
        return EstadoRespuestas.respuestaObtencion(
            "Reporte de planes y evidencias AIJ obtenido correctamente.",
            this.reportesAuditorInternoJefeServicio.reportePlanesYEvidenciasAIJ(
                body.getFecha_inicial(),
                body.getFecha_fin(),
                body.getOrganismo_control(),
                body.getEstado_seguimiento(),
                body.getAuditor(),
                body.getDpto()
            )
        );
    }

    @PostMapping("/reporte-prorrogas-ai-jefe")
    public ResponseEntity<LinkedHashMap<String, Object>> reporteProrrogasAIJ() {
        return EstadoRespuestas.respuestaObtencion(
            "Reporte de prórrogas AIJ obtenido correctamente.",
            this.reportesAuditorInternoJefeServicio.reporteProrrogasAIJ()
        );
    }

    @PostMapping("/reporte-prorrogas-realizadas")
    public ResponseEntity<LinkedHashMap<String, Object>> reporteProrrogasRealizadas() {
        return EstadoRespuestas.respuestaObtencion(
            "Reporte de prórrogas realizadas obtenido correctamente.",
            this.reportesAuditorInternoJefeServicio.reporteProrrogasRealizadas()
        );
    }
}
