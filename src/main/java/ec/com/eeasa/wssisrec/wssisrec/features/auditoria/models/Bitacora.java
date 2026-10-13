package ec.com.eeasa.wssisrec.wssisrec.features.auditoria.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
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
@Table(name = "AI_SEG_BITACORA")
public class Bitacora {
  @Id
  @Column(name = "ASEBI_CODIGO")
  private String asebit_codigo;

  @Column(name = "ASEBI_PROYECTO")
  private String asebit_proyecto;

  @Column(name = "ASEBI_TABLA")
  private String asebit_tabla;

  @Column(name = "ASEBI_HOST")
  private String asebit_host;

  @Column(name = "ASEBI_USUARIO")
  private String asebit_usuario;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ASEBI_FECHA")
  private Date asebit_fecha;

  @Lob
  @Column(name = "ASEBI_DETALLE")
  private String asebit_detalle;
}
