package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.mapper;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.mapper.EstadoVerMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.models.EstadoVerificable;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.ActualizarVerificableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.NuevoVerificableADTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.ValidarVerificableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.dtos.VerificableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.models.Verificable;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;

@Component
public class VerificableMapper {
  private static final Logger logger = LoggerFactory.getLogger(VerificableMapper.class);
  @Autowired
  private EstadoVerMapper estadoVerMapper;

  public Verificable convertirCrearResDTOModel(NuevoVerificableADTO nuevoVerificableDTO, EstadoVerificable estadoSeg,
      AuditoriaDTO auditoriaDTO,
      DetalleRecomendacion detalleRecomendacion, String rutaArchivo) {
    Verificable verificable = new Verificable();
    verificable.setAreve_codigo(null);
    verificable.setAreve_adjunto(rutaArchivo);
    verificable.setAreve_descripcion(nuevoVerificableDTO.getAreve_descripcion());
    verificable.setDetalleRecomendacion(detalleRecomendacion);
    verificable.setAreve_estado('1');
    verificable.setAreve_fecha(new Date());
    verificable.setAreve_fecha_carga(new Date());
    verificable.setEstadoVer(estadoSeg);
    verificable.setAreve_usuario(auditoriaDTO.getUsuario());
    verificable.setAreve_observacion(nuevoVerificableDTO.getAreve_motivo_estado());
    return verificable;

  }

  public VerificableDTO convertirModelDTO(Verificable verificable) {
    try {
      VerificableDTO verificableDTO = new VerificableDTO();
      verificableDTO.setAreve_codigo(verificable.getAreve_codigo());
 
      verificableDTO.setAreve_fecha_carga(verificable.getAreve_fecha_carga().toString());
      verificableDTO.setAreve_adjunto(verificable.getAreve_adjunto());
      verificableDTO.setAreve_descripcion(verificable.getAreve_descripcion());
      verificableDTO.setAreve_motivo_estado(verificable.getAreve_observacion());
      verificableDTO.setEstado_seguimiento(estadoVerMapper.convertirModelDTO(verificable.getEstadoVer()));
      verificableDTO.setAreve_usuario(verificable.getAreve_usuario());

      return verificableDTO;
    } catch (Exception e) {
      logger.error("Error al convertir el modelo a DTO: {}", e.getMessage());
      return null;
    }

  }

  public Verificable convertirCrearAudDTOModel(NuevoVerificableADTO nuevoVerificableDTO, AuditoriaDTO auditoriaDTO,
      DetalleRecomendacion detalleRecomendacion, EstadoVerificable estadoSeg, String rutaArchivo) {
    Verificable verificable = new Verificable();
    try {
      verificable.setAreve_codigo(null);
      verificable.setAreve_adjunto(rutaArchivo);
      verificable.setAreve_descripcion(nuevoVerificableDTO.getAreve_descripcion());
      verificable.setDetalleRecomendacion(detalleRecomendacion);
      verificable.setAreve_estado('1');
      verificable.setAreve_fecha(new Date());
      verificable.setAreve_fecha_carga(new Date());
      verificable.setAreve_usuario(auditoriaDTO.getUsuario());
      verificable.setEstadoVer(estadoSeg);
      verificable.setAreve_observacion(nuevoVerificableDTO.getAreve_motivo_estado());
      if (estadoSeg != null && estadoSeg.getAreev_codigo() != null && !estadoSeg.getAreev_codigo().equals(1L)) {
        verificable.setAreve_fecha_validacion(new Date());
      } else {
        verificable.setAreve_fecha_validacion(null);
      }

      return verificable;
    } catch (Exception e) {
      logger.error("Error al convertir el modelo a DTO: {}", e.getMessage());
      return verificable;
    }

  }

  // Primera version de actualización.
  public Verificable convertirValidarDTOModel(Long areve_codigo, ValidarVerificableDTO validarVerificableDTO,
      Verificable verificableOld, EstadoVerificable estadoSegNew) {
    verificableOld.setEstadoVer(estadoSegNew);
    verificableOld.setAreve_observacion(validarVerificableDTO.getAreve_motivo_estado());

    verificableOld.setAreve_fecha(new Date());
    verificableOld.setAreve_fecha_validacion(new Date());

    return verificableOld;
  }

  public Verificable convertirActualizarResDTOModel(Long areve_codigo, Verificable verificableOld,
      ActualizarVerificableDTO dto, String rutaArchivo, AuditoriaDTO auditoriaDTO) {
    try {
      verificableOld.setAreve_codigo(areve_codigo);
      verificableOld.setAreve_adjunto(rutaArchivo);
      verificableOld.setAreve_descripcion(dto.getAreve_descripcion());
      verificableOld.setAreve_fecha(new Date());
      if (verificableOld.getAreve_fecha_carga() == null) {
        verificableOld.setAreve_fecha_carga(new Date());
      }
      verificableOld.setAreve_usuario(auditoriaDTO.getUsuario());
      return verificableOld;
    } catch (Exception e) {
      logger.error("Error al convertir el modelo a DTO: {}", e.getMessage());
      return verificableOld;
    }
  }

  public Verificable convertirActualizarAudDTOModel(Long areve_codigo, Verificable verificableOld,
      EstadoVerificable estadoSeg,
      DetalleRecomendacion detalleRecomendacion,
      ActualizarVerificableDTO dto, String rutaArchivo, AuditoriaDTO auditoriaDTO) {
    try {
      verificableOld.setAreve_codigo(areve_codigo);
      verificableOld.setAreve_adjunto(rutaArchivo);
      verificableOld.setAreve_descripcion(dto.getAreve_descripcion());
      verificableOld.setAreve_fecha(new Date());
      
      // Preservar la fecha original de carga si ya existe
      if (verificableOld.getAreve_fecha_carga() == null) {
        verificableOld.setAreve_fecha_carga(new Date());
      }
      
      verificableOld.setAreve_usuario(auditoriaDTO.getUsuario());
      verificableOld.setEstadoVer(estadoSeg);
      if (dto.getAreve_motivo_estado() != null) {
        verificableOld.setAreve_observacion(dto.getAreve_motivo_estado());
      }
      
      // Asignar fecha de validación actual al ser validado con un estado definitivo
      if (estadoSeg != null && estadoSeg.getAreev_codigo() != null && !estadoSeg.getAreev_codigo().equals(1L)) {
        verificableOld.setAreve_fecha_validacion(new Date());
      }
      
      verificableOld.setDetalleRecomendacion(detalleRecomendacion);

      return verificableOld;
    } catch (Exception e) {
      logger.error("Error al convertir el modelo a DTO: {}", e.getMessage());
      return verificableOld;
    }
  }
}
