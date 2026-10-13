package ec.com.eeasa.wssisrec.wssisrec.features.ai_inf_informes.dto;

import java.util.List;

import ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto.AuditoriaDTO;
import lombok.Data;

@Data
public class ActualizarUsuarioDTO {
  List<Long> ainin_codigos;
  String aseru_cuenta;
  AuditoriaDTO auditoria;
}
