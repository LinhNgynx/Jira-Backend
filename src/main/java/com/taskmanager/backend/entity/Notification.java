package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.taskmanager.backend.enums.NotificationType; // Nhớ tạo Enum này

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter // ✅ 1. Vẫn quy tắc cũ: Bỏ @Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ✅ 2. Dùng LONG vì bảng này sẽ rất nhiều dòng (Integer dễ bị tràn)

    // Người nhận (To whom?)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User recipient; 

    // ✅ 3. Người gửi/tác động (Who did it?)
    // Để hiển thị avatar người đã comment/assign
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id") 
    private User sender;

    // Link đến Task liên quan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id") 
    private Task task;

    // ✅ 4. Loại thông báo (Để Frontend hiện icon khác nhau)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(columnDefinition = "TEXT")
    private String content; // Có thể null nếu dùng client-side rendering dựa trên Type

    @Builder.Default
    @Column(name = "is_read")
    private boolean isRead = false; // Dùng primitive boolean cho nhẹ

    @CreationTimestamp // ✅ 5. Hibernate tự lo
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}