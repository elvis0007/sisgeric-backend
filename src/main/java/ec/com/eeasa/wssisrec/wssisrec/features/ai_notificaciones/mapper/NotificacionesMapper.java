package ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.mapper;

import java.util.Date;

import org.springframework.stereotype.Component;

import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.dtos.NuevaNotificacionDTO;
import ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.models.Notificaciones;

@Component
public class NotificacionesMapper {
    
    public Notificaciones convertirDTOModel(NuevaNotificacionDTO dto){
        Notificaciones notificacion = new Notificaciones();
        notificacion.setAnotif_codigo(null);
        notificacion.setAnotif_titulo(dto.getAnotif_titulo());
        notificacion.setAnotif_mensaje(dto.getAnotif_mensaje());
        notificacion.setAnotif_estado(0);
        notificacion.setAnotif_fecha_creacion(new Date());
        notificacion.setAnotif_fecha_leida(null);
        notificacion.setAnotif_url_sistema(dto.getAnotif_url_sistema());
        notificacion.setAnotif_tipo(dto.getAnotif_tipo());
        notificacion.setAnotif_anio(dto.getAnotif_anio());
        notificacion.setAnotif_usuario_per(dto.getAnotif_usuario_per());
        notificacion.setAnotif_ainin_codigo(dto.getAnotif_ainin_codigo());
        notificacion.setAnotif_arere_codigo(dto.getAnotif_arere_codigo());
        notificacion.setAnotif_padre_id(dto.getAnotif_padre_id());
        return notificacion;
    }
}
