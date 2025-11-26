package com.taskmanager.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProjectRequest {
    @NotBlank(message = "Tên dự án không được để trống")
    private String name;

    @NotBlank(message = "Mã dự án không được để trống")
    @Size(min = 2, max = 10, message = "Mã dự án từ 2-10 ký tự")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Mã dự án chỉ chứa chữ hoa và số (VD: JIRA, PRJ1)")
    private String code;

    private String description;
}