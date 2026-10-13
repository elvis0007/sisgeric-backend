package ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.RecomendacionesResponsableBody;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteProgresoRecomendacionBody;
import ec.com.eeasa.wssisrec.wssisrec.features.reportes.Responsable.dto.ReporteVerificablesBody;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@RestController
@RequestMapping("api/reportes/responsable")
public class ReportesResponsableController {

    @Autowired
    private ReportesResponsableService reportesResponsableService;

    @PostMapping("/reporte-recomendaciones-responsable")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerReporteRecomendacionesResponsable(@RequestBody RecomendacionesResponsableBody recomendacionesResponsableBody) {
        return EstadoRespuestas.respuestaObtencion("Reporte de recomendaciones de progreso. Obtenido correctamente.", this.reportesResponsableService.obtenerReporteRecomendacionesResponsable(recomendacionesResponsableBody.getFecha_inicial(), recomendacionesResponsableBody.getFecha_fin(), recomendacionesResponsableBody.getEstado_seguimiento(), recomendacionesResponsableBody.getUsuario()));
    }

    @PostMapping("/reporte-progreso-recomendaciones-responsable")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerReporteProgresoRecomendacionesResponsable(@RequestBody ReporteProgresoRecomendacionBody reporteProgresoRecomendacionBody) {
        return EstadoRespuestas.respuestaObtencion("Reporte de progreso de recomendaciones. Obtenido correctamente.", this.reportesResponsableService.obtenerReporteProgresoRecomendacionesResponsable(reporteProgresoRecomendacionBody.getFecha_inicial(), reporteProgresoRecomendacionBody.getFecha_fin(), reporteProgresoRecomendacionBody.getUsuario()));
    }

    @PostMapping("/reporte-verificables")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerReporteVerificables(@RequestBody ReporteVerificablesBody reporteVerificablesBody) {
        return EstadoRespuestas.respuestaObtencion("Reporte de verificables. Obtenido correctamente.", this.reportesResponsableService.obtenerReporteVerificables(reporteVerificablesBody.getFecha_inicial(), reporteVerificablesBody.getFecha_fin(), reporteVerificablesBody.getUsuario()));
    }

    @PostMapping("/reporte-recomendaciones-vencidas-responsable")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerReporteRecomendacionesVencidasResponsable(@RequestBody RecomendacionesResponsableBody recomendacionesResponsableBody) {
        return EstadoRespuestas.respuestaObtencion("Reporte de recomendaciones vencidas. Obtenido correctamente.", this.reportesResponsableService.obtenerReporteRecomendacionesVencidasResponsable(recomendacionesResponsableBody.getFecha_inicial(), recomendacionesResponsableBody.getFecha_fin(), recomendacionesResponsableBody.getEstado_seguimiento(), recomendacionesResponsableBody.getUsuario()));
    }
}
