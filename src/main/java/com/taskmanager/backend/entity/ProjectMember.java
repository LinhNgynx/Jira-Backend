package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_members", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "user_id"}) // ✅ Đảm bảo 1 user không vào 1 dự án 2 lần
    }
)
@Getter // ✅ Thay @Data bằng @Getter + @Setter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ⚠️ QUAN TRỌNG 1: Luôn dùng LAZY cho @ManyToOne để tránh query thừa
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_role_id", nullable = false)
    private ProjectRole role;

    // ✅ BỔ SUNG: Cột ngày tham gia (như trong DBML)
    @Column(name = "joined_at", updatable = false)
    @CreationTimestamp // Tự động lấy giờ hiện tại khi insert
    private LocalDateTime joinedAt;
}