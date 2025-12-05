package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.*;
import com.taskmanager.backend.entity.Project;
import com.taskmanager.backend.entity.Sprint;
import com.taskmanager.backend.entity.Task;
import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.enums.SprintStatus;
import com.taskmanager.backend.enums.StatusCategory;
import com.taskmanager.backend.exception.ResourceNotFoundException;
import com.taskmanager.backend.repository.ProjectRepository;
import com.taskmanager.backend.repository.SprintRepository;
import com.taskmanager.backend.repository.TaskRepository;
import com.taskmanager.backend.utils.UserUtils;
import com.taskmanager.backend.validator.SprintValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintRepository sprintRepo;
    private final ProjectRepository projectRepo;
    private final TaskRepository taskRepo;
    
    private final UserUtils userUtils;
    private final SprintValidator sprintValidator;

    // =========================================================================
    // HELPER: Convert Entity -> DTO (🔥 FIX LỖI STACK OVERFLOW TẠI ĐÂY)
    // =========================================================================
    private SprintResponse mapToResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus().name())
                .duration(sprint.getDuration() != null ? sprint.getDuration().name() : null)
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .projectId(sprint.getProject().getId())     // ✅ Chỉ lấy ID, cắt đứt vòng lặp
                .projectName(sprint.getProject().getName()) // Lấy thêm tên để hiển thị
                .build();
    }

    // =========================================================================
    // 1. CREATE SPRINT
    // =========================================================================
    @Transactional
    public SprintResponse createSprint(CreateSprintRequest request) { // ✅ Return DTO
        User currentUser = userUtils.getCurrentUser();
        Project project = projectRepo.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        sprintValidator.validateManagePermission(project.getId(), currentUser.getId());

        long count = sprintRepo.countByProjectId(project.getId());
        String sprintName = project.getCode() + " Sprint " + (count + 1);

        Sprint sprint = Sprint.builder()
                .name(sprintName)
                .project(project)
                .status(SprintStatus.UPCOMING)
                .build();

        Sprint saved = sprintRepo.save(sprint);
        return mapToResponse(saved); // ✅ Map to DTO
    }

    // =========================================================================
    // 2. UPDATE SPRINT
    // =========================================================================
    @Transactional
    public SprintResponse updateSprint(Integer sprintId, UpdateSprintRequest request) { // ✅ Return DTO
        User currentUser = userUtils.getCurrentUser();
        Sprint sprint = sprintRepo.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        sprintValidator.validateManagePermission(sprint.getProject().getId(), currentUser.getId());
        sprintValidator.validateUpdateSprint(sprint);

        LocalDate finalEndDate = sprintValidator.calculateAndValidateEndDate(
                request.getDuration(), request.getStartDate(), request.getEndDate());

        sprint.setName(request.getName());
        sprint.setGoal(request.getGoal());
        sprint.setDuration(request.getDuration());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(finalEndDate);

        Sprint updated = sprintRepo.save(sprint);
        return mapToResponse(updated); // ✅ Map to DTO
    }

    // =========================================================================
    // 3. START SPRINT
    // =========================================================================
    @Transactional
    public SprintResponse startSprint(Integer sprintId, StartSprintRequest request) { // ✅ Return DTO
        User currentUser = userUtils.getCurrentUser();
        Sprint sprint = sprintRepo.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        sprintValidator.validateManagePermission(sprint.getProject().getId(), currentUser.getId());
        sprintValidator.validateStartSprint(sprint, request.getStartDate(), request.getEndDate());

        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setGoal(request.getGoal());
        sprint.setStatus(SprintStatus.ACTIVE);

        Sprint started = sprintRepo.save(sprint);
        return mapToResponse(started); // ✅ Map to DTO
    }

    // =========================================================================
    // 4. COMPLETE SPRINT (Có báo cáo chi tiết task bị move)
    // =========================================================================
    @Transactional
    public CompleteSprintResult completeSprint(Integer sprintId, CompleteSprintRequest request) {
        User currentUser = userUtils.getCurrentUser();

        // 1. Get & Validate Current Sprint
        Sprint currentSprint = sprintRepo.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint không tồn tại"));

        sprintValidator.validateManagePermission(currentSprint.getProject().getId(), currentUser.getId());
        sprintValidator.validateCompleteSprint(currentSprint);

        // 2. Handle Incomplete Tasks
        List<Task> incompleteTasks = taskRepo.findIncompleteTasks(sprintId, StatusCategory.DONE);

        // Chuẩn bị dữ liệu báo cáo
        int movedCount = 0;
        String destinationName = "Backlog";
        List<String> taskKeys = List.of();

        if (!incompleteTasks.isEmpty()) {
            List<Integer> taskIds = incompleteTasks.stream().map(Task::getId).collect(Collectors.toList());
            String projectCode = currentSprint.getProject().getCode();

            // Lấy danh sách Key task (VD: "PRJ-10") để FE hiển thị
            taskKeys = incompleteTasks.stream()
                    .map(t -> projectCode + "-" + t.getTaskIndex())
                    .collect(Collectors.toList());
            
            movedCount = taskIds.size();

            if (request.getTargetSprintId() != null) {
                // CASE A: Move to Target Sprint
                Sprint targetSprint = sprintValidator.validateTargetSprint(request.getTargetSprintId(), currentSprint);
                
                // Bulk Update
                taskRepo.moveTasksToSprint(taskIds, targetSprint.getId());
                destinationName = targetSprint.getName();
            } else {
                // CASE B: Move to Backlog
                taskRepo.moveTasksToBacklog(taskIds);
                destinationName = "Backlog";
            }
        }

        // 3. Close Sprint
        currentSprint.setStatus(SprintStatus.COMPLETED);
        sprintRepo.save(currentSprint);
        
        // 4. Return Report Result
        return CompleteSprintResult.builder()
                .sprintId(sprintId)
                .success(true)
                .movedTasksCount(movedCount)
                .movedTaskKeys(taskKeys)
                .destination(destinationName)
                .message(String.format("Đã hoàn thành Sprint. Di chuyển %d task sang %s.", movedCount, destinationName))
                .build();
    }
}