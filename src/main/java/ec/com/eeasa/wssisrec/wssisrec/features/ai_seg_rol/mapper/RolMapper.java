package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.mapper;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.dtos.ActualizarRolDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.dtos.NuevoRolDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.dtos.RolDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.models.Rol;

@Component
public class RolMapper {
  private static final Logger logger = LoggerFactory.getLogger(RolMapper.class);
  public RolDTO convertirModelDTO(Rol rol) {
    RolDTO rolDTO = new RolDTO();
    rolDTO.setAsero_codigo(rol.getAsero_codigo());
    rolDTO.setAsero_descripcion(rol.getAsero_descripcion());
    rolDTO.setAsero_estado(rol.getAsero_estado());
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    String formattedDate = formatter.format(rol.getAsero_fecha());
    rolDTO.setAsero_fecha(formattedDate);
    return rolDTO;
  }

  public Rol convertirDTOModel(NuevoRolDTO nuevoRolDTO) {
    try {
      Rol rol = new Rol();
      rol.setAsero_codigo(null);
      rol.setAsero_descripcion(nuevoRolDTO.getAsero_descripcion());
      rol.setAsero_estado('1');
      rol.setAsero_fecha(new Date());
      rol.setAsero_usuario(nuevoRolDTO.getAuditoria().getUsuario());
      return rol;
    } catch (Exception e) {
      logger.error("Error al convertir el DTO a modelo: {}", e.getMessage());
      return null;
    }
  }

  public Rol convertirActualizarDTOModel(ActualizarRolDTO actualizarRolDTO, Long asero_codigo, Rol rolInfo) {
    Rol rol = new Rol();
    rol.setAsero_codigo(asero_codigo);
    rol.setAsero_descripcion(actualizarRolDTO.getAsero_descripcion());
    rol.setAsero_estado(rolInfo.getAsero_estado());
    rol.setAsero_fecha(new Date());
    rol.setAsero_usuario(actualizarRolDTO.getAuditoria().getUsuario());
    return rol;
  }
}
