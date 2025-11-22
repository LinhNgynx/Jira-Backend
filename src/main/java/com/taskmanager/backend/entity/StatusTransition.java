package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "status_transitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusTransition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @ManyToOne
    @JoinColumn(name = "from_status_id", nullable = false)
    private WorkflowStatus fromStatus;

    @ManyToOne
    @JoinColumn(name = "to_status_id", nullable = false)
    private WorkflowStatus toStatus;
}