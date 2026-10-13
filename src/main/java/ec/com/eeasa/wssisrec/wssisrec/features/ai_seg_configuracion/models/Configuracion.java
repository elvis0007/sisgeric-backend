package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_configuracion.models;

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
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "AI_SEG_CONFIGURACION")
public class Configuracion {
  @Id
  @Column(name = "ASECO_CODIGO")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aseco_gen")
  @SequenceGenerator(name = "aseco_gen", sequenceName = "AI_SEG_CONFIGURACION_SEQ", allocationSize = 1)
  private Long aseco_codigo;

  @Column(name = "ASECO_HOST")
  private String aseco_host;

  @Column(name = "ASECO_PORT")
  private String aseco_port;

  @Column(name = "ASECO_USER")
  private String aseco_user;

  @Column(name = "ASECO_CONTRASENIA")
  private String aseco_contrasenia;

  @Column(name = "ASECO_NOMBRE_HOST")
  private String aseco_nombre_host;

  @Column(name = "ASECO_DIRECTORIO")
  private String aseco_directorio;

  @Column(name = "ASECO_SIZE_FILE_MB")
  private Long aseco_size_file_mb;

  @Column(name = "ASECO_USUARIO")
  private String aseco_usuario;

  @Column(name = "ASECO_ESTADO")
  private char aseco_estado;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ASECO_FECHA")
  private Date aseco_fecha;
}
