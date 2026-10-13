package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.models;

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
@Table(name = "AI_SEG_ROL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rol {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rol_gen")
  @SequenceGenerator(name = "rol_gen", sequenceName = "AI_SEG_ROL_SEQ", allocationSize = 1)
  @Column(name = "ASERO_CODIGO")
  private Long asero_codigo;

  @Column(name = "ASERO_DESCRIPCION")
  private String asero_descripcion;

  @Column(name = "ASERO_ESTADO", length = 1)
  private Character asero_estado;

  @Column(name = "ASERO_USUARIO")
  private String asero_usuario;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ASERO_FECHA")
  private Date asero_fecha;

}
