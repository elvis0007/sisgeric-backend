package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_seguimiento.models;

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

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AI_REC_ESTADO_SEGUIMIENTO")
public class EstadoSeg {

  @Id
  @Column(name = "AREES_CODIGO")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estado_seg_gen")
  @SequenceGenerator(name = "estado_seg_gen", sequenceName = "AI_REC_ESTADO_SEGUIMIENTO_SEQ", allocationSize = 1)
  private Long arees_codigo;

  @Column(name = "AREES_DESCRIPCION")
  private String arees_descripcion;

  @Column(name = "AREES_COLOR")
  private String arees_color;

  @Column(name = "AREES_USUARIO")
  private String arees_usuario;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AREES_FECHA")
  private Date arees_fecha;

  @Column(name = "AREES_ESTADO")
  private Character arees_estado;

}
