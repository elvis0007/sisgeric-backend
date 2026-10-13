package ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones;

import org.springframework.web.bind.annotation.RestController;


import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Autor: Mateo Diaz
*/

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionesControlador {
    
    @Autowired
    NotificacionesServicio notificacionesServicio;
    
    @GetMapping("/notificaciones-usuario/{anotif_usuario_per}")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerNotificacionesPorUsuario(
            @PathVariable String anotif_usuario_per,
            @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "100") Integer limite) {
        return this.notificacionesServicio.obtenerNotificacionesPorUsuarioConLimite(anotif_usuario_per, limite != null ? limite : 100);
    }
    
    @PutMapping("/actualizar-notif/{anotif_codigo}")
    public ResponseEntity<LinkedHashMap<String, Object>> actualizarNotificacionPorID(@PathVariable Long anotif_codigo) {
        return this.notificacionesServicio.actualizarEstadoNotificaciones(anotif_codigo);
    }

    @GetMapping("/buscar-recom-ids")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerIdsPorMemoYNumRec(
            @org.springframework.web.bind.annotation.RequestParam String memorando,
            @org.springframework.web.bind.annotation.RequestParam Integer numRec) {
        return this.notificacionesServicio.obtenerIdsPorMemoYNumRec(memorando, numRec);
    }

    @GetMapping("/cadena-ids/{arere_codigo}")
    public ResponseEntity<LinkedHashMap<String, Object>> obtenerCadenaIdsPorArereCodigo(@PathVariable Long arere_codigo) {
        return this.notificacionesServicio.obtenerCadenaIdsPorArereCodigo(arere_codigo);
    }

    @GetMapping("/test/crear")
    public ResponseEntity<LinkedHashMap<String, Object>> crearNotificacionesPrueba(@RequestParam String usuario) {
        return this.notificacionesServicio.crearNotificacionesPrueba(usuario);
    }
}
