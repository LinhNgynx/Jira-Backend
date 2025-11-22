package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workflow_steps",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"workflow_id", "status_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private WorkflowStatus status;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder; // 1, 2, 3...
}