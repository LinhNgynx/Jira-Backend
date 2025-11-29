package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.CreateTaskRequest;
import com.taskmanager.backend.dto.UpdateTaskRequest;
import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.enums.TaskPriority;
import com.taskmanager.backend.exception.ResourceNotFoundException;
import com.taskmanager.backend.repository.*;
import com.taskmanager.backend.utils.UserUtils;
import com.taskmanager.backend.validator.TaskValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    // --- Repositories ---
    private final TaskRepository taskRepo;
    private final ProjectRepository projectRepo;
    private final IssueTypeRepository issueTypeRepo;
    private final WorkflowStepRepository stepRepo;
    private final SprintRepository sprintRepo;
    private final TaskAssigneeRepository assigneeRepo;
    private final UserRepository userRepo;

    // --- Helpers ---
    private final UserUtils userUtils;
    private final TaskValidator taskValidator; // Chuyên gia check lỗi
    private final TaskEventService eventService; // ✅ Chuyên gia bắn tin (Class bạn vừa tách)

    // =========================================================================
    // 1. TẠO TASK MỚI
    // =========================================================================
    @Transactional
    public Task createTask(CreateTaskRequest request) {
        User currentUser = userUtils.getCurrentUser();

        // 1. Validate dữ liệu & Quyền hạn (Dùng Validator cho gọn)
        Project project = projectRepo.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        IssueType issueType = issueTypeRepo.findById(request.getIssueTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("IssueType not found"));

        taskValidator.validateWritePermission(project.getId(), currentUser.getId());

        Task parentTask = null;
        if (request.getParentTaskId() != null) {
            parentTask = taskRepo.findById(request.getParentTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Task not found"));
        }
        taskValidator.validateHierarchy(issueType, parentTask);

        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepo.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
            taskValidator.validateSprint(sprint, project.getId());
        }

        // 2. Tính toán Logic tự động (Start Step, Task Index)
        WorkflowStep startStep = stepRepo.findByWorkflowIdAndStepOrder(project.getWorkflow().getId(), 1)
                .orElseThrow(() -> new RuntimeException("Workflow error: No start step found"));

        Integer maxIndex = taskRepo.getMaxTaskIndex(project.getId());
        Integer nextIndex = (maxIndex == null ? 0 : maxIndex) + 1;

        // 3. Build & Save Task
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .issueType(issueType)
                .status(startStep.getStatus())
                .taskIndex(nextIndex)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .storyPoints(request.getStoryPoints())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .sprint(sprint)
                .parentTask(parentTask)
                .build();

        Task savedTask = taskRepo.save(task);

        // 4. Lưu Assignees (Người làm)
        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            List<User> assignees = userRepo.findAllById(request.getAssigneeIds());
            for (User user : assignees) {
                taskValidator.validateAssignee(project.getId(), user);
                assigneeRepo.save(TaskAssignee.builder().task(savedTask).user(user).build());
            }
        }

        // 5. 🔥 GỌI EVENT SERVICE (1 dòng duy nhất)
        // Service này sẽ tự tìm Assignee trong DB để gửi Noti và ghi Log
        eventService.publishTaskCreatedEvent(currentUser, savedTask);

        return savedTask;
    }

    // =========================================================================
    // 2. CẬP NHẬT TASK
    // =========================================================================
    @Transactional
    public Task updateTask(Integer taskId, UpdateTaskRequest request) {
        User currentUser = userUtils.getCurrentUser();

        // 1. Tìm Task & Check quyền
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        taskValidator.validateUpdatePermission(task, currentUser.getId());

        // 2. SO SÁNH & UPDATE (Change Detection)
        boolean isChanged = false;
        StringBuilder changesSummary = new StringBuilder();

        // --- Check Title ---
        if (request.getTitle() != null && !request.getTitle().equals(task.getTitle())) {
            // 🔥 Gọi Event Service để ghi log chi tiết
            eventService.logFieldChange(currentUser, task, "Title", task.getTitle(), request.getTitle());

            task.setTitle(request.getTitle());
            isChanged = true;
            changesSummary.append("tiêu đề, ");
        }

        // --- Check Description ---
        if (request.getDescription() != null && !request.getDescription().equals(task.getDescription())) {
            eventService.logFieldChange(currentUser, task, "Description", "Old Value", "New Value");
            task.setDescription(request.getDescription());
            isChanged = true;
            changesSummary.append("mô tả, ");
        }

        // --- Check Priority ---
        if (request.getPriority() != null && request.getPriority() != task.getPriority()) {
            eventService.logFieldChange(currentUser, task, "Priority", task.getPriority().name(),
                    request.getPriority().name());
            task.setPriority(request.getPriority());
            isChanged = true;
            changesSummary.append("độ ưu tiên, ");
        }

        // --- Check Story Points ---
        if (request.getStoryPoints() != null && !request.getStoryPoints().equals(task.getStoryPoints())) {
            String oldVal = String.valueOf(task.getStoryPoints());
            String newVal = String.valueOf(request.getStoryPoints());
            eventService.logFieldChange(currentUser, task, "Story Points", oldVal, newVal);

            task.setStoryPoints(request.getStoryPoints());
            isChanged = true;
            changesSummary.append("điểm story, ");
        }

        // --- Check Due Date ---
        if (request.getDueDate() != null && !request.getDueDate().equals(task.getDueDate())) {
            String oldVal = task.getDueDate() == null ? "None" : task.getDueDate().toString();
            eventService.logFieldChange(currentUser, task, "Due Date", oldVal, request.getDueDate().toString());

            task.setDueDate(request.getDueDate());
            isChanged = true;
            changesSummary.append("ngày hết hạn, ");
        }

        // 3. LƯU & BẮN NOTI TỔNG HỢP
        if (isChanged) {
            Task updatedTask = taskRepo.save(task);
            if (updatedTask.getAssignees() != null) {
                updatedTask.getAssignees().size(); // Chỉ cần truy cập để ép tải
            }
            String whatChanged = changesSummary.toString();
            if (whatChanged.endsWith(", ")) {
                whatChanged = whatChanged.substring(0, whatChanged.length() - 2);
            }

            // 🔥 Gọi Event Service để gửi Noti
            eventService.sendUpdateNotification(currentUser, updatedTask, whatChanged);

            return updatedTask;
        }

        return task;
    }
}