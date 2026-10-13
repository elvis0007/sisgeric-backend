package ec.com.eeasa.wssisrec.wssisrec.features.auditoria;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/bitacora")
public class BitacoraControlador {
  @Autowired
  BitacoraServicio bitacoraServicio;

  @GetMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerBitacoras() {
    return bitacoraServicio.obtener();
  }

  @GetMapping("/filtrarPorFechas")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorFecha(
      @RequestParam("fechaInicio") String fechaInicio,
      @RequestParam("fechaFin") String fechaFin) {
    return bitacoraServicio.obtenerPorFechas(fechaInicio, fechaFin);
  }

  @GetMapping("/filtrarPorUsuario")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorUsuario(
      @RequestParam("usuario") String usuario) {
    return bitacoraServicio.obtenerPorUsuario(usuario);
  }
}
