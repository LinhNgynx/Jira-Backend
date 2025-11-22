package com.taskmanager.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt chống CSRF để test API dễ dàng
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Cho phép TẤT CẢ các request (không cần login)
            );
        return http.build();
    }
}