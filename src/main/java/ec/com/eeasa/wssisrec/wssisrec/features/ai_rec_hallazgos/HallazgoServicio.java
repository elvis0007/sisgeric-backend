package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.InformeServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models.Informe;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.ConclusionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.models.Conclusion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.dtos.ActualizarHallazgoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.dtos.HallazgoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.dtos.InformesHallazgosDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.dtos.NuevoHallazgoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.dtos.TitulosDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.mapper.HallazgoMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.models.Hallazgo;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper;

@Service
public class HallazgoServicio {
  private static final Logger logger = LoggerFactory.getLogger(HallazgoServicio.class);
  @Autowired
  private HallazgoRepositorio hallazgoRepositorio;

  @Autowired
  private BitacoraServicio bitacoraServicio;

  @Autowired
  private HallazgoMapper hallazgoMapper;

  @Autowired
  private InformeServicio informeServicio;
  
  @Autowired
  private ConclusionRepositorio conclusionRepositorio;

  /**
   * Obtiene los hallazgos asociados a un informe específico.
   * 
   * @param ainin_codigo Código del informe.
   * @return ResponseEntity con la lista de hallazgos encontrados.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorInforme(Long ainin_codigo) {
    try {
      Informe informe = informeServicio.obtenerInforme(ainin_codigo);
      List<Hallazgo> hallazgos = hallazgoRepositorio.findByInforme(informe);

      List<HallazgoDTO> hallazgoDTOs = hallazgos.stream()
          .filter(hallazgo -> hallazgo.getAreha_estado() == '1')
          .map(hallazgo -> {
            HallazgoDTO dto = hallazgoMapper.convertirModelDTO(hallazgo);
            Long totalCon = conclusionRepositorio.countActivasByHallazgo(hallazgo.getAreha_codigo());
            dto.setTotalConclusiones(totalCon != null ? totalCon : 0L);
            return dto;
          })
          .collect(Collectors.toList());

      InformesHallazgosDTO informesHallazgosDTO = new InformesHallazgosDTO(
          ainin_codigo, informe.getAinin_titulo(),
          informe.getAinin_orden_trabajo(),
          hallazgoDTOs);

      return EstadoRespuestas.respuestaObtencion("Hallazgos obtenidos exitosamente.", informesHallazgosDTO);
    } catch (Exception e) {
      logger.error("Error al obtener los hallazgos: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener los hallazgos");
    }
  }

  /**
   * Obtiene los hallazgos de un informe filtrados por el texto de una recomendación.
   * 
   * @param ainin_codigo Código del informe.
   * @param termino Término de búsqueda en la descripción de recomendaciones.
   * @return ResponseEntity con la lista de hallazgos encontrados.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorInformeYRecomendacion(Long ainin_codigo, String termino) {
    try {
      Informe informe = informeServicio.obtenerInforme(ainin_codigo);
      List<Hallazgo> hallazgos;

      if (termino != null && termino.matches("\\d+")) {
        hallazgos = hallazgoRepositorio.findByInformeAndRecomendacionNumero(ainin_codigo, Integer.parseInt(termino));
      } else {
        String patron = "%" + (termino != null ? termino : "") + "%";
        hallazgos = hallazgoRepositorio.findByInformeAndRecomendacionDescripcion(ainin_codigo, patron);
      }

      List<HallazgoDTO> hallazgoDTOs = hallazgos.stream()
          .map(hallazgo -> {
            HallazgoDTO dto = hallazgoMapper.convertirModelDTO(hallazgo);
            Long totalCon = conclusionRepositorio.countActivasByHallazgo(hallazgo.getAreha_codigo());
            dto.setTotalConclusiones(totalCon != null ? totalCon : 0L);
            return dto;
          })
          .collect(Collectors.toList());

      InformesHallazgosDTO informesHallazgosDTO = new InformesHallazgosDTO(
          ainin_codigo, informe.getAinin_titulo(),
          informe.getAinin_orden_trabajo(),
          hallazgoDTOs);

      return EstadoRespuestas.respuestaObtencion("Hallazgos obtenidos exitosamente.", informesHallazgosDTO);
    } catch (Exception e) {
      logger.error("Error al obtener los hallazgos por recomendación: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener los hallazgos por recomendación");
    }
  }

  /**
   * Obtiene un hallazgo por su ID.
   * 
   * @param id Identificador del hallazgo.
   * @return ResponseEntity con la información del hallazgo.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorId(Long id) {
    try {
      if (!existeCodigo(id)) {
        return EstadoRespuestas.noEncontrado("Código no encontrado");
      }
      Hallazgo hallazgo = hallazgoRepositorio.findById(id).get();
      HallazgoDTO hallazgoDTO = hallazgoMapper.convertirModelDTO(hallazgo);
      Long totalCon = conclusionRepositorio.countActivasByHallazgo(hallazgo.getAreha_codigo());
      hallazgoDTO.setTotalConclusiones(totalCon != null ? totalCon : 0L);
      return EstadoRespuestas.respuestaObtencion("Hallazgo obtenido exitosamente", hallazgoDTO);
    } catch (Exception e) {
      logger.error("Error al obtener el hallazgo: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener el hallazgo");
    }
  }

  private Boolean existeCodigo(Long id) {
    return hallazgoRepositorio.existsById(id);
  }

  /**
   * Registra un nuevo hallazgo en el sistema.
   * 
   * @param nuevoHallazgoDTO Datos del nuevo hallazgo.
   * @return ResponseEntity indicando el resultado de la operación.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> registrar(NuevoHallazgoDTO nuevoHallazgoDTO) {
    try {
      Informe informe = informeServicio.obtenerInforme(nuevoHallazgoDTO.getAinin_codigo());
      Hallazgo hallazgo = hallazgoMapper.convertirCrearDTOModel(nuevoHallazgoDTO, informe);
      Hallazgo respuesta = hallazgoRepositorio.save(hallazgo);
      
      try {
        bitacoraServicio.registrarBitacora(respuesta, nuevoHallazgoDTO.getAuditoria(), "CREAR");
      } catch (Exception e) {
        logger.error("Error al registrar bitácora para hallazgo: {}", e.getMessage());
        // No propagar el error de bitácora para evitar rollback de la transacción principal
      }
      
      return EstadoRespuestas.respuestaCreacion("Hallazgo registrado correctamente");
    } catch (Exception e) {
      logger.error("Error al registrar el hallazgo: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al registrar el hallazgo");
    }
  }

  /**
   * Actualiza la información de un hallazgo existente.
   * 
   * @param dto Datos actualizados del hallazgo.
   * @param id  Identificador del hallazgo a actualizar.
   * @return ResponseEntity con el resultado de la actualización.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizar(ActualizarHallazgoDTO dto, Long id) {
    try {
      if (!existeCodigo(id)) {
        return EstadoRespuestas.noEncontrado("Hallazgo no encontrado");
      }
      Hallazgo hallazgoOld = hallazgoRepositorio.findById(id).get();
      Hallazgo hallazgo = hallazgoMapper.convertirActualizarDTOModel(dto, hallazgoOld);
      Hallazgo respuesta = hallazgoRepositorio.save(hallazgo);
      
      try {
        bitacoraServicio.registrarBitacora(respuesta, dto.getAuditoria(), "ACTUALIZAR");
      } catch (Exception e) {
        logger.error("Error al registrar bitácora para actualización de hallazgo: {}", e.getMessage());
        // No propagar el error de bitácora para evitar rollback de la transacción principal
      }
      
      return EstadoRespuestas.respuestaCreacion("Hallazgo actualizado correctamente");
    } catch (Exception e) {
      logger.error("Error al actualizar el hallazgo: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar el hallazgo");
    }
  }

  /**
   * Realiza la eliminación lógica de un hallazgo.
   * 
   * @param eliminarDTO Datos de auditoría.
   * @param id          Identificador del hallazgo a eliminar.
   * @return ResponseEntity con el resultado de la operación.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(EliminarDTO eliminarDTO, Long id) {
    try {
      if (!hallazgoRepositorio.existsById(id)) {
        return EstadoRespuestas.noEncontrado("Hallazgo no encontrado");
      }
      
      Hallazgo hallazgo = hallazgoRepositorio.findById(id).get();
      
      // Verificar si existen conclusiones activas para este hallazgo
      List<Conclusion> conclusiones = conclusionRepositorio.findByHallazgo(hallazgo);
      boolean tieneConclusionesActivas = conclusiones.stream()
          .anyMatch(conclusion -> conclusion.getAreco_estado() == '1');
      
      if (tieneConclusionesActivas) {
        return EstadoRespuestas.respuestaError("No se puede eliminar el hallazgo porque tiene conclusiones registradas");
      }
      
      hallazgo.setAreha_estado('0');
      Hallazgo respuesta = hallazgoRepositorio.save(hallazgo);
      
      try {
        bitacoraServicio.registrarBitacora(respuesta, eliminarDTO.getAuditoria(), "ELIMINAR LOGICO");
      } catch (Exception e) {
        logger.error("Error al registrar bitácora para eliminación lógica de hallazgo: {}", e.getMessage());
        // No propagar el error de bitácora para evitar rollback de la transacción principal
      }
      
      return EstadoRespuestas.respuestaEliminacion("Hallazgo eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar hallazgo: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar hallazgo");
    }
  }

  /**
   * Obtiene los títulos de un hallazgo y su informe asociado.
   * 
   * @param areha_codigo Código del hallazgo.
   * @return ResponseEntity con los títulos obtenidos.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerTitulos(Long areha_codigo) {
    try {
      Hallazgo hallazgo = hallazgoRepositorio.findById(areha_codigo).get();
      Informe informe = informeServicio.obtenerInformeByHallazgo(areha_codigo);
      
      // Manejar la fecha de aprobación que puede ser nula
      String fechaAprobacion = "";
      if (informe.getAinin_fecha_aprobacion() != null) {
        fechaAprobacion = DateMapper.convertirFechaFormato(informe.getAinin_fecha_aprobacion());
      }
      
      TitulosDTO titulosDTO = new TitulosDTO(
          informe.getAinin_titulo(),
          informe.getAinin_orden_trabajo(),
          fechaAprobacion,
          hallazgo.getAreha_titulo());
      return EstadoRespuestas.respuestaObtencion("Títulos obtenidos exitosamente", titulosDTO);
    } catch (Exception e) {
      logger.error("Error al obtener títulos: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener títulos");
    }
  }

  /**
   * Realiza la eliminación fisica de un hallazgo.
   * 
   * @param eliminarDTO Datos de auditoría.
   * @param id          Identificador del hallazgo a eliminar.
   * @return ResponseEntity con el resultado de la operación.
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisica(EliminarDTO eliminar, Long id) {
    try {
      if (!hallazgoRepositorio.existsById(id)) {
        return EstadoRespuestas.noEncontrado("Titulo de hallazgo no encontrado");
      }
      Hallazgo hallazgo = hallazgoRepositorio.findById(id).get();

      List<Conclusion> conclusiones = conclusionRepositorio.findByHallazgo(hallazgo);
      boolean tieneConclusionesActivas = conclusiones.stream()
          .anyMatch(conclusion -> conclusion.getAreco_estado() == '1');
      
      if (tieneConclusionesActivas) {
        return EstadoRespuestas.respuestaError("No se puede eliminar el hallazgo porque tiene conclusiones registradas");
      }

      hallazgoRepositorio.deleteById(id);
      
      try {
        bitacoraServicio.registrarBitacora(hallazgo, eliminar.getAuditoria(), "ELIMINAR FISICO");
      } catch (Exception e) {
        logger.error("Error al registrar bitácora para eliminación física de hallazgo: {}", e.getMessage());
        // No propagar el error de bitácora para evitar rollback de la transacción principal
      }
      
      return EstadoRespuestas.respuestaEliminacion("Titulo de hallazgo eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar el titulo del hallazgo: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar el titulo del hallazgo");
    }
  }

  /**
   * Obtiene el hallazgo en base el id
   * 
   * @param areha_codigo Identificador del hallazgo.
   * @return ResponseEntity con el resultado de la operación.
   */

  @Transactional
  public Hallazgo obtenerHallazgo(Long areha_codigo) {
    try {
      Hallazgo hallazgo = hallazgoRepositorio.findById(areha_codigo).get();
      return hallazgo;
    } catch (Exception e) {
      logger.error("Error al obtener el hallazgo: {}", e.getMessage());
      return null;
    }
  }
}
