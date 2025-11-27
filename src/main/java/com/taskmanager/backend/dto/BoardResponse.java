package com.taskmanager.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BoardResponse {
    private Integer projectId;
    private List<ColumnDto> columns; // Danh sách các cột (To Do, Done...)

    @Data
    @Builder
    public static class ColumnDto {
        private Integer stepId;
        private String statusName;   // Tên cột (VD: "To Do")
        private String colorCode;    // Màu cột
        private Integer order;       // Thứ tự hiển thị
        private List<TaskSummaryDto> tasks; // Danh sách task trong cột này
    }

    @Data
    @Builder
    public static class TaskSummaryDto {
        private Integer id;
        private String title;
        private String assigneeAvatar; // Avatar người làm
        private String priority;       // Màu ưu tiên (Đỏ/Vàng/Xanh)
        private String issueTypeIcon;  // Icon Bug/Story
    }
}
