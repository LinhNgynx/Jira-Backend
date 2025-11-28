package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {

    // 1. Dùng để check nhanh true/false (Ví dụ: khi mời thành viên mới)
    boolean existsByProjectIdAndUserId(Integer projectId, Integer userId);

    // 2. ✅ BỔ SUNG: Dùng trong TaskService để lấy Role
    // Spring Data JPA tự động generate câu SQL từ tên hàm
    Optional<ProjectMember> findByProjectIdAndUserId(Integer projectId, Integer userId);

    
    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.role WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    Optional<ProjectMember> findMemberWithRole(@Param("projectId") Integer projectId, @Param("userId") Integer userId);
}