package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_labels",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"task_id", "label_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskLabel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "label_id", nullable = false)
    private Label label;
}