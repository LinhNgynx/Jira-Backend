package com.taskmanager.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Xử lý lỗi KHÔNG TÌM THẤY (404 Not Found) - VD: Sai ID dự án, sai ID Task
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 2. Xử lý lỗi LOGIC NGHIỆP VỤ (400 Bad Request) - VD: Trùng tên, sai quy tắc cha con
    @ExceptionHandler(BusinessException.class) // Hoặc giữ RuntimeException nếu lười sửa Service
    public ResponseEntity<?> handleBusinessException(BusinessException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 3. Giữ nguyên cái Validation của bạn (Rất tốt)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 4. Giữ nguyên cái Auth của bạn (Rất tốt)
    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<?> handleAuthenticationException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Unauthorized");
        error.put("message", "Email hoặc mật khẩu không chính xác");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 5. QUAN TRỌNG: Xử lý lỗi hệ thống không mong muốn (500 Internal Server Error)
    // Cái này hứng tất cả các lỗi còn lại (NullPointer, SQL Error...)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", "Đã có lỗi xảy ra, vui lòng thử lại sau."); 
        // ex.printStackTrace(); // Log ra console để dev sửa, đừng trả về cho user
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}