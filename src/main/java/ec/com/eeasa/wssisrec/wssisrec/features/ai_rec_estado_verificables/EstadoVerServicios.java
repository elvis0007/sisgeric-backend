package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.dtos.ActualizarEstadoVerDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.dtos.EstadoVerDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.dtos.NuevoEstadoVerDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.mapper.EstadoVerMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.models.EstadoVerificable;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@Service
public class EstadoVerServicios {
  private static final Logger logger = LoggerFactory.getLogger(EstadoVerServicios.class);
  @Autowired
  private BitacoraServicio bitacoraServicio;

  @Autowired
  private EstadoVerRepositorio estadoVerRepositorio;
  @Autowired
  private EstadoVerMapper estadoVerMapper;

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> registrarEstado(NuevoEstadoVerDTO nuevoEstadoVerDTO) {
    try {
      EstadoVerificable estadoVerificable = estadoVerMapper.convertirCrearDTOModel(nuevoEstadoVerDTO);

      EstadoVerificable respuesta = estadoVerRepositorio.save(estadoVerificable);

      bitacoraServicio.registrarBitacora(respuesta, nuevoEstadoVerDTO.getAuditoria(), "CREAR");
      return EstadoRespuestas.respuestaCreacion("Estado verificable creado exitosamente");
    } catch (Exception e) {
      logger.error("Error al registrar estado verificable : {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al registrar estado verificable : " + e);
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerEstados() {
    try {
      List<EstadoVerificable> estadoVerificables = estadoVerRepositorio.findAll();
      List<EstadoVerDTO> estadoVerDTOs = estadoVerificables.stream()
          .filter(estado -> estado.getAreev_estado().equals('1')).map(estadoVerMapper::convertirModelDTO)
          .collect(Collectors.toList());
      return EstadoRespuestas.respuestaObtencion("Estados verificables obtenidos exitosamente", estadoVerDTOs);
    } catch (Exception e) {
      logger.error("Error al obtener los estados verificables : {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener los estados verificables : " + e);
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(EliminarDTO eliminarDTO, Long areev_codigo) {
    try {
      if (!estadoVerRepositorio.existsById(areev_codigo)) {
        return EstadoRespuestas.noEncontrado("Estado verificable no encontrado");
      }
      EstadoVerificable estadoVerificable = estadoVerRepositorio.findById(areev_codigo).get();
      estadoVerRepositorio.deleteById(areev_codigo);
      bitacoraServicio.registrarBitacora(estadoVerificable, eliminarDTO.getAuditoria(), "ELIMINACION FISICA");

      return EstadoRespuestas.respuestaEliminacion("Estado verificable eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar el estado verifcable : {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Erro al eliminar el estado verifcable : " + e);
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarEstado(ActualizarEstadoVerDTO actualizarEstadoVerDTO,
      Long areev_codigo) {
    try {

      if (estadoVerRepositorio.existsById(areev_codigo)) {
        return EstadoRespuestas.noEncontrado("Estado Verificable no encontrado");
      }
      EstadoVerificable estadoVerificable = estadoVerRepositorio.findById(areev_codigo).get();
      EstadoVerificable respuesta = estadoVerRepositorio.save(estadoVerificable);
      bitacoraServicio.registrarBitacora(respuesta, actualizarEstadoVerDTO.getAuditoria(), "ACTUALIZAR");
      return EstadoRespuestas.respuestaCreacion("Estado verificable actualizado exitosamente");
    } catch (Exception e) {
      logger.error("Error al actualizar estado : {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar estado: " + e);
    }
  }

}
