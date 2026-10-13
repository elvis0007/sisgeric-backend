package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.dtos.OrganismosControlDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.dto.TipoInfDTO;
import lombok.Data;

@Data
public class InformeResponsableDTO {
  private Long ainin_codigo;
  private String ainin_titulo;
  private Float progreso;
  private String auditor;
  private String ainin_memorando;
  private String ainin_fechaEmision;
  private TipoInfDTO tipoInforme;
  private OrganismosControlDTO organismoControl;
}
