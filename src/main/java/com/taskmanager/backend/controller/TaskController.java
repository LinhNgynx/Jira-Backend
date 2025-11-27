package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.CreateTaskRequest;
import com.taskmanager.backend.entity.Task;
import com.taskmanager.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task newTask = taskService.createTask(request);
        
        // Trả về Key đẹp (VD: "SCRUM-15") cho Frontend hiện thông báo
        String taskKey = newTask.getProject().getCode() + "-" + newTask.getTaskIndex();
        
        return ResponseEntity.ok(Map.of(
            "message", "Tạo task thành công",
            "id", newTask.getId(),
            "key", taskKey,
            "title", newTask.getTitle()
        ));
    }
}