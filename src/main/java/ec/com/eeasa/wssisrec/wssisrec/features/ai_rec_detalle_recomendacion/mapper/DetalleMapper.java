package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.mapper;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.dtos.DetalleRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.dtos.NuevoDetalleDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.mapper.EstadoSegMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.models.EstadoSeg;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.mapper.ResponsableMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models.Responsable;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;

@Component
public class DetalleMapper {

  @Autowired
  EstadoSegMapper estadoSegMapper;

  @Autowired
  ResponsableMapper responsableMapper;

  public DetalleRecomendacion convertirCrearDTOModel(NuevoDetalleDTO dto, EstadoSeg estadoSeg,
      Recomendacion recomendacion, Responsable responsable) {
    DetalleRecomendacion detalle = new DetalleRecomendacion();
    detalle.setAredr_codigo(null);
    detalle.setRecomendacion(recomendacion);
    detalle.setResponsable(responsable);
    detalle.setAredr_fecha(new Date());
    detalle.setAredr_estado('1');
    if (recomendacion != null && recomendacion.getArere_fecha_maxima() != null) {
      detalle.setAredr_fecha_maxima(recomendacion.getArere_fecha_maxima());
    }
    return detalle;
  }

  public DetalleRecomendacion convertirCrearAllDTOModel(AuditoriaDTO dto, EstadoSeg estadoSeg) {
    DetalleRecomendacion detalle = new DetalleRecomendacion();
    detalle.setAredr_codigo(null);
    detalle.setRecomendacion(null);
    detalle.setAredr_usuario(dto.getUsuario());
    detalle.setAredr_fecha(new Date());
    detalle.setAredr_estado('1');
    return detalle;
  }

  public DetalleRecomendacionDTO convertirModelDTO(DetalleRecomendacion detalleRecomendacion) {
    DetalleRecomendacionDTO dto = new DetalleRecomendacionDTO();
    dto.setAredr_codigo(detalleRecomendacion.getAredr_codigo());
    dto.setResponsable(responsableMapper.convertirDTOModel(detalleRecomendacion.getResponsable()));

    return dto;
  }

  public void actualizarDetalleModel(DetalleRecomendacion detalleRecomendacionNuevo,
      DetalleRecomendacion detalleRecomendacionOld) {
    detalleRecomendacionNuevo.setAredr_fecha_corte(detalleRecomendacionOld.getAredr_fecha_corte());
  }

}
