package com.taskmanager.backend.entity;

import com.taskmanager.backend.enums.SprintDuration; // ✅ Import mới
import com.taskmanager.backend.enums.SprintStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDate;

@Entity
@Table(name = "sprints")
@SQLDelete(sql = "UPDATE sprints SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
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
    
    @Column(columnDefinition = "TEXT")
    private String goal; // ✅ Mới: Sprint Goal

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration")
    private SprintDuration duration; // ✅ Mới: Chọn 1w, 2w...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SprintStatus status;
}