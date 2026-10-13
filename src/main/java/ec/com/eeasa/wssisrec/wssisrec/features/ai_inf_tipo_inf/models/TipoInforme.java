package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_tipo_inf.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AI_INF_TIPO_INF")
public class TipoInforme {
  @Id
  @Column(name = "AINTI_CODIGO")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ainti_gen")
  @SequenceGenerator(name = "ainti_gen", sequenceName = "AI_INF_TIPO_INF_SEQ", allocationSize = 1)
  private Long ainti_codigo;

  @Column(name = "AINTI_DESCRIPCION")
  private String ainti_descripcion;

  @Column(name = "AINTI_ESTADO")
  private Character ainti_estado;

  @Column(name = "AINTI_USUARIO")
  private String ainti_usuario;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AINTI_FECHA")
  private Date ainti_fecha;
}
