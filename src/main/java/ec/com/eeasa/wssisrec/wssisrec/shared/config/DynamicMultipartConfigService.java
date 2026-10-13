package ec.com.eeasa.wssisrec.wssisrec.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.ConfiguracionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.enums.ConfiguracionEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.models.Configuracion;

/**
 * Servicio para gestionar la configuración dinámica de multipart
 */
@Service
public class DynamicMultipartConfigService {

    private static final Logger logger = LoggerFactory.getLogger(DynamicMultipartConfigService.class);
    @Autowired
    private ConfiguracionRepositorio configuracionRepositorio;

    /**
     * Obtiene el tamaño máximo configurado en la base de datos
     * 
     * @return Tamaño máximo en MB
     */
    public Long obtenerTamanoMaximo() {
        try {
            Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo())
                    .orElse(null);
            
            if (configuracion != null && configuracion.getAseco_size_file_mb() != null && configuracion.getAseco_size_file_mb() > 0) {
                return configuracion.getAseco_size_file_mb();
            } else {
                return 20L; // Valor por defecto
            }
        } catch (Exception e) {
            logger.error("Error al obtener configuración de BD: " + e.getMessage());
            return 20L; // Valor por defecto en caso de error
        }
    }

    /**
     * Verifica si el archivo excede el límite configurado
     * 
     * @param tamanoArchivoBytes Tamaño del archivo en bytes
     * @return true si excede el límite, false en caso contrario
     */
    public boolean excedeLimite(long tamanoArchivoBytes) {
        Long tamanoMaximoMB = obtenerTamanoMaximo();
        long tamanoMaximoBytes = tamanoMaximoMB * 1024 * 1024;
        return tamanoArchivoBytes > tamanoMaximoBytes;
    }

    /**
     * Obtiene el límite en bytes
     * 
     * @return Límite máximo en bytes
     */
    public long obtenerLimiteBytes() {
        return obtenerTamanoMaximo() * 1024 * 1024;
    }
} 