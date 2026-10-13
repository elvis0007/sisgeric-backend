package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.core.auth.dtos.UsuarioDTO;
import ec.com.eeasa.wssisrec.wssisrec.core.auth.utils.PersonalServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.ActualizarResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.NuevoResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.ResponsableActDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.ResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.ResponsableViewDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.mapper.ResponsableMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models.Responsable;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.VerificableRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.models.Verificable;
import java.util.Optional;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.DetalleRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;

@Service
public class ResponsableServicio {

  private static final Logger logger = LoggerFactory.getLogger(ResponsableServicio.class);

  @Autowired
  private BitacoraServicio bitacoraServicio;

  @Autowired
  private ResponsableRepositorio responsableRepositorio;

  @Autowired
  private ResponsableMapper responsableMapper;

  @Autowired
  private PersonalServicio personalServicio;

  @Autowired
  private VerificableRepositorio verificableRepositorio;

  @Autowired
  private DetalleRepositorio detalleRepositorio;

  @Autowired
  private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

  @Transactional
  public Responsable registrarResponsable(NuevoResponsableDTO nuevoResponsableDTO) {
    try {
      // Convertir DTO a modelo
      Responsable responsable = responsableMapper.convertirCrearDTOModel(nuevoResponsableDTO);

      // Guardar en la base de datos
      Responsable respuesta = responsableRepositorio.save(responsable);

      // Registrar en la bitácora
      try {
        bitacoraServicio.registrarBitacora(respuesta, nuevoResponsableDTO.getAuditoria(), "CREAR");
      } catch (Exception e) {
        logger.error("Error al registrar bitácora para responsable: {}", e.getMessage());
        // No propagar el error de bitácora para evitar rollback de la transacción principal
      }

      return respuesta;

    } catch (Exception e) {
      logger.error("Error al registrar responsable: {}", e.getMessage());
      return null;
    }
  }

  @Transactional
  public ResponsableActDTO obtenerPorId(Long arers_codigo) {
    try {
      Responsable responsable = responsableRepositorio.findById(arers_codigo).get();
      ResponsableActDTO responsableActDTO = responsableMapper.convertirActDTOModel(responsable);
      return responsableActDTO;
    } catch (Exception e) {
      logger.error("Error al obtener responsables para la recomendación: {}", e.getMessage());
      return null;
    }
  }

  @Transactional
  public List<ResponsableViewDTO> obtenerPorRecomendacionResp(Long arere_codigo, String token_sisgerh) {
    try {
      List<Responsable> responsables = responsableRepositorio.findByRecomendacion(arere_codigo);

      if (token_sisgerh == "") {
        List<ResponsableViewDTO> responsableActDTOs = responsables.stream()
            .map((responsable) -> {
              ResponsableViewDTO responsableDTO = responsableMapper.convertirViewDTOModel(responsable);
              // Obtener la descripción del estado y observación del último verificable
              String descripcionEstado = obtenerDescripcionUltimoVerificable(arere_codigo, responsable.getArers_codigo());
              String observacion = obtenerObservacionUltimoVerificable(arere_codigo, responsable.getArers_codigo());
              responsableDTO.setAreev_descripcion(descripcionEstado);
              responsableDTO.setObservacion(observacion);
              enriquecerFechaYPlazo(arere_codigo, responsable.getArers_codigo(), responsableDTO);
              return responsableDTO;
            }).collect(Collectors.toList());
        return responsableActDTOs;
      }
      List<ResponsableViewDTO> responsableDTOs = responsables.stream()
          .map((responsable) -> {
            UsuarioDTO usuarioDTO = personalServicio.findUsuarioById(responsable.getArers_usuario_per(), token_sisgerh);
            ResponsableViewDTO responsableDTO = responsableMapper.convertirViewDTOModel(responsable);
            responsableDTO.setFotografia(usuarioDTO.getFotografia());
            responsableDTO.setNombre_completo(usuarioDTO.getNombre() + " " + usuarioDTO.getApellido());
            // Obtener la descripción del estado y observación del último verificable
            String descripcionEstado = obtenerDescripcionUltimoVerificable(arere_codigo, responsable.getArers_codigo());
            String observacion = obtenerObservacionUltimoVerificable(arere_codigo, responsable.getArers_codigo());
            responsableDTO.setAreev_descripcion(descripcionEstado);
            responsableDTO.setObservacion(observacion);
            enriquecerFechaYPlazo(arere_codigo, responsable.getArers_codigo(), responsableDTO);
            return responsableDTO;
          }).collect(Collectors.toList());
      return responsableDTOs;
    } catch (Exception e) {
      logger.error("Error al obtener responsables para la recomendación: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  private void enriquecerFechaYPlazo(Long arere_codigo, Long arers_codigo, ResponsableViewDTO dto) {
    try {
      DetalleRecomendacion det = detalleRepositorio.findByRecomendacionResponsable(arere_codigo, arers_codigo);
      if (det != null) {
        dto.setAredr_codigo(det.getAredr_codigo());
        // Consultar prórroga aprobada o en trámite vigente si existe
        Date fProrroga = null;
        try {
          String sqlProrroga = "SELECT MAX(S.ARESO_FECHA_MAX_NUE) FROM AI_REC_SOLICITUDES S " +
              "WHERE S.ARERE_CODIGO = ? AND S.ARERS_CODIGO = ? AND S.ARESO_ESTADO = '1' " +
              "AND S.ARESO_ESTADO_APROB = '3'";
          fProrroga = jdbcTemplate.queryForObject(sqlProrroga, Date.class, arere_codigo, arers_codigo);
        } catch (Exception e) {
          // ignore si no hay prórroga
        }

        Date fDet = det.getAredr_fecha_maxima();
        Date fRec = (det.getRecomendacion() != null) ? det.getRecomendacion().getArere_fecha_maxima() : null;

        Date fMax = fDet;
        if (fMax == null) {
          fMax = fRec;
        }
        if (fProrroga != null && (fMax == null || fProrroga.after(fMax))) {
          fMax = fProrroga;
        }

        if (fMax != null) {
          dto.setAredr_fecha_maxima(ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper.convertirFechaFormato(fMax));
        }

        // Si ya tiene verificable que JUSTIFICA
        boolean esJustificado = dto.getAreev_descripcion() != null && (
            "JUSTIFICA".equalsIgnoreCase(dto.getAreev_descripcion().trim()) ||
            "JUSTIFICADO".equalsIgnoreCase(dto.getAreev_descripcion().trim()) ||
            "VERIFICABLE JUSTIFICADO".equalsIgnoreCase(dto.getAreev_descripcion().trim())
        );

        if (esJustificado) {
          dto.setPlazo(0);
          dto.setEstado_plazo("CUMPLIDO");
        } else if (fMax != null) {
          int p = ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper.calcularDias(fMax);
          dto.setPlazo(p);
          dto.setEstado_plazo(p < 0 ? "VENCIDO" : "EN PLAZO");
        }
      }
    } catch (Exception ex) {
      logger.warn("Error al enriquecer fecha y plazo para responsable {}: {}", arers_codigo, ex.getMessage());
    }
  }

  @Transactional
  public List<ResponsableDTO> obtenerPorRecomendacion(Long arere_codigo, String token_sisgerh) {
    try {
      List<Responsable> responsables = responsableRepositorio.findByRecomendacion(arere_codigo);

      if (token_sisgerh == "") {
        List<ResponsableDTO> responsableActDTOs = responsables.stream()
            .map(responsableMapper::convertirDTOModel)
            .collect(Collectors.toList());
        return responsableActDTOs;
      }
      List<ResponsableDTO> responsableDTOs = responsables.stream()
          .map((responsable) -> {
            UsuarioDTO usuarioDTO = personalServicio.findUsuarioById(responsable.getArers_usuario_per(), token_sisgerh);
            ResponsableDTO responsableDTO = responsableMapper.convertirDTOModel(responsable);
            responsableDTO.setFotografia(usuarioDTO.getFotografia());
            responsableDTO.setNombre_completo(usuarioDTO.getNombre() + " " + usuarioDTO.getApellido());
            return responsableDTO;
          }).collect(Collectors.toList());
      return responsableDTOs;
    } catch (Exception e) {
      logger.error("Error al obtener responsables para la recomendación: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  @Transactional
  public ResponsableDTO obtenerPorCodigo(Long arers_codigo) {
    try {
      Responsable responsables = responsableRepositorio.findById(arers_codigo).get();
      ResponsableDTO responsableDTO = responsableMapper.convertirDTOModel(responsables);

      return responsableDTO;
    } catch (Exception e) {
      logger.error("Error al obtener responsables para la recomendación: {}", e.getMessage());
      return null;
    }
  }

  @Transactional
  public Responsable obtenerPorResponsable(Long arers_codigo) {
    try {
      Responsable responsables = responsableRepositorio.findById(arers_codigo).get();

      return responsables;
    } catch (Exception e) {
      logger.error("Error al obtener responsables para la recomendación: {}", e.getMessage());
      return null;
    }
  }

  @Transactional
  public Responsable actualizarResponsable(ActualizarResponsableDTO dto) {
    try {
      Responsable responsableOld = responsableRepositorio.findById(dto.getArers_codigo()).get();
      Responsable responsable = responsableMapper.convertirActualizarDTOModel(dto, responsableOld);
      Responsable respuesta = responsableRepositorio.save(responsable);
      
      try {
        bitacoraServicio.registrarBitacora(respuesta, dto.getAuditoria(), "ACTUALIZAR");
      } catch (Exception e) {
        logger.error("Error al registrar bitácora para actualización de responsable: {}", e.getMessage());
        // No propagar el error de bitácora para evitar rollback de la transacción principal
      }
      
      return respuesta;
    } catch (Exception e) {
      logger.error("Error al actualizar responsable: {}", e.getMessage());
      return null;
    }
  }

  @Transactional
  public boolean eliminarFisico(EliminarDTO eliminarDTO, Long id) {
    try {
      responsableRepositorio.deleteById(id);
      
      try {
        bitacoraServicio.registrarBitacora(id, eliminarDTO.getAuditoria(), "ELIMINAR");
      } catch (Exception e) {
        logger.error("Error al registrar bitácora para eliminación física de responsable: {}", e.getMessage());
        // No propagar el error de bitácora para evitar rollback de la transacción principal
      }
      
      return true;
    } catch (Exception e) {
      logger.error("Error al eliminar responsable: {}", e.getMessage());
      return false;
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(EliminarDTO dto, Long id) {
    try {
      if (!responsableRepositorio.existsById(id)) {
        return EstadoRespuestas.noEncontrado("Responsable no encontrado");
      }
      Responsable responsable = responsableRepositorio.findById(id).get();
      responsable.setArers_estado('0');
      Responsable respuesta = responsableRepositorio.save(responsable);
      
      try {
        bitacoraServicio.registrarBitacora(respuesta, dto.getAuditoria(), "ELIMINACIÓN LOGICA");
      } catch (Exception e) {
        logger.error("Error al registrar bitácora para eliminación lógica de responsable: {}", e.getMessage());
        // No propagar el error de bitácora para evitar rollback de la transacción principal
      }
      
      return EstadoRespuestas.respuestaEliminacion("Responsable eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar responsable: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar responsable : " + e);
    }
  }

  @Transactional
  public boolean reemplazar(EliminarDTO dto, Long id, NuevoResponsableDTO nuevoResponsableDTO) {
    try {
      if (!responsableRepositorio.existsById(id)) {
        return false;
      }
      Responsable responsable = responsableRepositorio.findById(id).get();
      responsable.setArers_estado('0');
      responsable.setArers_fecha_reempl(new Date());
      responsable
          .setArers_reempl_por(nuevoResponsableDTO.getArers_apellido() + " " + nuevoResponsableDTO.getArers_nombre());
      Responsable respuesta = responsableRepositorio.save(responsable);
      
      try {
        bitacoraServicio.registrarBitacora(respuesta, dto.getAuditoria(), "ELIMINACIÓN LOGICA");
      } catch (Exception e) {
        logger.error("Error al registrar bitácora para reemplazo de responsable: {}", e.getMessage());
        // No propagar el error de bitácora para evitar rollback de la transacción principal
      }
      
      return true;
    } catch (Exception e) {
      logger.error("Error al reemplazar responsable: {}", e.getMessage());
      return false;
    }
  }

  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerDepartamentos() {
    try {
      List<String> departamentos = responsableRepositorio.getDepartamentos();
      List<String> unicos = departamentos.stream().distinct().collect(Collectors.toList());
      return EstadoRespuestas.respuestaObtencion("Departamentos obtenidos exitosamente", unicos);
    } catch (Exception e) {
      logger.error("Error al obtener departamentos: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener departamentos");
    }
  }

  /**
   * Obtiene la descripción del estado del último verificable subido por el responsable
   * 
   * @param arere_codigo Código de la recomendación
   * @param arers_codigo Código del responsable
   * @return Descripción del estado del último verificable o "NO JUSTIFICA" por defecto
   */
  private String obtenerDescripcionUltimoVerificable(Long arere_codigo, Long arers_codigo) {
    try {
      // Obtener el detalle de recomendación para este responsable
      DetalleRecomendacion detalleRecomendacion = detalleRepositorio.findByRecomendacionResponsable(arere_codigo, arers_codigo);
      
      if (detalleRecomendacion == null) {
        return "NO JUSTIFICA";
      }

      // Buscar la descripción del estado del último verificable por detalle de recomendación
      return verificableRepositorio.findUltimaDescripcionEstadoPorDetalleRecomendacion(detalleRecomendacion.getAredr_codigo())
          .orElse("VERIFICABLE NO CREADO");
      
    } catch (Exception e) {
      logger.error("Error al obtener descripción del último verificable: {}", e.getMessage());
      return "NO JUSTIFICA";
    }
  }

  private String obtenerObservacionUltimoVerificable(Long arere_codigo, Long arers_codigo) {
    try {
      // Obtener el detalle de recomendación para este responsable
      DetalleRecomendacion detalleRecomendacion = detalleRepositorio.findByRecomendacionResponsable(arere_codigo, arers_codigo);
      
      if (detalleRecomendacion == null) {
        return "no existe observacion";
      }

      // Buscar el último verificable por detalle de recomendación
      Optional<Verificable> ultimoVerificable = verificableRepositorio.findUltimoVerificablePorDetalleRecomendacion(detalleRecomendacion.getAredr_codigo());
      
      if (ultimoVerificable.isPresent()) {
        String observacion = ultimoVerificable.get().getAreve_observacion();
        // Si la observación es null o está vacía, retornar "no existe observacion"
        if (observacion == null || observacion.trim().isEmpty()) {
          return "no existe observacion";
        }
        return observacion;
      }
      
      // Si no hay verificable, retornar "no existe observacion"
      return "no existe observacion";
      
    } catch (Exception e) {
      logger.error("Error al obtener observación del último verificable: {}", e.getMessage());
      return "no existe observacion";
    }
  }

}
