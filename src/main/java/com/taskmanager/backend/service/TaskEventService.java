package com.taskmanager.backend.service;

import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.enums.ActivityAction;
import com.taskmanager.backend.enums.NotificationType;
import com.taskmanager.backend.event.SystemEvent;
import com.taskmanager.backend.repository.TaskAssigneeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskEventService {

    private final ApplicationEventPublisher eventPublisher;
    private final TaskAssigneeRepository assigneeRepo;

    // =========================================================================
    // 1. PUBLISHERS (Public API)
    // =========================================================================

    /**
     * --- 1. Sự kiện TẠO TASK ---
     * Phát sự kiện ghi Log & Noti khi một Task mới được tạo.
     */
    @Transactional(readOnly = true)
    public void publishTaskCreatedEvent(User actor, Task task) {
        String taskKey = task.getProject().getCode() + "-" + task.getTaskIndex();
        
        List<TaskAssignee> assignees = assigneeRepo.findByTaskId(task.getId());
        
        // Cờ kiểm tra xem có Assignee nào là người khác không
        boolean hasExternalRecipient = false;
        
        // 1. Gửi Notification cho tất cả người được gán (trừ Actor)
        for (TaskAssignee ta : assignees) {
            User recipient = ta.getUser();
            
            if (!recipient.getId().equals(actor.getId())) {
                publishNotification(
                    actor, 
                    task, 
                    recipient, 
                    NotificationType.TASK_ASSIGNED, 
                    actor.getFullName() + " đã gán bạn vào task: " + task.getTitle()
                );
                hasExternalRecipient = true;
            }
        }

        // 2. Ghi Log (Log chỉ được ghi 1 lần)
        publishLog(actor, task, ActivityAction.CREATED, "Created task " + taskKey, null, null); 

        // LƯU Ý: Không cần logic isLogRecorded phức tạp nữa vì Log và Noti đã tách hàm
    }

    /**
     * --- 2. Sự kiện UPDATE FIELD (Ghi Log chi tiết) ---
     * Được gọi cho từng trường thay đổi (Title, Priority).
     */
    public void logFieldChange(User actor, Task task, String field, String oldVal, String newVal) {
        String detail = "Updated " + field; // Detail ngắn gọn hơn
        publishLog(actor, task, ActivityAction.UPDATE_TASK, detail, oldVal, newVal);
    }

    /**
     * --- 3. Sự kiện UPDATE CHUNG (Gửi Noti tổng hợp) ---
     */
    @Transactional(readOnly = true)
    public void sendUpdateNotification(User actor, Task task, String whatChanged) {
        List<TaskAssignee> assignees = assigneeRepo.findByTaskId(task.getId());
        
        for (TaskAssignee ta : assignees) {
            if (!ta.getUser().getId().equals(actor.getId())) {
                publishNotification(
                    actor, task, 
                    ta.getUser(), 
                    NotificationType.TASK_UPDATE, 
                    actor.getFullName() + " đã cập nhật " + whatChanged + " của task " + task.getTitle());
            }
        }
    }
    

    // =========================================================================
    // PRIVATE HELPER (Wrapper cho Event Publishing)
    // =========================================================================
    
    /**
     * Helper 1: Wrapper cho việc GHI LOG (Chỉ set Action)
     */
    private void publishLog(User actor, Object subject, ActivityAction action, String logDetail, String oldVal, String newVal) {
        eventPublisher.publishEvent(new SystemEvent(
            this, actor, subject, action, logDetail, 
            oldVal, newVal, // Truyền vào Event
            null, null, null 
        ));
    }
    
    /**
     * Helper 2: Wrapper cho việc GỬI NOTIFICATION (Chỉ set Recipient/Type/Msg)
     */
    private void publishNotification(User actor, Task task, User recipient, NotificationType type, String message) {
        eventPublisher.publishEvent(new SystemEvent(
            this, actor, task, 
            null, null, null, null, // Log null
            recipient, type, message
        ));
    }
}