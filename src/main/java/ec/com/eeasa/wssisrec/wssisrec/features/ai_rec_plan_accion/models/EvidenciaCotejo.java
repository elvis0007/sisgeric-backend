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

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AI_REC_EVIDENCIA_COTEJO")
public class EvidenciaCotejo {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evidencia_cotejo_gen")
  @SequenceGenerator(name = "evidencia_cotejo_gen", sequenceName = "AI_REC_EVIDENCIA_COTEJO_SEQ", allocationSize = 1)
  @Column(name = "AREC_CODIGO")
  private Long arec_codigo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ARER_CODIGO", referencedColumnName = "ARER_CODIGO")
  private EvidenciaRequerida evidenciaRequerida;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "AREDR_CODIGO", referencedColumnName = "AREDR_CODIGO")
  private DetalleRecomendacion detalleRecomendacion;

  @Column(name = "AREC_CUMPLE", length = 1, nullable = false)
  private Character arec_cumple = 'N'; // 'S' o 'N'

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AREC_FECHA_COTEJO", nullable = false)
  private Date arec_fecha_cotejo;

  @Column(name = "AREC_ESTADO", length = 1, nullable = false)
  private Character arec_estado = 'A';
}
