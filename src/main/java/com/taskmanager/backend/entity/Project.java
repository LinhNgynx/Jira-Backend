package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 10)
    private String code; // Ví dụ: "PRJ1"

    @Column(columnDefinition = "TEXT")
    private String description;

    // --- MỐI QUAN HỆ (RELATIONSHIPS) ---
    
    @ManyToOne // Một Project chỉ có một Owner
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne // Một Project chỉ chạy một Workflow
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    // -----------------------------------

    private LocalDate startDate;
    private LocalDate endDate;
    
    private String status; // ACTIVE, ARCHIVED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}