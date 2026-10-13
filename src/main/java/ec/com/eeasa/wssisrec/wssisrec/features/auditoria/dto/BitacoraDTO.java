package ec.com.eeasa.wssisrec.wssisrec.features.auditoria.dto;

import java.util.Date;

import lombok.Data;

@Data
public class BitacoraDTO {
  private String codigo;
  private String proyecto;
  private String tabla;
  private String host;
  private String usuario;
  private Date fecha;
  private String detalle;
}
