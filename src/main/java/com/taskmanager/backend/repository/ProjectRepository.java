package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

// 1. Project Repo
@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    boolean existsByCode(String code);

    @Query("SELECT p FROM Project p JOIN ProjectMember pm ON p.id = pm.project.id WHERE pm.user.email = :email")
    List<Project> findProjectsByUserEmail(@Param("email") String email);
}