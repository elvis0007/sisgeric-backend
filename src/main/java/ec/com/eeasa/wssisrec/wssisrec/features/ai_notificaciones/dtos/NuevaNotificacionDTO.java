package ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.dtos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NuevaNotificacionDTO {
    //VAR
    private String anotif_titulo;
    private String anotif_mensaje;
    private Integer anotif_estado;
    private Date anotif_fecha_creacion;
    private String anotif_url_sistema;
    private String anotif_tipo;
    private Integer anotif_anio;
    private String anotif_usuario_per;
    private Long anotif_ainin_codigo;
    private Long anotif_arere_codigo;
    private Long anotif_padre_id;

    public NuevaNotificacionDTO(String anotif_titulo, String anotif_mensaje, String anotif_url_sistema, String anotif_usuario_per){
        this.anotif_titulo = anotif_titulo;
        this.anotif_mensaje = anotif_mensaje;
        this.anotif_url_sistema = anotif_url_sistema;
        this.anotif_usuario_per = anotif_usuario_per;
    }

    public NuevaNotificacionDTO(String anotif_titulo, String anotif_mensaje, String anotif_url_sistema, String anotif_tipo, Integer anotif_anio, String anotif_usuario_per){
        this.anotif_titulo = anotif_titulo;
        this.anotif_mensaje = anotif_mensaje;
        this.anotif_url_sistema = anotif_url_sistema;
        this.anotif_tipo = anotif_tipo;
        this.anotif_anio = anotif_anio;
        this.anotif_usuario_per = anotif_usuario_per;
    }

    public NuevaNotificacionDTO(String anotif_titulo, String anotif_mensaje, String anotif_url_sistema, String anotif_tipo, Integer anotif_anio, Long anotif_ainin_codigo, Long anotif_arere_codigo, String anotif_usuario_per){
        this.anotif_titulo = anotif_titulo;
        this.anotif_mensaje = anotif_mensaje;
        this.anotif_url_sistema = anotif_url_sistema;
        this.anotif_tipo = anotif_tipo;
        this.anotif_anio = anotif_anio;
        this.anotif_ainin_codigo = anotif_ainin_codigo;
        this.anotif_arere_codigo = anotif_arere_codigo;
        this.anotif_usuario_per = anotif_usuario_per;
    }
}
