package com.taskmanager.backend.listener;

import com.taskmanager.backend.entity.ActivityLog;
import com.taskmanager.backend.entity.Task;
import com.taskmanager.backend.event.SystemEvent;
import com.taskmanager.backend.repository.ActivityLogRepository;
import com.taskmanager.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemEventListener {

    private final ActivityLogRepository logRepo;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleActivityLog(SystemEvent event) {
        if (event.getLogAction() == null)
            return;

        try {
            if (event.getSubject() instanceof Task task) {
                ActivityLog activityLog = ActivityLog.builder()
                        .user(event.getActor())
                        .task(task)
                        .action(event.getLogAction())

                        // ✅ MAP DỮ LIỆU CHUẨN:
                        .description(event.getLogDetail()) // Câu văn mô tả
                        .oldValue(event.getOldValue()) // Giá trị cũ
                        .newValue(event.getNewValue()) // Giá trị mới

                        .build();

                logRepo.save(activityLog);

                // Log ra console kiểm tra
                log.info("✅ Log Saved: [{}] Old='{}' -> New='{}'",
                        event.getLogDetail(), event.getOldValue(), event.getNewValue());
            }
        } catch (Exception e) {
            log.error("❌ Error saving log", e);
        }
    }

    /**
     * NHIỆM VỤ 2: Gửi Notification Realtime
     * Logic: Giữ nguyên, gọi sang NotificationService vì logic noti phức tạp
     * (socket + db)
     */
    @Async
    @EventListener
    public void handleNotification(SystemEvent event) {
        // Nếu không có người nhận -> Bỏ qua
        if (event.getRecipient() == null) {
            return;
        }

        try {
            // Lấy referenceId tùy theo loại đối tượng
            Long refId = null;
            if (event.getSubject() instanceof Task t)
                refId = t.getId().longValue();
            // if (event.getSubject() instanceof ProjectInvitation p) refId = p.getId();

            notificationService.createAndSendNotification(
                    event.getActor(),
                    event.getRecipient(),
                    event.getNotiType(),
                    refId,
                    event.getNotiMessage());
        } catch (Exception e) {
            log.error("Failed to send notification", e);
        }
    }
}