package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectorInformacionDTO {
    private String director;
    private String departamento;
    private String cargo;
}
