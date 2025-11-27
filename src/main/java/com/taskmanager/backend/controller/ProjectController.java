package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.BacklogResponse;
import com.taskmanager.backend.dto.CreateProjectRequest;
import com.taskmanager.backend.dto.ProjectDetailResponse;
import com.taskmanager.backend.dto.ProjectResponse;
import com.taskmanager.backend.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.taskmanager.backend.dto.ProjectListResponse;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping
    public ResponseEntity<List<ProjectListResponse>> getMyProjects() {
        return ResponseEntity.ok(projectService.getMyProjects());
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailResponse> getProjectDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(projectService.getProjectDetail(id));
    }
    @GetMapping("/{id}/backlog")
    public ResponseEntity<BacklogResponse> getBacklog(@PathVariable Integer id) {
        return ResponseEntity.ok(projectService.getBacklogData(id));
    }
}