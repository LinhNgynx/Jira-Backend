package com.taskmanager.backend.mapper;

import com.taskmanager.backend.dto.TaskResponse;
import com.taskmanager.backend.entity.Task;
import com.taskmanager.backend.entity.User;
import org.springframework.stereotype.Component;

@Component // Đánh dấu là Bean để Inject được
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }

        // Logic tính toán Key
        String taskKey = task.getProject().getCode() + "-" + task.getTaskIndex();

        // Logic lấy Assignee đầu tiên
        String assigneeName = null;
        String assigneeAvatar = null;
        
        // Kiểm tra null an toàn hơn
        if (task.getAssignees() != null && !task.getAssignees().isEmpty()) {
            User firstUser = task.getAssignees().get(0).getUser();
            if (firstUser != null) {
                assigneeName = firstUser.getFullName();
                assigneeAvatar = firstUser.getAvatarUrl();
            }
        }

        return TaskResponse.builder()
                .id(task.getId())
                .key(taskKey)
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .storyPoints(task.getStoryPoints())
                .dueDate(task.getDueDate())
                .statusName(task.getStatus() != null ? task.getStatus().getName() : null)
                .statusColor(task.getStatus() != null ? task.getStatus().getColorCode() : null)
                .assigneeName(assigneeName)
                .assigneeAvatar(assigneeAvatar)
                .build();
    }
}