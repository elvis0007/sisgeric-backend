package ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ec.com.eeasa.wssisrec.wssisrec.features.reportes.DirectorDepartamental.dto.ReporteRecomendacionesPorDptoBody;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@RestController
@RequestMapping("api/reportes/director-departamental")
public class ReportesDirectorDepartamentalController {

    @Autowired
    private ReportesDirectorDepartamentalService reportesDirectorDepartamentalService;

    @PostMapping("/reporte-recomendaciones-por-departamento")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerReporteRecomendacionesPorDepartamento(@RequestBody ReporteRecomendacionesPorDptoBody reporteRecomendacionesPorDptoBody) {	
        return EstadoRespuestas.respuestaObtencion("Reporte de recomendaciones por departamento. Obtenido correctamente.", this.reportesDirectorDepartamentalService.obtenerReporteRecomendacionesPorDepartamento(reporteRecomendacionesPorDptoBody));
    }

    @PostMapping("/reporte-departamento-fechas-vencidas")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerReporteDepartamentoFechasVencidas(@RequestBody ReporteRecomendacionesPorDptoBody reporteRecomendacionesPorDptoBody) {	
        return EstadoRespuestas.respuestaObtencion("Reporte de departamento fechas vencidas. Obtenido correctamente.", this.reportesDirectorDepartamentalService.obtenerReporteDepartamentoFechasVencidas(reporteRecomendacionesPorDptoBody));
    }
    
}
