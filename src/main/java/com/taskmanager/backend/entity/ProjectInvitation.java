package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.taskmanager.backend.enums.InvitationStatus; // Nhớ tạo Enum này

import java.time.Instant;

@Entity
@Table(name = "project_invitations")
@Getter // ✅ Dùng Getter/Setter thay vì @Data để tránh lỗi StackOverflow
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Dùng Long là tốt cho bảng có thể nhiều record

    // ✅ LUÔN dùng LAZY cho các quan hệ
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_user_id") // Nullable (cho người chưa có tài khoản)
    private User inviteeUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private ProjectRole role;

    @Column(nullable = false, unique = true)
    private String token;

    // ✅ NÊN dùng Enum thay vì String để tránh lỗi gõ sai (typo)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status; 

    private Instant expiredAt;

    // ✅ Dùng Annotation của Hibernate cho gọn code
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
    
    // Đã xóa @PrePersist và @PreUpdate thủ công vì đã dùng Annotation trên
}