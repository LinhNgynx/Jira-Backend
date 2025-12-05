package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.Sprint;
import com.taskmanager.backend.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Integer> {

    // 1. Lấy danh sách Sprint hiển thị ở Backlog (Active & Upcoming)
    // Logic: Lấy tất cả trừ COMPLETED
    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId AND s.status <> com.taskmanager.backend.enums.SprintStatus.COMPLETED ORDER BY s.status ASC, s.startDate ASC")
    List<Sprint> findActiveAndUpcomingSprints(@Param("projectId") Integer projectId);

    long countByProjectId(Integer projectId);

    boolean existsByProjectIdAndStatus(Integer projectId, SprintStatus status);

    // 2. Tìm sprint UPCOMING đầu tiên (để đẩy task dư sang khi complete sprint cũ)
    Optional<Sprint> findFirstByProjectIdAndStatusOrderByIdAsc(Integer projectId, SprintStatus status);
}