package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
    boolean existsByProjectIdAndUserId(Integer projectId, Integer userId);
}