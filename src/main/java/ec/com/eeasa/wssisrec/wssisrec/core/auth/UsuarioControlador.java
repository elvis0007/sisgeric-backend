package ec.com.eeasa.wssisrec.wssisrec.core.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UsuarioControlador {
  @Autowired
  private UsuarioService usuarioService;

  @GetMapping
  public ResponseEntity<?> getUsuarioInformacion(@RequestParam("DSGUS_CUENTA") String dsgus_cuenta) throws Exception {
    ResponseEntity<?> usuarioService2 = usuarioService.getUsuarioInformacion(dsgus_cuenta);
    return usuarioService2;
  }

  @GetMapping("/existe-rol")
  public ResponseEntity<?> validarRolUsuario(@RequestParam("DSGUS_CUE") String usuario) throws Exception {
      boolean usuarioService2 = usuarioService.validarRolUsuario(usuario);
      return ResponseEntity.ok(usuarioService2);
  }
  
}
