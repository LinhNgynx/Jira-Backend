package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.CreateTaskRequest;
import com.taskmanager.backend.dto.UpdateTaskRequest;
import com.taskmanager.backend.dto.TaskResponse;
import com.taskmanager.backend.mapper.TaskMapper;
import com.taskmanager.backend.entity.Task;
import com.taskmanager.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper; // 🔥 Inject Mapper vào

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(request);
        // Gọi Mapper
        return ResponseEntity.ok(taskMapper.toResponse(task));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Integer id,
            @RequestBody UpdateTaskRequest request
    ) {
        Task task = taskService.updateTask(id, request);
        // Gọi Mapper
        return ResponseEntity.ok(taskMapper.toResponse(task));
    }
}