package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.Task;
import com.taskmanager.backend.enums.SprintStatus;
import com.taskmanager.backend.enums.StatusCategory; // ✅ Nhớ import Enum này
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    // =========================================================================
    // 1. CÁC HÀM CŨ CỦA BẠN (GIỮ NGUYÊN)
    // =========================================================================

    @Query("SELECT t FROM Task t " +
            "LEFT JOIN FETCH t.issueType " +
            "LEFT JOIN FETCH t.status " +
            "LEFT JOIN FETCH t.assignees a " +
            "LEFT JOIN FETCH a.user " +
            "WHERE t.project.id = :projectId " +
            "AND (t.sprint IS NULL OR t.sprint.status != :status)")
    List<Task> findTasksForBacklog(@Param("projectId") Integer projectId,
            @Param("status") SprintStatus status);

    @Query("SELECT COALESCE(MAX(t.taskIndex), 0) FROM Task t WHERE t.project.id = :projectId")
    Integer getMaxTaskIndex(@Param("projectId") Integer projectId);

    // =========================================================================
    // 2. 🔥 CÁC HÀM BỔ SUNG CHO SPRINT MANAGEMENT (CẦN THÊM VÀO NGAY)
    // =========================================================================

    /**
     * A. Tìm các task CHƯA HOÀN THÀNH trong một Sprint.
     * Logic: Lấy task trong sprint đó MÀ category status khác DONE.
     * (Ví dụ: Status là TO_DO hoặc IN_PROGRESS thì lấy, DONE thì bỏ qua).
     */
    @Query("SELECT t FROM Task t WHERE t.sprint.id = :sprintId AND t.status.statusCategory != :category")
    List<Task> findIncompleteTasks(@Param("sprintId") Integer sprintId,
            @Param("category") StatusCategory category);

    /**
     * B. Di chuyển hàng loạt Task sang Sprint khác.
     * Dùng @Modifying để báo cho JPA biết đây là câu lệnh UPDATE/DELETE, không phải
     * SELECT.
     */
    @Modifying
    @Query("UPDATE Task t SET t.sprint.id = :targetSprintId WHERE t.id IN :taskIds")
    void moveTasksToSprint(@Param("taskIds") List<Integer> taskIds,
            @Param("targetSprintId") Integer targetSprintId);

    /**
     * C. Đẩy hàng loạt Task về Backlog (Sprint = null).
     */
    @Modifying
    @Query("UPDATE Task t SET t.sprint = null WHERE t.id IN :taskIds")
    void moveTasksToBacklog(@Param("taskIds") List<Integer> taskIds);

    /**
     * D. Lấy tất cả Task trong một Sprint (Dùng cho Board View sau này)
     * Hàm này JPA tự sinh query, không cần @Query
     */
    // Lấy tất cả task của 1 Sprint (Kèm theo thông tin User, IssueType để hiển thị
    // cho đẹp)
    @Query("SELECT t FROM Task t " +
           "LEFT JOIN FETCH t.issueType " +      // Lấy luôn thông tin loại Issue (để hiện icon)
           "LEFT JOIN FETCH t.status " +         // Lấy luôn Status (để biết màu cột)
           "LEFT JOIN FETCH t.assignees a " +    // Lấy bảng trung gian Assignees
           "LEFT JOIN FETCH a.user " +           // Lấy luôn User info (để hiện avatar)
           "WHERE t.sprint.id = :sprintId " +
           "AND t.deleted = false")              // Nhớ lọc task chưa xóa
    List<Task> findAllBySprintIdWithDetails(@Param("sprintId") Integer sprintId);
}