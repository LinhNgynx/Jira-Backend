package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.NotificationResponse;
import com.taskmanager.backend.entity.Notification;
import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.enums.NotificationType;
import com.taskmanager.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepo;
    
    // 🔥 Đây là "cái loa" để bắn tin realtime
    private final SimpMessagingTemplate messagingTemplate; 

    /**
     * Hàm này thực hiện 2 việc:
     * 1. Lưu thông báo vào Database (để F5 vẫn thấy).
     * 2. Bắn WebSocket tới người nhận (để hiện popup ngay lập tức).
     */
    @Transactional
    public void createAndSendNotification(User sender, User recipient, NotificationType type, Long referenceId, String content) {
        
        // BƯỚC 1: Lưu vào Database
        Notification notification = Notification.builder()
                .sender(sender)
                .recipient(recipient)
                .type(type)
                .referenceId(referenceId)
                .content(content)
                .isRead(false)
                .build();
        
        Notification savedNoti = notificationRepo.save(notification);

        // BƯỚC 2: Chuẩn bị dữ liệu để bắn Socket (Map Entity -> DTO)
        NotificationResponse response = NotificationResponse.builder()
                .id(savedNoti.getId().longValue())
                .content(savedNoti.getContent())
                .type(savedNoti.getType())
                .referenceId(savedNoti.getReferenceId())
                .isRead(savedNoti.isRead())
                .createdAt(savedNoti.getCreatedAt())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .senderAvatar(sender.getAvatarUrl())
                .build();

        // BƯỚC 3: Bắn tin Realtime
        // Gửi tới kênh: /user/{email}/queue/notifications
        // Frontend user B đang subscribe kênh này sẽ nhận được ngay.
        try {
            messagingTemplate.convertAndSendToUser(
                    recipient.getEmail(),     // Định danh người nhận (Username/Email)
                    "/queue/notifications",   // Tên hàng đợi
                    response                  // Dữ liệu gửi đi
            );
            log.info("Realtime notification sent to {}", recipient.getEmail());
        } catch (Exception e) {
            // Nếu lỗi bắn socket thì chỉ log lại, không làm rollback việc lưu DB
            log.error("Failed to send realtime notification", e);
        }
    }
}