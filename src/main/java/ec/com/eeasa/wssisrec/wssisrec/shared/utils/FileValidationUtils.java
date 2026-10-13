package ec.com.eeasa.wssisrec.wssisrec.shared.utils;

import org.springframework.web.multipart.MultipartFile;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.models.Configuracion;

/**
 * Utilidades para validación de archivos
 */
public class FileValidationUtils {

    /**
     * Valida si el tamaño del archivo no excede el límite máximo permitido
     * 
     * @param archivo MultipartFile a validar
     * @param configuracion Configuración que contiene el límite máximo
     * @return Resultado de la validación
     */
    public static FileValidationResult validarTamanoArchivo(MultipartFile archivo, Configuracion configuracion) {
        if (archivo == null || archivo.isEmpty()) {
            return new FileValidationResult(false, "El archivo está vacío o no se ha proporcionado");
        }
        
        if (configuracion == null || configuracion.getAseco_size_file_mb() == null || configuracion.getAseco_size_file_mb() <= 0) {
            return new FileValidationResult(true, ""); // Si no hay límite configurado, permitir cualquier tamaño
        }
        
        // Convertir MB a bytes (1 MB = 1024 * 1024 bytes)
        long tamanoMaximoBytes = configuracion.getAseco_size_file_mb() * 1024 * 1024;
        long tamanoArchivoBytes = archivo.getSize();
        
        if (tamanoArchivoBytes <= tamanoMaximoBytes) {
            return new FileValidationResult(true, "");
        } else {
            double tamanoArchivoMB = (double) tamanoArchivoBytes / (1024.0 * 1024.0);
            String mensaje = String.format(
                "El archivo excede el límite permitido. Tamaño del archivo: %.2f MB, Límite máximo: %d MB", 
                tamanoArchivoMB, configuracion.getAseco_size_file_mb()
            );
            return new FileValidationResult(false, mensaje);
        }
    }

    /**
     * Obtiene el tamaño del archivo en megabytes
     * 
     * @param archivo MultipartFile
     * @return Tamaño del archivo en MB
     */
    public static double obtenerTamanoArchivoMB(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return 0.0;
        }
        
        return (double) archivo.getSize() / (1024.0 * 1024.0);
    }

    /**
     * Clase para encapsular el resultado de la validación
     */
    public static class FileValidationResult {
        private final boolean valido;
        private final String mensaje;

        public FileValidationResult(boolean valido, String mensaje) {
            this.valido = valido;
            this.mensaje = mensaje;
        }

        public boolean isValido() {
            return valido;
        }

        public String getMensaje() {
            return mensaje;
        }
    }
} 