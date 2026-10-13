package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.mapper;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.mapper.RecomendacionMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.mapper.ResponsableMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models.Responsable;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.AprobarSolicitudDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.NuevoSolicitudDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos.SolicitudDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.models.Solicitud;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper;

@Component
public class SolicitudMapper {
  @Autowired
  private RecomendacionMapper recomendacionMapper;

  @Autowired
  private ResponsableMapper responsableMapper;

  public Solicitud convertirCrearDTOModel(NuevoSolicitudDTO nuevoSolicitudDTO, AuditoriaDTO auditoriaDTO,
      String rutaBD, Recomendacion recomendacion, Responsable responsable) {
    Solicitud solicitud = new Solicitud();
    solicitud.setAreso_codigo(null);
    solicitud.setAreso_descripcion(nuevoSolicitudDTO.getAreso_descripcion());
    solicitud.setAreso_estado_aprob('1');
    solicitud.setAreso_fecha_max_ant(DateMapper.convertirFechaFormato(nuevoSolicitudDTO.getAreso_fecha_max_ant()));
    solicitud.setAreso_fecha_max_nue(DateMapper.convertirFechaFormato(nuevoSolicitudDTO.getAreso_fecha_max_nue()));
    solicitud.setAreso_memorando(nuevoSolicitudDTO.getAreso_memorando());
    solicitud.setAreso_tipo_cambio(
      nuevoSolicitudDTO.getAreso_tipo_cambio() != null && !nuevoSolicitudDTO.getAreso_tipo_cambio().trim().isEmpty()
        ? nuevoSolicitudDTO.getAreso_tipo_cambio().trim()
        : "Ampliación");
    solicitud.setAreso_adjunto(rutaBD);
    solicitud.setAreso_fecha_carga(new Date());

    solicitud.setResponsable(responsable);
    solicitud.setRecomendacion(recomendacion);

    solicitud.setAreso_usuario(auditoriaDTO.getUsuario());
    solicitud.setAreso_estado('1');
    solicitud.setAreso_fecha(new Date());

    return solicitud;

  }

  public SolicitudDTO convertirModelDTO(Solicitud solicitud) {
    SolicitudDTO solicitudDTO = new SolicitudDTO();
    solicitudDTO.setAreso_codigo(solicitud.getAreso_codigo());
    solicitudDTO.setNumeroSolicitud(solicitud.getAreso_codigo());
    solicitudDTO.setAreso_descripcion(solicitud.getAreso_descripcion());
    solicitudDTO.setAsero_estado_aprob(solicitud.getAreso_estado_aprob());
    solicitudDTO.setAreso_fecha_max_ant(DateMapper.convertirFechaFormato(solicitud.getAreso_fecha_max_ant()));
    solicitudDTO.setAreso_fecha_max_nue(DateMapper.convertirFechaFormato(solicitud.getAreso_fecha_max_nue()));
    solicitudDTO.setAreso_memorando(solicitud.getAreso_memorando());
    solicitudDTO.setAreso_tipo_cambio(solicitud.getAreso_tipo_cambio());
    solicitudDTO.setAreso_adjunto(solicitud.getAreso_adjunto());
    solicitudDTO.setAreso_fecha_carga(DateMapper.convertirFechaFormato(solicitud.getAreso_fecha_carga()));
    solicitudDTO.setRecomendacion(recomendacionMapper.convertirModelDTO(solicitud.getRecomendacion()));
    solicitudDTO.setResponsable(responsableMapper.convertirDTOModel(solicitud.getResponsable()));

    return solicitudDTO;

  }

  public void aprobarSolicitud(Solicitud solicitud, AprobarSolicitudDTO aprobarSolicitudDTO) {
    solicitud.setAseru_codigo_aut(
        aprobarSolicitudDTO.getAseru_codigo_per() != null ? aprobarSolicitudDTO.getAseru_codigo_per()
            : solicitud.getAseru_codigo_aut());
    solicitud.setAreso_estado_aprob(aprobarSolicitudDTO.getAreso_estado_aprob());
    solicitud.setAreso_fecha(new Date());
  }

  public RecomendacionMapper getRecomendacionMapper() {
    return recomendacionMapper;
  }

  public ResponsableMapper getResponsableMapper() {
    return responsableMapper;
  }
}
