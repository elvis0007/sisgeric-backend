package ec.com.eeasa.wssisrec.wssisrec.shared.utils.Ftp;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FtpService {
  private static final Logger logger = LoggerFactory.getLogger(FtpService.class);
  
  // Usar ThreadLocal para que cada hilo HTTP maneje su propia conexión FTP de forma aislada e independiente
  private final ThreadLocal<FTPClient> threadLocalFtp = new ThreadLocal<>();

  private FTPClient getConeccionFtp() {
    return threadLocalFtp.get();
  }

  /**
   * Valida si el tamaño del archivo no excede el límite máximo permitido
   * 
   * @param archivo MultipartFile a validar
   * @param tamanoMaximoMB Tamaño máximo permitido en megabytes
   * @return true si el archivo es válido, false si excede el límite
   */
  public boolean validarTamanoArchivo(MultipartFile archivo, Long tamanoMaximoMB) {
    if (archivo == null || archivo.isEmpty()) {
      return false;
    }
    
    if (tamanoMaximoMB == null || tamanoMaximoMB <= 0) {
      return true; // Si no hay límite configurado, permitir cualquier tamaño
    }
    
    // Convertir MB a bytes (1 MB = 1024 * 1024 bytes)
    long tamanoMaximoBytes = tamanoMaximoMB * 1024 * 1024;
    long tamanoArchivoBytes = archivo.getSize();
    
    return tamanoArchivoBytes <= tamanoMaximoBytes;
  }

  /**
   * Obtiene el tamaño del archivo en megabytes
   * 
   * @param archivo MultipartFile
   * @return Tamaño del archivo en MB
   */
  public double obtenerTamanoArchivoMB(MultipartFile archivo) {
    if (archivo == null || archivo.isEmpty()) {
      return 0.0;
    }
    
    return (double) archivo.getSize() / (1024.0 * 1024.0);
  }

  public boolean conectarServidorFTP(String servidor, String usuario, String password) {
    FTPClient coneccionFtp = getConeccionFtp();
    
    // Si ya hay una conexión activa en este hilo, desconectarla primero
    if (coneccionFtp != null && coneccionFtp.isConnected()) {
      try {
        coneccionFtp.logout();
        coneccionFtp.disconnect();
      } catch (IOException e) {
        // Ignorar errores al desconectar previo
      }
    }
    
    coneccionFtp = new FTPClient();
    coneccionFtp.setConnectTimeout(5000);
    coneccionFtp.setDefaultTimeout(5000);
    coneccionFtp.setDataTimeout(5000);
    threadLocalFtp.set(coneccionFtp);
    int reply;

    try {
      coneccionFtp.connect(servidor);
      reply = coneccionFtp.getReplyCode();

      if (!FTPReply.isPositiveCompletion(reply)) {
        try {
          coneccionFtp.disconnect();
        } catch (IOException e) {
          logger.error("No fue posible conectarse al servidor , respuesta : " + reply + ". " + e.getMessage());
        }
        threadLocalFtp.remove();
        return false;
      }

      boolean exito = coneccionFtp.login(usuario, password);
      if (!exito) {
        try {
          coneccionFtp.logout();
          coneccionFtp.disconnect();
        } catch (IOException e) {
          // Ignorar errores al desconectar
        }
        threadLocalFtp.remove();
      }
      return exito;
    } catch (IOException e) {
      logger.error("Error al conectar con el servidor: " + servidor + ". " + e.getMessage());
      try {
        if (coneccionFtp != null && coneccionFtp.isConnected()) {
          coneccionFtp.disconnect();
        }
      } catch (IOException ex) {
        // Ignorar errores al desconectar
      }
      threadLocalFtp.remove();
      return false;
    }
  }

  public boolean desconectarServidorFTP() {
    FTPClient coneccionFtp = getConeccionFtp();
    if (coneccionFtp != null) {
      try {
        if (coneccionFtp.isConnected()) {
          try {
            coneccionFtp.logout();
          } catch (IOException e) {
            logger.warn("Error en logout FTP: {}", e.getMessage());
          }
          try {
            coneccionFtp.disconnect();
          } catch (IOException f) {
            logger.error("Error al desconectarse del servidor FTP: {}", f.getMessage());
          }
        }
      } finally {
        threadLocalFtp.remove();
      }
    }
    return true;
  }

  public boolean subirArchivoAlServidorFTP(MultipartFile archivo, String rutaFtp, String nombreArchivo) {
    FTPClient coneccionFtp = getConeccionFtp();
    if (coneccionFtp == null || !coneccionFtp.isConnected()) {
      logger.error("No se puede subir archivo: No hay conexión FTP activa en este hilo");
      return false;
    }

    try (InputStream inputStream = archivo.getInputStream()) {
      coneccionFtp.setFileType(FTP.BINARY_FILE_TYPE);
      coneccionFtp.enterLocalPassiveMode();
      if (!crearDirectorioSiNoExiste(rutaFtp)) {
        return false;
      }
      return coneccionFtp.storeFile(rutaFtp + nombreArchivo, inputStream);
    } catch (IOException e) {
      logger.error("No se pudo subir el archivo al servidor: " + e.getMessage());
      return false;
    }
  }

  public boolean eliminarArchivoDeServidorFTP(String rutaRelativaFtp) {
    FTPClient coneccionFtp = getConeccionFtp();
    if (coneccionFtp == null || !coneccionFtp.isConnected()) {
      logger.error("No se puede eliminar archivo: No hay conexión FTP activa en este hilo");
      return false;
    }

    try {
      if (rutaRelativaFtp != null) {
        if (coneccionFtp.deleteFile(rutaRelativaFtp)) {
          logger.info("Eliminacion exitosa.");
          return true;
        }
      }
      logger.error("Archivo no registrado en la Base datos.");
      return false;
    } catch (IOException e) {
      logger.error("No se pudo eliminar el archivo del servidor. " + e.getMessage());
    }
    return false;
  }

  public boolean descargarArchivoDeServidorFTP(String rutaRelativaFtp, String copiarRuta) {
    FTPClient coneccionFtp = getConeccionFtp();
    if (coneccionFtp == null || !coneccionFtp.isConnected()) {
      logger.error("No se puede descargar archivo: No hay conexión FTP activa en este hilo");
      return false;
    }

    try (FileOutputStream fos = new FileOutputStream(copiarRuta)) {
      return coneccionFtp.retrieveFile(rutaRelativaFtp, fos);
    } catch (FileNotFoundException e) {
      logger.error("No se pudo obtener la referencia a la carpeta relativa, verifique la ruta y los permisos: " + e.getMessage());
      return false;
    } catch (IOException e) {
      logger.error("No se pudo descargar el archivo: " + e.getMessage());
      return false;
    } 
  }

  public byte[] descargarArchivoFTPNuevo(String rutaRelativaFtp) throws IOException {
    FTPClient coneccionFtp = getConeccionFtp();
    if (coneccionFtp == null || !coneccionFtp.isConnected()) {
      logger.error("No se puede descargar archivo: No hay conexión FTP activa en este hilo");
      return null;
    }

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      coneccionFtp.enterLocalPassiveMode();
      coneccionFtp.setFileType(FTP.BINARY_FILE_TYPE);

      boolean success = coneccionFtp.retrieveFile(rutaRelativaFtp, outputStream);
      
      if (!success) {
        logger.error("Error al descargar el archivo: " + rutaRelativaFtp);
        return null;
      }

      return outputStream.toByteArray();
    } catch (Exception e) {
      logger.error("Error al descargar archivo: " + e.getMessage());
      return null;
    }
  }

  public boolean subirArchivoBytes(byte[] contenido, String rutaFtp, String nombreArchivo) {
    FTPClient coneccionFtp = getConeccionFtp();
    if (coneccionFtp == null || !coneccionFtp.isConnected()) {
      logger.error("No se puede subir archivo bytes: No hay conexión FTP activa en este hilo");
      return false;
    }

    if (contenido == null) {
      logger.error("Contenido nulo al intentar subir archivo bytes");
      return false;
    }
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(contenido)) {
      coneccionFtp.setFileType(FTP.BINARY_FILE_TYPE);
      coneccionFtp.enterLocalPassiveMode();
      if (!crearDirectorioSiNoExiste(rutaFtp)) {
        return false;
      }
      return coneccionFtp.storeFile(rutaFtp + nombreArchivo, inputStream);
    } catch (IOException e) {
      logger.error("No se pudo subir el archivo bytes al servidor: {}", e.getMessage());
      return false;
    }
  }

  public boolean verificarArchivoNombreExiste(String rutaRelativaFtp) throws IOException {
    FTPClient coneccionFtp = getConeccionFtp();
    if (coneccionFtp == null) return false;
    FTPFile remoteFiles = coneccionFtp.mlistFile(rutaRelativaFtp);
    return remoteFiles == null;
  }

  public boolean crearDirectorioSiNoExiste(String rutaDirectorio) throws IOException {
    FTPClient coneccionFtp = getConeccionFtp();
    if (coneccionFtp == null) return false;

    String[] directorios = rutaDirectorio.split("/");
    String rutaAcumulada = "";

    for (String dir : directorios) {
      if (!dir.isEmpty()) {
        rutaAcumulada += "/" + dir;
        if (!coneccionFtp.changeWorkingDirectory(rutaAcumulada)) {
          if (!coneccionFtp.makeDirectory(rutaAcumulada)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * Limpia la conexión FTP del hilo actual de forma segura
   */
  public void limpiarConexionFTP() {
    desconectarServidorFTP();
  }
}
