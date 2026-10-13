package ec.com.eeasa.wssisrec.wssisrec.features.correos.dtos;

import java.util.List;

public class WsCorreoRequestDTO {

    private String apiKeySys;
    private String apiKeyTemp;
    private String destinatario;
    private List<String> cc;
    private String usuarioNombre;
    private String asunto;
    private String parametros;

    public WsCorreoRequestDTO() {
    }

    public WsCorreoRequestDTO(String apiKeySys, String apiKeyTemp, String destinatario,
            List<String> cc, String usuarioNombre, String asunto, String parametros) {
        this.apiKeySys = apiKeySys;
        this.apiKeyTemp = apiKeyTemp;
        this.destinatario = destinatario;
        this.cc = cc;
        this.usuarioNombre = usuarioNombre;
        this.asunto = asunto;
        this.parametros = parametros;
    }

    public String getApiKeySys() {
        return apiKeySys;
    }

    public void setApiKeySys(String apiKeySys) {
        this.apiKeySys = apiKeySys;
    }

    public String getApiKeyTemp() {
        return apiKeyTemp;
    }

    public void setApiKeyTemp(String apiKeyTemp) {
        this.apiKeyTemp = apiKeyTemp;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getParametros() {
        return parametros;
    }

    public void setParametros(String parametros) {
        this.parametros = parametros;
    }
}
