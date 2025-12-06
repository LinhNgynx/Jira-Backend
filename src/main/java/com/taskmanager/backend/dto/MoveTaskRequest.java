package com.taskmanager.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveTaskRequest {
    @NotNull(message = "ID trạng thái đích không được để trống")
    private Integer targetStatusId;
}