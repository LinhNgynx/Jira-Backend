package com.taskmanager.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteSprintResult {
    private Integer sprintId;           // ID của Sprint vừa đóng
    private boolean success;            // Trạng thái thành công
    private String message;             // Tin nhắn cho Frontend hiển thị (Toast)
    
    private int movedTasksCount;        // Số lượng task chưa xong bị di chuyển
    private List<String> movedTaskKeys; // Danh sách mã task (VD: ["PROJ-10", "PROJ-12"])
    private String destination;         // Đích đến: "Backlog" hoặc "Tên Sprint mới"
}