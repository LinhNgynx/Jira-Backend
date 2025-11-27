package com.taskmanager.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.taskmanager.backend.entity.Sprint;
import java.util.List;
@Repository
public interface SprintRepository extends JpaRepository<Sprint, Integer> {
    // Lấy Sprint chưa hoàn thành (Active & Planned)
    // Ưu tiên hiển thị Active trước, sau đó đến Planned, và sắp xếp theo ngày bắt đầu
    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId AND s.status != 'COMPLETED' ORDER BY s.status ASC, s.startDate ASC")
    List<Sprint> findActiveAndPlannedSprints(Integer projectId);
}