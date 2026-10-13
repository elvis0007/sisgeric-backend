package ec.com.eeasa.wssisrec.wssisrec.features.auditoria;

import lombok.Data;

@Data
public class Auditoria {
  private String host;
  private String usuario;
  private String proyecto;

  public Auditoria(String host, String usuario) {
    this.host = host;
    this.usuario = usuario;
    this.proyecto = "SISREC";
  }
}
