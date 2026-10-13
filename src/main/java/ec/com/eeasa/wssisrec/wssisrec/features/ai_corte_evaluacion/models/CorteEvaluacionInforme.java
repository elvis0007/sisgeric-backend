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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "AI_CORTE_EVALUACION_INFORME")
public class CorteEvaluacionInforme {

    @Id
    @Column(name = "ARCEI_CODIGO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("arcei_codigo")
    private Long arceiCodigo;

    @Column(name = "ARCEV_CODIGO", nullable = false)
    @JsonProperty("arcev_codigo")
    private Long arcevCodigo;

    @Column(name = "AININ_CODIGO", nullable = false)
    @JsonProperty("ainin_codigo")
    private Long aininCodigo;

    @Column(name = "ARCEI_ES_MANUAL")
    @JsonProperty("arcei_es_manual")
    private Integer arceiEsManual;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ARCEI_FECHA_REGISTRO")
    @JsonProperty("arcei_fecha_registro")
    private Date arceiFechaRegistro;
}
