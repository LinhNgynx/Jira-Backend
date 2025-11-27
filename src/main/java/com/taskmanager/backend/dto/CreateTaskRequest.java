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
    private Integer issueTypeId; // 1=Epic, 2=Story...

    private TaskPriority priority; // HIGH, MEDIUM, LOW
    
    private Integer storyPoints;   // Điểm (1, 2, 3, 5...)

    private LocalDate startDate;
    private LocalDate dueDate;
    
    // Danh sách người làm (VD: [1, 5])
    private List<Integer> assigneeIds; 
    
    private Integer sprintId;     // Nếu muốn tạo xong nhét luôn vào Sprint
    private Integer parentTaskId; // Nếu tạo Subtask thì phải gửi ID cha
}