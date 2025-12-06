package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowStatusRepository extends JpaRepository<WorkflowStatus, Integer> {
    // Không cần viết gì thêm, JpaRepository đã bao thầu hết:
    // - findById(id)
    // - findAll()
    // - save(entity)
}