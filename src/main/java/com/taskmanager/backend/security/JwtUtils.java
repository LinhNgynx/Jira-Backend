package com.taskmanager.backend.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret:change_me_please_very_long_secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;

    // Helper: Tạo SecretKey chuẩn
    private SecretKey getSigningKey() {
        String secret = jwtSecret == null ? "" : jwtSecret.trim();
        byte[] keyBytes;
        try {
            // Ưu tiên giải mã Base64 (nếu key được cấu hình dạng Base64)
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            // Nếu không phải Base64, dùng byte thuần của chuỗi
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 1. Tạo Token
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey()) // JJWT tự động chọn thuật toán dựa trên độ dài key
                .compact();
    }

    // 2. Lấy Username từ Token (Sửa lại theo chuẩn 0.12.x)
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // Dùng verifyWith thay cho setSigningKey
                .build()
                .parseSignedClaims(token)    // Dùng parseSignedClaims thay cho parseClaimsJws
                .getPayload()                // Dùng getPayload thay cho getBody
                .getSubject();
    }

    // 3. Kiểm tra Token hợp lệ (Sửa lại theo chuẩn 0.12.x)
    public boolean validate(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey()) // Dùng verifyWith
                .build()
                .parseSignedClaims(token);   // Parse thử, nếu lỗi sẽ ném Exception
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Token hết hạn, sai chữ ký, hoặc cấu trúc hỏng
            return false;
        }
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}