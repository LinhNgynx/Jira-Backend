package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.TaskAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, Integer> {
    // Sau này nếu cần tìm "Task nào do User A làm", bạn sẽ thêm hàm ở đây.
    // Ví dụ: List<TaskAssignee> findByUserId(Integer userId);
}