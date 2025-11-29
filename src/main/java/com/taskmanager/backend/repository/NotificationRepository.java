package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 1. Lấy danh sách thông báo của một User cụ thể.
     * Sắp xếp: Mới nhất lên đầu (CreatedAt Descending).
     */
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Integer recipientId);

    /**
     * 2. Đếm số lượng thông báo CHƯA ĐỌC.
     * Dùng để hiển thị chấm đỏ trên quả chuông (VD: "5").
     */
    long countByRecipientIdAndIsReadFalse(Integer recipientId);

    /**
     * 3. Tính năng: "Đánh dấu tất cả là đã đọc" (Mark all as read).
     * Dùng @Modifying và @Query để update nhanh mà không cần load dữ liệu lên RAM.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Integer userId);
}