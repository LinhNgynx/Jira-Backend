package com.taskmanager.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String email; // or "username" if you prefer
    @NotBlank
    private String password;
}