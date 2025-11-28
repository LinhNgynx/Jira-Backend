package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction; // ✅ IMPORT MỚI
import com.taskmanager.backend.enums.TaskPriority;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tasks")
@SQLDelete(sql = "UPDATE tasks SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false") // ✅ THAY THẾ @Where
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "task_index", nullable = false)
    private Integer taskIndex;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @Column(name = "story_points")
    private Integer storyPoints;

    // --- RELATIONS ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_type_id", nullable = false)
    private IssueType issueType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private WorkflowStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    private List<TaskAssignee> assignees;

    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime completedAt;
}