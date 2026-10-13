package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto.ReporteDptoEstadoSeguimientoBody;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoOperativo.dto.ReporteProgresoRecomendacionesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.ReportesAuditorInternoJefeServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto.ReporteGeneralBody;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@RestController
@RequestMapping("api/reportes/auditor-interno-operativo")
public class ReportesAuditorInternoOperativoController {
    
    @Autowired
    private ReportesAuditorInternoOperativoService reportesAuditorInternoOperativoService;

    @Autowired
    private ReportesAuditorInternoJefeServicio reportesAuditorInternoJefeServicio;

    @PostMapping("/reporte-por-departamento-estado-seguimiento")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerReportePorDepartamentoEstadoSeguimiento(@RequestBody ReporteDptoEstadoSeguimientoBody reporteDptoEstadoSeguimientoBody) {
        return EstadoRespuestas.respuestaObtencion("Reporte por departamento y estado de seguimiento. Obtenido correctamente.", this.reportesAuditorInternoOperativoService.obtenerReportePorDepartamentoEstadoSeguimiento(reporteDptoEstadoSeguimientoBody.getFecha_inicial(), reporteDptoEstadoSeguimientoBody.getFecha_fin(), reporteDptoEstadoSeguimientoBody.getDpto(), reporteDptoEstadoSeguimientoBody.getEstado_seguimiento(), reporteDptoEstadoSeguimientoBody.getAuditor_responsable()));
    }
    
    @PostMapping("/reporte-recomendaciones-progreso")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerReporteRecomendacionesProgreso(@RequestBody ReporteProgresoRecomendacionesDTO reporteProgresoRecomendacionesDTO) {
        return EstadoRespuestas.respuestaObtencion("Reporte de recomendaciones de progreso. Obtenido correctamente.", this.reportesAuditorInternoOperativoService.obtenerReporteRecomendacionesProgreso(reporteProgresoRecomendacionesDTO.getFecha_inicial(), reporteProgresoRecomendacionesDTO.getFecha_fin(), reporteProgresoRecomendacionesDTO.getAuditor_responsable(), reporteProgresoRecomendacionesDTO.getOrg_control()));
    }

    @PostMapping("/reporte-recomendaciones-vencidas-operativo")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerReporteRecomendacionesVencidasOperativo(@RequestBody ReporteDptoEstadoSeguimientoBody reporteDptoEstadoSeguimientoBody) {
        return EstadoRespuestas.respuestaObtencion("Reporte de recomendaciones vencidas. Obtenido correctamente.", this.reportesAuditorInternoOperativoService.obtenerReporteRecomendacionesVencidasOperativo(reporteDptoEstadoSeguimientoBody.getFecha_inicial(), reporteDptoEstadoSeguimientoBody.getFecha_fin(), reporteDptoEstadoSeguimientoBody.getEstado_seguimiento(), reporteDptoEstadoSeguimientoBody.getAuditor_responsable()));
    }

    @PostMapping("/reporte-seguimiento-responsable")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerReporteSeguimientoResponsable(@RequestBody ReporteGeneralBody body) {
        return EstadoRespuestas.respuestaObtencion(
            "Reporte de seguimiento por responsable obtenido correctamente.",
            this.reportesAuditorInternoOperativoService.obtenerReporteSeguimientoResponsable(
                body.getFecha_inicial(),
                body.getFecha_fin(),
                body.getOrganismo_control(),
                body.getEstado_seguimiento(),
                body.getAuditor(),
                body.getDpto()
            )
        );
    }

    @PostMapping("/reporte-planes-evidencias-operativo")
    public ResponseEntity<LinkedHashMap<String, Object>> reportePlanesEvidenciasAO(@RequestBody ReporteGeneralBody body) {
        return EstadoRespuestas.respuestaObtencion(
            "Reporte de planes y evidencias AO obtenido correctamente.",
            this.reportesAuditorInternoOperativoService.reportePlanesYEvidenciasAO(
                body.getFecha_inicial(),
                body.getFecha_fin(),
                body.getOrganismo_control(),
                body.getEstado_seguimiento(),
                body.getAuditor(),
                body.getDpto()
            )
        );
    }
}
