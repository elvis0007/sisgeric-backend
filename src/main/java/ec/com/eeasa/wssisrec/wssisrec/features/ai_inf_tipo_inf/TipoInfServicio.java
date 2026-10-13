package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.dto.ActualizarTipoInfDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.dto.NuevoTipoInfDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.dto.TipoInfDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.mapper.TipoInformeMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.models.TipoInforme;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@Service
public class TipoInfServicio {
  private static final Logger logger = LoggerFactory.getLogger(TipoInfServicio.class);
  @Autowired
  TipoInfRepositorio tipoInfRepositorio;
  @Autowired
  TipoInformeMapper tipoInformeMapper;
  @Autowired
  BitacoraServicio bitacoraServicio;

  /**
   * Obtiene todos los tipos de informes registrados.
   * 
   * @return Lista de tipos de informes en un ResponseEntity
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtener() {
    try {
      List<TipoInforme> tipoInformes = tipoInfRepositorio.findAll();
      List<TipoInfDTO> tipoInfDTOs = tipoInformes.stream()
          .map(tipoInformeMapper::convertirModelDTO)
          .collect(Collectors.toList());
      return EstadoRespuestas.respuestaObtencion("Tipo de informes obtenidos exitosamente", tipoInfDTOs);
    } catch (Exception e) {
      logger.error("Error al obtener tipos de informes: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener tipos de informes");
    }
  }

  /**
   * Obtiene un tipo de informe por su código.
   * 
   * @param ainti_codigo Código del tipo de informe
   * @return TipoInforme si existe, null en caso contrario
   */
  @Transactional
  public TipoInforme obtenerTipoInforme(Long ainti_codigo) {
    try {
      return tipoInfRepositorio.findById(ainti_codigo).orElse(null);
    } catch (Exception e) {
      logger.error("Error al obtener tipo de informe: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Registra un nuevo tipo de informe.
   * 
   * @param nuevoTipoInfDTO Datos del nuevo tipo de informe
   * @return Respuesta con el estado de la operación
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> registrar(NuevoTipoInfDTO nuevoTipoInfDTO) {
    try {
      if (existeDescripcion(nuevoTipoInfDTO.getAinti_descripcion())) {
        return EstadoRespuestas.respuestaError("El tipo de informe ya existe");
      }
      TipoInforme tipoInforme = tipoInformeMapper.convertirCrearDTOModel(nuevoTipoInfDTO);
      TipoInforme respuesta = tipoInfRepositorio.save(tipoInforme);
      bitacoraServicio.registrarBitacora(respuesta, nuevoTipoInfDTO.getAuditoria(), "CREAR");
      return EstadoRespuestas.respuestaCreacion("Tipo de informe creado exitosamente");
    } catch (Exception e) {
      logger.error("Error al registrar tipo de informe: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al registrar tipo de informe");
    }
  }

  /**
   * Actualiza un tipo de informe existente.
   * 
   * @param actualizarTipoInfDTO Datos actualizados
   * @param id                   Código del tipo de informe
   * @return Respuesta con el estado de la operación
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizar(ActualizarTipoInfDTO actualizarTipoInfDTO, Long id) {
    try {
      if (!existeCodigo(id)) {
        return EstadoRespuestas.respuestaError("El tipo de informe no existe");
      }
      TipoInforme actualizarTipoInforme = tipoInfRepositorio.findById(id).get();
      TipoInforme tipoInforme = tipoInformeMapper.convertirActualizarDTOModel(actualizarTipoInfDTO,
          actualizarTipoInforme);
      TipoInforme respuesta = tipoInfRepositorio.save(tipoInforme);
      bitacoraServicio.registrarBitacora(respuesta, actualizarTipoInfDTO.getAuditoria(), "ACTUALIZAR");
      return EstadoRespuestas.respuestaCreacion("Tipo de informe actualizado correctamente");
    } catch (Exception e) {
      logger.error("Error al actualizar tipo de informe: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar tipo de informe");
    }
  }

  /**
   * Realiza la eliminación lógica de un tipo de informe.
   * 
   * @param auditoriaDTO Datos de auditoría
   * @param ainti_codigo Código del tipo de informe a eliminar
   * @return Respuesta con el estado de la operación
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(EliminarDTO auditoriaDTO, Long ainti_codigo) {
    try {
      TipoInforme tipoInforme = tipoInfRepositorio.findById(ainti_codigo).get();
      tipoInforme.setAinti_estado('0');
      TipoInforme response = tipoInfRepositorio.save(tipoInforme);
      bitacoraServicio.registrarBitacora(response, auditoriaDTO.getAuditoria(), "ELIMINACIÓN LÓGICA");
      return EstadoRespuestas.respuestaEliminacion("Tipo de informe eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar tipo de informe: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar tipo de informe");
    }
  }

  /**
   * Realiza la eliminación física de un tipo de informe.
   * 
   * @param auditoriaDTO Datos de auditoría
   * @param ainti_codigo Código del tipo de informe a eliminar
   * @return Respuesta con el estado de la operación
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisica(EliminarDTO auditoriaDTO, Long ainti_codigo) {
    try {
      TipoInforme tipoInforme = tipoInfRepositorio.findById(ainti_codigo).get();
      tipoInfRepositorio.deleteById(ainti_codigo);
      bitacoraServicio.registrarBitacora(tipoInforme, auditoriaDTO.getAuditoria(), "ELIMINACIÓN FISICA");
      return EstadoRespuestas.respuestaEliminacion("Tipo de informe eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar tipo de informe: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar tipo de informe");
    }
  }

  /**
   * Verifica si un tipo de informe con una descripción específica existe.
   * 
   * @param descripcion Descripción del tipo de informe
   * @return true si existe, false en caso contrario
   */
  private Boolean existeDescripcion(String descripcion) {
    return tipoInfRepositorio.findByAinti_descripcion(descripcion) != null;
  }

  /**
   * Verifica si un tipo de informe con un código específico existe.
   * 
   * @param id Código del tipo de informe
   * @return true si existe, false en caso contrario
   */
  private Boolean existeCodigo(Long id) {
    return tipoInfRepositorio.existsById(id);
  }
}
