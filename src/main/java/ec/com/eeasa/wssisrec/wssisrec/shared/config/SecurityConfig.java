package ec.com.eeasa.wssisrec.wssisrec.shared.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import ec.com.eeasa.wssisrec.wssisrec.shared.utils.JWT.JWTFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Autowired
  private JWTFilter jwtFilter;

  @Bean
  public SecurityFilterChain secuuFilterChain(HttpSecurity http) throws Exception {
    http.csrf().disable().cors().configurationSource(CorsConfigurationSource()).and().authorizeRequests()
        .antMatchers(
            "/topic/**",
            "/app/**",
            "/api/auth",
            "/api/notificaciones/test/**",
            "/api/email/test-recordatorio-plan-accion/**",
            "/api/email/test-retraso-plan-accion/**",
            "/test-connection",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/v2/api-docs/**",
            "/swagger-ui.html",
            "/ws/**")
        .permitAll()
        .anyRequest().authenticated()
        .and().sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public CorsConfigurationSource CorsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        Arrays.asList("http://localhost:4200",
            "http://172.20.35.209:7001",
            "http://localhost:8080",
            "http://172.20.35.209:7001/sisrec", // Agrega el contexto
            "https://app.eeasa.com.ec",
            "https://app.eeasa.com.ec/sisrec",
            "http://172.20.35.212:7001/sisrec",
            "http://172.20.35.212:7001"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", configuration); // Cambiado de /api/** a /**
    return source;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
      throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}
