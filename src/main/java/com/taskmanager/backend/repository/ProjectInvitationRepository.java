package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {
    Optional<ProjectInvitation> findByToken(String token);
    // Optional: find pending invites by email or project
}