package com.taskmanager.backend.dto;

import com.taskmanager.backend.enums.TaskPriority;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private TaskPriority priority;
    private Integer storyPoints;
    private LocalDate startDate;
    private LocalDate dueDate;
}