package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.RegisterRequest;
import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional // <--- QUAN TRỌNG: Đảm bảo mọi thứ thành công hoặc thất bại cùng lúc
    public User register(RegisterRequest request) {
        // 1. Chuẩn hóa email (về chữ thường + cắt khoảng trắng)
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        // 2. Check trùng (Vẫn giữ check này để báo lỗi thân thiện)
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        // 3. Tạo User
        User newUser = User.builder()
                .email(normalizedEmail) // Lưu email đã chuẩn hóa
                .fullName(request.getFullName().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isSuperuser(false)
                .build();

        return userRepository.save(newUser);
    }
}