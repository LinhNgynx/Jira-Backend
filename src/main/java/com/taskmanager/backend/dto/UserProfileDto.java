package com.taskmanager.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private Integer id;
    private String email;
    private String fullName;
    private List<String> roles; // VD: ["ROLE_USER", "ROLE_PROJECT_ADMIN"]
}
