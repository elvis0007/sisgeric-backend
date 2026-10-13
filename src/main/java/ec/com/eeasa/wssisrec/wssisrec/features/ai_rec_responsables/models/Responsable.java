package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models;

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
@Table(name = "AI_REC_RESPONSABLES")
public class Responsable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "responsable_gen")
  @SequenceGenerator(name = "responsable_gen", sequenceName = "AI_REC_RESPONSABLE_SEQ", allocationSize = 1)
  @Column(name = "ARERS_CODIGO")
  private Long arers_codigo;

  @Column(name = "ARERS_CARGO")
  private String arers_cargo;

  @Column(name = "ARERS_NOMBRE")
  private String arers_nombre;

  @Column(name = "ARERS_CORREO")
  private String arers_correo;

  @Column(name = "ARERS_DEPARTAMENTO")
  private String arers_departamento;

  @Column(name = "ARERS_DEPARTAMENTO_ABREV")
  private String arers_departamento_abrev;
  
  @Column(name = "ARERS_CODIGO_PER")
  private String arers_codigo_per;

  @Column(name = "ARERS_APELLIDO")
  private String arers_apellido;

  @Column(name = "ARERS_NUM_ROL")
  private String arers_num_rol;

  @Column(name = "ARERS_SECCION")
  private String arers_seccion;

  @Column(name = "ARERS_USUARIO_PER")
  private String arers_usuario_per;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARERS_FECHA_REEMPL")
  private Date arers_fecha_reempl;

  @Column(name = "ARERS_REEMPL_POR")
  private String arers_reempl_por;

  @Column(name = "ARERS_ESTADO")
  private Character arers_estado;

  @Column(name = "ARERS_USUARIO")
  private String arers_usuario;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARERS_FECHA")
  private Date arers_fecha;

  public String getNombre_completo() {
    return (arers_nombre != null ? arers_nombre.trim() : "") + " " + (arers_apellido != null ? arers_apellido.trim() : "");
  }

  // @OneToMany(cascade = CascadeType.ALL, mappedBy = "responsable", orphanRemoval
  // = true, fetch = FetchType.LAZY)
  // @JsonManagedReference
  // private List<DetalleRecomendacion> detalleRecomendaciones;
}
