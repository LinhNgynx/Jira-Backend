package com.taskmanager.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class SprintResponse {
    private Integer id;
    private String name;
    private String goal;
    private String status;       // Trả về String cho FE dễ đọc
    private String duration;     // Trả về String
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer projectId;   // Chỉ trả về ID, không trả cả Project object
    private String projectName;  // Kèm tên cho tiện hiển thị
}