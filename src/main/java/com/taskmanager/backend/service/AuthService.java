package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.AuthResponse; // Nhớ import cái này
import com.taskmanager.backend.dto.LoginRequest;
import com.taskmanager.backend.dto.RegisterRequest;
import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.repository.UserRepository;
import com.taskmanager.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    // --- HÀM REGISTER (CŨ) ---
    @Transactional
    public User register(RegisterRequest request) {
        // ... (Code cũ của bạn giữ nguyên) ...
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email đã tồn tại!");
        }
        User newUser = User.builder()
                .email(normalizedEmail)
                .fullName(request.getFullName().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isSuperuser(false)
                .build();
        return userRepository.save(newUser);
    }

    // --- HÀM LOGIN (MỚI - BẠN CẦN THÊM ĐOẠN NÀY) ---
    public AuthResponse login(LoginRequest request) {
        // 1. Chuẩn hóa email (tránh lỗi chữ hoa/thường)
        String email = request.getEmail().toLowerCase().trim();

        // 2. Gọi "Sếp tổng" AuthenticationManager để kiểm tra
        // Hàm này sẽ tự động:
        // - Gọi UserDetailsServiceImpl để tìm user
        // - Gọi PasswordEncoder để so sánh pass hash
        // - Nếu sai -> Ném lỗi BadCredentialsException
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        // 3. Nếu code chạy đến đây nghĩa là Đăng nhập thành công -> Tạo Token
        String token = jwtUtils.generateToken(authentication);

        // 4. Trả về kết quả
        return new AuthResponse(token, "Bearer", jwtUtils.getExpirationMs());
    }
}