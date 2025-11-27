package com.taskmanager.backend.dto;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectDetailResponse {
    private Integer id;
    private String name;
    private String code;
    private String description;
    private String status; // ACTIVE, ARCHIVED...
    
    // Thông tin người tạo
    private UserSummaryDto owner;
    
    // Danh sách thành viên (Quan trọng để Assign Task)
    private List<MemberDto> members;
    
    private String workflowName;
    private LocalDateTime createdAt;

    // --- Inner Classes cho gọn ---
    
    @Data
    @Builder
    public static class UserSummaryDto {
        private Integer id;
        private String fullName;
        private String email;
        private String avatarUrl;
    }

    @Data
    @Builder
    public static class MemberDto {
        private Integer userId;
        private String fullName;
        private String email;
        private String avatarUrl;
        private String role; // PRODUCT_OWNER, DEVELOPER...
    }
}