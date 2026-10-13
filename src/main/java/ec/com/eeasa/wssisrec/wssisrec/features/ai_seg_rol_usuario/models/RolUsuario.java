package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.models;

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

import ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol.models.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "AI_SEG_ROL_USU")
public class RolUsuario {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rol_usu_gen")
  @SequenceGenerator(name = "rol_usu_gen", sequenceName = "AI_SEG_ROL_USU_SEQ", allocationSize = 1)
  @Column(name = "ASERU_CODIGO")
  private Long aseru_codigo;

  @Column(name = "ASERU_CUENTA")
  private String dsgus_cuenta;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "ASERO_CODIGO", referencedColumnName = "ASERO_CODIGO")
  private Rol rol;

  @Column(name = "ASERU_CODIGO_PER")
  private String aseru_codigo_per;

  @Column(name = "ASERU_NUM_ROL")
  private String aseru_num_rol;

  @Column(name = "ASERU_NOMBRE")
  private String aseru_nombre;

  @Column(name = "ASERU_APELLIDO")
  private String aseru_apellido;

  @Column(name = "ASERU_DEPARTAMENTO")
  private String aseru_departamento;

  @Column(name = "ASERU_ESTADO", length = 1)
  private Character aseru_estado;

  @Column(name = "ASERU_USUARIO")
  private String aseru_usuario;

  @Column(name = "ASERU_CORREO")
  private String aseru_correo;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ASERU_FECHA")
  private Date aseru_fecha;

}
