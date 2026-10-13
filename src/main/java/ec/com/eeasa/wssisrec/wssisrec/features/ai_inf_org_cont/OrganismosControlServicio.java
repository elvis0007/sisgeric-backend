package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.dtos.ActualizarOrganismoControlDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.dtos.NuevoOrganismoControlDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.dtos.OrganismosControlDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.mapper.OrganismoControlMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.models.OrganismoControl;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@Service
public class OrganismosControlServicio {
  private static final Logger logger = LoggerFactory.getLogger(OrganismosControlServicio.class);
  @Autowired
  OrganismosControlRepositorio organismosControlRepositorio;

  @Autowired
  BitacoraServicio bitacoraServicio;

  @Autowired
  OrganismoControlMapper organismoControlMapper;

  /**
   * Obtiene todos los organismos de control registrados.
   * 
   * @return ResponseEntity con la lista de organismos de control o un mensaje de
   *         error.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtener() {
    try {
      List<OrganismoControl> organismosControl = organismosControlRepositorio.findByEstadoActivo();
      List<OrganismosControlDTO> OrganismosControlDTO = organismosControl.stream()
          .map((organismo) -> organismoControlMapper.convertirModelDTO(organismo))
          .collect(Collectors.toList());

      return EstadoRespuestas.respuestaObtencion("Organismos de control obtenidos exitosamente", OrganismosControlDTO);
    } catch (Exception e) {
      logger.error("Error al obtener organismos de control: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener organismos de control: " + e.getMessage());
    }
  }

  /**
   * Obtiene un organismo de control por su código.
   * 
   * @param ainoc_codigo Código del organismo de control.
   * @return El organismo de control si existe, de lo contrario null.
   */
  @Transactional
  public OrganismoControl obtenerOrganismo(Long ainoc_codigo) {
    try {
      if (!organismosControlRepositorio.existsById(ainoc_codigo)) {
        return null;
      }
      return organismosControlRepositorio.findById(ainoc_codigo).get();
    } catch (Exception e) {
      logger.error("Error al obtener organismo de control: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Registra un nuevo organismo de control.
   * 
   * @param dto Datos del nuevo organismo de control.
   * @return ResponseEntity con la respuesta de creación o mensaje de error.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> registrar(NuevoOrganismoControlDTO dto) {
    try {
      if (!this.existeDescripcion(dto.getAinoc_descripcion())) {
        return EstadoRespuestas.noEncontrado("El organismo de control ya existe");
      }
      OrganismoControl organismoControl = organismoControlMapper.convertirDTOModel(dto);
      OrganismoControl resultado = organismosControlRepositorio.save(organismoControl);
      bitacoraServicio.registrarBitacora(resultado, dto.getAuditoria(), "CREAR");
      return EstadoRespuestas.respuestaCreacion("Organismo de control creado exitosamente");
    } catch (Exception e) {
      logger.error("Error al crear organismo de control: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al crear organismo de control: " + e.getMessage());
    }
  }

  /**
   * Actualiza un organismo de control existente.
   * 
   * @param dto Datos actualizados del organismo de control.
   * @param id  Identificador del organismo a actualizar.
   * @return ResponseEntity con la respuesta de actualización o mensaje de error.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizar(ActualizarOrganismoControlDTO dto, Long id) {
    try {
      if (!existeDescripcion(dto.getAinoc_descripcion()) && !existeCodigo(id)) {
        return EstadoRespuestas.respuestaError("El organismo de control ya existe");
      }
      OrganismoControl organismoOld = organismosControlRepositorio.findById(id).get();
      OrganismoControl organismo = organismoControlMapper.convertirActualizarDTOModel(dto, organismoOld);
      OrganismoControl resultado = organismosControlRepositorio.save(organismo);
      bitacoraServicio.registrarBitacora(resultado, dto.getAuditoria(), "ACTUALIZAR");
      return EstadoRespuestas.respuestaCreacion("Organismo de control actualizado exitosamente");
    } catch (Exception e) {
      logger.error("Error al actualizar organismo de control: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar organismo de control: " + e.getMessage());
    }
  }

  /**
   * Realiza una eliminación lógica de un organismo de control.
   * 
   * @param auditoria Datos de auditoría.
   * @param id        Identificador del organismo a eliminar.
   * @return ResponseEntity con la respuesta de eliminación o mensaje de error.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminar(AuditoriaDTO auditoria, Long id) {
    try {
      if (!existeCodigo(id)) {
        return EstadoRespuestas.respuestaError("Organismo de control no encontrado");
      }
      OrganismoControl organismo = organismosControlRepositorio.findById(id).get();
      organismo.setAinoc_estado('0');
      OrganismoControl resultado = organismosControlRepositorio.save(organismo);
      bitacoraServicio.registrarBitacora(resultado, auditoria, "ELIMINACIÓN LÓGICA");
      return EstadoRespuestas.respuestaEliminacion("Organismo de Control eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar organismo de control: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar organismo de control: " + e.getMessage());
    }
  }

  /**
   * Elimina físicamente un organismo de control de la base de datos.
   * 
   * @param auditoriaDTO Datos de auditoría.
   * @param id           Identificador del organismo a eliminar.
   * @return ResponseEntity con la respuesta de eliminación o mensaje de error.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(AuditoriaDTO auditoriaDTO, Long id) {
    try {
      OrganismoControl organismoControl = organismosControlRepositorio.findById(id).get();
      organismosControlRepositorio.deleteById(id);
      bitacoraServicio.registrarBitacora(organismoControl, auditoriaDTO, "ELIMINACIÓN FISICA");
      return EstadoRespuestas.respuestaEliminacion("Organismo de control eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar organismo de control: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar organismo de control: " + e.getMessage());
    }
  } 

  /**
   * Verifica si un organismo de control existe por su descripción.
   * 
   * @param descripcion Descripción del organismo.
   * @return true si no existe, false si ya existe.
   */
  private Boolean existeDescripcion(String descripcion) {
    return organismosControlRepositorio.findByAinoc_descripcion(descripcion) == null;
  }

  /**
   * Verifica si un organismo de control existe por su código.
   * 
   * @param id Identificador del organismo.
   * @return true si existe, false en caso contrario.
   */
  private Boolean existeCodigo(Long id) {
    return organismosControlRepositorio.existsById(id);
  }

}
