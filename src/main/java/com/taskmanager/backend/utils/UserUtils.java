package com.taskmanager.backend.utils;

import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component // Đánh dấu để Spring quản lý (Bean)
@RequiredArgsConstructor
public class UserUtils {

    private final UserRepository userRepo;

    public User getCurrentUser() {
        // 1. Lấy email từ Context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Query DB
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User (Token không hợp lệ hoặc User bị xóa)"));
    }
}