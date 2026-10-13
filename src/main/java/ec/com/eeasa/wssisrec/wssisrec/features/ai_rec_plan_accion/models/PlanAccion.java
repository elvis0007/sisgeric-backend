package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.models;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AI_REC_PLAN_ACCION")
public class PlanAccion {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "plan_accion_gen")
  @SequenceGenerator(name = "plan_accion_gen", sequenceName = "AI_REC_PLAN_ACCION_SEQ", allocationSize = 1)
  @Column(name = "ARPA_CODIGO")
  private Long arpa_codigo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ARERE_CODIGO", referencedColumnName = "ARERE_CODIGO")
  private Recomendacion recomendacion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "AREDR_CODIGO", referencedColumnName = "AREDR_CODIGO")
  private DetalleRecomendacion detalleRecomendacion;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARPA_FECHA_MAXIMA_REF", nullable = false)
  private Date arpa_fecha_maxima_ref;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARPA_FECHA_ACTUALIZACION", nullable = false)
  private Date arpa_fecha_actualizacion;

  @Column(name = "ARPA_ESTADO", length = 1, nullable = false)
  private Character arpa_estado = 'A';

  @OneToMany(mappedBy = "planAccion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("arpac_num_actividad ASC")
  private List<PlanActividad> actividades;
}
