package com.taskmanager.backend.listener;

import com.taskmanager.backend.entity.ActivityLog;
import com.taskmanager.backend.entity.Task;
import com.taskmanager.backend.event.SystemEvent;
import com.taskmanager.backend.repository.ActivityLogRepository;
import com.taskmanager.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <--- Cái này sinh ra biến 'log'
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j // Tự động tạo: private static final Logger log = LoggerFactory.getLogger(...)
public class SystemEventListener {

    private final ActivityLogRepository logRepo;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleActivityLog(SystemEvent event) {
        if (event.getLogAction() == null) {
            return;
        }
        try {
            if (event.getSubject() instanceof Task task) {
                // 1. Tạo Entity để lưu xuống DB (Cho user xem)
                ActivityLog activityLog = ActivityLog.builder()
                        .user(event.getActor())
                        .task(task)
                        .action(event.getLogAction())
                        .newValue(event.getLogDetail()) // Map detail vào newValue
                        .oldValue(null) // Hiện tại event chưa có oldValue, để null hoặc mở rộng Event sau
                        .build();

                logRepo.save(activityLog); // <--- LƯU DB LÀ DÒNG NÀY

                // 2. In ra màn hình đen (Cho Dev xem để biết là đã lưu thành công)
                log.info("✅ [Async Thread] Đã lưu ActivityLog ID: {} - User: {}",
                        activityLog.getId(), event.getActor().getEmail());
            }
        } catch (Exception e) {
            // Quan trọng: Nếu lưu DB lỗi, nó sẽ in lỗi ra đây để bạn sửa
            log.error("❌ Lỗi khi lưu ActivityLog: ", e);
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