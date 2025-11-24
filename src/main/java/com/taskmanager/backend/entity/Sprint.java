package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.taskmanager.backend.enums.SprintStatus; // Nhớ tạo Enum này

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sprints")
@Getter // ✅ 1. Bỏ @Data, dùng Getter/Setter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    // ✅ 2. Dùng LAZY để tránh query thừa (N+1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private LocalDate startDate;
    private LocalDate endDate;
    
    // ✅ 3. Dùng Enum để quản lý trạng thái chặt chẽ
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SprintStatus status; 

    // ✅ 4. Tự động timestamp
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}