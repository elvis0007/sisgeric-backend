package ec.com.eeasa.wssisrec.wssisrec.core.auth;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos.UsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos.UsuarioInfoDTO;
import ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos.ValidarUsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.core.auth.utils.PersonalServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.dtos.RolDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.mapper.RolMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models.RolUsuario;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.JWT.JWTService;

@Service
public class UsuarioService {
  private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private PersonalServicio personalServicio;

  @Autowired
  private RolUsuarioRepositorio rolUsuarioRepositorio;

  @Autowired
  private RolMapper rolMapper;

  @Autowired
  private JWTService jwtService;

  public ResponseEntity<?> getUsuarioInformacion(String DSGUS_CUENTA) {
    try {
      ValidarUsuarioDTO validarUsuarioDTO = personalServicio.validarUsuario();
      if (validarUsuarioDTO == null) {
        return EstadoRespuestas.respuestaError("NO EXISTE INFORMACION VALIDARUSUARIODTO");
      }
      UsuarioDTO usuario = personalServicio.findUsuarioById(DSGUS_CUENTA, validarUsuarioDTO.getToken());
      if (usuario == null) {
        return EstadoRespuestas.respuestaError("NO EXISTE INFORMACION USURIODTO");
      }
      UserDetails userDetails = userDetailsService.loadUserByUsername(DSGUS_CUENTA);
      String token = jwtService.generateToken(userDetails);
      UsuarioInfoDTO dto = convertirInfoDTO(usuario, token, validarUsuarioDTO);
      return ResponseEntity.ok().body(dto);
    } catch (Exception e) {
      logger.error("Error al obtener información del usuario: {}", e.getMessage());
      return EstadoRespuestas.respuestaError(e.getMessage());
    }
  }

  private UsuarioInfoDTO convertirInfoDTO(UsuarioDTO usuario, String token, ValidarUsuarioDTO validarUsuarioDTO) {
    UsuarioInfoDTO usuarioInfoDTO = new UsuarioInfoDTO();
    usuarioInfoDTO.setDsgus_cuenta(usuario.getDsgus_cuenta());
    usuarioInfoDTO.setNombre_completo(usuario.getNombre() + " " + usuario.getApellido());
    usuarioInfoDTO.setNumerol_rol(usuario.getNumero_rol());
    usuarioInfoDTO.setCodigo_per(usuario.getCodigo());
    usuarioInfoDTO.setFotografia(usuario.getFotografia());
    usuarioInfoDTO.setDepartamento(usuario.getDepartamento());
    usuarioInfoDTO.setToken(token);
    List<RolUsuario> roles = rolUsuarioRepositorio.findByDsgus_cuenta(usuario.getDsgus_cuenta());
    List<RolDTO> rolDTO = roles.stream().map((rol) -> rolMapper.convertirModelDTO(rol.getRol()))
        .collect(Collectors.toList());
    usuarioInfoDTO.setRoles(rolDTO);

    usuarioInfoDTO.setToken_sisgerh(validarUsuarioDTO.getToken());
    return usuarioInfoDTO;
  }

  public boolean validarRolUsuario(String usuario) {
    int conteo = this.rolUsuarioRepositorio.conteoRolRegistrado(usuario);
    return conteo > 0;
  }
}
