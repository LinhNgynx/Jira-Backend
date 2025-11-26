package com.taskmanager.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponse {
    private Integer id;
    private String name;
    private String code;
    private String description;
    private String ownerName;     // Tên người tạo
    private String workflowName;  // Tên quy trình đang áp dụng
    private String status;
    private LocalDateTime createdAt;
}