package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {

    // ✅ Hàm này dùng để: "Lấy lịch sử của 1 task cụ thể, cái mới nhất hiện lên đầu"
    // Dùng cho API: GET /api/tasks/{id}/history (Làm sau)
    List<ActivityLog> findByTaskIdOrderByCreatedAtDesc(Integer taskId);
}