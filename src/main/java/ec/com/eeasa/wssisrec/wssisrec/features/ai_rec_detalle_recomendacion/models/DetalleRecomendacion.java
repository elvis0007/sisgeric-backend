package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models;

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
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models.Responsable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AI_REC_DETALLE_RECOMENDACION")

public class DetalleRecomendacion {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "detalle_gen")
  @SequenceGenerator(name = "detalle_gen", sequenceName = "AI_REC_DETALLE_RECOMENDACION_SEQ", allocationSize = 1)
  @Column(name = "AREDR_CODIGO")
  private Long aredr_codigo;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AREDR_FECHA_CORTE")
  private Date aredr_fecha_corte;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AREDR_FECHA")
  private Date aredr_fecha;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AREDR_FECHA_MAXIMA")
  private Date aredr_fecha_maxima;

  @Column(name = "AREDR_ESTADO")
  private Character aredr_estado;

  @Column(name = "AREDR_USUARIO")
  private String aredr_usuario;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "ARERE_CODIGO", referencedColumnName = "ARERE_CODIGO")
  private Recomendacion recomendacion;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "ARERS_CODIGO", referencedColumnName = "ARERS_CODIGO")
  private Responsable responsable;

}
