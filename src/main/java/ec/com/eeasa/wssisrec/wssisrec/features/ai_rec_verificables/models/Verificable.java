package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_verificables.models;

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

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_detalle_recomendacion.models.DetalleRecomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_estado_verificables.models.EstadoVerificable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "AI_REC_VERIFICABLES")
public class Verificable {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "verificable_gen")
  @SequenceGenerator(name = "verificable_gen", sequenceName = "AI_REC_VERIFICABLE_SEQ", allocationSize = 1)
  @Column(name = "AREVE_CODIGO")
  private Long areve_codigo;

  @Column(name = "AREVE_DESCRIPCION")
  private String areve_descripcion;

  @Column(name = "AREVE_ADJUNTO")
  private String areve_adjunto;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AREVE_FECHA_CARGA")
  private Date areve_fecha_carga;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AREVE_FECHA_VALIDACION")
  private Date areve_fecha_validacion;

  @Column(name = "AREVE_OBSERVACION")
  private String areve_observacion;

  @Column(name = "AREVE_USUARIO")
  private String areve_usuario;

  @Column(name = "AREVE_ESTADO")
  private Character areve_estado;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AREVE_FECHA")
  private Date areve_fecha;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  @JoinColumn(name = "AREDR_CODIGO", referencedColumnName = "AREDR_CODIGO")
  private DetalleRecomendacion detalleRecomendacion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "AREEV_CODIGO", referencedColumnName = "AREEV_CODIGO")
  private EstadoVerificable estadoVer;

  @Override
  public String toString() {
    return "Verificable{" +
        "areve_codigo=" + areve_codigo +
        ", areve_descripcion='" + areve_descripcion + '\'' +
        ", areve_adjunto='" + areve_adjunto + '\'' +
        ", areve_fecha_carga=" + areve_fecha_carga +
        ", areve_usuario='" + areve_usuario + '\'' +
        ", areve_estado=" + areve_estado +
        ", areve_fecha=" + areve_fecha +
        '}';
  }
}