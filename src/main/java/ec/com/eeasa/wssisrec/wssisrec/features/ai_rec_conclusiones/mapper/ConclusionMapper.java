package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos.ActualizarConclusionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos.ConclusionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos.NuevoConclusionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.models.Conclusion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.models.Hallazgo;

@Component
public class ConclusionMapper {

  /**
   * Convierte un modelo de dominio Conclusion a su correspondiente DTO
   * (ConclusionDTO).
   *
   * @param conclusion El modelo de dominio Conclusion que se quiere convertir.
   * @return El DTO ConclusionDTO correspondiente al modelo Conclusion.
   */
  public ConclusionDTO convertirModelDTO(Conclusion conclusion) {
    ConclusionDTO conclusionDTO = new ConclusionDTO();
    conclusionDTO.setAreco_codigo(conclusion.getAreco_codigo());
    conclusionDTO.setAreco_conclusion(conclusion.getAreco_conclusion());
    return conclusionDTO;
  }

  /**
   * Convierte un DTO de actualización (ActualizarConclusionDTO) en un modelo de
   * dominio Conclusion.
   * Se actualiza el modelo Conclusion con los datos proporcionados en el DTO.
   *
   * @param dto           El DTO con los nuevos datos para actualizar.
   * @param conclusionOld El modelo de dominio Conclusion que se actualizará.
   * @return El modelo de dominio Conclusion actualizado.
   */
  public Conclusion convertirActualizarDTOModel(ActualizarConclusionDTO dto, Conclusion conclusionOld) {
    conclusionOld.setAreco_conclusion(dto.getAreco_conclusion());
    conclusionOld.setAreco_fecha(new Date());
    conclusionOld.setAreco_usuario(dto.getAuditoria().getUsuario());
    return conclusionOld;
  }

  /**
   * Convierte un DTO de creación (NuevoConclusionDTO) en un nuevo modelo de
   * dominio Conclusion.
   * Se crea un nuevo objeto Conclusion con los datos del DTO.
   *
   * @param dto      El DTO con los datos para crear un nuevo modelo Conclusion.
   * @param hallazgo El hallazgo al que se asociará esta conclusión.
   * @return El nuevo modelo de dominio Conclusion creado.
   */
  public Conclusion convertirCrearDTOModel(NuevoConclusionDTO dto, Hallazgo hallazgo) {
    Conclusion conclusion = new Conclusion();
    conclusion.setAreco_codigo(null);
    conclusion.setAreco_conclusion(dto.getAreco_conclusion());
    conclusion.setAreco_estado('1');
    conclusion.setAreco_estado_seg('1');
    conclusion.setAreco_fecha(new Date());
    conclusion.setAreco_usuario(dto.getAuditoria().getUsuario());
    conclusion.setHallazgo(hallazgo);
    return conclusion;
  }

  /**
   * Convierte una lista de modelos de dominio Conclusion en una lista de DTOs
   * (ConclusionDTO).
   *
   * @param conclusiones La lista de modelos de dominio Conclusion que se desean
   *                     convertir.
   * @return Una lista de DTOs ConclusionDTO correspondientes a los modelos
   *         Conclusion.
   */
  public List<ConclusionDTO> convertirListModelDTO(List<Conclusion> conclusiones) {
    List<ConclusionDTO> conclusionesDTO = new ArrayList<>();
    conclusionesDTO = conclusiones.stream().map(this::convertirModelDTO).collect(Collectors.toList());
    return conclusionesDTO;
  }
}
