package com.taskmanager.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken; 
    private String tokenType = "Bearer"; 
    private long expiresIn;
}