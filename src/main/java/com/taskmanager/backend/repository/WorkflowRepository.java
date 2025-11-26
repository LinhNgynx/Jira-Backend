package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Integer> {

    // Tự động dịch thành:
    // SELECT * FROM workflows WHERE name = ?
    Optional<Workflow> findByName(String name);
}