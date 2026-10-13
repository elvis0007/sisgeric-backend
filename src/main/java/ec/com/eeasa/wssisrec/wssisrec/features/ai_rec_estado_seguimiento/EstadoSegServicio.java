package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.ActualizarEstadoSegDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.EstadoSegDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.NuevoEstadoSegDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.mapper.EstadoSegMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.models.EstadoSeg;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@Service
public class EstadoSegServicio {

  private static final Logger logger = LoggerFactory.getLogger(EstadoSegServicio.class);
  @Autowired
  private BitacoraServicio bitacoraServicio;

  @Autowired
  private EstadoSegRepositorio estadoSegRepositorio;

  @Autowired
  private EstadoSegMapper estadoSegMapper;

  public ResponseEntity<LinkedHashMap<String, Object>> obtener() {
    try {

      List<EstadoSeg> estadoSeg = estadoSegRepositorio.findByEstadoActivo();
      List<EstadoSegDTO> estadoSegDTOs = estadoSeg.stream().map(estadoSegMapper::convertirModelDTO)
          .collect(Collectors.toList());
      return EstadoRespuestas.respuestaObtencion("Estados de seguimiento obtenido existosamente", estadoSegDTOs);
    } catch (Exception e) {
      logger.error("Error al obtener los estados de seguimiento: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener los estados de seguimiento");
    }
  }

  public EstadoSegDTO obtenerPorId(Long id) {
    try {
      if (!validarCodigo(id)) {
        return null;
      }
      EstadoSeg estadoSeg = estadoSegRepositorio.findById(id).get();
      EstadoSegDTO estadoSegDTO = estadoSegMapper.convertirModelDTO(estadoSeg);
      return estadoSegDTO;
    } catch (Exception e) {
      logger.error("Error al obtener el estado de seguimiento: {}", e.getMessage());
      return null;
    }
  }

  public ResponseEntity<LinkedHashMap<String, Object>> registrar(NuevoEstadoSegDTO dto) {
    try {

      if (validarDescripcion(dto.getArees_descripcion())) {
        return EstadoRespuestas.respuestaError("La descripción ya existe");
      }
      EstadoSeg estadoSeg = estadoSegMapper.convertirCrearDTOModel(dto);
      EstadoSeg response = estadoSegRepositorio.save(estadoSeg);
      bitacoraServicio.registrarBitacora(response, dto.getAuditoria(), "CREAR");
      return EstadoRespuestas.respuestaCreacion("Estado de seguimiento creado exitosamente");
    } catch (Exception e) {
      logger.error("Error al crear el estado de seguimiento: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al crear el estado de seguimiento");
    }
  }

  public ResponseEntity<LinkedHashMap<String, Object>> actualiza(ActualizarEstadoSegDTO dto, Long arees_codigo) {
    try {
      if (!validarCodigo(arees_codigo) && !validarDescripcion(dto.getArees_descripcion())) {
        return EstadoRespuestas.respuestaError("El código no existe");
      }
      EstadoSeg estadoSeg = estadoSegRepositorio.findById(arees_codigo).get();
      EstadoSeg estadoSeg2 = estadoSegMapper.convertirActualizarDTOModel(dto, estadoSeg);
      EstadoSeg response = estadoSegRepositorio.save(estadoSeg2);
      bitacoraServicio.registrarBitacora(response, dto.getAuditoria(), "ACTUALIZAR");
      return EstadoRespuestas.respuestaCreacion("Estado de seguimiento actualizado exitosamente");
    } catch (Exception e) {
      logger.error("Error al actualizar el estado de seguimiento: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar el estado de seguimiento");
    }
  }

  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(EliminarDTO eliminar, Long id) {
    try {
      if (!validarCodigo(id)) {
        return EstadoRespuestas.respuestaError("El código no existe");
      }

      EstadoSeg estadoSeg = estadoSegRepositorio.findById(id).get();
      estadoSeg.setArees_estado('1');
      EstadoSeg response = estadoSegRepositorio.save(estadoSeg);
      bitacoraServicio.registrarBitacora(response, eliminar.getAuditoria(), "ELIMINAR LOGICO");
      return EstadoRespuestas.respuestaCreacion("Estado de seguimiento eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar el estado de seguimiento: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar el estado de seguimiento");
    }

  }

  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(EliminarDTO eliminar, Long id) {
    try {
      if (!validarCodigo(id)) {
        return EstadoRespuestas.respuestaError("El código no existe");
      }
      EstadoSeg estadoSeg = estadoSegRepositorio.findById(id).get();
      estadoSegRepositorio.deleteById(id);
      bitacoraServicio.registrarBitacora(estadoSeg, eliminar.getAuditoria(), "ELIMINAR FISICO");
      return EstadoRespuestas.respuestaEliminacion("Estado de seguimiento eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar el estado de seguimiento: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar el estado de seguimiento");
    }
  }

  private Boolean validarDescripcion(String descripcion) {
    return estadoSegRepositorio.findByDescripcion(descripcion) != null;
  }

  private Boolean validarCodigo(Long id) {
    return estadoSegRepositorio.existsById(id);
  }

}
