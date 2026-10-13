package ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.dtos.ActualizarInformesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.dtos.ActualizarRecomendacionesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.dtos.ActualizarResponsablesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.dtos.CrearPeriodoDTO;

@RestController
@RequestMapping("/api/periodos-evaluacion")
@CrossOrigin(origins = "*")
public class CorteEvaluacionControlador {

    @Autowired
    private CorteEvaluacionServicio corteEvaluacionServicio;

    @GetMapping
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerPeriodos(@RequestParam(required = false) Integer anio) {
        return corteEvaluacionServicio.obtenerTodos(anio);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorId(@PathVariable Long id) {
        return corteEvaluacionServicio.obtenerPorId(id);
    }

    @GetMapping("/{id}/informes")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerInformesPorPeriodo(@PathVariable Long id) {
        return corteEvaluacionServicio.obtenerInformesPorPeriodo(id);
    }

    @GetMapping("/{id}/recomendaciones")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerRecomendacionesPorPeriodo(@PathVariable Long id) {
        return corteEvaluacionServicio.obtenerRecomendacionesPorPeriodo(id);
    }

    @GetMapping("/{id}/responsables")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerResponsablesPorPeriodo(@PathVariable Long id) {
        return corteEvaluacionServicio.obtenerResponsablesPorPeriodo(id);
    }

    @PostMapping
    public ResponseEntity<LinkedHashMap<String, Object>> crearPeriodo(@RequestBody CrearPeriodoDTO dto) {
        return corteEvaluacionServicio.crear(dto);
    }

    @PutMapping("/{id}/informes")
    public ResponseEntity<LinkedHashMap<String, Object>> actualizarInformesPeriodo(
            @PathVariable Long id, 
            @RequestBody ActualizarInformesDTO dto) {
        return corteEvaluacionServicio.actualizarInformes(id, dto);
    }

    @PostMapping("/{id}/generar-snapshot")
    public ResponseEntity<LinkedHashMap<String, Object>> generarSnapshot(@PathVariable Long id) {
        return corteEvaluacionServicio.generarSnapshot(id);
    }

    @PutMapping("/{id}/recomendaciones")
    public ResponseEntity<LinkedHashMap<String, Object>> actualizarRecomendaciones(
            @PathVariable Long id,
            @RequestBody ActualizarRecomendacionesDTO dto) {
        return corteEvaluacionServicio.actualizarRecomendaciones(id, dto);
    }

    @PutMapping("/{id}/responsables")
    public ResponseEntity<LinkedHashMap<String, Object>> actualizarResponsables(
            @PathVariable Long id,
            @RequestBody ActualizarResponsablesDTO dto) {
        return corteEvaluacionServicio.actualizarResponsables(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<LinkedHashMap<String, Object>> eliminarPeriodo(@PathVariable Long id) {
        return corteEvaluacionServicio.eliminar(id);
    }
}
