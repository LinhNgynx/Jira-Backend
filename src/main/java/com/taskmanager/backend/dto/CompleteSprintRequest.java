package com.taskmanager.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteSprintRequest {
    
    // Nếu null -> Chuyển Task về Backlog
    // Nếu có giá trị -> Chuyển Task sang Sprint ID này
    private Integer targetSprintId; 
}