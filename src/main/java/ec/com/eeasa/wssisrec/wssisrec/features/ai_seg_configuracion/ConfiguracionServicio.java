package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.dtos.ActualizarConfiguracionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.dtos.ConfiguracionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.dtos.NuevaConfiguracionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.enums.ConfiguracionEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.mapper.ConfiguracionMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.models.Configuracion;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.shared.config.DynamicMultipartConfigService;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;

@Service
public class ConfiguracionServicio {
  private static final Logger logger = LoggerFactory.getLogger(ConfiguracionServicio.class);
  @Autowired
  BitacoraServicio bitacoraServicio;

  @Autowired
  ConfiguracionRepositorio configuracionRepositorio;

  @Autowired
  ConfiguracionMapper configuracionMapper;

  @Autowired
  DynamicMultipartConfigService dynamicMultipartConfigService;

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorId(Long id) {
    try {
      Configuracion configuracion = configuracionRepositorio.findById(id).get();
      ConfiguracionDTO configuracionDTO = configuracionMapper.convertirModelDto(configuracion);
      return EstadoRespuestas.respuestaObtencion("Configuracion obtenida exitosamente", configuracionDTO);
    } catch (Exception e) {
      logger.error("Error al obtener configuración: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener");
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtener() {
    try {
      List<Configuracion> configuraciones = configuracionRepositorio.findAll();
      List<ConfiguracionDTO> configuracionDTO = configuraciones.stream()
          .map((config) -> configuracionMapper.convertirModelDto(config)).collect(Collectors.toList());
      return EstadoRespuestas.respuestaObtencion("Configuracion obtenida exitosamente", configuracionDTO);
    } catch (Exception e) {
      logger.error("Error al obtener configuración: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener");
    }

  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> registrar(NuevaConfiguracionDTO nuevaConfDto) {
    try {
      Configuracion configuracion = configuracionMapper.convertirCrearDtoModel(nuevaConfDto);

      Configuracion respuesta = configuracionRepositorio.save(configuracion);
      bitacoraServicio.registrarBitacora(respuesta, nuevaConfDto.getAuditoria(), "CREAR");
      return EstadoRespuestas.respuestaCreacion("Configuracion registrada exitosamente");
    } catch (Exception e) {
      logger.error("Error al registrar configuración: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al registrar");
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizar(ActualizarConfiguracionDTO actualizarDto, Long id) {
    try {
      Configuracion config = configuracionRepositorio.findById(id).get();
      Configuracion configuracion = configuracionMapper.convertirActualizarDtoModel(actualizarDto, config);
      Configuracion respuesta = configuracionRepositorio.save(configuracion);
      bitacoraServicio.registrarBitacora(respuesta, actualizarDto.getAuditoria(),
          "ACTUALIZAR");

      return EstadoRespuestas.respuestaCreacion("Configuracion actualizada exitosamente");
    } catch (Exception e) {
      logger.error("Error al actualizar configuración: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar");
    }
  }

  /**
   * Obtiene la configuración del NAS
   * 
   * @return Configuración del NAS o null si no existe
   */
  @Transactional
  public Configuracion obtenerConfiguracionNAS() {
    try {
      return configuracionRepositorio.findById(ConfiguracionEnum.NAS.getCodigo()).orElse(null);
    } catch (Exception e) {
      logger.error("Error al obtener configuración NAS: {}", e.getMessage());
      return null;
    }
  }

}
