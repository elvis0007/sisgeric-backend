package ec.com.eeasa.wssisrec.wssisrec.features.correos.dtos;

/**
 * DTO para mapear la respuesta del microservicio centralizado de correos (ws_correo).
 * 
 * Autor: Generado automáticamente
 */
public class WsCorreoResponseDTO {

    private boolean success;
    private String message;
    private String code;

    public WsCorreoResponseDTO() {
    }

    public WsCorreoResponseDTO(boolean success, String message, String code) {
        this.success = success;
        this.message = message;
        this.code = code;
    }

    // Getters y Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "WsCorreoResponseDTO{success=" + success + ", message='" + message + "', code='" + code + "'}";
    }
}
