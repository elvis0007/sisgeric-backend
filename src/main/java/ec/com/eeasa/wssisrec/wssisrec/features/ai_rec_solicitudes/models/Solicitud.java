package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedStoredProcedureQueries;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.ParameterMode;
import javax.persistence.SequenceGenerator;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.models.Recomendacion;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_responsables.models.Responsable;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "AI_REC_SOLICITUDES")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(name = "SP_SOLICITUDES_DEPARTAMENTAL", procedureName = "SP_SOLICITUDES_DEPARTAMENTAL", resultClasses = Solicitud.class, parameters = {
        @StoredProcedureParameter(mode = ParameterMode.IN, type = String.class, name = "ARERS_DEPARTAMENTO"),
        @StoredProcedureParameter(mode = ParameterMode.IN, type = String.class, name = "ARERS_CODIGO_PER"),
        @StoredProcedureParameter(mode = ParameterMode.IN, type = Integer.class, name = "ANIO"),
        @StoredProcedureParameter(mode = ParameterMode.IN, type = String.class, name = "ARESO_ESTADO_APROB"),
        @StoredProcedureParameter(mode = ParameterMode.REF_CURSOR, type = void.class, name = "RESULTADO")
    }),
    @NamedStoredProcedureQuery(name = "SP_SOLICITUDES_DEPARTAMENTAL_AUD", procedureName = "SP_SOLICITUDES_DEPARTAMENTAL_AUD", resultClasses = Solicitud.class, parameters = {
        @StoredProcedureParameter(mode = ParameterMode.IN, type = String.class, name = "ARERS_DEPARTAMENTO"),
        @StoredProcedureParameter(mode = ParameterMode.IN, type = String.class, name = "AININ_USUARIO"),
        @StoredProcedureParameter(mode = ParameterMode.IN, type = Integer.class, name = "ANIO"),
        @StoredProcedureParameter(mode = ParameterMode.IN, type = String.class, name = "ARESO_ESTADO_APROB"),
        @StoredProcedureParameter(mode = ParameterMode.REF_CURSOR, type = void.class, name = "RESULTADO")
    })
})

public class Solicitud {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "solicitud_gen")
  @SequenceGenerator(name = "solicitud_gen", sequenceName = "AI_REC_SOLICITUD_SEQ", allocationSize = 1)
  @Column(name = "ARESO_CODIGO")
  private Long areso_codigo;

  @Column(name = "ASERU_CODIGO_AUT")
  private String aseru_codigo_aut;

  @Column(name = "ARESO_TIPO_CAMBIO")
  private String areso_tipo_cambio;

  @Lob
  @Column(name = "ARESO_DESCRIPCION")
  private String areso_descripcion;

  @Column(name = "ARESO_MEMORANDO")
  private String areso_memorando;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARESO_FECHA_MAX_ANT")
  private Date areso_fecha_max_ant;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARESO_FECHA_MAX_NUE")
  private Date areso_fecha_max_nue;

  @Column(name = "ARESO_ADJUNTO")
  private String areso_adjunto;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARESO_FECHA_CARGA")
  private Date areso_fecha_carga;

  @Column(name = "ARESO_ESTADO_APROB")
  private Character areso_estado_aprob;

  @Column(name = "ARESO_ESTADO")
  private Character areso_estado;

  @Column(name = "ARESO_USUARIO")
  private String areso_usuario;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ARESO_FECHA")
  private Date areso_fecha;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "ARERS_CODIGO", referencedColumnName = "ARERS_CODIGO")
  @NotFound(action = NotFoundAction.IGNORE)
  private Responsable responsable;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ARERE_CODIGO", referencedColumnName = "ARERE_CODIGO")
  @JsonIgnore
  private Recomendacion recomendacion;
}
