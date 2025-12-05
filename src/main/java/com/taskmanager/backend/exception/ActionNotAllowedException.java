package com.taskmanager.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Cách 1: Dùng Annotation (Nhanh gọn) -> Tự động trả về 403 Forbidden
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ActionNotAllowedException extends RuntimeException {
    public ActionNotAllowedException(String message) {
        super(message);
    }
}