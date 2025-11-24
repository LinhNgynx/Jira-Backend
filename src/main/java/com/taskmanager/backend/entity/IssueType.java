package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "issue_types")
@Getter
@Setter
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

    @Builder.Default
    @Column(name = "is_subtask")
    private Boolean isSubtask = false;
}