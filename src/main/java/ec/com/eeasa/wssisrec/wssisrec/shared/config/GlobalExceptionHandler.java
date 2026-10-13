package ec.com.eeasa.wssisrec.wssisrec.shared.config;

import java.util.LinkedHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

/**
 * Captura excepciones globales del sistema para retornar respuestas HTTP limpias y estructuradas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<LinkedHashMap<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException exc) {
        return EstadoRespuestas.respuestaError(
            "El archivo excede el tamaño máximo de subida permitido (50 MB). Por favor seleccione un archivo más pequeño."
        );
    }
}
