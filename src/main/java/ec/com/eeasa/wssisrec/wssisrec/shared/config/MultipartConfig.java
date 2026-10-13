package ec.com.eeasa.wssisrec.wssisrec.shared.config;

import javax.servlet.MultipartConfigElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.ConfiguracionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.enums.ConfiguracionEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.models.Configuracion;

/**
 * Configuración dinámica para el tamaño máximo de archivos multipart
 * basada en los valores almacenados en la base de datos
 */
@Configuration
public class MultipartConfig {
    private static final Logger logger = LoggerFactory.getLogger(MultipartConfig.class);
    @Autowired
    private ConfiguracionRepositorio configuracionRepositorio;

    /**
     * Configura dinámicamente el tamaño máximo de archivos y requests multipart
     * usando los valores de la base de datos
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        try {
            // Obtener la configuración desde la base de datos
            Configuracion configuracion = configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo())
                    .orElse(null);
            
            long tamanoMaximoMB = 200L;
            if (configuracion != null && configuracion.getAseco_size_file_mb() != null && configuracion.getAseco_size_file_mb() > 0) {
                // Dar margen a nivel de contenedor Tomcat para evitar caídas de socket TCP (0 Unknown Error) y permitir validación HTTP 400
                tamanoMaximoMB = Math.max(configuracion.getAseco_size_file_mb(), 200L);
            }
            
            // Configurar el tamaño máximo de archivo y request
            factory.setMaxFileSize(DataSize.ofMegabytes(tamanoMaximoMB));
            factory.setMaxRequestSize(DataSize.ofMegabytes(tamanoMaximoMB));
        } catch (Exception e) {
            // En caso de error, usar 200MB por defecto
            logger.error("Error al obtener configuración de BD, usando 200MB por defecto: " + e.getMessage());
            factory.setMaxFileSize(DataSize.ofMegabytes(200L));
            factory.setMaxRequestSize(DataSize.ofMegabytes(200L));
        }
        
        return factory.createMultipartConfig();
    }
} 