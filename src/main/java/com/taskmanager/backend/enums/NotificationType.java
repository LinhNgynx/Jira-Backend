package com.taskmanager.backend.enums;

public enum NotificationType {
    // Nhóm Task
    TASK_ASSIGNED,          // Được gán task
    TASK_COMMENT,           // Có người cmt vào task (Sửa lại tên cho ngắn gọn nếu thích)
    TASK_STATUS_CHANGED,    // Task thay đổi trạng thái
    MENTIONED,              // Được tag tên @
    DUE_DATE_SOON,          // Sắp hết hạn
    TASK_UPDATE,
    // Nhóm Project (MỚI THÊM)
    PROJECT_INVITE,         // <--- QUAN TRỌNG: Dùng cho InvitationService
    PROJECT_REMOVED         // (Optional) Khi bị kick khỏi dự án
}