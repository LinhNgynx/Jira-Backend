package com.taskmanager.backend.controller;

import com.taskmanager.backend.entity.Notification;
import com.taskmanager.backend.repository.NotificationRepository;
import com.taskmanager.backend.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepo; // Gọi Repo trực tiếp cho nhanh (Query đơn giản)
    private final UserUtils userUtils;

    // 1. Lấy danh sách thông báo
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications() {
        Integer currentUserId = userUtils.getCurrentUser().getId();
        return ResponseEntity.ok(notificationRepo.findByRecipientIdOrderByCreatedAtDesc(currentUserId));
    }

    // 2. Đếm số thông báo chưa đọc (để hiện chấm đỏ trên chuông)
    @GetMapping("/count")
    public ResponseEntity<Long> countUnread() {
        Integer currentUserId = userUtils.getCurrentUser().getId();
        return ResponseEntity.ok(notificationRepo.countByRecipientIdAndIsReadFalse(currentUserId));
    }

    // 3. Đánh dấu tất cả là đã đọc
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Integer currentUserId = userUtils.getCurrentUser().getId();
        notificationRepo.markAllAsRead(currentUserId);
        return ResponseEntity.ok().build();
    }
}