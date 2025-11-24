package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.taskmanager.backend.enums.ProjectStatus; // Nhớ tạo Enum này

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Getter // ✅ 1. Bỏ @Data, dùng Getter/Setter để tránh StackOverflow
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    // ✅ 2. Code dự án nên viết hoa (VD: "JIRA", "BE") và unique
    @Column(unique = true, nullable = false, length = 10)
    private String code; 

    @Column(columnDefinition = "TEXT")
    private String description;

    // --- MỐI QUAN HỆ (RELATIONSHIPS) ---
    
    // ✅ 3. QUAN TRỌNG: Luôn dùng LAZY cho owner
    @ManyToOne(fetch = FetchType.LAZY, optional = false) 
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // ✅ 3. QUAN TRỌNG: Luôn dùng LAZY cho workflow
    @ManyToOne(fetch = FetchType.LAZY, optional = false) 
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    // -----------------------------------

    private LocalDate startDate;
    private LocalDate endDate;
    
    // ✅ 4. Dùng Enum quản lý trạng thái
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status; 

    // ✅ 5. Tự động quản lý ngày tạo/ngày sửa
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}