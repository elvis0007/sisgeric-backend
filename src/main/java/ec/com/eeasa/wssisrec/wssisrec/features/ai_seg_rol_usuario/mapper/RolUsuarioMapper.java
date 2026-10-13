package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.mapper;

import java.util.Date;

import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos.UsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.models.Rol;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.ActualizarRolUsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.NuevoRolUsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.UsuarioResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.UsuarioRolDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models.RolUsuario;

@Component
public class RolUsuarioMapper {
  public UsuarioRolDTO convertirModelDTO(RolUsuario rolUsuario) {
    UsuarioRolDTO usuarioRolDTO = new UsuarioRolDTO();
    usuarioRolDTO.setAseru_codigo(rolUsuario.getAseru_codigo());
    usuarioRolDTO.setDsgus_cuenta(rolUsuario.getDsgus_cuenta());
    usuarioRolDTO.setFecha(rolUsuario.getAseru_fecha());
    usuarioRolDTO.setNombre_completo(rolUsuario.getAseru_nombre() + " " + rolUsuario.getAseru_apellido());
    usuarioRolDTO.setDepartamento(rolUsuario.getAseru_departamento());
    usuarioRolDTO.setCodigo_per(rolUsuario.getAseru_codigo_per());
    usuarioRolDTO.setEmail(rolUsuario.getAseru_correo());
    return usuarioRolDTO;
  }

  public RolUsuario convertirDTOModel(NuevoRolUsuarioDTO nuevoRolUsuarioDTO, UsuarioDTO usuarioDTO, Rol rol) {
    RolUsuario rolUsuario = new RolUsuario();
    // UsuarioDTO usuarioDTO =
    // personalServicio.findUsuarioById(nuevoRolUsuarioDTO.getDsgus_cuenta(),
    // tokenSisgerh);
    rolUsuario.setAseru_codigo(null);
    rolUsuario.setDsgus_cuenta(nuevoRolUsuarioDTO.getAseru_cuenta());
    rolUsuario.setRol(rol);
    rolUsuario.setAseru_estado('1');
    rolUsuario.setAseru_fecha(new Date());
    rolUsuario.setAseru_usuario(nuevoRolUsuarioDTO.getAuditoria().getUsuario());
    rolUsuario.setAseru_nombre(usuarioDTO.getNombre());
    rolUsuario.setAseru_apellido(usuarioDTO.getApellido());
    rolUsuario.setAseru_num_rol(usuarioDTO.getNumero_rol());
    rolUsuario.setAseru_departamento(usuarioDTO.getDepartamento());
    rolUsuario.setAseru_codigo_per(usuarioDTO.getCodigo());
    rolUsuario.setAseru_correo(usuarioDTO.getEmail());
    return rolUsuario;
  }

  public RolUsuario convertirActualizarDTOModel(RolUsuario rolUsuarioOld, ActualizarRolUsuarioDTO dto,
      UsuarioDTO usuarioDTO) {
    // Actualizar con la información del nuevo usuario
    rolUsuarioOld.setDsgus_cuenta(usuarioDTO.getDsgus_cuenta());
    rolUsuarioOld.setAseru_fecha(new Date());
    rolUsuarioOld.setAseru_usuario(dto.getAuditoria().getUsuario());
    rolUsuarioOld.setAseru_apellido(usuarioDTO.getApellido());
    rolUsuarioOld.setAseru_nombre(usuarioDTO.getNombre());
    rolUsuarioOld.setAseru_codigo_per(usuarioDTO.getCodigo());
    rolUsuarioOld.setAseru_correo(usuarioDTO.getEmail());
    return rolUsuarioOld;
  }

  public UsuarioResponsableDTO convertirResponsableDTOModel(RolUsuario rolUsuario) {
    UsuarioResponsableDTO dto = new UsuarioResponsableDTO();

    dto.setNombre_completo(rolUsuario.getAseru_nombre() + " " + rolUsuario.getAseru_apellido());
    dto.setAseru_codigo_per(rolUsuario.getAseru_codigo_per());
    return dto;
  }

}
