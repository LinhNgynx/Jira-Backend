package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workflow_statuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    // MAPPING: TO_DO, IN_PROGRESS, DONE (Để máy tính hiểu)
    @Column(name = "status_category", nullable = false)
    private String statusCategory; 

    @Column(name = "color_code")
    private String colorCode;
}