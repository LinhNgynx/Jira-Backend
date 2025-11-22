package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "issue_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "is_subtask")
    private Boolean isSubtask = false;
}