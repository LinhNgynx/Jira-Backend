package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_labels",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"task_id", "label_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskLabel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_id", nullable = false)
    private Label label;
}