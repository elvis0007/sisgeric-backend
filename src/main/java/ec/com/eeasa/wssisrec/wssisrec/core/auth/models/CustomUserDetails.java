package ec.com.eeasa.wssisrec.wssisrec.core.auth.models;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos.UsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models.RolUsuario;

public class CustomUserDetails implements UserDetails {

  private final UsuarioDTO usuarioDto;
  private final RolUsuarioRepositorio rolUsuarioRepositorio;

  public CustomUserDetails(UsuarioDTO usuarioDto, RolUsuarioRepositorio rolUsuarioRepositorio) {
    this.usuarioDto = usuarioDto;
    this.rolUsuarioRepositorio = rolUsuarioRepositorio;
  }

  @Override
  @Transactional
  public Collection<? extends GrantedAuthority> getAuthorities() {
    try {
      List<RolUsuario> roles = rolUsuarioRepositorio.findByDsgus_cuenta(usuarioDto.getDsgus_cuenta());
      if (roles.isEmpty()) {
        return Collections.emptyList();
      }
      return roles.stream().map(rol -> new SimpleGrantedAuthority(rol.getRol().getAsero_codigo().toString()))
          .collect(Collectors.toList());
    } catch (Exception e) {
      return Collections.emptyList();
    }

  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return usuarioDto.getDsgus_cuenta();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;

  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;

  }

  @Override
  public boolean isEnabled() {
    return true;
  }

}
