package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.TaskAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, Integer> {

    // ✅ 1. Dùng cho Notification & Hiển thị Task Detail
    // Mục đích: Tìm xem ai đang làm Task này?
    // (Đoạn sendUpdateNotification trong TaskService đang gọi hàm này đấy!)
    @Query("SELECT ta FROM TaskAssignee ta JOIN FETCH ta.user u WHERE ta.task.id = :taskId")
    List<TaskAssignee> findByTaskId(@Param("taskId") Integer taskId);

    // ✅ 2. Dùng cho màn hình "My Work" / "My Tasks"
    // Mục đích: Tìm tất cả task mà user này được gán.
    List<TaskAssignee> findByUserId(Integer userId);

    // ✅ 3. Dùng cho Logic Gán/Bỏ gán (Assign/Unassign)
    // Mục đích: Tìm dòng liên kết cụ thể để xóa đi (Khi user bị remove khỏi task)
    Optional<TaskAssignee> findByTaskIdAndUserId(Integer taskId, Integer userId);

    // ✅ 4. Check nhanh sự tồn tại (Validate)
    // Mục đích: Kiểm tra xem User đã được gán vào Task chưa (để tránh add trùng)
    boolean existsByTaskIdAndUserId(Integer taskId, Integer userId);

    // 🔥 5. (Nâng cao) Xóa hết người làm của 1 task
    // Dùng khi xóa Task, hoặc khi Reset Assignee
    void deleteAllByTaskId(Integer taskId);
    
    /* * 💡 TỐI ƯU HIỆU NĂNG (Optional):
     * Hàm số 1 ở trên mặc định sẽ Lười (Lazy Load) thông tin User.
     * Nếu bạn muốn lấy luôn thông tin User (Tên, Avatar) để bắn Noti cho nhanh
     * mà không bị lỗi N+1 Query, hãy dùng @Query này:
     */
    @Query("SELECT ta FROM TaskAssignee ta JOIN FETCH ta.user WHERE ta.task.id = :taskId")
    List<TaskAssignee> findAssigneesWithUserByTaskId(@Param("taskId") Integer taskId);
}