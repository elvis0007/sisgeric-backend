package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_historial.models;

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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "AI_REC_HISTORIAL")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HistorialRecomendacion {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "historial_gen")
  @SequenceGenerator(name = "historial_gen", sequenceName = "AI_REC_HISTORIAL_SEQ", allocationSize = 1)
  @Column(name = "AREHI_CODIGO")
  private Long arehi_codigo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ARERE_CODIGO", referencedColumnName = "ARERE_CODIGO")
  @JsonIgnore
  private Recomendacion recomendacion;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AREHI_FECHA")
  private Date arehi_fecha;

  @Column(name = "AREHI_ACCION")
  private String arehi_accion;

  @Column(name = "AREHI_DETALLE")
  private String arehi_detalle;

  @Column(name = "AREHI_RESPONSABLE_ANT")
  private String arehi_responsable_ant;

  @Column(name = "AREHI_RESPONSABLE_NUE")
  private String arehi_responsable_nue;

  @Temporal(TemporalType.DATE)
  @Column(name = "AREHI_FECHA_MAX_ANT")
  private Date arehi_fecha_max_ant;

  @Temporal(TemporalType.DATE)
  @Column(name = "AREHI_FECHA_MAX_NUE")
  private Date arehi_fecha_max_nue;

  @Column(name = "AREHI_AUDITOR")
  private String arehi_auditor;

  @Column(name = "AREHI_OBSERVACION")
  private String arehi_observacion;

  @Column(name = "AREHI_USUARIO")
  private String arehi_usuario;

}
