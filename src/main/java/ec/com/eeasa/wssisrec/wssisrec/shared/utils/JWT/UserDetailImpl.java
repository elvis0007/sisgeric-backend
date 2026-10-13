package ec.com.eeasa.wssisrec.wssisrec.shared.utils.JWT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos.UsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos.ValidarUsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.core.auth.models.CustomUserDetails;
import ec.com.eeasa.wssisrec.wssisrec.core.auth.utils.PersonalServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioRepositorio;

@Service
public class UserDetailImpl implements UserDetailsService {
  private static final Logger logger = LoggerFactory.getLogger(UserDetailImpl.class);
  @Autowired
  private PersonalServicio personalServicio;
  @Autowired
  private RolUsuarioRepositorio rolUsuarioRepositorio;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    try {
      ValidarUsuarioDTO validarUsuarioDTO = personalServicio.validarUsuario();
      if (validarUsuarioDTO == null) {
        logger.error("Usuario no encontrado");
        return null;
      }
      UsuarioDTO usuario = personalServicio.findUsuarioById(username, validarUsuarioDTO.getToken());
      return new CustomUserDetails(usuario, rolUsuarioRepositorio);
    } catch (Exception e) {
      logger.error("Error al cargar el usuario: " + e.getMessage());
      return null;
    }

  }

}
