package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.mapper;

import java.util.Date;

import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.dtos.ActualizarOrganismoControlDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.dtos.NuevoOrganismoControlDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.dtos.OrganismosControlDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.models.OrganismoControl;
import ec.com.eeasa.wssisrec.wssisrec.shared.utils.DateMapper;

@Component
public class OrganismoControlMapper {

  /**
   * Convierte un DTO de nuevo organismo de control a una entidad
   * OrganismoControl.
   * 
   * @param dto DTO que contiene los datos del nuevo organismo de control.
   * @return Instancia de OrganismoControl con los valores del DTO.
   */
  public OrganismoControl convertirDTOModel(NuevoOrganismoControlDTO dto) {
    OrganismoControl organismoControl = new OrganismoControl();
    organismoControl.setAinoc_codigo(null);
    organismoControl.setAinoc_descripcion(dto.getAinoc_descripcion());
    organismoControl.setAinoc_estado('1');
    organismoControl.setAinoc_fecha(new Date());
    organismoControl.setAinoc_usuario(dto.getAuditoria().getUsuario());
    return organismoControl;
  }

  /**
   * Convierte un modelo OrganismoControl a su correspondiente DTO.
   * 
   * @param organismoControl Entidad OrganismoControl a convertir.
   * @return DTO que representa el organismo de control.
   */
  public OrganismosControlDTO convertirModelDTO(OrganismoControl organismoControl) {
    OrganismosControlDTO dto = new OrganismosControlDTO();
    dto.setAinoc_codigo(organismoControl.getAinoc_codigo());
    dto.setAinoc_descripcion(organismoControl.getAinoc_descripcion());
    dto.setAinoc_fecha(DateMapper.convertirFechaFormato(organismoControl.getAinoc_fecha()));
    return dto;
  }

  /**
   * Convierte un DTO de actualización a una entidad OrganismoControl,
   * manteniendo ciertos valores de la entidad original.
   * 
   * @param dto          DTO con los datos a actualizar.
   * @param organismoOld Entidad OrganismoControl existente que se actualizará.
   * @return Nueva instancia de OrganismoControl con los valores actualizados.
   */
  public OrganismoControl convertirActualizarDTOModel(ActualizarOrganismoControlDTO dto,
      OrganismoControl organismoOld) {
    OrganismoControl actualizarOrganismo = new OrganismoControl();
    actualizarOrganismo.setAinoc_codigo(organismoOld.getAinoc_codigo());
    actualizarOrganismo.setAinoc_descripcion(dto.getAinoc_descripcion());
    actualizarOrganismo.setAinoc_fecha(new Date());
    actualizarOrganismo.setAinoc_estado(organismoOld.getAinoc_estado());
    actualizarOrganismo.setAinoc_usuario(dto.getAuditoria().getUsuario());
    return actualizarOrganismo;
  }
}
