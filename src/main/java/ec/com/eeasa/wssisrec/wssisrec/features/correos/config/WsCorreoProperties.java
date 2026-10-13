package ec.com.eeasa.wssisrec.wssisrec.features.correos.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ws-correo")
public class WsCorreoProperties {

    private String baseUrl;
    private String url;
    private String apiKeySys;
    private String emisor;
    private String usuarioNombre;
    private int timeoutConnect = 30000;
    private int timeoutRead = 120000;
    private String testWhitelist;
    private Map<String, String> templateKeys = new HashMap<>();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApiKeySys() {
        return apiKeySys;
    }

    public void setApiKeySys(String apiKeySys) {
        this.apiKeySys = apiKeySys;
    }

    public String getEmisor() {
        return emisor;
    }

    public void setEmisor(String emisor) {
        this.emisor = emisor;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public int getTimeoutConnect() {
        return timeoutConnect;
    }

    public void setTimeoutConnect(int timeoutConnect) {
        this.timeoutConnect = timeoutConnect;
    }

    public int getTimeoutRead() {
        return timeoutRead;
    }

    public void setTimeoutRead(int timeoutRead) {
        this.timeoutRead = timeoutRead;
    }

    public String getTestWhitelist() {
        return testWhitelist;
    }

    public void setTestWhitelist(String testWhitelist) {
        this.testWhitelist = testWhitelist;
    }

    public Map<String, String> getTemplateKeys() {
        return templateKeys;
    }

    public void setTemplateKeys(Map<String, String> templateKeys) {
        this.templateKeys = templateKeys;
    }
}
