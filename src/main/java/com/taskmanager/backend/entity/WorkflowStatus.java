package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workflow_statuses")
@Getter
@Setter
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
    @Enumerated(EnumType.STRING)
    @Column(name = "status_category", nullable = false)
    private com.taskmanager.backend.enums.StatusCategory statusCategory; 

    @Column(name = "color_code")
    private String colorCode;
}