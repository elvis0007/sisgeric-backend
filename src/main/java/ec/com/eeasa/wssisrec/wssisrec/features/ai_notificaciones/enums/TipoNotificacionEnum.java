package ec.com.eeasa.wssisrec.wssisrec.features.ai_notificaciones.enums;

public class TipoNotificacionEnum {
    private TipoNotificacionEnum() {}

    public static final String VERIFICABLE_PENDIENTE = "verificable-pendiente";
    public static final String VERIFICABLE_ACTUALIZADO = "verificable-actualizado";
    public static final String VERIFICABLE_ELIMINADO = "verificable-eliminado";
    public static final String VERIFICABLE_ELIMINADO_AUDITOR = "verificable-eliminado-auditor";

    public static final String SOLICITUD_NUEVA = "solicitud-nueva";
    public static final String SOLICITUD_PREAPROBADA_DIRECTOR = "solicitud-preaprobada-director";
    public static final String SOLICITUD_PREAPROBADA = "solicitud-preaprobada";
    public static final String SOLICITUD_ACTUALIZADA = "solicitud-actualizada";
    public static final String SOLICITUD_RECHAZADA = "solicitud-rechazada";
    public static final String SOLICITUD_CADUCADA = "solicitud-caducada";
    public static final String SOLICITUD_ELIMINADA = "solicitud-eliminada";
    public static final String SOLICITUD_ELIMINADA_AUDITOR = "solicitud-eliminada-auditor";

    public static final String RECOMENDACION_ASIGNADA = "recomendacion-asignada";
    public static final String RECOMENDACION_REASIGNADA = "recomendacion-reasignada";

    public static final String RECOMENDACION_VENCER = "recomendacion-vencer";
    public static final String RECOMENDACION_VENCIDA = "recomendacion-vencida";
    public static final String RECOMENDACION_CUMPLIDA = "recomendacion-cumplida";

    public static final String RECOMENDACION_FECHA_CAMBIADA = "recomendacion-fecha-cambiada";
    public static final String PLAN_ACCION_REQUERIDO = "plan-accion-requerido";
    public static final String PLAN_ACCION_RECORDATORIO = "plan-accion-recordatorio";
    public static final String PLAN_ACCION_VENCIDO = "plan-accion-vencido";
}

