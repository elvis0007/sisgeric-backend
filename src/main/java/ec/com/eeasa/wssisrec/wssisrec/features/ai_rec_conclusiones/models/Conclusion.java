package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_conclusiones.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.models.Hallazgo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AI_REC_CONCLUSIONES")
public class Conclusion {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "conclusion_gen")
  @SequenceGenerator(name = "conclusion_gen", sequenceName = "AI_REC_CONCLUSION_SEQ", allocationSize = 1)
  @Column(name = "ARECO_CODIGO")
  private Long areco_codigo;

  @Lob
  @Column(name = "ARECO_CONCLUSION")
  private String areco_conclusion;

  @Column(name = "ARECO_ESTADO_SEG")
  private Character areco_estado_seg;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARECO_FECHA")
  private Date areco_fecha;

  @Column(name = "ARECO_USUARIO")
  private String areco_usuario;

  @Column(name = "ARECO_ESTADO")
  private Character areco_estado;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "AREHA_CODIGO", referencedColumnName = "AREHA_CODIGO")
  private Hallazgo hallazgo;

}
