package com.taskmanager.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.taskmanager.backend.entity.Task;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("SELECT t FROM Task t " +
           "LEFT JOIN FETCH t.issueType " +
           "LEFT JOIN FETCH t.status " +
           "LEFT JOIN FETCH t.assignees a " + // Lưu ý: 'assignees' phải khớp tên field trong Task.java
           "LEFT JOIN FETCH a.user " +
           "WHERE t.project.id = :projectId " +
           "AND (t.sprint IS NULL OR t.sprint.status != :status)") 
    // 👇 Đổi tên hàm cho rõ nghĩa hơn và nhận thêm tham số status
    List<Task> findTasksForBacklog(@Param("projectId") Integer projectId, 
                                   @Param("status") com.taskmanager.backend.enums.SprintStatus status);
    // ✅ Hàm tính Task Index tiếp theo
    // Logic: Lấy số lớn nhất hiện tại. Nếu chưa có task nào thì trả về 0 (để tí nữa cộng 1 thành 1).
    @Query("SELECT COALESCE(MAX(t.taskIndex), 0) FROM Task t WHERE t.project.id = :projectId")
    Integer getMaxTaskIndex(@Param("projectId") Integer projectId);
}