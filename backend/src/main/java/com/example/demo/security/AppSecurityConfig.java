package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class AppSecurityConfig {
  @Bean
  SecurityFilterChain securityConfig(HttpSecurity http) throws Exception {
    return http
      .cors(Customizer.withDefaults())
      .authorizeHttpRequests((auth) -> auth
        .requestMatchers("/api/**").hasRole("USER")
        .anyRequest().permitAll())
      .httpBasic(Customizer.withDefaults())
      .build();
  }
  @Bean
  public UserDetailsService userDetailsService() {
    return new CustomUserDetailsService();
  }
}
