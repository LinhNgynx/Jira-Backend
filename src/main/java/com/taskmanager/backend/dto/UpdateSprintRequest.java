package com.taskmanager.backend.dto;

import com.taskmanager.backend.enums.SprintDuration;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateSprintRequest {
    @NotNull(message = "Tên Sprint không được để trống")
    private String name;

    private String goal;

    @NotNull(message = "Vui lòng chọn thời lượng Sprint")
    private SprintDuration duration;

    // StartDate bắt buộc phải có để tính toán
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    // EndDate chỉ bắt buộc nếu duration = CUSTOM
    private LocalDate endDate;
}