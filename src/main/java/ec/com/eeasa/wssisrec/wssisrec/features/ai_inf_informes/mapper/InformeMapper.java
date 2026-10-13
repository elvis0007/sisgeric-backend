package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.mapper;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto.ActualizarInformeDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto.InformeDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto.NuevoInformeDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models.Informe;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.mapper.OrganismoControlMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.models.OrganismoControl;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.mapper.TipoInformeMapper;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.models.TipoInforme;
import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper;

@Component
public class InformeMapper {
  @Autowired
  OrganismoControlMapper organismoControlMapper;

  /**
   * Convierte un {@link NuevoInformeDTO} a un modelo {@link Informe}.
   * 
   * @param nuevoInformeDTO  DTO con los datos del informe nuevo.
   * @param auditoria        Información de auditoría.
   * @param organismoControl Entidad de Organismo de Control asociada.
   * @param rutaArchivo      Ruta del archivo adjunto.
   * @param tipoInforme      Tipo de informe asociado.
   * @return Una instancia de {@link Informe}.
   */
  public Informe convertirCrearDTOModel(NuevoInformeDTO nuevoInformeDTO, AuditoriaDTO auditoria,
      OrganismoControl organismoControl, String rutaArchivo, TipoInforme tipoInforme) {
    try {
      Informe informe = new Informe();

      informe.setAinin_codigo(null);
      informe.setAinin_titulo(nuevoInformeDTO.getAinin_titulo() != null ? nuevoInformeDTO.getAinin_titulo().toUpperCase().trim() : null);
      informe.setAinin_inf_adjunto(rutaArchivo);

      if (nuevoInformeDTO.getAinin_fechaAprobacion() == null || nuevoInformeDTO.getAinin_fechaAprobacion().trim().isEmpty()) {
        informe.setAinin_fecha_aprobacion(null);
      } else {
        informe.setAinin_fecha_aprobacion(DateMapper.convertirFechaFormato(nuevoInformeDTO.getAinin_fechaAprobacion()));
      }

      informe.setAinin_fecha_memo(DateMapper.convertirFechaFormato(nuevoInformeDTO.getAinin_fechaMemo()));
      informe.setAinin_memo_aprobacion(nuevoInformeDTO.getAinin_memoAprobacion() != null ? nuevoInformeDTO.getAinin_memoAprobacion().toUpperCase().trim() : null);
      informe.setAinin_memorando(nuevoInformeDTO.getAinin_memorando() != null ? nuevoInformeDTO.getAinin_memorando().toUpperCase().trim() : null);
      informe.setAinin_otnum(nuevoInformeDTO.getAinin_otnum());
      informe.setAinin_otanio(nuevoInformeDTO.getAinin_otanio());
      // Concatenar orden de trabajo: OT-AININ_OTNUM-AININ_OTANIO
      String ordenTrabajo = "OT-" + nuevoInformeDTO.getAinin_otnum() + "-" + nuevoInformeDTO.getAinin_otanio();
      informe.setAinin_orden_trabajo(ordenTrabajo);
      informe.setTipoInforme(tipoInforme);
      informe.setAinin_progreso(0);
      informe.setOrganismoControl(organismoControl);
      informe.setAinin_estado('1');
      informe.setAinin_fecha(new Date());
      informe.setAinin_usuario(auditoria.getUsuario());
      informe.setAinin_estado_seg('0');
      informe.setAinin_notificado('0');
      return informe;
    } catch (Exception e) {
      return new Informe();
    }
  }

  /**
   * Convierte un {@link ActualizarInformeDTO} en un modelo {@link Informe}
   * actualizado.
   * 
   * @param dto              DTO con los datos a actualizar.
   * @param auditoria        Información de auditoría.
   * @param id               Identificador del informe.
   * @param informe          Informe original.
   * @param organismoControl Organismo de Control asociado.
   * @param rutaArchivo      Ruta del archivo adjunto.
   * @param tipoInforme      Tipo de informe asociado.
   * @return Un {@link Informe} actualizado.
   * @throws Exception Si ocurre un error durante la conversión.
   */
  public Informe convertirActualizarDTOModel(ActualizarInformeDTO dto, AuditoriaDTO auditoria,
      Long id, Informe informe,
      OrganismoControl organismoControl, String rutaArchivo, TipoInforme tipoInforme) throws Exception {
    Informe actualizarInforme = new Informe();
    actualizarInforme.setAinin_inf_adjunto(rutaArchivo);
    actualizarInforme.setAinin_codigo(id);
    actualizarInforme.setAinin_titulo(dto.getAinin_titulo() != null ? dto.getAinin_titulo().toUpperCase().trim() : null);
    
    // Verificar si la fecha de aprobación está vacía y asignar null si es así
    if (dto.getAinin_fechaAprobacion() == null || dto.getAinin_fechaAprobacion().trim().isEmpty()) {
      actualizarInforme.setAinin_fecha_aprobacion(null);
    } else {
      actualizarInforme.setAinin_fecha_aprobacion(DateMapper.convertirFechaFormato(dto.getAinin_fechaAprobacion()));
    }
    
    actualizarInforme.setAinin_fecha_memo(DateMapper.convertirFechaFormato(dto.getAinin_fechaMemo()));
    actualizarInforme.setAinin_memo_aprobacion(dto.getAinin_memoAprobacion() != null ? dto.getAinin_memoAprobacion().toUpperCase().trim() : null);
    actualizarInforme.setAinin_otnum(dto.getAinin_otnum());
    actualizarInforme.setAinin_otanio(dto.getAinin_otanio());
    // Concatenar orden de trabajo: OT-AININ_OTNUM-AININ_OTANIO
    String ordenTrabajo = "OT-" + dto.getAinin_otnum() + "-" + dto.getAinin_otanio();
    actualizarInforme.setAinin_orden_trabajo(ordenTrabajo);
    // Calcular el progreso nuevo
    // actualizarInforme.setAinin_progreso(#calculo)
    actualizarInforme.setAinin_memorando(dto.getAinin_memorando() != null ? dto.getAinin_memorando().toUpperCase().trim() : null);
    actualizarInforme.setTipoInforme(tipoInforme);
    ;
    actualizarInforme.setOrganismoControl(organismoControl);
    actualizarInforme.setAinin_estado(informe.getAinin_estado());
    actualizarInforme.setAinin_estado_seg('0');
    actualizarInforme.setAinin_notificado(informe.getAinin_notificado());
    actualizarInforme.setAinin_usuario(auditoria.getUsuario());
    actualizarInforme.setAinin_fecha(new Date());
    return actualizarInforme;
  }

  /**
   * Convierte un modelo {@link Informe} en un DTO {@link InformeDTO}.
   * 
   * @param informe El modelo de informe a convertir.
   * @return Un {@link InformeDTO} con los datos convertidos.
   */
  public InformeDTO convertirModelDTO(Informe informe) {
    InformeDTO informeDTO = new InformeDTO();
    TipoInformeMapper tipoInformeMapper = new TipoInformeMapper();

    informeDTO.setAinin_codigo(informe.getAinin_codigo());
    informeDTO.setAinin_titulo(informe.getAinin_titulo());
    informeDTO.setAinin_ordenTrabajo(informe.getAinin_orden_trabajo());

    // Verificar si la fecha de aprobación es null y devolver una cadena vacía en ese caso
    informeDTO.setAinin_fechaAprobacion(
        informe.getAinin_fecha_aprobacion() != null ? DateMapper.convertirFechaFormato(informe.getAinin_fecha_aprobacion()) : "");

    informeDTO.setAinin_memorando(informe.getAinin_memorando());
    informeDTO.setUrlArchivo(informe.getAinin_inf_adjunto());
    informeDTO.setAinin_fechaMemo(
        informe.getAinin_fecha_memo() != null ? DateMapper.convertirFechaFormato(informe.getAinin_fecha_memo()) : "");
    informeDTO.setAinin_memoAprobacion(informe.getAinin_memo_aprobacion());
    informeDTO.setAinin_otnum(informe.getAinin_otnum());
    informeDTO.setAinin_otanio(informe.getAinin_otanio());
    informeDTO.setTipoInf(tipoInformeMapper.convertirModelDTO(informe.getTipoInforme()));
    informeDTO.setOrganismoControl(organismoControlMapper.convertirModelDTO(informe.getOrganismoControl()));
    informeDTO.setAinin_usuario(informe.getAinin_usuario());
    informeDTO.setAinin_fechaEmision(DateMapper.convertirFechaFormato(informe.getAinin_fecha()));
    informeDTO.setAinin_notificado(informe.getAinin_notificado());

    // informeDTO.setAinin_infAdjunto(informe.getAinin_inf_adjunto());
    return informeDTO;
  }
}
