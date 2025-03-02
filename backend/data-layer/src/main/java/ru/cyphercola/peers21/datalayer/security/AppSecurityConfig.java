package ru.cyphercola.peers21.datalayer.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers.hasScope;

@Configuration
@EnableWebSecurity
public class AppSecurityConfig {
  @Autowired
  SecurityProperties securityProperties;

  @Bean
  @Order(1)
  SecurityFilterChain securityConfigAuth(HttpSecurity http) throws Exception {
    return http
        .securityMatcher("/api/auth/**")
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            (auth) -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login").authenticated())
        .httpBasic(Customizer.withDefaults())
        .build();
  }
  @Bean
  @Order(2)
  SecurityFilterChain securityConfigBackend(HttpSecurity http) throws Exception {
    return http
        .securityMatcher("/api/backend/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests((auth) -> auth.requestMatchers("/api/backend/**").access(hasScope("api")))
        .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
        .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling((exceptions) -> exceptions
            .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
            .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
        )
        .build();
  }
  @Bean
  @Order(3)
  SecurityFilterChain securityConfigFrontend(HttpSecurity http) throws Exception {
    return http
      .cors(Customizer.withDefaults())
      .authorizeHttpRequests((auth) -> auth
        .requestMatchers(HttpMethod.GET, "/mockapi/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/ping").permitAll()
        .requestMatchers(HttpMethod.GET,"/api/**").access(hasScope("user"))
        .requestMatchers(HttpMethod.PUT,"/api/**").access(hasScope("admin"))
        .requestMatchers(HttpMethod.DELETE, "/api/**").access(hasScope("admin"))
        .anyRequest().permitAll())
      .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
      .exceptionHandling((exceptions) -> exceptions
        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
      )
      .build();
  }
  @Bean
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(securityProperties.rsaPublic()).build();
  }

  @Bean
  JwtEncoder jwtEncoder() {
    JWK jwk = new RSAKey.Builder(securityProperties.rsaPublic()).privateKey(securityProperties.rsaPrivate()).build();
    JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwks);
  }
}
