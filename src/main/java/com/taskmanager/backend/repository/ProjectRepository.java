package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// 1. Project Repo
@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    boolean existsByCode(String code);
}