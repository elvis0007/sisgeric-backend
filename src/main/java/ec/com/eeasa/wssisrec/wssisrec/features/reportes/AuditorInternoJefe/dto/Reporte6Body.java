package ec.com.eeasa.wssisrec.wssisrec.features.reportes.AuditorInternoJefe.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reporte6Body {
   private String fechaInicio;
    private String fechaFin;
    private int estadoSeguimiento; 
}
