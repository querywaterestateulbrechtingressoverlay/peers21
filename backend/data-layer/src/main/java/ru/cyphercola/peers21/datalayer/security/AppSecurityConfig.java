package ru.cyphercola.peers21.datalayer.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
public class AppSecurityConfig {
  @Autowired
  Environment environment;
  @Bean
  SecurityFilterChain securityConfig(HttpSecurity http) throws Exception {
    HttpSecurity h = http
      .cors(Customizer.withDefaults())
      .authorizeHttpRequests((auth) -> auth
        .requestMatchers(HttpMethod.GET, "/error").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/error").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyAuthority("USER", "ADMIN")
        .requestMatchers(HttpMethod.PUT,"/api/**").hasAuthority("ADMIN")
        .requestMatchers(HttpMethod.DELETE,"/api/**").hasAuthority("ADMIN")
        .anyRequest().permitAll())
      .httpBasic(Customizer.withDefaults());
      // CSRF - NEEDS A PROPER FIX
    // if (List.of(environment.getActiveProfiles()).contains("test")) {
      h.csrf(AbstractHttpConfigurer::disable);
    // }
    return h.build();
  }
}
