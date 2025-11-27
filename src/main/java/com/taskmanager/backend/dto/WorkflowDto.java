package com.taskmanager.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkflowDto {
    private Integer id;
    private String name;
    private String description;
}