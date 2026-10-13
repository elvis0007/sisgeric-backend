package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.InformeServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models.Informe;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos.ActualizarConclusionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos.ConclusionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos.HallazgoConclusionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.dtos.NuevoConclusionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.mapper.ConclusionMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.models.Conclusion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.DetalleServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.dtos.NuevoDetalleDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.HallazgoServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.models.Hallazgo;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.RecomendacionRepositorio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.RecomendacionServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos.NuevaRecomendacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.ResponsableServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.NuevoResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models.Responsable;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.BitacoraServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.NotificacionesServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.dtos.NuevaNotificacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.enums.TipoNotificacionEnum;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_historial.HistorialRecomendacionServicio;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.RolUsuarioServicio;
import ec.com.eeasa.wssisrec.wssisrec.services.EmailService;
import ec.com.eeasa.wssisrec.wssisrec.shared.dtos.EliminarDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.EstadoRespuestas;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.UtilsFunctions;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConclusionServicio {
  private static final Logger logger = LoggerFactory.getLogger(ConclusionServicio.class);
  @Autowired
  private BitacoraServicio bitacoraServicio;

  @Autowired
  private ConclusionRepositorio conclusionRepositorio;

  @Autowired
  private ConclusionMapper conclusionMapper;

  @Autowired
  private HallazgoServicio hallazgoServicio;

  @Autowired
  private RecomendacionServicio recomendacionServicio;
  
  @Autowired
  private RecomendacionRepositorio recomendacionRepositorio;

  @Autowired
  private ResponsableServicio responsableServicio;

  @Autowired
  private DetalleServicio detalleServicio;

  @Autowired
  private InformeServicio informeServicio;

  @Autowired
  private RolUsuarioServicio rolUsuarioServicio;

  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  @Autowired
  private NotificacionesServicio notificacionesServicio;

  @Autowired
  @Lazy
  private EmailService emailService;

  @Autowired
  private HistorialRecomendacionServicio historialServicio;

  /**
   * Obtiene las conclusiones asociadas a un hallazgo específico por su código.
   * 
   * @param areha_codigo el código del hallazgo
   * @return una respuesta con las conclusiones obtenidas o un mensaje de error
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorHallazgo(Long areha_codigo) {
    try {
      // Validar si existe el codigo hallazgo
      Hallazgo hallazgo = hallazgoServicio.obtenerHallazgo(areha_codigo);

      // Obtener el hallazgo
      List<Conclusion> conclusiones = conclusionRepositorio.findByHallazgo(hallazgo);

      // Convertir el model a dto
      List<ConclusionDTO> conclusionDTOs = conclusiones.stream()
          .filter((conclusion) -> conclusion.getAreco_estado().equals('1'))
          .map(conclusion -> {
            ConclusionDTO dto = conclusionMapper.convertirModelDTO(conclusion);
            Long totalRec = recomendacionRepositorio.countActivasByConclusion(conclusion.getAreco_codigo());
            dto.setTotalRecomendaciones(totalRec != null ? totalRec : 0L);
            return dto;
          })
          .collect(Collectors.toList());

      Informe informe = informeServicio.obtenerInformeByHallazgo(areha_codigo);
      HallazgoConclusionDTO hallazgoConclusionDTO = new HallazgoConclusionDTO(hallazgo.getAreha_codigo(),
          hallazgo.getAreha_titulo(),
          informe.getAinin_orden_trabajo(),
          informe.getAinin_titulo(),
          conclusionDTOs);

      return EstadoRespuestas.respuestaObtencion("Conclusiones obtenidos  exitosamente", hallazgoConclusionDTO);
    } catch (Exception e) {
      logger.error("Error al obtener las conclusiones: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener las conclusiones");
    }
  }

  /**
   * Obtiene las conclusiones de un hallazgo filtradas por el número de recomendación.
   * 
   * @param areha_codigo el código del hallazgo
   * @param termino término de búsqueda por número de recomendación
   * @return una respuesta con las conclusiones obtenidas o un mensaje de error
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorHallazgoYRecomendacion(Long areha_codigo, String termino) {
    try {
      List<Conclusion> conclusiones;

      if (termino != null && termino.matches("\\d+")) {
        conclusiones = conclusionRepositorio.findByHallazgoAndRecomendacionNumero(areha_codigo, Integer.parseInt(termino));
      } else {
        String patron = "%" + (termino != null ? termino : "") + "%";
        conclusiones = conclusionRepositorio.findByHallazgoAndRecomendacionDescripcion(areha_codigo, patron);
      }

      List<ConclusionDTO> conclusionDTOs = conclusiones.stream()
          .filter((conclusion) -> conclusion.getAreco_estado().equals('1'))
          .map(conclusion -> {
            ConclusionDTO dto = conclusionMapper.convertirModelDTO(conclusion);
            Long totalRec = recomendacionRepositorio.countActivasByConclusion(conclusion.getAreco_codigo());
            dto.setTotalRecomendaciones(totalRec != null ? totalRec : 0L);
            return dto;
          })
          .collect(Collectors.toList());

      Hallazgo hallazgo = hallazgoServicio.obtenerHallazgo(areha_codigo);
      Informe informe = informeServicio.obtenerInformeByHallazgo(areha_codigo);
      HallazgoConclusionDTO hallazgoConclusionDTO = new HallazgoConclusionDTO(hallazgo.getAreha_codigo(),
          hallazgo.getAreha_titulo(),
          informe.getAinin_orden_trabajo(),
          informe.getAinin_titulo(),
          conclusionDTOs);

      return EstadoRespuestas.respuestaObtencion("Conclusiones obtenidos exitosamente", hallazgoConclusionDTO);
    } catch (Exception e) {
      logger.error("Error al obtener las conclusiones por recomendación: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener las conclusiones por recomendación");
    }
  }

  /**
   * Obtiene una conclusión por su identificador único.
   * 
   * @param id el identificador de la conclusión
   * @return una respuesta con la conclusión obtenida o un mensaje de error
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> obtenerPorId(Long id) {
    try {
      // Validar si existe el id de la conclusion

      // Obtener conclusion
      Conclusion conclusion = conclusionRepositorio.findById(id).get();
      // Convertir model a DTO
      ConclusionDTO conclusionDTO = conclusionMapper.convertirModelDTO(conclusion);
      Long totalRec = recomendacionRepositorio.countActivasByConclusion(conclusion.getAreco_codigo());
      conclusionDTO.setTotalRecomendaciones(totalRec != null ? totalRec : 0L);
      return EstadoRespuestas.respuestaObtencion("Conclusión obtenido exitosamente", conclusionDTO);
    } catch (Exception e) {
      logger.error("Error al obtener la conclusión: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al obtener la conclusión");
    }
  }

  /**
   * Actualiza una conclusión existente con los datos proporcionados en el DTO.
   * 
   * @param dto          el DTO con los nuevos datos de la conclusión
   * @param areco_codigo el código de la conclusión a actualizar
   * @return una respuesta indicando si la actualización fue exitosa o fallida
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> actualizarConclusion(ActualizarConclusionDTO dto,
      Long areco_codigo) {
    try {
      Conclusion conclusionOld = conclusionRepositorio.findById(areco_codigo).get();
      Conclusion conclusion = conclusionMapper.convertirActualizarDTOModel(dto, conclusionOld);
      Conclusion respuesta = conclusionRepositorio.save(conclusion);
      bitacoraServicio.registrarBitacora(respuesta, dto.getAuditoria(), "ACTUALIZAR");
      return EstadoRespuestas.respuestaCreacion("Conclusion actualizada exitosamente");
    } catch (Exception e) {
      logger.error("Error al actualizar conclusión: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al actualizar conclusión");
    }
  }

  /**
   * Registra una nueva conclusión junto con las recomendaciones y responsables
   * asociadas.
   * 
   * @param nuevoConclusionDTO el DTO con los datos de la nueva conclusión
   * @return una respuesta indicando si el registro fue exitoso o fallido
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> registrarConclusion(NuevoConclusionDTO nuevoConclusionDTO) {
    try {
      Hallazgo hallazgo = hallazgoServicio.obtenerHallazgo(nuevoConclusionDTO.getAreha_codigo());
      Informe informeHallazgo = (hallazgo != null) ? hallazgo.getInforme() : null;

      // Protección contra peticiones duplicadas / doble clic
      if (hallazgo != null) {
        List<Conclusion> conclusionesExistentes = conclusionRepositorio.findByHallazgo(hallazgo);
        if (conclusionesExistentes != null) {
          String concTexto = nuevoConclusionDTO.getAreco_conclusion() != null ? nuevoConclusionDTO.getAreco_conclusion().trim() : "";
          long ahora = System.currentTimeMillis();
          boolean yaExisteDuplicadaReciente = conclusionesExistentes.stream()
              .anyMatch(c -> c.getAreco_estado() != null && (c.getAreco_estado() == '1' || "1".equals(String.valueOf(c.getAreco_estado())))
                  && c.getAreco_conclusion() != null
                  && c.getAreco_conclusion().trim().equalsIgnoreCase(concTexto)
                  && c.getAreco_fecha() != null
                  && (ahora - c.getAreco_fecha().getTime()) < 30000);
          if (yaExisteDuplicadaReciente) {
            logger.warn("[REGISTRO CONCLUSIÓN] Conclusión duplicada detectada recientemente para hallazgo {}. Omitiendo creación redundante.",
                nuevoConclusionDTO.getAreha_codigo());
            return EstadoRespuestas.respuestaCreacion("Conclusiones y recomendaciones creados exitosamente");
          }
        }
      }

      Conclusion conclusion = conclusionMapper.convertirCrearDTOModel(nuevoConclusionDTO, hallazgo);
      Conclusion respuesta = conclusionRepositorio.save(conclusion);

      // Procesar recomendaciones y responsables
      if (nuevoConclusionDTO.getRecomendaciones() != null) {
        for (NuevaRecomendacionDTO recomendacionDTO : nuevoConclusionDTO.getRecomendaciones()) {
          try {
            recomendacionDTO.setAuditoria(nuevoConclusionDTO.getAuditoria());
            Recomendacion recomendacion = recomendacionServicio.registrarRecomendacion(recomendacionDTO,
                conclusion.getAreco_codigo());

            if (recomendacion != null && recomendacionDTO.getResponsables() != null) {
              Informe informeRec = (informeHallazgo != null) ? informeHallazgo
                  : informeServicio.obtenerInformeByRecomendacion(recomendacion.getArere_codigo());
              String memoInforme = recomendacionRepositorio.obtenerMemoInforme(recomendacion.getArere_codigo());
              if ((memoInforme == null || memoInforme.isEmpty()) && informeRec != null) {
                memoInforme = informeRec.getAinin_memorando();
              }

              for (NuevoResponsableDTO responsableDTO : recomendacionDTO.getResponsables()) {
                try {
                  // Asegurar que el responsable tenga el rol de RESPONSABLE (código 4)
                  rolUsuarioServicio.asegurarRolResponsable(responsableDTO, nuevoConclusionDTO.getAuditoria());

                  responsableDTO.setAuditoria(nuevoConclusionDTO.getAuditoria());
                  Responsable responsable = responsableServicio.registrarResponsable(responsableDTO);
                  if (responsable != null) {
                    detalleServicio.registar(new NuevoDetalleDTO(nuevoConclusionDTO.getAuditoria()), recomendacion, responsable);

                    // 1. Registrar en el historial de recomendaciones
                    try {
                      String respNueName = responsable.getArers_nombre() + " " + responsable.getArers_apellido();
                      historialServicio.registrarEvento(
                          recomendacion.getArere_codigo(),
                          "ASIGNACION_RESPONSABLE",
                          "Se asignó como responsable a: " + respNueName + " (" + (responsable.getArers_cargo() != null ? responsable.getArers_cargo() : "Sin cargo") + ").",
                          null,
                          respNueName,
                          null, null, null, null,
                          nuevoConclusionDTO.getAuditoria().getUsuario()
                      );
                    } catch (Exception e) {
                      logger.error("Error al registrar historial para responsable: {}", e.getMessage());
                    }

                    // 2. Notificación en el sistema (WebSocket + BD)
                    try {
                      this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + responsable.getArers_usuario_per(),
                          "Tiene una nueva notificacion.");
                      Integer anioRecAsig = extraerAnio(recomendacion.getArere_fecha_inicio_control());
                      Long aininCodigo = (informeRec != null) ? informeRec.getAinin_codigo() : null;
                      this.notificacionesServicio.registrarNotificacion(new NuevaNotificacionDTO(
                          "Nueva recomendación",
                          "Se le asignó como responsable de cumplimiento de la recomendacion del informe con memorando "
                              + (memoInforme != null ? memoInforme : "") + " Num. Recomendacion: " + recomendacion.getArere_num_rec(),
                          TipoNotificacionEnum.RECOMENDACION_ASIGNADA,
                          TipoNotificacionEnum.RECOMENDACION_ASIGNADA,
                          anioRecAsig,
                          aininCodigo,
                          recomendacion.getArere_codigo(),
                          responsable.getArers_usuario_per()));
                      logger.info("[REGISTRO CONCLUSIÓN] Notificación en sistema enviada a: {}", responsable.getArers_usuario_per());

                      // Notificación en sistema para registro de Plan de Acción (plazo máximo 48h)
                      try {
                        String descRec = recomendacion.getArere_num_rec() != null ? String.valueOf(recomendacion.getArere_num_rec()) : "";
                        String memoFinal = (memoInforme != null && !memoInforme.trim().isEmpty())
                            ? memoInforme.trim()
                            : (informeRec != null && informeRec.getAinin_memorando() != null && !informeRec.getAinin_memorando().trim().isEmpty()
                                ? informeRec.getAinin_memorando().trim()
                                : (informeRec != null && informeRec.getAinin_memo_aprobacion() != null ? informeRec.getAinin_memo_aprobacion().trim() : ""));
                        String mensajePlan = "Dispone de un plazo de hasta 48 horas posteriores a esta notificación para formular y registrar su Plan de Acción en el sistema SISREC para la recomendación "
                            + descRec + (memoFinal != null && !memoFinal.isEmpty() ? (" del informe con memorando " + memoFinal) : "") + ".";
                        NuevaNotificacionDTO notifPlan = new NuevaNotificacionDTO(
                            "Registro de Plan de Acción",
                            mensajePlan,
                            "/inicio/plan-accion-responsables",
                            TipoNotificacionEnum.PLAN_ACCION_REQUERIDO,
                            anioRecAsig,
                            aininCodigo,
                            recomendacion.getArere_codigo(),
                            responsable.getArers_usuario_per()
                        );
                        this.notificacionesServicio.registrarNotificacion(notifPlan);
                        this.simpMessagingTemplate.convertAndSend("/topic/notificacion/" + responsable.getArers_usuario_per(),
                            "Tiene una nueva notificación: Registro de Plan de Acción.");
                      } catch (Exception exPlan) {
                        logger.error("Error al registrar notificación de Plan de Acción en conclusión: {}", exPlan.getMessage());
                      }
                    } catch (Exception e) {
                      logger.error("Error al enviar notificación en sistema para responsable: {}", e.getMessage());
                    }

                    // 3. Envío de correo de asignación
                    try {
                      String memoAuditoria = (memoInforme != null && !memoInforme.isEmpty()) 
                          ? memoInforme 
                          : (informeRec != null && informeRec.getAinin_memorando() != null ? informeRec.getAinin_memorando() : "");
                    String memoPresidencia = (informeRec != null && informeRec.getAinin_memo_aprobacion() != null) 
                        ? informeRec.getAinin_memo_aprobacion() 
                        : "";

                    Map<String, Object> variables = new HashMap<>();
                    variables.put("fecha", UtilsFunctions.getDateHour());
                    variables.put("nombres", responsable.getArers_nombre());
                    variables.put("apellidos", responsable.getArers_apellido());
                    variables.put("titulo_informe", informeRec != null ? informeRec.getAinin_titulo() : "");
                    variables.put("tituloInforme", informeRec != null ? informeRec.getAinin_titulo() : "");
                    variables.put("memorando", memoAuditoria);
                    variables.put("memoaprobacion", memoPresidencia);
                    variables.put("memoAprobacion", memoPresidencia);
                    variables.put("memorando_aprobacion", memoPresidencia);
                    variables.put("recomendacion", recomendacion.getArere_descripcion());
                    variables.put("fecha_max_cumplimiento",
                        recomendacion.getArere_fecha_maxima() != null
                            ? DateMapper.convertirFechaFormato(recomendacion.getArere_fecha_maxima())
                            : "");
                    variables.put("fechaMaxCumplimiento",
                        recomendacion.getArere_fecha_maxima() != null
                            ? DateMapper.convertirFechaFormato(recomendacion.getArere_fecha_maxima())
                            : "");

                      String correoResponsable = responsableDTO.getArers_correo();
                      if (correoResponsable != null && !correoResponsable.isEmpty()) {
                        emailService.sendCorreoAsignacion(correoResponsable, variables);
                        logger.info("[REGISTRO CONCLUSIÓN] Correo de asignación enviado a: {}", correoResponsable);
                      } else {
                        logger.warn("[REGISTRO CONCLUSIÓN] No se pudo enviar correo: el responsable no tiene correo registrado.");
                      }
                    } catch (Exception e) {
                      logger.error("Error al enviar correo de asignación: {}", e.getMessage());
                    }
                  }
                } catch (Exception e) {
                  logger.error("Error al registrar responsable: {}", e.getMessage());
                }
              }
            }
          } catch (Exception e) {
            logger.error("Error al registrar recomendación: {}", e.getMessage());
          }
        }
      }

      bitacoraServicio.registrarBitacora(respuesta, nuevoConclusionDTO.getAuditoria(), "CREAR");
      return EstadoRespuestas.respuestaCreacion("Conclusiones y recomendaciones creados exitosamente");
    } catch (Exception e) {
      logger.error("Error al registrar conclusión: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al realizar el registro: " + e.getMessage());
    }
  }

  private Integer extraerAnio(Date fecha) {
    if (fecha == null) {
      return null;
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(fecha);
    return calendar.get(Calendar.YEAR);
  }

  /**
   * Elimina una conclusión de manera lógica (cambia su estado).
   * 
   * @param eliminarDTO  los datos de auditoría para registrar la eliminación
   * @param areco_codigo el código de la conclusión a eliminar
   * @return una respuesta indicando si la eliminación fue exitosa o fallida
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarLogico(EliminarDTO eliminarDTO, Long areco_codigo) {
    try {
      if (!conclusionRepositorio.existsById(areco_codigo)) {
        return EstadoRespuestas.noEncontrado("Conclusión no encontrado");
      }

      Conclusion conclusion = conclusionRepositorio.findById(areco_codigo).get();
      
      // Verificar si existen recomendaciones activas para esta conclusión
      List<Recomendacion> recomendaciones = recomendacionRepositorio.findByConclusion(areco_codigo);
      boolean tieneRecomendacionesActivas = recomendaciones.stream()
          .anyMatch(recomendacion -> recomendacion.getArere_estado() == '1');
      
      if (tieneRecomendacionesActivas) {
        return EstadoRespuestas.respuestaError("No se puede eliminar la conclusión porque tiene recomendaciones registradas");
      }
      
      conclusion.setAreco_estado('0');
      Conclusion respuesta = conclusionRepositorio.save(conclusion);
      bitacoraServicio.registrarBitacora(respuesta, eliminarDTO.getAuditoria(), "ELIMINAR LOGICO");
      return EstadoRespuestas.respuestaEliminacion("Conclusión eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar la conclusión: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar la conclusion");
    }
  }

  /**
   * Elimina físicamente una conclusión del sistema.
   * 
   * @param eliminarDTO  los datos de auditoría para registrar la eliminación
   * @param areco_codigo el código de la conclusión a eliminar
   * @return una respuesta indicando si la eliminación fue exitosa o fallida
   */
  @Transactional
  public ResponseEntity<LinkedHashMap<String, Object>> eliminarFisico(EliminarDTO eliminarDTO, Long areco_codigo) {
    try {
      if (!conclusionRepositorio.existsById(areco_codigo)) {
        return EstadoRespuestas.noEncontrado("Conclusión no encontrado");
      }

      List<Recomendacion> recomendaciones = recomendacionRepositorio.findByConclusion(areco_codigo);
      boolean tieneRecomendacionesActivas = recomendaciones.stream()
          .anyMatch(recomendacion -> recomendacion.getArere_estado() == '1');
      
      if (tieneRecomendacionesActivas) {
        return EstadoRespuestas.respuestaError("No se puede eliminar la conclusión porque tiene recomendaciones registradas");
      }

      Conclusion conclusion = conclusionRepositorio.findById(areco_codigo).get();
      conclusionRepositorio.deleteById(areco_codigo);
      bitacoraServicio.registrarBitacora(conclusion, eliminarDTO.getAuditoria(), "ELIMINAR FISICO");
      return EstadoRespuestas.respuestaEliminacion("Conclusión eliminado exitosamente");
    } catch (Exception e) {
      logger.error("Error al eliminar la conclusión: {}", e.getMessage());
      return EstadoRespuestas.respuestaError("Error al eliminar la conclusión");
    }
  }

  /**
   * Obtiene la conclusión asociada a una recomendación específica.
   * 
   * @param arere_codigo el código de la recomendación
   * @return la conclusión asociada a la recomendación
   */
  @Transactional
  public Conclusion obtenerConclusionByRecomendacion(Long arere_codigo) {
    try {
      Conclusion conclusion = conclusionRepositorio.findByRecomendacion(arere_codigo);
      return conclusion;
    } catch (Exception e) {
      logger.error("Error al obtener la conclusión por recomendación: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Obtiene una conclusión por su código.
   * 
   * @param areco_codigo el código de la conclusión
   * @return la conclusión obtenida o null en caso de error
   */
  @Transactional
  public Conclusion obtenerConclusion(Long areco_codigo) {
    try {
      Conclusion conclusion = conclusionRepositorio.findById(areco_codigo).get();
      return conclusion;
    } catch (Exception e) {
      logger.error("Error al obtener la conclusión: {}", e.getMessage());
      return null;
    }
  }

}
