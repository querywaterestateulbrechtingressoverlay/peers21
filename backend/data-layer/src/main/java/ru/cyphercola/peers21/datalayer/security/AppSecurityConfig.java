package ru.cyphercola.peers21.datalayer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class AppSecurityConfig {
  @Bean
  SecurityFilterChain securityConfig(HttpSecurity http) throws Exception {
    return http
      .cors(Customizer.withDefaults())
      .authorizeHttpRequests((auth) -> auth
        .requestMatchers(HttpMethod.GET, "/api/**").hasRole("USER")
        .requestMatchers(HttpMethod.PUT,"/api/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE,"/api/**").hasRole("ADMIN")
        .anyRequest().permitAll())
      .httpBasic(Customizer.withDefaults())
      .build();
  }
  @Bean
  public UserDetailsService userDetailsService() {
    return new CustomUserDetailsService();
  }
}
