package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.mapper;

import java.util.Date;

import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.ActualizarEstadoSegDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.EstadoSegDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.NuevoEstadoSegDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.models.EstadoSeg;

@Component
public class EstadoSegMapper {

  public EstadoSegDTO convertirModelDTO(EstadoSeg estadoSeg) {
    EstadoSegDTO estadoSegDTO = new EstadoSegDTO();
    estadoSegDTO.setArees_codigo(estadoSeg.getArees_codigo());
    estadoSegDTO.setArees_descripcion(estadoSeg.getArees_descripcion());
    return estadoSegDTO;
  }

  public EstadoSeg convertirCrearDTOModel(NuevoEstadoSegDTO dto) {
    EstadoSeg estadoSeg = new EstadoSeg();
    estadoSeg.setArees_codigo(null);
    estadoSeg.setArees_descripcion(dto.getArees_descripcion());
    estadoSeg.setArees_estado('1');
    estadoSeg.setArees_usuario(dto.getAuditoria().getUsuario());
    estadoSeg.setArees_fecha(new Date());

    return estadoSeg;
  }

  public EstadoSeg convertirActualizarDTOModel(ActualizarEstadoSegDTO dto, EstadoSeg estadoSeg) {
    EstadoSeg estadoSeg2 = new EstadoSeg();
    estadoSeg2.setArees_codigo(estadoSeg.getArees_codigo());
    estadoSeg2.setArees_descripcion(dto.getArees_descripcion());
    estadoSeg2.setArees_estado(estadoSeg.getArees_estado());
    estadoSeg2.setArees_fecha(new Date());
    estadoSeg2.setArees_usuario(dto.getAuditoria().getUsuario());
    return estadoSeg2;

  }

}
