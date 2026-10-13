package ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.dtos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionesDTO {
    private Integer anotif_codigo;
    private String anotif_titulo;
    private String anotif_mensaje;
    private Integer anotif_estado;
    private Date anotif_fecha_creacion;
    private Date anotif_fecha_leida;
    private String anotif_url_sistema;
    private String anotif_tipo;
    private Integer anotif_anio;
    private String anotif_usuario_per;
    private String anotif_auditor_asignado;
    private Long anotif_ainin_codigo;
    private Long anotif_arere_codigo;
    private Long anotif_padre_id;

    public NotificacionesDTO(Integer anotif_codigo, String anotif_titulo, String anotif_mensaje, Integer anotif_estado, Date anotif_fecha_creacion, Date anotif_fecha_leida, String anotif_url_sistema, String anotif_tipo, Integer anotif_anio, String anotif_usuario_per, String anotif_auditor_asignado, Long anotif_ainin_codigo, Long anotif_arere_codigo) {
        this.anotif_codigo = anotif_codigo;
        this.anotif_titulo = anotif_titulo;
        this.anotif_mensaje = anotif_mensaje;
        this.anotif_estado = anotif_estado;
        this.anotif_fecha_creacion = anotif_fecha_creacion;
        this.anotif_fecha_leida = anotif_fecha_leida;
        this.anotif_url_sistema = anotif_url_sistema;
        this.anotif_tipo = anotif_tipo;
        this.anotif_anio = anotif_anio;
        this.anotif_usuario_per = anotif_usuario_per;
        this.anotif_auditor_asignado = anotif_auditor_asignado;
        this.anotif_ainin_codigo = anotif_ainin_codigo;
        this.anotif_arere_codigo = anotif_arere_codigo;
        this.anotif_padre_id = null;
    }
}
