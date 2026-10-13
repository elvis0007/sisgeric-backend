package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * Autor: Mateo Díaz
 */
@Data
@AllArgsConstructor
public class UsuarioRecordatorioNotificacionDTO {
    private String correo;
    private String usuario;
}
