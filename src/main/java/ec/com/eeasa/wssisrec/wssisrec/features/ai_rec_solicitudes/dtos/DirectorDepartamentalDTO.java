package ec.com.eeasa.wssisrec.wssisrec.features.ai_rec_solicitudes.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectorDepartamentalDTO {
  private Long numeroSolicitud;
  private String nombreDirector;
  private String departamento;
  private String correoDirector;
  private String cargoResponsable;
}
