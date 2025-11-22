package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String priority; // HIGH, MEDIUM, LOW
    
    @Column(name = "story_points")
    private Integer storyPoints;

    // --- MỐI QUAN HỆ (FOREIGN KEYS) ---
    
    @ManyToOne(fetch = FetchType.LAZY) // Lazy: Khi lấy Task không tự lấy Project ngay (để nhẹ)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "issue_type_id", nullable = false)
    private IssueType issueType;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private WorkflowStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id") // Có thể null (nếu ở Backlog)
    private Sprint sprint;

    // TỰ THAM CHIẾU (QUAN TRỌNG): Cha - Con
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    // -----------------------------------

    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime completedAt;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}