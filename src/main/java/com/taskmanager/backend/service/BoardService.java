package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.BoardResponse;
import com.taskmanager.backend.dto.MoveTaskRequest;
import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.enums.SprintStatus;
import com.taskmanager.backend.enums.StatusCategory;
import com.taskmanager.backend.exception.ActionNotAllowedException;
import com.taskmanager.backend.exception.ResourceNotFoundException;
import com.taskmanager.backend.repository.*;
import com.taskmanager.backend.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    // --- REPOSITORIES ---
    private final ProjectMemberRepository memberRepo;
    private final SprintRepository sprintRepo;
    private final TaskRepository taskRepo;
    private final WorkflowStepRepository workflowStepRepo;

    // ✅ Cần thêm 2 Repo này để xử lý Move Task
    private final WorkflowStatusRepository statusRepo;
    private final StatusTransitionRepository transitionRepo;
    private final TaskEventService eventService;

    private final UserUtils userUtils;

    // =========================================================================
    // 1. GET BOARD DATA (Hiển thị bảng)
    // =========================================================================
    @Transactional(readOnly = true)
    public BoardResponse getBoardData(Integer projectId) {
        User currentUser = userUtils.getCurrentUser();

        // 1. Security Check
        if (!memberRepo.existsByProjectIdAndUserId(projectId, currentUser.getId())) {
            throw new ActionNotAllowedException("Truy cập bị từ chối: Bạn không phải thành viên dự án.");
        }

        // 2. Find Active Sprint
        Sprint activeSprint = sprintRepo.findFirstByProjectIdAndStatusOrderByIdAsc(projectId, SprintStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Dự án chưa có Sprint nào đang chạy (Active)."));

        // 3. Get Columns
        Integer workflowId = activeSprint.getProject().getWorkflow().getId();
        List<WorkflowStep> steps = workflowStepRepo.findAllByWorkflowIdOrderByStepOrderAsc(workflowId);

        // 4. Get Tasks (Eager Load)
        List<Task> tasks = taskRepo.findAllBySprintIdWithDetails(activeSprint.getId());

        // 5. Mapping Logic (O(N))
        Map<Integer, List<Task>> tasksByStatus = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().getId()));

        List<BoardResponse.ColumnResponse> columns = steps.stream().map(step -> {
            Integer statusId = step.getStatus().getId();
            List<Task> taskEntities = tasksByStatus.getOrDefault(statusId, new ArrayList<>());

            List<BoardResponse.TaskPreviewDto> taskDtos = taskEntities.stream()
                    .map(this::mapTaskToDto)
                    .collect(Collectors.toList());

            return BoardResponse.ColumnResponse.builder()
                    .statusId(statusId)
                    .statusName(step.getStatus().getName())
                    .statusCategory(step.getStatus().getStatusCategory().name())
                    .order(step.getStepOrder())
                    .tasks(taskDtos)
                    .build();
        }).collect(Collectors.toList());

        return BoardResponse.builder()
                .sprintId(activeSprint.getId())
                .sprintName(activeSprint.getName())
                .columns(columns)
                .build();
    }

    // =========================================================================
    // 2. MOVE TASK (Kéo thả - Chuyển trạng thái) - ✅ MỚI
    // =========================================================================
    @Transactional
    public void moveTask(Integer taskId, MoveTaskRequest request) {
        User currentUser = userUtils.getCurrentUser();

        // 1. Check Tồn tại
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"));

        WorkflowStatus targetStatus = statusRepo.findById(request.getTargetStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Trạng thái đích không tồn tại"));

        // 2. Check Quyền (Member Project)
        if (!memberRepo.existsByProjectIdAndUserId(task.getProject().getId(), currentUser.getId())) {
            throw new ActionNotAllowedException("Bạn không có quyền chỉnh sửa task trong dự án này.");
        }

        // 3. Check Logic cơ bản (Nếu kéo vào chỗ cũ thì thôi)
        if (task.getStatus().getId().equals(targetStatus.getId())) {
            return;
        }

        // 4. Check Quy trình (Transition Rules)
        Integer workflowId = task.getProject().getWorkflow().getId();
        long rulesCount = transitionRepo.countByWorkflowId(workflowId);

        if (rulesCount > 0) {
            boolean isAllowed = transitionRepo.isTransitionAllowed(workflowId, task.getStatus().getId(),
                    targetStatus.getId());
            if (!isAllowed) {
                throw new ActionNotAllowedException(
                        String.format("Quy trình không cho phép chuyển từ '%s' sang '%s'",
                                task.getStatus().getName(), targetStatus.getName()));
            }
        }
        WorkflowStatus oldStatus = task.getStatus();
        // 5. Update & Save
        task.setStatus(targetStatus);

        // Optional: Update CompletedAt
        if (targetStatus.getStatusCategory() == StatusCategory.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        taskRepo.save(task);

        eventService.logFieldChange(currentUser, task, "Status", oldStatus.getName(), targetStatus.getName());

        // 2. Bắn Noti: "User A đã cập nhật trạng thái của task..."
        eventService.sendUpdateNotification(currentUser, task, "trạng thái sang " + targetStatus.getName());
    }

    // --- Helper Method ---
    private BoardResponse.TaskPreviewDto mapTaskToDto(Task t) {
        String avatar = null;
        if (t.getAssignees() != null && !t.getAssignees().isEmpty()) {
            avatar = t.getAssignees().get(0).getUser().getAvatarUrl();
        }

        String projectCode = t.getProject().getCode();

        return BoardResponse.TaskPreviewDto.builder()
                .id(t.getId())
                .title(t.getTitle())
                .key(projectCode + "-" + t.getTaskIndex())
                .priority(t.getPriority().name())
                .storyPoints(t.getStoryPoints())
                .issueTypeIcon(t.getIssueType().getIconUrl())
                .assigneeAvatar(avatar)
                .build();
    }
}