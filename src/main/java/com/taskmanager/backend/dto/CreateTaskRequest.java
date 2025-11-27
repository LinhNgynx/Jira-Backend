package com.taskmanager.backend.dto;

import com.taskmanager.backend.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateTaskRequest {
    @NotNull(message = "Project ID không được để trống")
    private Integer projectId;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;

    @NotNull(message = "Loại task (Issue Type) không được để trống")
    private Integer issueTypeId; // ID của Bug, Story...

    private TaskPriority priority; // HIGH, MEDIUM, LOW

    private LocalDate startDate;
    private LocalDate dueDate;
    
    // Danh sách ID người được giao việc (Vì TaskAssignee là OneToMany)
    private List<Integer> assigneeIds; 
    
    // Nếu tạo Subtask (để sau, giờ cứ để null)
    private Integer parentTaskId; 
}