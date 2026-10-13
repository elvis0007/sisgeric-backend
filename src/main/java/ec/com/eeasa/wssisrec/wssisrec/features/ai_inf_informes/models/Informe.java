package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedStoredProcedureQueries;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.SequenceGenerator;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.models.OrganismoControl;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.models.TipoInforme;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "AI_INF_INFORMES")
@AllArgsConstructor
@NoArgsConstructor
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(name = "SP_INFORMES_FILTROS_D", procedureName = "SP_INFORMES_FILTROS_D", parameters = {
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "AINOC_ORGANISMO_CONTROL", type = Long.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "AINTI_TIPO_INF", type = Long.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "FECHA_INICIO", type = String.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "FECHA_FIN", type = String.class),
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "DEPARTAMENTO", type = String.class),
        @StoredProcedureParameter(mode = ParameterMode.REF_CURSOR, name = "RESULTADO", type = void.class)
    })
})
public class Informe {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inf_gen")
  @SequenceGenerator(name = "inf_gen", sequenceName = "AI_INF_INFORMES_SEQ", allocationSize = 1)
  @Column(name = "AININ_CODIGO")
  private Long ainin_codigo;

  @Column(name = "AININ_TITULO")
  private String ainin_titulo;

  @Column(name = "AININ_INF_ADJUNTO")
  private String ainin_inf_adjunto;

  @Column(name = "AININ_MEMO_APROBACION")
  private String ainin_memo_aprobacion;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AININ_FECHA_APROBACION")
  private Date ainin_fecha_aprobacion;

  @Column(name = "AININ_MEMORANDO")
  private String ainin_memorando;

  @Column(name = "AININ_ORDEN_TRABAJO")
  private String ainin_orden_trabajo;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AININ_FECHA_MEMO")
  private Date ainin_fecha_memo;

  @Column(name = "AININ_PROGRESO")
  private Integer ainin_progreso;

  @Column(name = "AININ_ESTADO_SEG")
  private Character ainin_estado_seg;

  @Column(name = "AININ_OTNUM")
  private Long ainin_otnum;

  @Column(name = "AININ_OTANIO")
  private Long ainin_otanio;

  @Column(name = "AININ_ESTADO")
  private Character ainin_estado;

  @Column(name = "AININ_USUARIO")
  private String ainin_usuario;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AININ_FECHA")
  private Date ainin_fecha;

  @Column(name = "AININ_NOTIFICADO")
  private Character ainin_notificado; // '0' = no notificado, '1' = notificado

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "AINTI_CODIGO", referencedColumnName = "AINTI_CODIGO")
  private TipoInforme tipoInforme;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "AINOC_CODIGO", referencedColumnName = "AINOC_CODIGO")
  private OrganismoControl organismoControl;

  @Transient
  private double porcentaje;

}
