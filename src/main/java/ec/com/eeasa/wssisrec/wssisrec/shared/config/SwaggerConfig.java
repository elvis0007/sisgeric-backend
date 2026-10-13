package ec.com.eeasa.wssisrec.wssisrec.shared.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
  @Bean
  public OpenAPI openAPI() {
    Server localServer = new Server();
    localServer.setUrl("http://localhost:8080");
    localServer.setDescription("Servidor Local");

    Server devServer = new Server();
    devServer.setUrl("https://dev.tudominio.com");
    devServer.setDescription("Servidor de Desarrollo");

    List<Server> servers = new ArrayList<>();
    servers.add(localServer);
    servers.add(devServer);

    // Información de contacto
    Contact contact = new Contact()
        .name("EEASA")
        .email("soporte@eeasa.com.ec")
        .url("https://www.eeasa.com.ec");

    // Información de licencia
    License license = new License()
        .name("Apache 2.0")
        .url("https://www.apache.org/licenses/LICENSE-2.0.html");

    // Información general de la API
    Info info = new Info()
        .title("API REST EEASA")
        .version("1.0")
        .description("Documentación de la API REST de EEASA")
        .termsOfService("https://www.eeasa.com.ec/terms")
        .contact(contact)
        .license(license);

    // Configuración de seguridad (si usas JWT o Bearer token)
    SecurityScheme securityScheme = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .in(SecurityScheme.In.HEADER)
        .name("Authorization");

    SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

    // Construir y retornar la configuración de OpenAPI
    return new OpenAPI()
        .info(info)
        .servers(servers)
        .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
        .addSecurityItem(securityRequirement);
  }
}