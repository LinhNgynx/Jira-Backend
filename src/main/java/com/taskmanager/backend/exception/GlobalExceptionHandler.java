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

    // 1. Xử lý lỗi KHÔNG TÌM THẤY (404 Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    // 2. Xử lý lỗi LOGIC NGHIỆP VỤ (400 Bad Request)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    // 🔥 3. [MỚI] Xử lý lỗi KHÔNG CÓ QUYỀN (403 Forbidden)
    // Đây là cái cần thiết cho SprintValidator (ActionNotAllowedException)
    @ExceptionHandler(ActionNotAllowedException.class)
    public ResponseEntity<?> handleActionNotAllowed(ActionNotAllowedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Action Not Allowed", ex.getMessage());
    }

    // 4. Xử lý lỗi VALIDATION (400 Bad Request - Form không hợp lệ)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 5. Xử lý lỗi AUTH (401 Unauthorized - Sai pass/token)
    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<?> handleAuthenticationException(Exception ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "Email hoặc mật khẩu không chính xác");
    }

    // 6. Xử lý lỗi HỆ THỐNG (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex) {
        // Nên log lỗi ra console để dev biết đường sửa
        ex.printStackTrace(); 
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Đã có lỗi xảy ra, vui lòng thử lại sau.");
    }

    // --- HELPER METHOD CHO GỌN CODE ---
    private ResponseEntity<Map<String, String>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        body.put("message", message);
        // body.put("timestamp", LocalDateTime.now().toString()); // Có thể thêm nếu thích
        return ResponseEntity.status(status).body(body);
    }
}