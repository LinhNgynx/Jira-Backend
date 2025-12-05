package com.taskmanager.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BoardResponse {
    
    // Thông tin Sprint đang hiển thị
    private Integer sprintId;
    private String sprintName;
    
    // Danh sách các cột (Swimlanes)
    private List<ColumnResponse> columns;

    // --- INNER CLASS: Định nghĩa cấu trúc 1 Cột ---
    @Data
    @Builder
    public static class ColumnResponse {
        private Integer statusId;       // ID trạng thái (để gửi lên API khi kéo thả)
        private String statusName;      // Tên hiển thị (VD: "In Progress")
        private String statusCategory;  // Loại (TO_DO, DONE...) -> Để FE tô màu cột
        private Integer order;          // Thứ tự sắp xếp cột
        private List<TaskPreviewDto> tasks; // Danh sách task trong cột này
    }

    // --- INNER CLASS: Định nghĩa cấu trúc 1 Task (Thẻ nhỏ trên bảng) ---
    @Data
    @Builder
    public static class TaskPreviewDto {
        private Integer id;
        private String title;
        private String key;             // Mã định danh (VD: "PROJ-101")
        private String priority;        // Độ ưu tiên (HIGH, MEDIUM...)
        private Integer storyPoints;    // Điểm độ khó
        private String issueTypeIcon;   // URL icon loại task (Bug, Story...)
        private String assigneeAvatar;  // URL avatar người làm (để hiện hình tròn nhỏ)
    }
}