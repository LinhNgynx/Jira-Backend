package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "project_invitations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inviter_id")
    private User inviter;

    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;

    @ManyToOne
    @JoinColumn(name = "invitee_user_id")
    private User inviteeUser; // nullable

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private ProjectRole role;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED, DECLINED, EXPIRED

    private Instant expiredAt;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}