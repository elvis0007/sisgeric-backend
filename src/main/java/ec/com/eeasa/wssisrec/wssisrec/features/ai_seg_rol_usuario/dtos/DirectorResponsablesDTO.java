package ec.com.eeasa.wssisrec.wssisrec.features.ai_seg_rol_usuario.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectorResponsablesDTO {
    private DirectorDTO director;
    private List<ResponsableDTO> responsables;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class DirectorDTO {
        private String nombres;
        private String apellidos;
        private String correo;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ResponsableDTO {
        private String nombres;
    }       
}
