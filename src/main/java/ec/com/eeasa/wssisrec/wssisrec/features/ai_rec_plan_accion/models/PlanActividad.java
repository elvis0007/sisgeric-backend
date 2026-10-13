package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AI_REC_PLAN_ACTIVIDADES")
public class PlanActividad {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "plan_act_gen")
  @SequenceGenerator(name = "plan_act_gen", sequenceName = "AI_REC_PLAN_ACT_SEQ", allocationSize = 1)
  @Column(name = "ARPAC_CODIGO")
  private Long arpac_codigo;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ARPA_CODIGO", referencedColumnName = "ARPA_CODIGO")
  private PlanAccion planAccion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ARER_CODIGO", referencedColumnName = "ARER_CODIGO")
  private EvidenciaRequerida evidenciaRequerida;

  @Column(name = "ARPAC_NUM_ACTIVIDAD", nullable = false)
  private Integer arpac_num_actividad;

  @Column(name = "ARPAC_ACTIVIDAD", length = 1000, nullable = false)
  private String arpac_actividad;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARPAC_FECHA_EJECUCION", nullable = false)
  private Date arpac_fecha_ejecucion;

  @Column(name = "ARPAC_ENTREGABLE_ESPERADO", length = 500, nullable = false)
  private String arpac_entregable_esperado;

  @Column(name = "ARPAC_ESTADO", length = 1, nullable = false)
  private Character arpac_estado = 'A';
}
