package ec.com.eeasa.wssisrec.wssisrec.shared.utils;

import java.util.LinkedHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class EstadoRespuestas {
  private Object status;
  private String message;

  public Object getStatus() {
    return this.status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getMessage() {
    return this.message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public static ResponseEntity<LinkedHashMap<String, Object>> respuestaError(String mensaje) {
    LinkedHashMap<String, Object> response = new LinkedHashMap<>();
    response.put("codigo", 0);
    response.put("mensaje", mensaje);
    response.put("datos", null);
    response.put("status", HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<LinkedHashMap<String, Object>>(response, HttpStatus.BAD_REQUEST);
  }

  public static ResponseEntity<LinkedHashMap<String, Object>> noEncontrado(String mensaje) {
    LinkedHashMap<String, Object> response = new LinkedHashMap<>();
    response.put("codigo", 0);
    response.put("mensaje", mensaje);
    response.put("datos", null);
    response.put("status", HttpStatus.NOT_FOUND.value());
    return new ResponseEntity<LinkedHashMap<String, Object>>(response, HttpStatus.NOT_FOUND);
  }

  public static ResponseEntity<LinkedHashMap<String, Object>> respuestaCreacion(String mensaje) {
    LinkedHashMap<String, Object> response = new LinkedHashMap<>();
    response.put("codigo", 1);
    response.put("mensaje", mensaje);
    response.put("datos", null);
    response.put("status", HttpStatus.CREATED.value());
    return new ResponseEntity<LinkedHashMap<String, Object>>(response, HttpStatus.CREATED);
  }

  public static ResponseEntity<LinkedHashMap<String, Object>> respuestaObtencion(String mensaje, Object datos) {
    LinkedHashMap<String, Object> response = new LinkedHashMap<>();
    response.put("codigo", 1);
    response.put("mensaje", mensaje);
    response.put("datos", datos);
    response.put("status", HttpStatus.OK.value());
    return new ResponseEntity<LinkedHashMap<String, Object>>(response, HttpStatus.OK);
  }

  public static ResponseEntity<LinkedHashMap<String, Object>> respuestaEliminacion(String mensaje) {
    LinkedHashMap<String, Object> response = new LinkedHashMap<>();
    response.put("codigo", 1);
    response.put("mensaje", mensaje);
    response.put("datos", null);
    response.put("status", HttpStatus.OK.value());
    return new ResponseEntity<LinkedHashMap<String, Object>>(response, HttpStatus.OK);
  }

}
