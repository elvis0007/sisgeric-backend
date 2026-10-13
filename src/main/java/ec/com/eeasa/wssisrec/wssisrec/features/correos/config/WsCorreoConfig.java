package ec.com.eeasa.wssisrec.wssisrec.features.correos.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WsCorreoConfig {

    private final WsCorreoProperties properties;

    public WsCorreoConfig(WsCorreoProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "wsCorreoRestTemplate")
    public RestTemplate wsCorreoRestTemplate() throws Exception {
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{"TLSv1.2"},
                null,
                NoopHostnameVerifier.INSTANCE);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(properties.getTimeoutConnect());
        factory.setReadTimeout(properties.getTimeoutRead());

        RestTemplate restTemplate = new RestTemplate(factory);

        List<HttpMessageConverter<?>> converters = new ArrayList<>(restTemplate.getMessageConverters());

        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof StringHttpMessageConverter) {
                converters.set(i, new StringHttpMessageConverter(StandardCharsets.UTF_8));
                break;
            }
        }

        // FIX TILDES: el FormHttpMessageConverter es el que realmente serializa
        // el body multipart/form-data (destinatario, asunto, parametros, etc.).
        // Por defecto su charset puede no ser UTF-8, lo que corrompe tildes/eñes
        // aunque los headers de cada parte (textPartHeaders) digan UTF-8.
        FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
        formConverter.setCharset(StandardCharsets.UTF_8);

        boolean reemplazado = false;
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof FormHttpMessageConverter) {
                converters.set(i, formConverter);
                reemplazado = true;
                break;
            }
        }
        if (!reemplazado) {
            converters.add(formConverter);
        }

        restTemplate.setMessageConverters(converters);

        return restTemplate;
    }
}