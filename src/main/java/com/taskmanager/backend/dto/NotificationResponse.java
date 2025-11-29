package com.taskmanager.backend.dto;

import com.taskmanager.backend.enums.NotificationType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String content;
    private NotificationType type;      // TASK_ASSIGNED, PROJECT_INVITE...
    private Long referenceId;           // ID của Task hoặc ProjectInvitation
    private boolean isRead;
    private LocalDateTime createdAt;
    
    // Thông tin người gửi (để hiện avatar bên cạnh thông báo)
    private Integer senderId;
    private String senderName;
    private String senderAvatar;
}