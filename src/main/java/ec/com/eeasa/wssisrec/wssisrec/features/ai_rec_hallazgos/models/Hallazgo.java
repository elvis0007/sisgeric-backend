package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_hallazgos.models;
  
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

import ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.models.Informe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AI_REC_HALLAZGOS")

public class Hallazgo {
  @Id
  @Column(name = "AREHA_CODIGO")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hallazgo_gen")
  @SequenceGenerator(name = "hallazgo_gen", sequenceName = "AI_REC_HALLAZGO_SEQ", allocationSize = 1)
  private Long areha_codigo;

  @Column(name = "AREHA_TITULO")
  private String areha_titulo;

  @Column(name = "AREHA_ESTADO_SEG")
  private Character areha_estado_seg;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AREHA_FECHA")
  private Date areha_fecha;

  @Column(name = "AREHA_USUARIO")
  private String areha_usuario;

  @Column(name = "AREHA_ESTADO")
  private Character areha_estado;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "AININ_CODIGO", referencedColumnName = "AININ_CODIGO")
  private Informe informe;

}
