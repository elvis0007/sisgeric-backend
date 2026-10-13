package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.mapper;

import java.util.Date;

import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.ActualizarResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.NuevoResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.ResponsableActDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.ResponsableDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.dtos.ResponsableViewDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models.Responsable;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper;

@Component
public class ResponsableMapper {

  public ResponsableDTO convertirDTOModel(Responsable responsable) {
    ResponsableDTO responsableDTO = new ResponsableDTO();
    responsableDTO.setArers_codigo(responsable.getArers_codigo());
    // responsableDTO.setNombre_completo(responsable.getArers_nombre() + " " +
    // responsable.getArers_apellido());
    responsableDTO.setArers_usuario(responsable.getArers_usuario_per());
    responsableDTO.setArers_correo(responsable.getArers_correo());
    responsableDTO.setNombre_completo(responsable.getArers_apellido() + " " + responsable.getArers_nombre());
    responsableDTO.setArers_cargo(responsable.getArers_cargo());
    responsableDTO.setArers_codigo_per(responsable.getArers_codigo_per());
    responsableDTO.setArers_correo(responsable.getArers_correo());
    responsableDTO.setArers_departamento(responsable.getArers_departamento());
    responsableDTO.setArers_departamento_abrev(responsable.getArers_departamento_abrev());
    responsableDTO.setArers_num_rol(responsable.getArers_num_rol());
    responsableDTO.setArers_seccion(responsable.getArers_seccion());
    responsableDTO.setArers_fecha(DateMapper.convertirFechaFormato(responsable.getArers_fecha()));
    if (responsable.getArers_fecha_reempl() != null) {
      responsableDTO.setArers_fecha_reempl(DateMapper.convertirFechaFormato(responsable.getArers_fecha_reempl()));
    }

    if (responsable.getArers_reempl_por() != null) {
      responsableDTO.setArers_reemp_por(responsable.getArers_reempl_por());
    }

    responsableDTO.setArers_estado(responsable.getArers_estado());
    return responsableDTO;
  }

  public ResponsableViewDTO convertirViewDTOModel(Responsable responsable) {
    ResponsableViewDTO responsableDTO = new ResponsableViewDTO();
    responsableDTO.setArers_codigo(responsable.getArers_codigo());
    responsableDTO.setArers_usuario(responsable.getArers_usuario_per());
    responsableDTO.setArers_correo(responsable.getArers_correo());
    responsableDTO.setNombre_completo(responsable.getArers_apellido() + " " + responsable.getArers_nombre());
    responsableDTO.setArers_cargo(responsable.getArers_cargo());
    responsableDTO.setArers_codigo_per(responsable.getArers_codigo_per());
    responsableDTO.setArers_correo(responsable.getArers_correo());
    responsableDTO.setArers_departamento(responsable.getArers_departamento());
    responsableDTO.setArers_departamento_abrev(responsable.getArers_departamento_abrev());
    responsableDTO.setArers_num_rol(responsable.getArers_num_rol());
    responsableDTO.setArers_seccion(responsable.getArers_seccion());
    responsableDTO.setArers_fecha(DateMapper.convertirFechaFormato(responsable.getArers_fecha()));
    if (responsable.getArers_fecha_reempl() != null) {
      responsableDTO.setArers_fecha_reempl(DateMapper.convertirFechaFormato(responsable.getArers_fecha_reempl()));
    }

    if (responsable.getArers_reempl_por() != null) {
      responsableDTO.setArers_reemp_por(responsable.getArers_reempl_por());
    }

    responsableDTO.setArers_estado(responsable.getArers_estado());
    return responsableDTO;
  }

  public Responsable convertirCrearDTOModel(NuevoResponsableDTO dto) {

    Responsable responsable = new Responsable();

    responsable.setArers_codigo(null);

    responsable.setArers_codigo_per(dto.getArers_codigo_per());

    responsable.setArers_apellido(dto.getArers_apellido());

    responsable.setArers_nombre(dto.getArers_nombre());

    responsable.setArers_cargo(dto.getArers_cargo());

    responsable.setArers_correo(dto.getArers_correo());

    responsable.setArers_departamento(dto.getArers_departamento());

    responsable.setArers_departamento_abrev(dto.getArers_departamento_abrev());

    responsable.setArers_num_rol(dto.getArers_num_rol());

    responsable.setArers_seccion(dto.getArers_seccion());

    responsable.setArers_usuario_per(dto.getArers_usuario_per());

    responsable.setArers_fecha(new Date());

    responsable.setArers_usuario(dto.getAuditoria().getUsuario());

    responsable.setArers_estado('1');
    return responsable;
  }

  public ResponsableActDTO convertirActDTOModel(Responsable responsable) {
    ResponsableActDTO responsableActDTO = new ResponsableActDTO();
    responsableActDTO.setArers_codigo(responsable.getArers_codigo());
    responsableActDTO.setArers_codigo_per(responsable.getArers_codigo_per());
    return responsableActDTO;
  }

  public Responsable convertirActualizarDTOModel(ActualizarResponsableDTO dto, Responsable responsableOld) {
    Responsable responsable = new Responsable();
    responsable.setArers_codigo(responsableOld.getArers_codigo());
    responsable.setArers_codigo_per(dto.getArers_codigo_per());
    responsable.setArers_apellido(dto.getArers_apellido());
    responsable.setArers_nombre(dto.getArers_nombre());
    responsable.setArers_cargo(dto.getArers_cargo());
    responsable.setArers_correo(dto.getArers_correo());
    responsable.setArers_departamento(dto.getArers_departamento());
    responsable.setArers_departamento_abrev(dto.getArers_departamento_abrev());
    responsable.setArers_num_rol(dto.getArers_num_rol());
    responsable.setArers_seccion(dto.getArers_seccion());
    responsable.setArers_usuario_per(dto.getArers_usuario_per());
    responsable.setArers_fecha(new Date());
    responsable.setArers_usuario(dto.getAuditoria().getUsuario());
    responsable.setArers_estado(responsableOld.getArers_estado());
    return responsable;

  }

}
