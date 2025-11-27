package com.taskmanager.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BacklogResponse {
    private Integer projectId;
    private String projectName;
    
    // Danh sách Sprint (Active & Planned)
    private List<SprintDto> sprints;
    
    // Danh sách Backlog (Task chưa vào Sprint)
    private List<TaskDto> backlogTasks;

    // --- Inner Class: Sprint ---
    @Data
    @Builder
    public static class SprintDto {
        private Integer id;
        private String name;        
        private String status;      // ACTIVE, PLANNED
        private String startDate;   
        private String endDate;    
        private Integer totalIssues; 
        private List<TaskDto> tasks; 
    }

    // --- Inner Class: Task ---
    @Data
    @Builder
    public static class TaskDto {
        private Integer id;
        private String key;          // ✅ QUAN TRỌNG: "BE-10", "BE-11" (Dùng taskIndex)
        private String title;        
        private String priority;     
        private Integer storyPoints; 
        
        private String issueTypeIcon; // Icon Bug/Story
        
        private String statusName;
        private String statusColor;
        
        private String assigneeAvatar; // Avatar người làm
    }
}