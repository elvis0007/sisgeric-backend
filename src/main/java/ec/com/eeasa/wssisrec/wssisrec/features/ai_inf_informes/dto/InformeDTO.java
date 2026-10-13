package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.dtos.OrganismosControlDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.dto.TipoInfDTO;
import lombok.Data;

@Data
public class InformeDTO {
  private Long ainin_codigo;
  private Float progreso;
  private String ainin_titulo;
  private String ainin_ordenTrabajo;
  private byte[] ainin_infAdjunto;
  private String ainin_fechaEmision;
  private String ainin_fechaAprobacion;
  private String ainin_fechaMemo;
  private String ainin_memoAprobacion;
  private String ainin_memorando;
  private Long ainin_otnum;
  private Long ainin_otanio;
  private String urlArchivo;
  private String ainin_usuario;
  private Integer totalRecomendaciones;
  private Character ainin_notificado;
  private TipoInfDTO tipoInf;
  private OrganismosControlDTO organismoControl;
  private String advertencia;
}
