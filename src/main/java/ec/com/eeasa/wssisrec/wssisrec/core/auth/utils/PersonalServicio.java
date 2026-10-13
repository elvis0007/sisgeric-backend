package ec.com.eeasa.wssisrec.wssisrec.core.auth.utils;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos.UsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos.ValidarUsuarioDTO;

@Service
public class PersonalServicio {
  @Value("${webservice.url}")
  private String webServiceUrl;
  @Value("${webservice.personal}")
  private String webServicePersonal;
  @Value("${webservice.auth}")
  private String webServiceAuth;
  @Value("${webservice.inDsgus_cuenta}")
  private String inDsgus_cuenta;
  @Value("${webservice.inDsgus_clave}")
  private String inDsgus_clave;
  @Autowired
  private final RestTemplate restTemplate;

  private String tokenSisgerh = null;

  public PersonalServicio(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public UsuarioDTO findUsuarioById(String DSGUS_CUENTA, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer"
        + token);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<Map> response = restTemplate.exchange(
        webServicePersonal + DSGUS_CUENTA,
        HttpMethod.GET, entity, Map.class);

    Map<String, Object> respuesta = (Map) response.getBody().get("DATA");

    return convertirUsuarioDTO(respuesta);
  }

  public UsuarioDTO findUsuarioByCodigoPer(String DMPER_CODIGO, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer" + token);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    String urlAll = webServiceUrl
        + "sisgerh/selectInformacionPersonaEEASA?inParametroBusqueda=CODIGO&inParametroValor=" + DMPER_CODIGO;


    ResponseEntity<Map> response = restTemplate.exchange(
        urlAll,
        HttpMethod.GET, entity,
        Map.class);
    List<Map<String, Object>> lista = (List<Map<String, Object>>) response.getBody().get("DATA");

    Map<String, Object> respeusta = lista.get(0);

    return convertirUsuarioNewDTO(respeusta);
  }

  public ValidarUsuarioDTO validarUsuario() {
    if (tokenSisgerh != null) {
      return convertirValidarUsuarioDTO(tokenSisgerh);
    }
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<Map> response = restTemplate.exchange(this.webServiceAuth + "/validarUsuario?inDsgus_cuenta="
        + this.inDsgus_cuenta + "&inDsgus_clave=" + this.inDsgus_clave, HttpMethod.GET, entity, Map.class);
    tokenSisgerh = (String) response.getBody().get("TOKEN");
    return convertirValidarUsuarioDTO(tokenSisgerh);
  }

  private ValidarUsuarioDTO convertirValidarUsuarioDTO(String tokenSisgerh) {
    ValidarUsuarioDTO validarUsuarioDTO = new ValidarUsuarioDTO();
    validarUsuarioDTO.setToken(tokenSisgerh);
    return validarUsuarioDTO;
  }

  private UsuarioDTO convertirUsuarioDTO(Map<String, Object> usuario) {
    UsuarioDTO usuarioDTO = new UsuarioDTO();
    usuarioDTO.setDsgus_cuenta((String) usuario.get("DSGUS_CUENTA"));
    usuarioDTO.setDepartamento((String) usuario.get("DEPARTAMENTO"));
    usuarioDTO.setApellido((String) usuario.get("APELLIDOS"));
    usuarioDTO.setNombre((String) usuario.get("NOMBRES"));
    usuarioDTO.setNumero_rol((String) usuario.get("DMPER_NUMERO_ROL"));
    usuarioDTO.setFotografia((String) usuario.get("DMPER_FOTOGRAFIA"));
    usuarioDTO.setCodigo((String) usuario.get("DMPER_CODIGO"));
    usuarioDTO.setEmail((String) usuario.get("EMAIL"));
    return usuarioDTO;

  }

  private UsuarioDTO convertirUsuarioNewDTO(Map<String, Object> usuario) {
    UsuarioDTO usuarioDTO = new UsuarioDTO();
    usuarioDTO.setDsgus_cuenta((String) usuario.get("USUARIO"));
    usuarioDTO.setDepartamento((String) usuario.get("DEPARTAMENTO"));
    usuarioDTO.setApellido((String) usuario.get("APELLIDOS"));
    usuarioDTO.setNombre((String) usuario.get("NOMBRES"));
    usuarioDTO.setNumero_rol((String) usuario.get("ROL"));
    usuarioDTO.setCodigo((String) usuario.get("CODIGO"));
    usuarioDTO.setEmail((String) usuario.get("EMAIL"));
    return usuarioDTO;
  }
}
