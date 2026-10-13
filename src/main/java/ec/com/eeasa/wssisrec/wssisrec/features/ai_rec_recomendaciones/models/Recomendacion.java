package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.models.Conclusion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.models.EstadoSeg;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.enums.EstadoSegEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "AI_REC_RECOMENDACIONES")
@NoArgsConstructor
@AllArgsConstructor
public class Recomendacion {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recomendacion_gen")
  @SequenceGenerator(name = "recomendacion_gen", sequenceName = "AI_REC_RECOMENDACION_SEQ", allocationSize = 1)
  private Long arere_codigo;

  @Column(name = "ARERE_DESCRIPCION")
  private String arere_descripcion;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARERE_FECHA_MAXIMA")
  private Date arere_fecha_maxima;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARERE_FECHA_INICIO_CONTROL")
  private Date arere_fecha_inicio_control;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARERE_FECHA_CUMPLIDA")
  private Date arere_fecha_cumplida;

  @Column(name = "ARERE_OBS_NO_CUMPLIDA")
  private String arere_obs_no_cumplida;

  @Column(name = "ARERE_OBS_EN_PROCESO")
  private String arere_obs_en_proceso;

  @Column(name = "ARERE_OBS_EN_VALIDACION")
  private String arere_obs_en_validacion;

  @Column(name = "ARERE_OBS_NO_APLICA")
  private String arere_obs_no_aplica;

  @Column(name = "ARERE_NUM_REC")
  private Integer arere_num_rec;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARERE_FECHA_CORTE")
  private Date arere_fecha_corte;

  @Column(name = "ARERE_NUM_PRORROGA")
  private Integer arere_num_prorroga;

  @Column(name = "ARERE_ESTADO")
  private Character arere_estado;

  @Column(name = "ARERE_USUARIO")
  private String arere_usuario;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARERE_FECHA")
  private Date arere_fecha;

  @Column(name = "ARERE_EVIDENCIA_REQUERIDA", length = 2000, insertable = false, updatable = false)
  private String arere_evidencia_requerida;

  @Column(name = "ARERE_RIESGO_VERIFICADO")
  private String arere_riesgo_verificado;


  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ARECO_CODIGO", referencedColumnName = "ARECO_CODIGO")
  private Conclusion conclusion;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "AREES_CODIGO", referencedColumnName = "AREES_CODIGO")
  private EstadoSeg estadoSeg;

  public void setEstadoSeg(EstadoSeg estadoSeg) {
    this.estadoSeg = estadoSeg;
    normalizarRiesgoCumplida();
  }

  public String getArere_riesgo_verificado() {
    return estaCumplida() ? "BAJO" : arere_riesgo_verificado;
  }

  private boolean estaCumplida() {
    return estadoSeg != null && EstadoSegEnum.CUMPLIDA.getCodigo().equals(estadoSeg.getArees_codigo());
  }

  @PrePersist
  @PreUpdate
  private void normalizarRiesgoCumplida() {
    if (estaCumplida()) arere_riesgo_verificado = "BAJO";
  }

}
