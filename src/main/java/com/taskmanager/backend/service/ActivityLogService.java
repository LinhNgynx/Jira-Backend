package com.taskmanager.backend.service;

import com.taskmanager.backend.entity.ActivityLog;
import com.taskmanager.backend.entity.Task;
import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.enums.ActivityAction;
import com.taskmanager.backend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository logRepo;

    /**
     * Hàm ghi log chung cho toàn hệ thống.
     * @Async: Chạy ở luồng riêng (Optional) để không làm chậm luồng chính.
     */
    @Async // (Cần cấu hình @EnableAsync ở main class nếu muốn dùng)
    @Transactional
    public void log(User actor, Task task, ActivityAction action, String oldValue, String newValue) {
        
        try {
            ActivityLog log = ActivityLog.builder()
                    .user(actor)
                    .task(task)
                    .action(action)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build();

            logRepo.save(log);
        } catch (Exception e) {
            // Log lỗi ra console nhưng KHÔNG ném exception để tránh làm rollback nghiệp vụ chính
            // (Ví dụ: Tạo task thành công nhưng ghi log lỗi thì vẫn nên cho tạo task)
            System.err.println("Lỗi ghi Activity Log: " + e.getMessage());
        }
    }
    
    // Hàm overload cho gọn nếu không có old/new value
    public void log(User actor, Task task, ActivityAction action) {
        log(actor, task, action, null, null);
    }
}