package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_plan_accion.models;

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

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AI_REC_EVIDENCIAS_REQUERIDAS")
public class EvidenciaRequerida {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evidencia_req_gen")
  @SequenceGenerator(name = "evidencia_req_gen", sequenceName = "AI_REC_EVIDENCIA_REQ_SEQ", allocationSize = 1)
  @Column(name = "ARER_CODIGO")
  private Long arer_codigo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ARERE_CODIGO", referencedColumnName = "ARERE_CODIGO")
  private Recomendacion recomendacion;

  @Column(name = "ARER_DESCRIPCION", length = 500, nullable = false)
  private String arer_descripcion;

  @Column(name = "ARER_ESTADO", length = 1, nullable = false)
  private Character arer_estado = 'A';
}
