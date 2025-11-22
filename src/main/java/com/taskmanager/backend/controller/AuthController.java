package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.RegisterRequest;
import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.service.AuthService;
import jakarta.validation.Valid; // Import cái này
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    // Thêm @Valid để kích hoạt kiểm tra DTO
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User createdUser = authService.register(request);
        
        // Trả về JSON đẹp đẽ thay vì String
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đăng ký thành công");
        response.put("userId", createdUser.getId());
        response.put("email", createdUser.getEmail());

        // Trả về status 201 CREATED
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}