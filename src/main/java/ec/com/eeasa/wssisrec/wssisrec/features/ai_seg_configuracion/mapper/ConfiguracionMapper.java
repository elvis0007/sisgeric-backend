package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.mapper;

import java.util.Date;

import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.dtos.ActualizarConfiguracionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.dtos.ConfiguracionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.dtos.NuevaConfiguracionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.models.Configuracion;

@Component
public class ConfiguracionMapper {
  public ConfiguracionDTO convertirModelDto(Configuracion configuracion) {
    ConfiguracionDTO dto = new ConfiguracionDTO();
    dto.setAseco_codigo(configuracion.getAseco_codigo());
    dto.setAseco_host(configuracion.getAseco_host());
    dto.setAseco_port(configuracion.getAseco_port());
    dto.setAseco_user(configuracion.getAseco_user());
    dto.setAseco_nombre_host(configuracion.getAseco_nombre_host());
    dto.setAseco_directorio(configuracion.getAseco_directorio());
    dto.setAseco_size_file_mb(configuracion.getAseco_size_file_mb());
    return dto;
  }

  public Configuracion convertirCrearDtoModel(NuevaConfiguracionDTO dto) {
    Configuracion configuracion = new Configuracion();
    configuracion.setAseco_codigo(null);
    configuracion.setAseco_host(dto.getAseco_host());
    configuracion.setAseco_port(dto.getAseco_port());
    configuracion.setAseco_user(dto.getAseco_user());
    configuracion.setAseco_contrasenia(dto.getAseco_contrasenia());
    configuracion.setAseco_nombre_host(dto.getAseco_nombre_host());
    configuracion.setAseco_directorio(dto.getAseco_directorio());
    configuracion.setAseco_size_file_mb(dto.getAseco_size_file_mb());
    configuracion.setAseco_estado('1');
    configuracion.setAseco_fecha(new Date());
    configuracion.setAseco_usuario(dto.getAuditoria().getUsuario());
    return configuracion;
  }

  public Configuracion convertirActualizarDtoModel(ActualizarConfiguracionDTO dto, Configuracion config) {
    Configuracion configuracion = new Configuracion();
    configuracion.setAseco_codigo(config.getAseco_codigo());
    configuracion.setAseco_host(dto.getAseco_host());
    configuracion.setAseco_port(dto.getAseco_port());
    configuracion.setAseco_user(dto.getAseco_user());
    
    // Solo actualizar la contraseña si se proporciona y no está vacía
    if (dto.getAseco_contrasenia() != null && !dto.getAseco_contrasenia().trim().isEmpty()) {
      configuracion.setAseco_contrasenia(dto.getAseco_contrasenia());
    } else {
      // Mantener la contraseña existente
      configuracion.setAseco_contrasenia(config.getAseco_contrasenia());
    }
    
    configuracion.setAseco_nombre_host(dto.getAseco_nombre_host());
    configuracion.setAseco_directorio(dto.getAseco_directorio());
    configuracion.setAseco_size_file_mb(dto.getAseco_size_file_mb());
    configuracion.setAseco_estado(config.getAseco_estado());
    configuracion.setAseco_fecha(new Date());
    configuracion.setAseco_usuario(dto.getAuditoria().getUsuario());
    return configuracion;
  }

}
