package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.taskmanager.backend.enums.NotificationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Người nhận (Bắt buộc)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User recipient;

    // Người gửi (Có thể null nếu là thông báo hệ thống)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    // ❌ BỎ CÁI NÀY: private Task task; 
    // Vì thông báo có thể là về ProjectInvitation, không chỉ Task.

    // ✅ THAY BẰNG CÁI NÀY:
    @Column(name = "reference_id")
    private Long referenceId; 
    // Logic: 
    // - Nếu Type = TASK_ASSIGNED -> referenceId là taskId
    // - Nếu Type = PROJECT_INVITE -> referenceId là invitationId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "is_read")
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}