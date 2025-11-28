package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction; // ✅ IMPORT MỚI

@Entity
@Table(name = "comments")
@SQLDelete(sql = "UPDATE comments SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false") // ✅ THAY THẾ @Where
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}