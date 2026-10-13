package ec.com.eeasa.wssisrec.wssisrec.features.ai_corte_evaluacion.models;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "AI_CORTE_EVALUACION")
public class CorteEvaluacion {

    @Id
    @Column(name = "ARCEV_CODIGO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("arcev_codigo")
    private Long arcevCodigo;

    @Column(name = "ARCEV_NOMBRE", nullable = false)
    @JsonProperty("arcev_nombre")
    private String arcevNombre;

    @Column(name = "ARCEV_TIPO", nullable = false)
    @JsonProperty("arcev_tipo")
    private String arcevTipo;

    @Temporal(TemporalType.DATE)
    @Column(name = "ARCEV_FECHA_INICIO", nullable = false)
    @JsonProperty("arcev_fecha_inicio")
    private Date arcevFechaInicio;

    @Temporal(TemporalType.DATE)
    @Column(name = "ARCEV_FECHA_FIN", nullable = false)
    @JsonProperty("arcev_fecha_fin")
    private Date arcevFechaFin;

    @Temporal(TemporalType.DATE)
    @Column(name = "ARCEV_FECHA_CORTE", nullable = false)
    @JsonProperty("arcev_fecha_corte")
    private Date arcevFechaCorte;

    @Column(name = "ARCEV_ANIO", nullable = false)
    @JsonProperty("arcev_anio")
    private Integer arcevAnio;

    @Column(name = "ARCEV_ESTADO")
    @JsonProperty("arcev_estado")
    private Integer arcevEstado;

    @Transient
    @JsonProperty("total_informes")
    private Integer totalInformes = 0;

    @Transient
    @JsonProperty("total_recomendaciones")
    private Integer totalRecomendaciones = 0;
}
