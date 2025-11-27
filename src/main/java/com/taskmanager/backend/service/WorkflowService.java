package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.WorkflowDto;
import com.taskmanager.backend.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import đúng gói

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepo;

    // Hàm lấy tất cả workflow
    @Transactional(readOnly = true) // Chỉ đọc nên tối ưu hiệu năng
    public List<WorkflowDto> getAllWorkflows() {
        return workflowRepo.findAll().stream()
                .map(workflow -> WorkflowDto.builder()
                        .id(workflow.getId())
                        .name(workflow.getName())
                        .description(workflow.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
}