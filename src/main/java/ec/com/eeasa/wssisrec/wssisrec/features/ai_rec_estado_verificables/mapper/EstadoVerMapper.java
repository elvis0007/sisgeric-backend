package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.mapper;

import java.util.Date;

import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.dtos.ActualizarEstadoVerDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.dtos.EstadoVerDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.dtos.NuevoEstadoVerDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.models.EstadoVerificable;

@Component
public class EstadoVerMapper {

  public EstadoVerificable convertirCrearDTOModel(NuevoEstadoVerDTO nuevoEstadoVerDTO) {
    EstadoVerificable estadoVerificable = new EstadoVerificable();
    estadoVerificable.setAreev_codigo(null);
    estadoVerificable.setAreev_descripcion(nuevoEstadoVerDTO.getAreev_descripcion());
    estadoVerificable.setAreev_usuario(nuevoEstadoVerDTO.getAuditoria().getUsuario());
    estadoVerificable.setAreev_estado('1');
    estadoVerificable.setAreev_fecha(new Date());
    return estadoVerificable;
  }

  public EstadoVerDTO convertirModelDTO(EstadoVerificable estadoVerificable) {
    EstadoVerDTO estadoVerDTO = new EstadoVerDTO();
    estadoVerDTO.setAreev_codigo(estadoVerificable.getAreev_codigo());
    estadoVerDTO.setAreev_descripcion(estadoVerificable.getAreev_descripcion());
    return estadoVerDTO;
  }

  public void convertirActualizarDTOModel(EstadoVerificable estadoVerificableOld,
      ActualizarEstadoVerDTO actualizarEstadoVerDTO) {
    estadoVerificableOld.setAreev_descripcion(actualizarEstadoVerDTO.getAreev_descripcion());
    estadoVerificableOld.setAreev_fecha(new Date());
    estadoVerificableOld.setAreev_usuario(actualizarEstadoVerDTO.getAuditoria().getUsuario());
  }
}
