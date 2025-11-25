package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.AuthResponse;
import com.taskmanager.backend.dto.LoginRequest;
import com.taskmanager.backend.dto.RegisterRequest;
import com.taskmanager.backend.dto.UserProfileDto;
import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.repository.UserRepository;
import com.taskmanager.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    // 1. API ĐĂNG KÝ
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User createdUser = authService.register(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đăng ký thành công");
        response.put("userId", createdUser.getId());
        response.put("email", createdUser.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. API ĐĂNG NHẬP (Cái bạn đang thiếu)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Gọi Service để xử lý đăng nhập & lấy Token
            AuthResponse authResponse = authService.login(request);
            
            // Trả về Token cho Client
            return ResponseEntity.ok(authResponse);
            
        } catch (BadCredentialsException ex) {
            // Nếu sai mật khẩu/email -> Trả về 401 Unauthorized
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email hoặc mật khẩu không chính xác");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // 3. API LẤY PROFILE CỦA USER HIỆN TẠI
    @GetMapping("/users/me")
    public ResponseEntity<UserProfileDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User not authenticated");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Lấy danh sách roles từ ProjectMembers
        java.util.List<String> roles = user.getProjectMembers().stream()
                .map(pm -> pm.getRole().getName().toString())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        
        UserProfileDto profileDto = UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .build();
        
        return ResponseEntity.ok(profileDto);
    }
}