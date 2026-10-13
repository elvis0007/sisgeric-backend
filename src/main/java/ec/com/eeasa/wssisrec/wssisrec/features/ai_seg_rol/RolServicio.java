package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.dtos.ActualizarRolDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.dtos.NuevoRolDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.dtos.RolDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.mapper.RolMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.models.Rol;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@Service
public class RolServicio {

  private static final Logger logger = LoggerFactory.getLogger(RolServicio.class);
  @Autowired
  private RolReporitorio rolReporitorio;

  @Autowired
  private BitacoraServicio bitacoraService;

  @Autowired
  private RolMapper rolMapper;

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtener() {
    List<Rol> roles = rolReporitorio.findAll();
    List<RolDTO> rolDto = roles.stream()
        .map(rol -> rolMapper.convertirModelDTO(rol))
        .collect(Collectors.toList());
    return EstadoRespuestas.respuestaObtencion("Roles obtenidos exitosamente", rolDto);
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> registrar(NuevoRolDTO nuevoRolDTO) {
    try {
      // Validar que el rol no exista
      if (this.validarExiste(nuevoRolDTO.getAsero_descripcion())) {
        return EstadoRespuestas.respuestaError("El rol ya existe");
      }
      // Convertir DTO a modelo
      Rol rol = rolMapper.convertirDTOModel(nuevoRolDTO);
      // Guardar en la base de datos
      Rol resultado = rolReporitorio.save(rol);
      // Registrar en la bitácora
      bitacoraService.registrarBitacora(resultado, nuevoRolDTO.getAuditoria(), "CREAR");
      return EstadoRespuestas.respuestaCreacion("Rol creado exitosamente");
    } catch (Exception e) {
      logger.error("Error al crear el rol: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al crear el rol");
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizar(ActualizarRolDTO actualizarRolDTO,
      Long asero_codigo) {
    try {
      // valida si existe codigo
      if (!this.estaPresente(asero_codigo)) {
        return EstadoRespuestas.respuestaError("El rol no existe");
      }
      // validar que el rol no exista
      if (validarExiste(actualizarRolDTO.getAsero_descripcion())) {
        return EstadoRespuestas.respuestaError("El rol ya existe");
      }

      Rol rolInfo = rolReporitorio.findById(asero_codigo).get();
      // Convertir el actualizarDTO a entidad
      Rol rol = rolMapper.convertirActualizarDTOModel(actualizarRolDTO, asero_codigo, rolInfo);

      Rol resultado = rolReporitorio.save(rol);
      // Registrar en la bitácora
      bitacoraService.registrarBitacora(resultado, actualizarRolDTO.getAuditoria(), "ACTUALIZAR");
      return EstadoRespuestas.respuestaCreacion("Rol actualizado exitosamente");
    } catch (Exception e) {
      logger.error("Error al actualizar el rol: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar el rol");
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(EliminarDTO eliminarDTO, Long rolId) {
    try {
      // valida si esta presente
      if (!this.estaPresente(rolId)) {
        return EstadoRespuestas.respuestaError("El rol que intenta eliminar no existe");
      }
      // obtener el rol
      Rol rol = rolReporitorio.findById(rolId).get();
      // update estado rol
      char nuevoEstado = rol.getAsero_estado() == '1' ? '0' : '1';
      rol.setAsero_estado(nuevoEstado);
      // actualizamos el rol
      Rol resultado = rolReporitorio.save(rol);
      // Registrar en la bitácora
      bitacoraService.registrarBitacora(resultado, eliminarDTO.getAuditoria(), "ELIMINAR");
      return EstadoRespuestas.respuestaEliminacion("Rol eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar el rol: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar el rol");
    }

  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(EliminarDTO eliminarDTO, Long rolId) {
    try {
      // valida si esta presente
      if (!this.estaPresente(rolId)) {
        return EstadoRespuestas.respuestaError("El rol que intenta eliminar no existe");
      }
      Rol rol = rolReporitorio.findById(rolId).get();
      rolReporitorio.deleteById(rolId);
      bitacoraService.registrarBitacora(rol, eliminarDTO.getAuditoria(), "ELIMINAR");
      return EstadoRespuestas.respuestaEliminacion("Rol eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar el rol: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("");
    }
  }

  public Boolean estaPresente(Long asero_codigo) {
    boolean result = rolReporitorio.existsById(asero_codigo);
    return result;
  }

  private Boolean validarExiste(String asero_descripcion) {
    Rol rolEntidad = rolReporitorio.findByAsero_descripcion(asero_descripcion);
    return rolEntidad != null;
  }

}
