package ec.com.eeasa.wssisrec.wssisrec.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import ec.com.eeasa.wssisrec.wssisrec.features.correos.config.WsCorreoProperties;

@Service
public class WsCorreoService {

    private static final Logger logger = LoggerFactory.getLogger(WsCorreoService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WsCorreoProperties properties;

    public WsCorreoService(@Qualifier("wsCorreoRestTemplate") RestTemplate restTemplate,
                           ObjectMapper objectMapper,
                           WsCorreoProperties properties) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public boolean enviarCorreo(String destinatario, String asunto, String htmlContent, List<String> cc) {
        try {
            destinatario = filtrarDestinatario(destinatario);
            Map<String, Object> parametrosMap = new HashMap<>();
            parametrosMap.put("cuerpoHtml", htmlContent);
            String parametrosJson = objectMapper.writeValueAsString(parametrosMap);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add("apiKey_sys", properties.getApiKeySys());
            headers.add("destinatario", encodeHeaderUtf8(destinatario));
            headers.add("usuarioNombre", encodeHeaderUtf8(properties.getUsuarioNombre()));
            headers.add("asunto", encodeHeaderUtf8(asunto));

            if (cc != null && !cc.isEmpty()) {
                headers.add("cc", encodeHeaderUtf8(String.join(",", cc)));
            }

            HttpHeaders textPartHeaders = new HttpHeaders();
            textPartHeaders.setContentType(new org.springframework.http.MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("apiKey_sys", new HttpEntity<>(properties.getApiKeySys(), textPartHeaders));
            body.add("destinatario", new HttpEntity<>(destinatario, textPartHeaders));
            body.add("usuarioNombre", new HttpEntity<>(properties.getUsuarioNombre(), textPartHeaders));
            body.add("asunto", new HttpEntity<>(asunto, textPartHeaders));
            body.add("parametros", new HttpEntity<>(parametrosJson, textPartHeaders));

            if (cc != null && !cc.isEmpty()) {
                for (String correo : cc) {
                    body.add("cc", new HttpEntity<>(correo, textPartHeaders));
                }
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String apiKeyMasked = properties.getApiKeySys() != null && properties.getApiKeySys().length() > 8
                    ? properties.getApiKeySys().substring(0, 8) + "..." : "NULL";
            logger.info("[WS_CORREO] Enviando correo | URL: {} | apiKey_sys: {} | destinatario: {} | asunto: {}",
                    properties.getUrl(), apiKeyMasked, destinatario, asunto);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.getUrl(), requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("[WS_CORREO] Correo enviado exitosamente a: {} | Respuesta: {}",
                        destinatario, response.getBody());
                return true;
            } else {
                logger.error("[WS_CORREO] Error al enviar correo a: {} | Status: {} | Respuesta: {}",
                        destinatario, response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (HttpClientErrorException e) {
            logger.error("[WS_CORREO] Error HTTP {} al enviar correo a {} | Body: {} | apiKey_sys: {}...",
                    e.getStatusCode(), destinatario, e.getResponseBodyAsString(),
                    properties.getApiKeySys() != null && properties.getApiKeySys().length() > 8
                            ? properties.getApiKeySys().substring(0, 8) : "NULL");
            return false;
        } catch (HttpServerErrorException e) {
            logger.error("[WS_CORREO] Error del servidor {} al enviar correo a {} | Body: {}",
                    e.getStatusCode(), destinatario, e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            logger.error("[WS_CORREO] Excepción al enviar correo a {}: {}", destinatario, e.getMessage(), e);
            return false;
        }
    }

    public boolean enviarCorreo(String destinatario, String asunto, String htmlContent) {
        return enviarCorreo(destinatario, asunto, htmlContent, null);
    }

    public boolean enviarCorreoConAdjunto(String destinatario, String asunto, String htmlContent,
                                          byte[] adjuntoBytes, String adjuntoNombre, List<String> cc) {
        try {
            destinatario = filtrarDestinatario(destinatario);
            Map<String, Object> parametrosMap = new HashMap<>();
            parametrosMap.put("cuerpoHtml", htmlContent);
            String parametrosJson = objectMapper.writeValueAsString(parametrosMap);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add("apiKey_sys", properties.getApiKeySys());
            headers.add("destinatario", encodeHeaderUtf8(destinatario));
            headers.add("usuarioNombre", encodeHeaderUtf8(properties.getUsuarioNombre()));
            headers.add("asunto", encodeHeaderUtf8(asunto));

            if (cc != null && !cc.isEmpty()) {
                headers.add("cc", encodeHeaderUtf8(String.join(",", cc)));
            }

            HttpHeaders textPartHeaders = new HttpHeaders();
            textPartHeaders.setContentType(new org.springframework.http.MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("apiKey_sys", new HttpEntity<>(properties.getApiKeySys(), textPartHeaders));
            body.add("destinatario", new HttpEntity<>(destinatario, textPartHeaders));
            body.add("usuarioNombre", new HttpEntity<>(properties.getUsuarioNombre(), textPartHeaders));
            body.add("asunto", new HttpEntity<>(asunto, textPartHeaders));
            body.add("parametros", new HttpEntity<>(parametrosJson, textPartHeaders));

            if (cc != null && !cc.isEmpty()) {
                for (String correo : cc) {
                    body.add("cc", new HttpEntity<>(correo, textPartHeaders));
                }
            }

            if (adjuntoBytes != null && adjuntoNombre != null) {
                org.springframework.core.io.ByteArrayResource adjuntoResource =
                        new org.springframework.core.io.ByteArrayResource(adjuntoBytes) {
                            @Override
                            public String getFilename() {
                                return adjuntoNombre;
                            }
                        };
                body.add("adjuntos", adjuntoResource);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            logger.info("[WS_CORREO] Enviando correo con adjunto a: {} | Asunto: {} | Adjunto: {}",
                    destinatario, asunto, adjuntoNombre);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.getUrl(), requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("[WS_CORREO] Correo con adjunto enviado exitosamente a: {}", destinatario);
                return true;
            } else {
                logger.error("[WS_CORREO] Error al enviar correo con adjunto a: {} | Status: {} | Respuesta: {}",
                        destinatario, response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (HttpClientErrorException e) {
            logger.error("[WS_CORREO] Error HTTP {} al enviar correo con adjunto a {} | Body: {}",
                    e.getStatusCode(), destinatario, e.getResponseBodyAsString());
            return false;
        } catch (HttpServerErrorException e) {
            logger.error("[WS_CORREO] Error del servidor {} al enviar correo con adjunto a {} | Body: {}",
                    e.getStatusCode(), destinatario, e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            logger.error("[WS_CORREO] Excepción al enviar correo con adjunto a {}: {}",
                    destinatario, e.getMessage(), e);
            return false;
        }
    }

    public boolean enviarCorreoConPlantillaYAdjunto(String destinatario, String asunto, String plantilla,
                                                    Map<String, Object> variables, byte[] adjuntoBytes,
                                                    String adjuntoNombre, List<String> cc) {
        try {
            destinatario = filtrarDestinatario(destinatario);
            String apiKeyTemp = obtenerApiKeyTemp(plantilla);
            if (apiKeyTemp == null) {
                logger.error("[WS_CORREO] No se encontró apiKey_temp para la plantilla: {}", plantilla);
                return false;
            }

            String parametrosJson = objectMapper.writeValueAsString(
                    variables != null ? variables : new HashMap<>());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add("apiKey_sys", properties.getApiKeySys());
            headers.add("apiKey_temp", apiKeyTemp);
            headers.add("destinatario", encodeHeaderUtf8(destinatario));
            headers.add("usuarioNombre", encodeHeaderUtf8(properties.getUsuarioNombre()));
            headers.add("asunto", encodeHeaderUtf8(asunto));

            if (cc != null && !cc.isEmpty()) {
                headers.add("cc", encodeHeaderUtf8(String.join(",", cc)));
            }

            HttpHeaders textPartHeaders = new HttpHeaders();
            textPartHeaders.setContentType(new org.springframework.http.MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("apiKey_sys", new HttpEntity<>(properties.getApiKeySys(), textPartHeaders));
            body.add("apiKey_temp", new HttpEntity<>(apiKeyTemp, textPartHeaders));
            body.add("destinatario", new HttpEntity<>(destinatario, textPartHeaders));
            body.add("usuarioNombre", new HttpEntity<>(properties.getUsuarioNombre(), textPartHeaders));
            body.add("asunto", new HttpEntity<>(asunto, textPartHeaders));
            body.add("parametros", new HttpEntity<>(parametrosJson, textPartHeaders));

            if (cc != null && !cc.isEmpty()) {
                for (String correo : cc) {
                    body.add("cc", new HttpEntity<>(correo, textPartHeaders));
                }
            }

            if (adjuntoBytes != null && adjuntoNombre != null) {
                org.springframework.core.io.ByteArrayResource adjuntoResource =
                        new org.springframework.core.io.ByteArrayResource(adjuntoBytes) {
                            @Override
                            public String getFilename() {
                                return adjuntoNombre;
                            }
                        };
                body.add("adjuntos", adjuntoResource);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            logger.info("[WS_CORREO] Enviando correo con plantilla y adjunto | URL: {} | plantilla: {} | destinatario: {} | asunto: {} | adjunto: {}",
                    properties.getUrl(), plantilla, destinatario, asunto, adjuntoNombre);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.getUrl(), requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("[WS_CORREO] Correo con plantilla y adjunto enviado exitosamente a: {} | Respuesta: {}",
                        destinatario, response.getBody());
                return true;
            } else {
                logger.error("[WS_CORREO] Error al enviar correo con plantilla y adjunto a: {} | Status: {} | Respuesta: {}",
                        destinatario, response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (HttpClientErrorException e) {
            logger.error("[WS_CORREO] Error HTTP {} al enviar correo con plantilla y adjunto a {} | Body: {}",
                    e.getStatusCode(), destinatario, e.getResponseBodyAsString());
            return false;
        } catch (HttpServerErrorException e) {
            logger.error("[WS_CORREO] Error del servidor {} al enviar correo con plantilla y adjunto a {} | Body: {}",
                    e.getStatusCode(), destinatario, e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            logger.error("[WS_CORREO] Excepción al enviar correo con plantilla y adjunto a {}: {}",
                    destinatario, e.getMessage(), e);
            return false;
        }
    }

    public boolean enviarCorreoConPlantilla(String destinatario, String asunto, String plantilla, Map<String, Object> variables, List<String> cc) {
        try {
            destinatario = filtrarDestinatario(destinatario);
            String apiKeyTemp = obtenerApiKeyTemp(plantilla);
            if (apiKeyTemp == null) {
                logger.info("[WS_CORREO] No hay apiKey_temp para la plantilla '{}'. Usando renderizado local...", plantilla);
                String htmlLocal = cargarYRenderizarPlantillaLocal(plantilla, variables);
                if (htmlLocal != null) {
                    return this.enviarCorreo(destinatario, asunto, htmlLocal, cc);
                }
                logger.error("[WS_CORREO] No se encontró apiKey_temp ni plantilla local para: {}", plantilla);
                return false;
            }

            String parametrosJson = objectMapper.writeValueAsString(
                    variables != null ? variables : new HashMap<>());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add("apiKey_sys", properties.getApiKeySys());
            headers.add("apiKey_temp", apiKeyTemp);
            headers.add("destinatario", encodeHeaderUtf8(destinatario));
            headers.add("usuarioNombre", encodeHeaderUtf8(properties.getUsuarioNombre()));
            headers.add("asunto", encodeHeaderUtf8(asunto));

            if (cc != null && !cc.isEmpty()) {
                headers.add("cc", encodeHeaderUtf8(String.join(",", cc)));
            }

            HttpHeaders textPartHeaders = new HttpHeaders();
            textPartHeaders.setContentType(new org.springframework.http.MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("apiKey_sys", new HttpEntity<>(properties.getApiKeySys(), textPartHeaders));
            body.add("apiKey_temp", new HttpEntity<>(apiKeyTemp, textPartHeaders));
            body.add("destinatario", new HttpEntity<>(destinatario, textPartHeaders));
            body.add("usuarioNombre", new HttpEntity<>(properties.getUsuarioNombre(), textPartHeaders));
            body.add("asunto", new HttpEntity<>(asunto, textPartHeaders));
            body.add("parametros", new HttpEntity<>(parametrosJson, textPartHeaders));

            if (cc != null && !cc.isEmpty()) {
                for (String correo : cc) {
                    body.add("cc", new HttpEntity<>(correo, textPartHeaders));
                }
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String apiKeyMasked = properties.getApiKeySys() != null && properties.getApiKeySys().length() > 8
                    ? properties.getApiKeySys().substring(0, 8) + "..." : "NULL";
            logger.info("[WS_CORREO] Enviando correo con plantilla | URL: {} | plantilla: {} | apiKey_temp: {} | destinatario: {} | asunto: {} | parametros: {}",
                    properties.getUrl(), plantilla, apiKeyTemp, destinatario, asunto, parametrosJson);

            ResponseEntity<String> response = null;
            int maxIntentos = 2;
            for (int intento = 1; intento <= maxIntentos; intento++) {
                try {
                    response = restTemplate.postForEntity(
                            properties.getUrl(), requestEntity, String.class);
                    break;
                } catch (org.springframework.web.client.ResourceAccessException ex) {
                    if (intento < maxIntentos) {
                        logger.warn("[WS_CORREO] Timeout o retraso de red en intento {}/{} al enviar a {}: {}. Reintentando...",
                                intento, maxIntentos, destinatario, ex.getMessage());
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        throw ex;
                    }
                }
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("[WS_CORREO] Correo con plantilla enviado exitosamente a: {} | Respuesta: {}",
                        destinatario, response.getBody());
                return true;
            } else {
                logger.error("[WS_CORREO] Error al enviar correo con plantilla a: {} | Status: {} | Respuesta: {}",
                        destinatario, response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (HttpClientErrorException e) {
            logger.error("[WS_CORREO] Error HTTP {} al enviar correo con plantilla a {} | Body: {}",
                    e.getStatusCode(), destinatario, e.getResponseBodyAsString());
            return false;
        } catch (HttpServerErrorException e) {
            logger.error("[WS_CORREO] Error del servidor {} al enviar correo con plantilla a {} | Body: {}",
                    e.getStatusCode(), destinatario, e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            logger.error("[WS_CORREO] Excepción al enviar correo con plantilla a {}: {}", destinatario, e.getMessage(), e);
            return false;
        }
    }

    private String obtenerApiKeyTemp(String plantilla) {
        if (plantilla == null) return null;
        String key = properties.getTemplateKeys().get(plantilla);
        if (key == null && "asignacion-template".equals(plantilla)) {
            key = properties.getTemplateKeys().get("reasignacion-template");
        }
        if (key == null) {
            logger.warn("[WS_CORREO] No hay apiKey_temp configurada para la plantilla '{}'. Keys disponibles: {}",
                    plantilla, properties.getTemplateKeys().keySet());
        }
        return key;
    }

    private static String encodeHeaderUtf8(String value) {
        if (value == null) return null;
        return new String(value.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                           java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    private String cargarYRenderizarPlantillaLocal(String plantilla, Map<String, Object> variables) {
        try (java.io.InputStream is = getClass().getResourceAsStream("/templates/" + plantilla + ".html")) {
            if (is == null) {
                logger.warn("[WS_CORREO] No existe archivo local /templates/{}.html", plantilla);
                return null;
            }
            String html = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            if (variables != null) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    if (entry.getValue() != null && !(entry.getValue() instanceof Map) && !(entry.getValue() instanceof List)) {
                        String key = "{{" + entry.getKey() + "}}";
                        html = html.replace(key, entry.getValue().toString());
                    }
                }
            }
            return html;
        } catch (Exception e) {
            logger.error("[WS_CORREO] Error al renderizar plantilla local {}: {}", plantilla, e.getMessage(), e);
            return null;
        }
    }

    private String filtrarDestinatario(String destinatario) {
        String whitelist = properties.getTestWhitelist();
        if (whitelist != null && !whitelist.trim().isEmpty()) {
            logger.warn("[WS_CORREO] MODO DE PRUEBAS ACTIVO | Interceptando correo original para [{}] -> Redirigiendo a whitelist de pruebas: [{}]",
                    destinatario, whitelist);
            return whitelist.trim();
        }
        return destinatario;
    }

    public String getApiKey() {
        return properties.getApiKeySys();
    }

    public String getEmisor() {
        return properties.getEmisor();
    }

    public String getUsuarioNombre() {
        return properties.getUsuarioNombre();
    }

    public String getWsCorreoUrl() {
        return properties.getUrl();
    }
}
