package ec.com.eeasa.wssisrec.wssisrec.shared.utils.Ftp;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.ConfiguracionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.enums.ConfiguracionEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.models.Configuracion;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.FileValidationUtils;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.Ftp.dto.ArchivoDTO;

@RestController
@RequestMapping("/api/archivo")
public class FtpControlador {

  private static final Logger logger = LoggerFactory.getLogger(FtpControlador.class);
  @Autowired
  private FtpService ftpService;

  @Autowired
  private ConfiguracionRepositorio configuracionRepositorio;

  @PutMapping(value = "{id}", consumes = "multipart/form-data")
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarArchivo(@PathVariable Long id,
      @RequestParam("archivo") MultipartFile archivo) {
    return EstadoRespuestas.respuestaCreacion("Archivo actualizado correctamente.");
  }

  @GetMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> obtener(@RequestParam("ruta") String ruta) {
    try {
      Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).get();
      boolean estaConectado = ftpService.conectarServidorFTP(configuracion.getAseco_host(), configuracion.getAseco_user(),
          configuracion.getAseco_contrasenia());
      if (!estaConectado) {
        return EstadoRespuestas.respuestaError("Error del servidor externo (FTP/NAS): No se pudo establecer conexión. Consulte con el administrador del sistema.");
      }
      String rutaRelativa = ruta;
      if (ruta.contains(configuracion.getAseco_nombre_host())) {
        rutaRelativa = ruta.split(configuracion.getAseco_nombre_host())[1];
      }

      byte[] fileBytes = ftpService.descargarArchivoFTPNuevo(rutaRelativa);
      ftpService.desconectarServidorFTP();
      ArchivoDTO archivoDTO = new ArchivoDTO();
      archivoDTO.setArchivo(fileBytes);
      return EstadoRespuestas.respuestaObtencion("Archivo obtenido", archivoDTO);
    } catch (Exception e) { 
      logger.error("Error al obtener el archivo: " + e.getMessage(), e); 
      ftpService.desconectarServidorFTP();
      return EstadoRespuestas.respuestaError("Error al obtener el archivo: " + e.getMessage());
    }
  }

  @PostMapping(consumes = "multipart/form-data")
  public ResponseEntity<LinkedHashMap<String, Object>> registrar(@RequestParam("archivo") MultipartFile archivo,
      @RequestParam("ruta") String ruta) {
    try {
      Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).get();
      
      // Validar tamaño del archivo usando la nueva utilidad
      FileValidationUtils.FileValidationResult validationResult = 
          FileValidationUtils.validarTamanoArchivo(archivo, configuracion);
      
      if (!validationResult.isValido()) {
        return EstadoRespuestas.respuestaError(validationResult.getMensaje());
      }
      
      boolean estaConectado = ftpService.conectarServidorFTP(configuracion.getAseco_host(), configuracion.getAseco_user(),
          configuracion.getAseco_contrasenia());
      if (!estaConectado) {
        return EstadoRespuestas.respuestaError("Error del servidor externo (FTP/NAS): No se pudo establecer conexión. Consulte con el administrador del sistema.");
      }
      String nombreArchivo = archivo.getOriginalFilename();
      ftpService.subirArchivoAlServidorFTP(archivo, ruta, nombreArchivo);
      ftpService.desconectarServidorFTP();
      return EstadoRespuestas.respuestaCreacion("Archivo subido exitosamente");
    } catch (Exception e) {
      ftpService.desconectarServidorFTP();
      logger.error("Error al subir el archivo: " + e.getMessage(), e);
      return EstadoRespuestas.respuestaError("Error al subir el archivo: " + e.getMessage());
    }
  }

  @DeleteMapping()
  public ResponseEntity<LinkedHashMap<String, Object>> eliminar(@RequestParam("ruta") String ruta) {
    try {
      Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).get();
      boolean estaConectado = ftpService.conectarServidorFTP(configuracion.getAseco_host(), configuracion.getAseco_user(),
          configuracion.getAseco_contrasenia());
      if (!estaConectado) {
        return EstadoRespuestas.respuestaError("Error del servidor externo (FTP/NAS): No se pudo establecer conexión. Consulte con el administrador del sistema.");
      }
      String rutaRelativa = ruta;
      if (ruta.contains(configuracion.getAseco_nombre_host())) {
        rutaRelativa = ruta.split(configuracion.getAseco_nombre_host())[1];
      }
      ftpService.eliminarArchivoDeServidorFTP(rutaRelativa);
      ftpService.desconectarServidorFTP();
      return EstadoRespuestas.respuestaEliminacion("Archivo eliminado correctamente");
    } catch (Exception e) {
      ftpService.desconectarServidorFTP();
      logger.error("Error al eliminar el archivo: " + e.getMessage(), e);
      return EstadoRespuestas.respuestaError("Error al eliminar el archivo: " + e.getMessage());
    }
  }

  @GetMapping("/limite-tamaño")
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerLimiteTamano() {
    try {
      Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).get();
      LinkedHashMap<String, Object> respuesta = new LinkedHashMap<>();
      respuesta.put("limite_maximo_mb", configuracion.getAseco_size_file_mb());
      respuesta.put("limite_maximo_bytes", configuracion.getAseco_size_file_mb() * 1024 * 1024);
      respuesta.put("limite_spring_activo", "Configuración dinámica activada");
      return EstadoRespuestas.respuestaObtencion("Límite de tamaño obtenido", respuesta);
    } catch (Exception e) {
      logger.error("Error al obtener el límite de tamaño: " + e.getMessage(), e);
      return EstadoRespuestas.respuestaError("Error al obtener el límite de tamaño: " + e.getMessage());
    }
  }
}
