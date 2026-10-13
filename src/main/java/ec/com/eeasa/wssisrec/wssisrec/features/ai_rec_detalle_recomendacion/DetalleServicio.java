package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.dtos.DetalleRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.dtos.NuevoDetalleDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.dtos.RecomendacionDetalleDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.mapper.DetalleMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.EstadoSegRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.models.EstadoSeg;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.RecomendacionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models.Responsable;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;

@Service
public class DetalleServicio {

  private static final Logger logger = LoggerFactory.getLogger(DetalleServicio.class);
  @Autowired
  private BitacoraServicio bitacoraServicio;

  @Autowired
  private DetalleRepositorio detalleRepositorio;

  @Autowired
  private DetalleMapper detalleMapper;

  @Autowired
  private EstadoSegRepositorio estadoSegRepositorio;

  @Autowired
  private RecomendacionRepositorio recomendacionRepositorio;

  @Transactional
  public DetalleRecomendacion registar(NuevoDetalleDTO dto,
      Recomendacion recomendacion, Responsable responsable) {
    try {
      EstadoSeg estadoSeg = estadoSegRepositorio.findByDescripcion("EN PROCESO");

      DetalleRecomendacion detalle = detalleMapper.convertirCrearDTOModel(dto, estadoSeg, recomendacion, responsable);

      DetalleRecomendacion respuesta = detalleRepositorio.save(detalle);

      try {
        bitacoraServicio.registrarBitacora(respuesta, dto.getAuditoria(), "CREAR");
      } catch (Exception e) {
        logger.error("Error al registrar bitácora para detalle de recomendación: {}", e.getMessage());
        // No propagar el error de bitácora para evitar rollback de la transacción principal
      }

      return detalle;
    } catch (Exception e) {
      logger.error("Error al registrar detalle: {}", e.getMessage());
      return null;
    }
  }

  @Transactional
  public List<DetalleRecomendacion> obtenerDetallesRecomendaciones(Recomendacion recomendacion) {
    List<DetalleRecomendacion> detalleRecomendacions = detalleRepositorio
        .findByRecomendacion(recomendacion.getArere_codigo());
    return detalleRecomendacions;
  }

  @Transactional
  public RecomendacionDetalleDTO obtenerRecomendacionDetalle(Long arere_codigo) {

    List<DetalleRecomendacion> detalleRecomendaciones = detalleRepositorio.findByRecomendacion(arere_codigo);
    Recomendacion recomendacion = recomendacionRepositorio.findById(arere_codigo).get();

    List<DetalleRecomendacionDTO> detalleRecomendacionDTOs = detalleRecomendaciones.stream()
        .map((detalle) -> detalleMapper.convertirModelDTO(detalle)).collect(Collectors.toList());
    RecomendacionDetalleDTO recomendacionDetalleDTO = new RecomendacionDetalleDTO(recomendacion.getArere_codigo(),
        recomendacion.getArere_descripcion(), detalleRecomendacionDTOs);
    return recomendacionDetalleDTO;
  }

  @Transactional
  public DetalleRecomendacion obtenerDetalleById(Long aredr_codigo) {
    DetalleRecomendacion detalleRecomendacion = detalleRepositorio.findById(aredr_codigo).get();
    return detalleRecomendacion;
  }

  @Transactional
  public DetalleRecomendacion obtenerPorRecomendacionResponsablePer(Long arere_codigo, String arers_codigo_per) {
    
    DetalleRecomendacion detalle = detalleRepositorio.findByRecomendacionResponsablePer(arere_codigo, arers_codigo_per);
    if (detalle == null) {
      logger.error("No se encontró detalle de recomendación");
    }
    return detalle;
  }

  @Transactional
  public DetalleRecomendacion obtenerPorResponsableRecomendacion(Long arere_codigo, Long arers_codigo) {
    DetalleRecomendacion detalle = detalleRepositorio.findByRecomendacionResponsable(arere_codigo, arers_codigo);
    return detalle;
  }


  @Transactional
  public DetalleRecomendacion obtenerPorVerificable(Long arere_codigo) {
    DetalleRecomendacion detalleRecomendacion = detalleRepositorio.findByVerificable(arere_codigo);
    return detalleRecomendacion;

  }

  @Transactional
  public boolean eliminarLogico(EliminarDTO eliminarDTO, Long aredr_codigo) {
    try {
      DetalleRecomendacion detalleRecomendacion = detalleRepositorio.findById(aredr_codigo).get();
      detalleRecomendacion.setAredr_estado('0');
      detalleRepositorio.save(detalleRecomendacion);
      
      try {
        bitacoraServicio.registrarBitacora(detalleRecomendacion, eliminarDTO.getAuditoria(), "ELIMINACION LOGICA");
      } catch (Exception e) {
        logger.error("Error al registrar bitácora para eliminación lógica de detalle: {}", e.getMessage());
        // No propagar el error de bitácora para evitar rollback de la transacción principal
      }
      
      return true;
    } catch (Exception e) {
      logger.error("Error al eliminar detalle recomendacion : {}", e.getMessage());
      return false;
    }

  }

}
