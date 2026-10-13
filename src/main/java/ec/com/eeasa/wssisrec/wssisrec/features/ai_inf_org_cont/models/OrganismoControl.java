package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_org_cont.models;

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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "AI_INF_ORG_CONT")
public class OrganismoControl {
  @Id
  @Column(name = "AINOC_CODIGO")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ainoc_gen")
  @SequenceGenerator(name = "ainoc_gen", sequenceName = "AI_INF_ORG_CON_SEQ", allocationSize = 1)
  private Long ainoc_codigo;

  @Column(name = "AINOC_DESCRIPCION")
  private String ainoc_descripcion;

  @Column(name = "AINOC_ESTADO")
  private Character ainoc_estado;

  @Column(name = "AINOC_USUARIO")
  private String ainoc_usuario;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AINOC_FECHA")
  private Date ainoc_fecha;

}
