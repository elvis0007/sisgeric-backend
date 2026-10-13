package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario;

import java.sql.Types;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import java.util.Map;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos.UsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.core.auth.utils.PersonalServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.RolReporitorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.enums.RolEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.models.Rol;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.ActualizarRolUsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.DirectorInformacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.DirectorResponsablesDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.NuevoRolUsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.RolesUsuariosDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.UsuarioResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos.UsuarioRolDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.mapper.RolUsuarioMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models.RolUsuario;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.NuevoResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

/**
 * Breve descripción de la clase
 * 
 * @author KEVIN CAYO
 * @creado 27/9/2024
 */

@Service
public class RolUsuarioServicio {
  private static final Logger logger = LoggerFactory.getLogger(RolUsuarioServicio.class);

  @Autowired
  private PersonalServicio personalServicio;

  @Autowired
  private RolUsuarioRepositorio rolUsuarioRepositorio;

  @Autowired
  private RolReporitorio rolRepositorio;

  @Autowired
  private BitacoraServicio bitacoraServicio;

  @Autowired
  private RolUsuarioMapper rolUsuarioMapper;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> registrar(NuevoRolUsuarioDTO nuevoRolUsuarioDTO,
        String token_sisgerh) {
    try {
      // Validar si existe rol
      if (!this.existeRol(nuevoRolUsuarioDTO.getRol())) {
        return EstadoRespuestas.respuestaError("El rol no existe");
      }
      // Obtener usuario
      UsuarioDTO usuarioDTO = personalServicio.findUsuarioByCodigoPer(nuevoRolUsuarioDTO.getCodigo_per(), token_sisgerh);
      // Obtener objeto Rol
      Rol rol = rolRepositorio.findById(nuevoRolUsuarioDTO.getRol()).orElse(null);
      if (rol == null) {
        return EstadoRespuestas.respuestaError("Rol no encontrado en base de datos");
      }
  
      // Mapear DTO a entidad
      RolUsuario rolUsuario = rolUsuarioMapper.convertirDTOModel(nuevoRolUsuarioDTO, usuarioDTO, rol);
  
      // Guardar en base de datos
      RolUsuario resultado = rolUsuarioRepositorio.save(rolUsuario);
  
      // Registrar en bitácora
      bitacoraServicio.registrarBitacora(resultado, nuevoRolUsuarioDTO.getAuditoria(), "CREAR");
  
      return EstadoRespuestas.respuestaCreacion("Registro de asignación de roles exitoso");
  
    } catch (Exception e) {
      logger.error("Error al asignar el rol al usuario: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al asignar el rol al usuario");
    }
  }
  

  public ResponseEntity<LinkedHashMap<String, Object>> obtenerResponsables() {
    try {
      List<RolUsuario> responsables = rolUsuarioRepositorio.findByAsero_codigo(RolEnum.RESPONSABLE.getCode());
      List<UsuarioResponsableDTO> responsableDTOs = responsables.stream()
          .map(rolUsuarioMapper::convertirResponsableDTOModel)
          .collect(Collectors.toList());
      return EstadoRespuestas.respuestaObtencion("Responsables obtenidos exitosamente", responsableDTOs);
    } catch (Exception e) {
      logger.error("Error al obtener los responsables: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener los responsables");
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarUsuario(Long aseru_codigo,
      ActualizarRolUsuarioDTO actRolUsuarioDTO,
      String token_sisgerh) {
    try {
      // Validar si existe rol
      if (!this.existeRol(actRolUsuarioDTO.getRol())) {
        return EstadoRespuestas.respuestaError("El rol no existe");
      }
      
      // Obtener el registro anterior
      RolUsuario rolUsuarioOld = rolUsuarioRepositorio.findById(aseru_codigo)
          .orElseThrow(() -> new RuntimeException("Registro de rol no encontrado"));
      
      // Obtener información del nuevo usuario por código de personal
      UsuarioDTO usuarioDTO = personalServicio.findUsuarioByCodigoPer(actRolUsuarioDTO.getCodigo_per(), token_sisgerh);
      if (usuarioDTO == null) {
        return EstadoRespuestas.respuestaError("El usuario no existe");
      }
      
      // Validar si el nuevo usuario ya tiene el mismo rol activo en el mismo departamento
      if (actRolUsuarioDTO.getRol().equals(rolUsuarioOld.getRol().getAsero_codigo())) {
        List<RolUsuario> rolesExistentes = rolUsuarioRepositorio.findByAsero_codigoAndDepartamento(
            actRolUsuarioDTO.getRol(), rolUsuarioOld.getAseru_departamento());
        boolean yaTieneRol = rolesExistentes.stream()
            .anyMatch(ru -> ru.getDsgus_cuenta().equals(usuarioDTO.getDsgus_cuenta()) 
                && ru.getAseru_estado().equals('1'));
        if (yaTieneRol) {
          return EstadoRespuestas.respuestaError("El usuario ya tiene asignado el rol activo en este departamento");
        }
      }

      // Si es director departamental, eliminar el registro anterior en lugar de deshabilitarlo
      if (actRolUsuarioDTO.getRol().equals(RolEnum.DIRECTOR_DEPARTAMENTAL.getCode())) {
        // Registrar bitácora de eliminación antes de eliminar
        bitacoraServicio.registrarBitacora(rolUsuarioOld, actRolUsuarioDTO.getAuditoria(), "ELIMINAR");
        // Eliminar el registro anterior
        rolUsuarioRepositorio.deleteById(aseru_codigo);
      } else {
        // Para otros roles, deshabilitar el registro anterior (cambiar estado a '0')
        rolUsuarioOld.setAseru_estado('0');
        rolUsuarioOld.setAseru_fecha(new Date());
        rolUsuarioOld.setAseru_usuario(actRolUsuarioDTO.getAuditoria().getUsuario());
        rolUsuarioRepositorio.save(rolUsuarioOld);
        // Registrar bitácora de deshabilitación
        bitacoraServicio.registrarBitacora(rolUsuarioOld, actRolUsuarioDTO.getAuditoria(), "DESHABILITAR");
      }

      // Crear nuevo registro con el nuevo director
      Rol nuevoRol = rolRepositorio.findById(actRolUsuarioDTO.getRol())
          .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
      
      RolUsuario nuevoRolUsuario = new RolUsuario();
      nuevoRolUsuario.setDsgus_cuenta(usuarioDTO.getDsgus_cuenta());
      nuevoRolUsuario.setRol(nuevoRol);
      nuevoRolUsuario.setAseru_codigo_per(usuarioDTO.getCodigo());
      nuevoRolUsuario.setAseru_num_rol(usuarioDTO.getNumero_rol());
      nuevoRolUsuario.setAseru_nombre(usuarioDTO.getNombre());
      nuevoRolUsuario.setAseru_apellido(usuarioDTO.getApellido());
      nuevoRolUsuario.setAseru_departamento(usuarioDTO.getDepartamento());
      nuevoRolUsuario.setAseru_estado('1');
      nuevoRolUsuario.setAseru_fecha(new Date());
      nuevoRolUsuario.setAseru_usuario(actRolUsuarioDTO.getAuditoria().getUsuario());
      nuevoRolUsuario.setAseru_correo(usuarioDTO.getEmail());

      RolUsuario resultado = rolUsuarioRepositorio.save(nuevoRolUsuario);

      // Si es director departamental, transferir responsabilidades
      if (actRolUsuarioDTO.getRol().equals(RolEnum.DIRECTOR_DEPARTAMENTAL.getCode())) {
        logger.info("Iniciando transferencia de responsabilidades para director departamental. " +
                   "Director anterior: {} ({}), Director nuevo: {} ({})", 
                   rolUsuarioOld.getDsgus_cuenta(), 
                   rolUsuarioOld.getAseru_departamento(),
                   nuevoRolUsuario.getDsgus_cuenta(),
                   nuevoRolUsuario.getAseru_departamento());
        transferirResponsabilidadesDirector(rolUsuarioOld, nuevoRolUsuario, actRolUsuarioDTO.getAuditoria());
      }

      // Registrar bitácora de creación
      bitacoraServicio.registrarBitacora(resultado, actRolUsuarioDTO.getAuditoria(), "CREAR");

      return EstadoRespuestas.respuestaCreacion("Director actualizado exitosamente. Las responsabilidades han sido transferidas.");
    } catch (Exception e) {
      logger.error("Error al actualizar el usuario: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar el usuario: " + e.getMessage());
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorRol(Long rolId, String token_sisgerh) {
    try {
      Rol rol = rolRepositorio.findById(rolId).get();
      List<RolUsuario> rolesUsuarios = rolUsuarioRepositorio.findByAsero_codigo(rolId);
      List<UsuarioRolDTO> usuarioRolDTOs = rolesUsuarios.stream()
          .map(rolUsuario -> rolUsuarioMapper.convertirModelDTO(rolUsuario))
          .collect(Collectors.toList());
      RolesUsuariosDTO rolesUsuariosDTO = new RolesUsuariosDTO(rolId, rol.getAsero_descripcion(), usuarioRolDTOs);
      return EstadoRespuestas.respuestaObtencion("Roles obtenidos exitosamente", rolesUsuariosDTO);
    } catch (Exception e) {
      logger.error("Error al obtener los roles: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener los roles");
    }
  }

  /**
   * Obtiene la información de un rol de usuario por su ID.
   * Útil para prellenar el modal de edición.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorId(Long aseru_codigo) {
    try {
      RolUsuario rolUsuario = rolUsuarioRepositorio.findById(aseru_codigo)
          .orElseThrow(() -> new RuntimeException("Registro de rol no encontrado"));
      
      UsuarioRolDTO usuarioRolDTO = rolUsuarioMapper.convertirModelDTO(rolUsuario);
      return EstadoRespuestas.respuestaObtencion("Rol de usuario obtenido exitosamente", usuarioRolDTO);
    } catch (Exception e) {
      logger.error("Error al obtener el rol de usuario: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener el rol de usuario: " + e.getMessage());
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminar(Long aseru_codigo, EliminarDTO eliminarDTO) {
    try {
      if (!existeUsuarioAsignado(aseru_codigo)) {
        return EstadoRespuestas.respuestaError("El usuario asignado no existe");
      }
    } catch (Exception e) {
      logger.error("Error al eliminar el usuario: {}", e.getMessage());
    }
    try {
      RolUsuario entidad = rolUsuarioRepositorio.findById(aseru_codigo).get();
      rolUsuarioRepositorio.deleteById(aseru_codigo);
      // Registrar bitacora
      bitacoraServicio.registrarBitacora(entidad,
          eliminarDTO.getAuditoria(), "ELIMINAR");
    } catch (Exception e) {
      logger.error("Error al eliminar el usuario: {}", e.getMessage());
    }
    return EstadoRespuestas.respuestaEliminacion("Rol eliminado exitosamente");
  }

  @Transactional
  public List<DirectorResponsablesDTO> getDirectoresYResponsables(Integer codigoInforme) {
    try {
        // Llamar al procedimiento
        List<DirectorResponsablesDTO> result = rolUsuarioRepositorio
            .callGetCorreosDirectoresYResponsables(codigoInforme);
        
        return result;
        
    } catch (Exception e) {
      logger.error("Error al obtener directores y responsables: {}", e.getMessage());
      return null;
    }
  }

  public boolean existeUsuario(String dsgus_cuenta, String token_sisgerh) {
    return personalServicio.findUsuarioById(dsgus_cuenta, token_sisgerh) != null;
  }

  public boolean existeRol(Long asero_codigo) {
    return rolRepositorio.existsById(asero_codigo);
  }

  public boolean existeUsuarioAsignado(Long aseru_codigo) {
    boolean valor = rolUsuarioRepositorio.existsById(aseru_codigo);
    return valor;
  }

  public boolean tieneRol(String dsgus_cuenta, Long asero_codigo) {
    try {
      Integer cantidad = rolUsuarioRepositorio.findByDsgus_cuentaAndAsero_codigo(dsgus_cuenta, asero_codigo);
      if (cantidad > 0) {
        return true;
      }
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Transfiere las responsabilidades del director anterior al nuevo director.
   * Obtiene todas las recomendaciones no justificadas del departamento y las registra
   * para que queden bajo la responsabilidad del nuevo director.
   * 
   * Recomendaciones no justificadas: EN_PROCESO (1) o NO_CUMPLIDA (3)
   * Excluye: CUMPLIDA (2) y NO_APLICA (4)
   * 
   * @param directorAnterior Director que será deshabilitado
   * @param directorNuevo Nuevo director que tomará las responsabilidades
   * @param auditoria Información de auditoría
   */
  @Transactional
  private void transferirResponsabilidadesDirector(RolUsuario directorAnterior, RolUsuario directorNuevo, 
      ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO auditoria) {
    try {
      String departamento = directorAnterior.getAseru_departamento();
      
      // Verificar que ambos sean del mismo departamento
      if (!departamento.equals(directorNuevo.getAseru_departamento())) {
        logger.warn("Los directores no son del mismo departamento. Director anterior: {}, Director nuevo: {}", 
            directorAnterior.getAseru_departamento(), directorNuevo.getAseru_departamento());
      }

      // Obtener solo las recomendaciones no justificadas que pertenecían al director anterior
      // y que tienen verificables diferentes a JUSTIFICA (estado != 2)
      // Estados no justificados: EN_PROCESO (1) y NO_CUMPLIDA (3)
      // Excluir: CUMPLIDA (2) y NO_APLICA (4)
      // IMPORTANTE: Solo transferir las que tenían ARERE_USUARIO = director_anterior
      // y que tienen al menos un verificable con estado diferente a JUSTIFICA (AREEV_CODIGO != 2)
      String sql = "SELECT DISTINCT REC.ARERE_CODIGO " +
                   "FROM AI_REC_RECOMENDACIONES REC " +
                   "INNER JOIN AI_REC_DETALLE_RECOMENDACION DR ON REC.ARERE_CODIGO = DR.ARERE_CODIGO " +
                   "INNER JOIN AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO " +
                   "INNER JOIN AI_REC_VERIFICABLES V ON DR.AREDR_CODIGO = V.AREDR_CODIGO " +
                   "WHERE REC.ARERE_USUARIO = ? " + // Solo las que pertenecían al director anterior
                   "AND RES.ARERS_DEPARTAMENTO = ? " +
                   "AND REC.ARERE_ESTADO = '1' " +
                   "AND DR.AREDR_ESTADO = '1' " +
                   "AND RES.ARERS_ESTADO = '1' " +
                   "AND V.AREVE_ESTADO = '1' " + // Verificables activos
                   "AND V.AREEV_CODIGO != 2 " + // Verificables diferentes a JUSTIFICA (2)
                   "AND REC.AREES_CODIGO IN (1, 3) " + // EN_PROCESO o NO_CUMPLIDA (no justificadas)
                   "ORDER BY REC.ARERE_CODIGO";

      List<Long> recomendacionesIds = jdbcTemplate.queryForList(sql, Long.class, 
          directorAnterior.getDsgus_cuenta(), departamento);

      int cantidadRecomendaciones = recomendacionesIds.size();
      
      logger.info("Transferencia de responsabilidades: {} recomendaciones con verificables no justificados " +
                  "del director {} (departamento: {}) transferidas al nuevo director {}", 
                  cantidadRecomendaciones,
                  directorAnterior.getDsgus_cuenta(),
                  departamento,
                  directorNuevo.getDsgus_cuenta());
      
      // Registrar en bitácora la transferencia de cada recomendación
      // Nota: Las recomendaciones se relacionan por departamento, por lo que el nuevo director
      // automáticamente verá estas recomendaciones al filtrar por su departamento.
      // Aquí registramos la transferencia para auditoría.
      
      if (cantidadRecomendaciones > 0) {
        logger.info("Recomendaciones transferidas (IDs): {}", recomendacionesIds);
        
        // Actualizar el campo ARERE_USUARIO de las recomendaciones para reflejar la transferencia
        // Solo actualizar las que pertenecían específicamente al director anterior
        // y que tienen verificables diferentes a JUSTIFICA (estado != 2)
        String updateSql = "UPDATE AI_REC_RECOMENDACIONES REC " +
                          "SET REC.ARERE_USUARIO = ? " +
                          "WHERE REC.ARERE_USUARIO = ? " + // Solo las que pertenecían al director anterior
                          "AND EXISTS (" +
                          "  SELECT 1 " +
                          "  FROM AI_REC_DETALLE_RECOMENDACION DR " +
                          "  INNER JOIN AI_REC_RESPONSABLES RES ON DR.ARERS_CODIGO = RES.ARERS_CODIGO " +
                          "  INNER JOIN AI_REC_VERIFICABLES V ON DR.AREDR_CODIGO = V.AREDR_CODIGO " +
                          "  WHERE DR.ARERE_CODIGO = REC.ARERE_CODIGO " +
                          "  AND RES.ARERS_DEPARTAMENTO = ? " +
                          "  AND REC.ARERE_ESTADO = '1' " +
                          "  AND DR.AREDR_ESTADO = '1' " +
                          "  AND RES.ARERS_ESTADO = '1' " +
                          "  AND V.AREVE_ESTADO = '1' " + // Verificables activos
                          "  AND V.AREEV_CODIGO != 2 " + // Verificables diferentes a JUSTIFICA (2)
                          "  AND REC.AREES_CODIGO IN (1, 3)" + // EN_PROCESO o NO_CUMPLIDA
                          ")";
        
        try {
          int registrosActualizados = jdbcTemplate.update(updateSql, 
              directorNuevo.getDsgus_cuenta(),  // Nuevo director
              directorAnterior.getDsgus_cuenta(), // Director anterior (filtro)
              departamento);
          
          logger.info("Actualizadas {} recomendaciones con verificables no justificados del director anterior {} " +
                     "(departamento: {}) con el nuevo director: {}", 
                     registrosActualizados, 
                     directorAnterior.getDsgus_cuenta(),
                     departamento,
                     directorNuevo.getDsgus_cuenta());
          
          if (registrosActualizados == 0) {
            logger.warn("No se actualizaron recomendaciones. Verificar que el director anterior {} " +
                       "tenga recomendaciones con verificables no justificados (diferentes a JUSTIFICA) " +
                       "en el departamento: {}", 
                       directorAnterior.getDsgus_cuenta(), departamento);
          }
        } catch (Exception e) {
          logger.error("Error al actualizar recomendaciones durante la transferencia: {}", 
                      e.getMessage(), e);
          // No lanzamos excepción para no interrumpir el proceso de actualización del director
        }
      } else {
        logger.info("No hay recomendaciones con verificables no justificados para transferir " +
                   "del director {} en el departamento: {}", 
                   directorAnterior.getDsgus_cuenta(), departamento);
      }
      
    } catch (Exception e) {
      logger.error("Error al transferir responsabilidades del director: {}", e.getMessage(), e);
      // No lanzamos excepción para no interrumpir el proceso de actualización
    }
  }

  public DirectorInformacionDTO obtenerInfoDirector(String correo){
    try {
        SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
            .withProcedureName("obtenerInfoDirector")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlOutParameter("p_json", Types.CLOB),
                new SqlParameter("correo", Types.VARCHAR)
            ).withoutProcedureColumnMetaDataAccess();

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("correo", correo);

        Map<String, Object> result = simpleJdbcCall.execute(params);
        Clob clob = (Clob) result.get("p_json");
        String json = clobToString(clob);

        return this.objectMapper.readValue(json, DirectorInformacionDTO.class);
    } catch (Exception e) {
      logger.error("Error al obtener los datos para el director departamental: {}", e.getMessage());
      return null;
    }
}

  public boolean existeUsuarioByCuenta(String aseru_cuenta) {
    return rolUsuarioRepositorio.findByDsgus_cuenta(aseru_cuenta).isEmpty();
  }

  public ResponseEntity<LinkedHashMap<String, Object>> verificarTieneRol(String dsgus_cuenta, String aseru_num_rol) {
    try {
      boolean tieneRol = this.tieneRolParaResponsable(dsgus_cuenta, aseru_num_rol);
      return EstadoRespuestas.respuestaObtencion("Verificación de rol realizada exitosamente", tieneRol);
    } catch (Exception e) {
      logger.error("Error al verificar si el usuario tiene el rol: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al verificar si el usuario tiene el rol");
    }
  }

  public boolean tieneRolParaResponsable(String dsgus_cuenta, String aseru_num_rol) {
    try {
      Integer cantidad = rolUsuarioRepositorio.findByDsgus_cuentaAndAseru_num_rol(dsgus_cuenta, aseru_num_rol);
      if (cantidad > 0) {
        return true;
      }
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  private String clobToString(Clob clob) throws SQLException {
    if (clob == null) return "";
    
    try {
        return clob.getSubString(1, (int) clob.length());
    } finally {
        // Cerrar explícitamente el Clob para liberar recursos
        if (clob != null) {
            try {
                clob.free();
            } catch (SQLException e) {
                logger.warn("Error al cerrar el Clob: " + e.getMessage());
            }
        }
  }
}

  /**
   * Asegura que el usuario tenga asignado el rol de RESPONSABLE (código 4).
   * Si no lo tiene, crea automáticamente el registro correspondiente en AI_SEG_ROL_USU.
   */
  @Transactional
  public void asegurarRolResponsable(NuevoResponsableDTO responsableDTO, AuditoriaDTO auditoria) {
    if (responsableDTO == null || responsableDTO.getArers_usuario_per() == null) {
      return;
    }
    String usuario = responsableDTO.getArers_usuario_per().trim();
    Integer conteo = rolUsuarioRepositorio.findByDsgus_cuentaAndAsero_codigo(usuario, RolEnum.RESPONSABLE.getCode());
    if (conteo == null || conteo == 0) {
      logger.info("[ROL RESPONSABLE] Creando rol de RESPONSABLE para el usuario: {}", usuario);
      Rol rol = rolRepositorio.findById(RolEnum.RESPONSABLE.getCode()).orElse(null);
      if (rol != null) {
        RolUsuario nuevoRolUsuario = new RolUsuario();
        nuevoRolUsuario.setDsgus_cuenta(usuario);
        nuevoRolUsuario.setRol(rol);
        nuevoRolUsuario.setAseru_codigo_per(responsableDTO.getArers_codigo_per());
        nuevoRolUsuario.setAseru_num_rol(responsableDTO.getArers_num_rol());
        nuevoRolUsuario.setAseru_nombre(responsableDTO.getArers_nombre());
        nuevoRolUsuario.setAseru_apellido(responsableDTO.getArers_apellido());
        nuevoRolUsuario.setAseru_departamento(responsableDTO.getArers_departamento());
        nuevoRolUsuario.setAseru_correo(responsableDTO.getArers_correo());
        nuevoRolUsuario.setAseru_estado('1');
        nuevoRolUsuario.setAseru_fecha(new Date());
        nuevoRolUsuario.setAseru_usuario(auditoria != null && auditoria.getUsuario() != null ? auditoria.getUsuario() : "SISTEMA");
        RolUsuario guardado = rolUsuarioRepositorio.save(nuevoRolUsuario);
        if (auditoria != null) {
          try {
            bitacoraServicio.registrarBitacora(guardado, auditoria, "CREAR");
          } catch (Exception e) {
            logger.error("Error al registrar bitacora de rol responsable: {}", e.getMessage());
          }
        }
        logger.info("[ROL RESPONSABLE] Rol de RESPONSABLE creado exitosamente para el usuario: {}", usuario);
      } else {
        logger.warn("[ROL RESPONSABLE] No se encontró la entidad Rol con código {}", RolEnum.RESPONSABLE.getCode());
      }
    } else {
      logger.info("[ROL RESPONSABLE] El usuario '{}' ya posee el rol de RESPONSABLE activo.", usuario);
    }
  }
}
