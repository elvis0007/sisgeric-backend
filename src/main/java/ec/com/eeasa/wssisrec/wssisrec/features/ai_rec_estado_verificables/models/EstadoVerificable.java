package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.models;

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
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AI_REC_ESTADO_VERIFICABLE")
public class EstadoVerificable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estado_ver_gen")
  @SequenceGenerator(name = "estado_ver_gen", sequenceName = "AI_REC_ESTADO_VERIFICABLE_SEQ", allocationSize = 1)
  @Column(name = "AREEV_CODIGO")
  private Long areev_codigo;

  @Column(name = "AREEV_DESCRIPCION")
  private String areev_descripcion;

  @Column(name = "AREEV_COLOR")
  private String areev_color;

  @Column(name = "AREEV_USUARIO")
  private String areev_usuario;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AREEV_FECHA")
  private Date areev_fecha;

  @Column(name = "AREEV_ESTADO")
  private Character areev_estado;

}
