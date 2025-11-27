package com.taskmanager.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectListResponse {
    private Integer id;
    private String name;
    private String code;       // Để hiển thị avatar chữ (VD: "BE")
    private String ownerName;  // Để biết ai là trùm
    private String myRole;     // Quan trọng: Để hiện icon chìa khóa nếu là ADMIN/OWNER
}