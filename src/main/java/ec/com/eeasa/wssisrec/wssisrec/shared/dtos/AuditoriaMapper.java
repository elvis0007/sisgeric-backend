package ec.com.eeasa.wssisrec.wssisrec.shared.dtos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;

public class AuditoriaMapper {
  private static final Logger logger = LoggerFactory.getLogger(AuditoriaMapper.class);
  public static AuditoriaDTO convertirAuditoriaJSONDTO(String auditoria) {
    ObjectMapper objectMapper = new ObjectMapper();
    AuditoriaDTO auditoriaDTO = null;
    try {
      auditoriaDTO = objectMapper.readValue(auditoria, AuditoriaDTO.class);
    } catch (Exception e) {
      logger.error("Error al convertir la auditoria a JSON: " + e.getMessage());
      return null;
    }
    return auditoriaDTO;
  }
}
