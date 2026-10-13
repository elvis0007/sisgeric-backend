package ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.models;

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
@Table(name = "AI_NOTIFICACIONES")

public class Notificaciones {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notificacion_gen")
    @SequenceGenerator(name = "notificacion_gen", sequenceName = "AI_NOTIFICACIONES_SEQ", allocationSize = 1)
    @Column(name = "ANOTIF_CODIGO")
    private Long anotif_codigo;

    @Column(name = "ANOTIF_TITULO")
    private String anotif_titulo;

    @Column(name = "ANOTIF_MENSAJE")
    private String anotif_mensaje;

    @Column(name = "ANOTIF_ESTADO")
    private Integer anotif_estado;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ANOTIF_FECHA_CREACION")
    private Date anotif_fecha_creacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ANOTIF_FECHA_LEIDA")
    private Date anotif_fecha_leida;

    @Column(name = "ANOTIF_URL_SISTEMA")
    private String anotif_url_sistema;

    @Column(name = "ANOTIF_TIPO")
    private String anotif_tipo;

    @Column(name = "ANOTIF_ANIO")
    private Integer anotif_anio;

    @Column(name = "ANOTIF_USUARIO_PER")
    private String anotif_usuario_per;

    @Column(name = "ANOTIF_AININ_CODIGO")
    private Long anotif_ainin_codigo;

    @Column(name = "ANOTIF_ARERE_CODIGO")
    private Long anotif_arere_codigo;

    @Column(name = "ANOTIF_PADRE_ID")
    private Long anotif_padre_id;
}
