package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.mapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.models.Conclusion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.dtos.EstadoSegDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.models.EstadoSeg;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarFechaMaxDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ActualizarRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.NuevaRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.RecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.ValidarRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper;

@Component
public class RecomendacionMapper {
  @org.springframework.beans.factory.annotation.Autowired
  private ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.EvidenciaRequeridaServicio evidenciaRequeridaServicio;

  private static final Logger logger = LoggerFactory.getLogger(RecomendacionMapper.class);

  public Recomendacion convertirCrearDTOModel(NuevaRecomendacionDTO dto, EstadoSeg estadoSeg, Conclusion conclusion) {
    Recomendacion recomendacion = new Recomendacion();
    recomendacion.setArere_codigo(null);
    recomendacion.setArere_descripcion(dto.getArere_descripcion());
    recomendacion.setArere_fecha_maxima(DateMapper.convertirFechaFormato(dto.getArere_fecha_maxima()));
    recomendacion.setArere_fecha_inicio_control(DateMapper.convertirFechaFormato(dto.getArere_fecha_inicio_control()));
    recomendacion.setArere_num_rec(dto.getArere_num_rec()); // Este numero de recomendacion es del informe, toca ver el
                                                            // numero de
    // recomendaciones que hay en el informe
    recomendacion.setArere_fecha_corte(null);
    recomendacion.setArere_obs_en_proceso(null);
    recomendacion.setArere_obs_en_validacion(null);
    recomendacion.setArere_obs_no_aplica(null);
    recomendacion.setArere_obs_no_cumplida(null);
    recomendacion.setArere_usuario(dto.getAuditoria().getUsuario());
    recomendacion.setArere_fecha(new Date());
    recomendacion.setArere_estado('1');
    recomendacion.setArere_riesgo_verificado(dto.getArere_riesgo_verificado());
    recomendacion.setEstadoSeg(estadoSeg);
    recomendacion.setConclusion(conclusion);
    return recomendacion;
  }

  public RecomendacionDTO convertirToActModelDTO(Recomendacion recomendacion) {
    RecomendacionDTO recomendacionDTO = new RecomendacionDTO();
    recomendacionDTO.setArere_codigo(recomendacion.getArere_codigo());
    recomendacionDTO.setArere_descripcion(recomendacion.getArere_descripcion());
    recomendacionDTO.setArere_fecha_maxima(DateMapper.convertirFechaFormato(recomendacion.getArere_fecha_maxima()));
    return recomendacionDTO;
  }

  public RecomendacionDTO convertirModelDTO(Recomendacion recomendacion) {
    try {
      // Crear RecomendacionDTO
      RecomendacionDTO recomendacionDTO = new RecomendacionDTO();

      // Asignar y verificar arere_codigo
      recomendacionDTO.setArere_codigo(recomendacion.getArere_codigo());
      // Asignar y verificar arere_descripcion
      recomendacionDTO.setArere_descripcion(recomendacion.getArere_descripcion());
      // Asignar y verificar arere_num_rec
      recomendacionDTO.setArere_num_rec(recomendacion.getArere_num_rec());
      //
      recomendacionDTO.setArere_num_prorroga(recomendacion.getArere_num_prorroga());
      // Asignar y verificar arere_fecha_maxima
      String fechaInicioControl = DateMapper.convertirFechaFormato(recomendacion.getArere_fecha_inicio_control());
      if (recomendacion.getConclusion() != null && recomendacion.getConclusion().getHallazgo() != null && recomendacion.getConclusion().getHallazgo().getInforme() != null) {
        ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models.Informe inf = recomendacion.getConclusion().getHallazgo().getInforme();
        recomendacionDTO.setAinin_memorando(inf.getAinin_memorando());
        recomendacionDTO.setAinin_titulo(inf.getAinin_titulo());
        if (fechaInicioControl == null) {
          Date fAprob = inf.getAinin_fecha_aprobacion() != null ? inf.getAinin_fecha_aprobacion() : inf.getAinin_fecha();
          fechaInicioControl = DateMapper.convertirFechaFormato(fAprob);
        }
      }
      recomendacionDTO.setArere_fecha_inicio_control(fechaInicioControl);
      String fechaMaximaFormateada = DateMapper.convertirFechaFormato(recomendacion.getArere_fecha_maxima());
      recomendacionDTO.setArere_fecha_maxima(fechaMaximaFormateada);

      // Calcular y asignar plazo
      int plazoDias = DateMapper.calcularDias(recomendacion.getArere_fecha_maxima());
      recomendacionDTO.setPlazo(plazoDias);
      recomendacionDTO.setArere_evidencias_requeridas(evidenciaRequeridaServicio.listar(recomendacion.getArere_codigo()));
      recomendacionDTO.setArere_evidencia_requerida(recomendacionDTO.getArere_evidencias_requeridas().isEmpty() ? null
          : evidenciaRequeridaServicio.serializar(recomendacionDTO.getArere_evidencias_requeridas()));
      recomendacionDTO.setArere_riesgo_verificado(recomendacion.getArere_riesgo_verificado());

      // Asignar y verificar estado_seguimiento
      if (recomendacion.getEstadoSeg() != null) {
        recomendacionDTO.setEstado_seguimiento(
            new EstadoSegDTO(recomendacion.getEstadoSeg().getArees_codigo(),
                recomendacion.getEstadoSeg().getArees_descripcion()));
      } else {
      }
      // Asignar y verificar motivo_estado
      String motivoEstado = obtenerMotivoEstado(recomendacion);
      recomendacionDTO.setMotivo_estado(motivoEstado);
      return recomendacionDTO;
    } catch (Exception e) {
      logger.error("Error al convertir el modelo a DTO: {}", e.getMessage());
      return null;
    }

  }

  public Recomendacion convertirActFechaMaxModelDTO(ActualizarFechaMaxDTO aFechaMaxDTO,
      Recomendacion recomendacionOld) {
    try {
      Recomendacion recomendacion = recomendacionOld;
      recomendacion.setArere_fecha_maxima(DateMapper.convertirFechaFormato(aFechaMaxDTO.getArere_fecha_maxima()));
      Integer prorroga = recomendacionOld.getArere_num_prorroga();
      if (prorroga == null) {
        prorroga = 0; // Valor predeterminado
      }
      recomendacion.setArere_num_prorroga(prorroga + 1);
      return recomendacion;
    } catch (Exception e) {
      logger.error("Error al convertir el modelo a DTO: {}", e.getMessage());
      return null;
    }
  }

  public RecomendacionDTO convertirDetalleModelDTO(DetalleRecomendacion detalle) {
    try {

      RecomendacionDTO recomendacionDTO = new RecomendacionDTO();
      recomendacionDTO.setArere_codigo(detalle.getRecomendacion().getArere_codigo());
      recomendacionDTO.setArere_descripcion(detalle.getRecomendacion().getArere_descripcion());
      recomendacionDTO.setArere_num_rec(detalle.getRecomendacion().getArere_num_rec());
      Date fechaMax = (detalle.getAredr_fecha_maxima() != null) ? detalle.getAredr_fecha_maxima() : detalle.getRecomendacion().getArere_fecha_maxima();
      recomendacionDTO
          .setArere_fecha_maxima(DateMapper.convertirFechaFormato(fechaMax));
      int plazo = DateMapper.calcularDias(fechaMax);
      recomendacionDTO.setPlazo(plazo);

      Long estadoGlobal = (detalle.getRecomendacion() != null && detalle.getRecomendacion().getEstadoSeg() != null) 
          ? detalle.getRecomendacion().getEstadoSeg().getArees_codigo() : 1L;

      if (Long.valueOf(2L).equals(estadoGlobal)) {
        recomendacionDTO.setEstado_seguimiento(new EstadoSegDTO(2L, "CUMPLIDA"));
      } else if (Long.valueOf(4L).equals(estadoGlobal)) {
        recomendacionDTO.setEstado_seguimiento(new EstadoSegDTO(4L, "NO APLICA"));
      } else if (plazo < 0) {
        recomendacionDTO.setEstado_seguimiento(new EstadoSegDTO(3L, "NO CUMPLIDA"));
      } else {
        recomendacionDTO.setEstado_seguimiento(new EstadoSegDTO(1L, "EN PROCESO"));
      }
      recomendacionDTO.setMotivo_estado(obtenerMotivoEstado(detalle.getRecomendacion()));
      recomendacionDTO.setArere_evidencia_requerida(detalle.getRecomendacion().getArere_evidencia_requerida());
      recomendacionDTO.setArere_riesgo_verificado(detalle.getRecomendacion().getArere_riesgo_verificado());
      return recomendacionDTO;
    } catch (Exception e) {
      logger.error("Error al convertir el modelo a DTO: {}", e.getMessage());
      return null;
    }
  }

  public RecomendacionDTO convertirDetalleResponsableModelDTO(DetalleRecomendacion detalle) {
    try {

      RecomendacionDTO recomendacionDTO = new RecomendacionDTO();
      recomendacionDTO.setArere_codigo(detalle.getRecomendacion().getArere_codigo());
      recomendacionDTO.setArere_descripcion(detalle.getRecomendacion().getArere_descripcion());
      recomendacionDTO.setArere_num_rec(detalle.getRecomendacion().getArere_num_rec());
      Date fechaMax = (detalle.getAredr_fecha_maxima() != null) ? detalle.getAredr_fecha_maxima() : detalle.getRecomendacion().getArere_fecha_maxima();
      recomendacionDTO
          .setArere_fecha_maxima(DateMapper.convertirFechaFormato(fechaMax));
      int plazo = DateMapper.calcularDias(fechaMax);
      recomendacionDTO.setPlazo(plazo);

      Long estadoGlobal = (detalle.getRecomendacion() != null && detalle.getRecomendacion().getEstadoSeg() != null) 
          ? detalle.getRecomendacion().getEstadoSeg().getArees_codigo() : 1L;

      if (Long.valueOf(2L).equals(estadoGlobal)) {
        recomendacionDTO.setEstado_seguimiento(new EstadoSegDTO(2L, "CUMPLIDA"));
      } else if (Long.valueOf(4L).equals(estadoGlobal)) {
        recomendacionDTO.setEstado_seguimiento(new EstadoSegDTO(4L, "NO APLICA"));
      } else if (plazo < 0) {
        recomendacionDTO.setEstado_seguimiento(new EstadoSegDTO(3L, "NO CUMPLIDA"));
      } else {
        recomendacionDTO.setEstado_seguimiento(new EstadoSegDTO(1L, "EN PROCESO"));
      }
      recomendacionDTO.setArere_evidencia_requerida(detalle.getRecomendacion().getArere_evidencia_requerida());
      recomendacionDTO.setArere_riesgo_verificado(detalle.getRecomendacion().getArere_riesgo_verificado());
      recomendacionDTO.setAredr_codigo(detalle.getAredr_codigo());
      if (detalle.getResponsable() != null) {
        recomendacionDTO.setArers_codigo(detalle.getResponsable().getArers_codigo());
        recomendacionDTO.setArers_codigo_per(detalle.getResponsable().getArers_codigo_per());
      }
      // recomendacionDTO.setMotivo_estado(detalleMapper.obtenerMotivoEstado(detalle));
      return recomendacionDTO;
    } catch (Exception e) {
      logger.error("Error al convertir el modelo a DTO: {}", e.getMessage());
      return null;
    }
  }

  public Recomendacion convertirActualizarDTOModel(ActualizarRecomendacionDTO dto, Recomendacion recomendacionOld) {

    recomendacionOld.setArere_descripcion(dto.getArere_descripcion());
    recomendacionOld.setArere_fecha_maxima(DateMapper.convertirFechaFormato(dto.getArere_fecha_maxima()));
    recomendacionOld.setArere_num_rec(dto.getArere_num_rec());
    if (dto.getArere_evidencia_requerida() != null) {
      recomendacionOld.setArere_evidencia_requerida(dto.getArere_evidencia_requerida());
    }
    if (dto.getArere_riesgo_verificado() != null) {
      recomendacionOld.setArere_riesgo_verificado(dto.getArere_riesgo_verificado());
    }
    recomendacionOld.setArere_usuario(dto.getAuditoria().getUsuario());
    recomendacionOld.setArere_fecha(new Date());
    return recomendacionOld;
  }

  public String obtenerMotivoEstado(Recomendacion recomendacion) {
    try {

      Map<String, Supplier<String>> estadosMotivos = new HashMap<>();
      estadosMotivos.put("NO CUMPLIDA", recomendacion::getArere_obs_no_cumplida);
      estadosMotivos.put("EN PROCESO", recomendacion::getArere_obs_en_proceso);
      estadosMotivos.put("NO APLICA", recomendacion::getArere_obs_no_aplica);
      estadosMotivos.put("CUMPLIDA", recomendacion::getArere_obs_en_validacion);

      String estadoDescripcion = recomendacion.getEstadoSeg().getArees_descripcion();

      Supplier<String> supplierMotivo = estadosMotivos.get(estadoDescripcion);

      String motivo = supplierMotivo.get();
      return motivo != null ? motivo : "Ninguna";
    } catch (Exception e) {
      logger.error("Error al obtener el motivo del estado: {}", e.getMessage());
      return "Ninguna";
    }
  }

  public Recomendacion convertirValidarRecomendacionDTOModel(Recomendacion recomendacionOld, EstadoSeg estadoSeg,
      ValidarRecomendacionDTO validarRecomendacionDTO) {
    recomendacionOld.setEstadoSeg(estadoSeg);
    setMotivoEstado(recomendacionOld, estadoSeg, validarRecomendacionDTO.getArere_motivo_estado());
    recomendacionOld.setArere_fecha(new Date());
    recomendacionOld.setArere_fecha_cumplida(new Date()); // Capturar fecha actual cuando se valida la recomendación
    recomendacionOld.setArere_usuario(validarRecomendacionDTO.getAuditoria().getUsuario());
    return recomendacionOld;
  }

  private void setMotivoEstado(Recomendacion verificable, EstadoSeg estadoSeg, String descripcionMotivo) {
    Map<String, Consumer<String>> estadosObservaciones = new HashMap<>();
    estadosObservaciones.put("NO CUMPLIDA",
        verificable::setArere_obs_no_cumplida);
    estadosObservaciones.put("EN PROCESO", verificable::setArere_obs_en_proceso);
    estadosObservaciones.put("NO APLICA", verificable::setArere_obs_no_aplica);
    estadosObservaciones.put("CUMPLIDA",
        verificable::setArere_obs_en_validacion);

    String estadoDescripcion = estadoSeg.getArees_descripcion();
    Consumer<String> setter = estadosObservaciones.get(estadoDescripcion);
    if (setter != null) {
      setter.accept(descripcionMotivo);
    }
  }

}
