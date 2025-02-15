package ru.cyphercola.peers21.datalayer.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
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
        .requestMatchers(HttpMethod.GET, "/ error").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/error").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/**").hasRole("USER")
        .requestMatchers(HttpMethod.PUT,"/api/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE,"/api/**").hasRole("ADMIN")
        .anyRequest().permitAll())
      .httpBasic(Customizer.withDefaults());
    if (List.of(environment.getActiveProfiles()).contains("test")) {
      h.csrf(AbstractHttpConfigurer::disable);
    }
    return h.build();
  }
  @Bean
  public UserDetailsService userDetailsService() {
    return new CustomUserDetailsService();
  }
}
