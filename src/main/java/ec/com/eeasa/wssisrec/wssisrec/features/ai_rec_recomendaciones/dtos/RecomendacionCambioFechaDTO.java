package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_recomendaciones.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecomendacionCambioFechaDTO {
    private String nombreApellidoRS;
    private String responsableUsuarioPer;
    private String responsableCorreo;
    private String infTitulo;
    private String infMemoAprobacion;
    private String infMemorando;
    private String recFechaMaxCumplimiento;
    private String recDescripcion;
}
