package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.mapper;

import java.util.Date;

import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.dto.ActualizarTipoInfDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.dto.NuevoTipoInfDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.dto.TipoInfDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.models.TipoInforme;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper;
import lombok.Data;

@Component
@Data
public class TipoInformeMapper {
  /**
   * Convierte un objeto TipoInforme en un objeto TipoInfDTO.
   *
   * @param tipoInforme Objeto TipoInforme a convertir.
   * @return Objeto TipoInfDTO convertido.
   */
  public TipoInfDTO convertirModelDTO(TipoInforme tipoInforme) {
    TipoInfDTO tipoInfDTO = new TipoInfDTO();
    tipoInfDTO.setAinti_codigo(tipoInforme.getAinti_codigo());
    tipoInfDTO.setAinti_descripcion(tipoInforme.getAinti_descripcion());
    tipoInfDTO.setAinti_fecha(DateMapper.convertirFechaFormato(tipoInforme.getAinti_fecha()));
    return tipoInfDTO;
  }

  /**
   * Convierte un objeto NuevoTipoInfDTO en un objeto TipoInforme.
   *
   * @param nuevoTipoInfDTO Objeto NuevoTipoInfDTO con los datos a convertir.
   * @return Objeto TipoInforme convertido.
   */
  public TipoInforme convertirCrearDTOModel(NuevoTipoInfDTO nuevoTipoInfDTO) {
    TipoInforme tipoInforme = new TipoInforme();
    tipoInforme.setAinti_codigo(null);
    tipoInforme.setAinti_descripcion(nuevoTipoInfDTO.getAinti_descripcion());
    tipoInforme.setAinti_estado('1');
    tipoInforme.setAinti_fecha(new Date());
    tipoInforme.setAinti_usuario(nuevoTipoInfDTO.getAuditoria().getUsuario());
    return tipoInforme;
  }

  /**
   * Convierte un objeto ActualizarTipoInfDTO en un objeto TipoInforme.
   *
   * @param actualizarTipoInfDTO  Datos actualizados del tipo de informe.
   * @param actualizarTipoInforme Objeto TipoInforme existente que se actualizará.
   * @return Objeto TipoInforme actualizado.
   */
  public TipoInforme convertirActualizarDTOModel(ActualizarTipoInfDTO actualizarTipoInfDTO,
      TipoInforme actualizarTipoInforme) {
    TipoInforme tipoInforme = new TipoInforme();
    tipoInforme.setAinti_codigo(actualizarTipoInforme.getAinti_codigo());
    tipoInforme.setAinti_descripcion(actualizarTipoInfDTO.getAinti_descripcion());
    tipoInforme.setAinti_estado(actualizarTipoInforme.getAinti_estado());
    tipoInforme.setAinti_fecha(new Date());
    tipoInforme.setAinti_usuario(actualizarTipoInfDTO.getAuditoria().getUsuario());
    return tipoInforme;
  }
}
