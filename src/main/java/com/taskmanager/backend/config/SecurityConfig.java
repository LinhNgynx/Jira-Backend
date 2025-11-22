package com.taskmanager.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Mã hóa mật khẩu an toàn
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt CSRF để test API dễ dàng
            .authorizeHttpRequests(auth -> auth
                // Cho phép truy cập tự do vào các API bắt đầu bằng /api/auth/
                .requestMatchers("/api/auth/**").permitAll()
                // Các API khác bắt buộc phải đăng nhập
                .anyRequest().authenticated()
            );
        return http.build();
    }
}