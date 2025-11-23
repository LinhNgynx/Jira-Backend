package com.taskmanager.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException; // Thêm import này cho JwtException
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication; // Thêm import này để dùng Authentication
import org.springframework.security.core.userdetails.UserDetails; // Thêm import này
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import org.springframework.security.core.userdetails.User; // Dùng cho ví dụ Authentication

@Component
public class JwtUtils {

    @Value("${jwt.secret:change_me_please_very_long_secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;

    private Key getSigningKey() {
        String secret = jwtSecret == null ? "" : jwtSecret.trim();
        byte[] keyBytes;
        try {
            // Try decode as Base64 (recommended)
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            // Not Base64; use raw UTF-8 bytes
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        // Keys.hmacShaKeyFor validates key length and builds SecretKey
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // SỬA: Dùng đối tượng Authentication chuẩn để lấy username
    public String generateToken(Authentication authentication) {
        // Lấy thông tin UserDetails từ Authentication
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        
        // SỬA: Chuyển từ setSubject, setIssuedAt, setExpiration sang cú pháp ngắn gọn
        return Jwts.builder()
                .subject(userPrincipal.getUsername()) // FIX DEPRECATION
                .issuedAt(now) // FIX DEPRECATION
                .expiration(expiry) // FIX DEPRECATION
                .signWith(getSigningKey()) // Cập nhật: Không cần chỉ định SignatureAlgorithm nếu dùng Keys.hmacShaKeyFor
                .compact();
    }

    // SỬA: getUsernameFromToken phải dùng interface ClaimsJws, không phải Jws (cũ)
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Log lỗi nếu cần
            return false;
        }
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}