package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.mapper;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models.Informe;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos.ConclusionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos.HallazgoConclusionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.dtos.ActualizarHallazgoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.dtos.HallazgoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.dtos.NuevoHallazgoDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.models.Hallazgo;

@Component
public class HallazgoMapper {

  /**
   * Convierte un modelo de Hallazgo a un DTO con sus conclusiones asociadas.
   * 
   * @param hallazgo     El objeto Hallazgo a convertir.
   * @param conclusiones Lista de conclusiones asociadas al hallazgo.
   * @return Un objeto HallazgoConclusionDTO con los datos del hallazgo y sus
   *         conclusiones.
   */
  public HallazgoConclusionDTO convertirConModelDTO(Hallazgo hallazgo, List<ConclusionDTO> conclusiones) {
    HallazgoConclusionDTO hallazgoDTO = new HallazgoConclusionDTO();
    hallazgoDTO.setAreha_codigo(hallazgo.getAreha_codigo());
    hallazgoDTO.setAreha_titulo(hallazgo.getAreha_titulo());
    hallazgoDTO.setConclusiones(conclusiones);
    return hallazgoDTO;
  }

  /**
   * Convierte un DTO de creación de hallazgo a un modelo de Hallazgo.
   * 
   * @param dto     DTO con los datos del nuevo hallazgo.
   * @param informe Informe asociado al hallazgo.
   * @return Un objeto Hallazgo con los datos establecidos.
   */
  public Hallazgo convertirCrearDTOModel(NuevoHallazgoDTO dto, Informe informe) {
    Hallazgo hallazgo = new Hallazgo();
    hallazgo.setAreha_codigo(null);
    hallazgo.setAreha_titulo(dto.getAreha_titulo());
    hallazgo.setInforme(informe);
    hallazgo.setAreha_estado('1');
    hallazgo.setAreha_estado_seg('1');
    hallazgo.setAreha_usuario(dto.getAuditoria().getUsuario());
    hallazgo.setAreha_fecha(new Date());
    return hallazgo;
  }

  /**
   * Convierte un modelo de Hallazgo a un DTO básico.
   * 
   * @param hallazgo El objeto Hallazgo a convertir.
   * @return Un objeto HallazgoDTO con los datos básicos del hallazgo.
   */
  public HallazgoDTO convertirModelDTO(Hallazgo hallazgo) {
    HallazgoDTO hallazgoDTO = new HallazgoDTO();
    hallazgoDTO.setAreha_codigo(hallazgo.getAreha_codigo());
    hallazgoDTO.setAreha_titulo(hallazgo.getAreha_titulo());
    return hallazgoDTO;
  }

  /**
   * Convierte un DTO de actualización de hallazgo en un modelo de Hallazgo
   * actualizado.
   * 
   * @param dto         DTO con los datos actualizados del hallazgo.
   * @param hallazgoOld El objeto Hallazgo original a actualizar.
   * @return El objeto Hallazgo actualizado.
   */
  public Hallazgo convertirActualizarDTOModel(ActualizarHallazgoDTO dto, Hallazgo hallazgoOld) {
    hallazgoOld.setAreha_codigo(hallazgoOld.getAreha_codigo());
    hallazgoOld.setAreha_titulo(dto.getAreha_titulo());
    hallazgoOld.setAreha_estado(hallazgoOld.getAreha_estado());
    hallazgoOld.setAreha_fecha(new Date());
    hallazgoOld.setAreha_usuario(dto.getAuditoria().getUsuario());
    return hallazgoOld;
  }
}
