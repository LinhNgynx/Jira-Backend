package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction; // ✅ IMPORT MỚI
import com.taskmanager.backend.enums.ProjectStatus;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "projects")
@SQLDelete(sql = "UPDATE projects SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false") // ✅ THAY THẾ @Where
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 10)
    private String code; 

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<ProjectMember> projectMembers;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;
}