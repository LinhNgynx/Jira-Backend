package com.taskmanager.backend.dto; // Nên để trong package response

import com.taskmanager.backend.enums.TaskPriority;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class TaskResponse {
    private Integer id;
    private String key;         // JIRA-123
    private String title;
    private String description;
    private TaskPriority priority;
    private Integer storyPoints;
    private LocalDate dueDate;
    
    // Thông tin phụ rút gọn
    private String assigneeName; 
    private String assigneeAvatar;
    private String statusName;
    private String statusColor;
}