package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction; // ✅ IMPORT MỚI
import com.taskmanager.backend.enums.SprintStatus;
import java.time.LocalDate;

@Entity
@Table(name = "sprints")
@SQLDelete(sql = "UPDATE sprints SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false") // ✅ THAY THẾ @Where
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sprint extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SprintStatus status;
}