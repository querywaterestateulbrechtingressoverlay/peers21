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
  @Bean
  SecurityFilterChain securityConfig(HttpSecurity http) throws Exception {
    return http
      .cors(Customizer.withDefaults())
      .authorizeHttpRequests((auth) -> auth
        .requestMatchers(HttpMethod.GET, "/mockapi/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/ping").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyAuthority("USER", "ADMIN")
        .requestMatchers(HttpMethod.PUT,"/api/**").hasAuthority("ADMIN")
        .requestMatchers(HttpMethod.DELETE,"/api/**").hasAuthority("ADMIN")
        .anyRequest().permitAll())
      .httpBasic(Customizer.withDefaults())
      .csrf(AbstractHttpConfigurer::disable)
      .build();
  }
}
