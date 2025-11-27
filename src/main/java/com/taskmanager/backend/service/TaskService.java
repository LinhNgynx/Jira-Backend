package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.CreateTaskRequest;
import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.enums.IssueLevel;
import com.taskmanager.backend.enums.RoleType;
import com.taskmanager.backend.enums.TaskPriority;
import com.taskmanager.backend.repository.*;
import com.taskmanager.backend.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepo;
    private final ProjectRepository projectRepo;
    private final IssueTypeRepository issueTypeRepo;
    private final WorkflowStepRepository stepRepo;
    private final SprintRepository sprintRepo;
    private final TaskAssigneeRepository assigneeRepo;
    private final ProjectMemberRepository memberRepo; // Dùng để check member tồn tại
    private final UserRepository userRepo;
    private final UserUtils userUtils;

    @Transactional
    public Task createTask(CreateTaskRequest request) {
        User currentUser = userUtils.getCurrentUser();

        // 1. Tìm Project
        Project project = projectRepo.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Dự án không tồn tại (Project not found)"));

        // 2. CHECK QUYỀN: Người tạo phải là thành viên & KHÔNG được là VIEWER
        ProjectMember currentMember = project.getProjectMembers().stream()
                .filter(m -> m.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Bạn không phải thành viên dự án này!"));

        if (currentMember.getRole().getName() == RoleType.VIEWER) {
            throw new RuntimeException("Bạn chỉ có quyền Xem (Viewer), không được phép tạo Task!");
        }

        // 3. Tìm Issue Type (Loại task)
        IssueType issueType = issueTypeRepo.findById(request.getIssueTypeId())
                .orElseThrow(() -> new RuntimeException("Loại Task (Issue Type) không tồn tại"));

        // 4. Xử lý Cha Con (Hierarchy) & Validate
        Task parentTask = null;
        if (request.getParentTaskId() != null) {
            parentTask = taskRepo.findById(request.getParentTaskId())
                    .orElseThrow(() -> new RuntimeException("Task cha (Parent Task) không tồn tại"));
        }
        // Gọi hàm validate logic gia phả
        validateTaskHierarchy(issueType, parentTask);

        // 5. Tìm Sprint (Nếu có)
        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepo.findById(request.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint không tồn tại"));
            
            // Check kỹ: Sprint này có thuộc Project này không?
            if (!sprint.getProject().getId().equals(project.getId())) {
                throw new RuntimeException("Sprint không thuộc dự án này!");
            }
        }

        // 6. Tự động tìm Status khởi đầu (To Do)
        // Tìm bước 1 (stepOrder = 1) của Workflow dự án
        WorkflowStep startStep = stepRepo.findByWorkflowIdAndStepOrder(project.getWorkflow().getId(), 1)
                .orElseThrow(() -> new RuntimeException("Lỗi cấu hình Workflow: Chưa có bước khởi đầu (Step 1)"));

        // 7. Tự động tính Task Index (Max + 1) -> Để ra key JIRA-1, JIRA-2
        Integer nextIndex = taskRepo.getMaxTaskIndex(project.getId()) + 1;

        // 8. Build & Save Task
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .issueType(issueType)
                .status(startStep.getStatus()) // Auto Status: To Do
                .taskIndex(nextIndex)          // Auto Index: 1, 2, 3...
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .storyPoints(request.getStoryPoints())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .sprint(sprint)         // Gán sprint (hoặc null)
                .parentTask(parentTask) // Gán cha (hoặc null)
                .build();

        Task savedTask = taskRepo.save(task);

        // 9. Lưu Assignees (Người thực hiện) & Validate từng người
        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            List<User> assignees = userRepo.findAllById(request.getAssigneeIds());
            
            for (User user : assignees) {
                // Check: Người được giao có thuộc dự án không?
                boolean isProjectMember = memberRepo.existsByProjectIdAndUserId(project.getId(), user.getId());
                if (!isProjectMember) {
                    throw new RuntimeException("Lỗi: User " + user.getEmail() + " không thuộc dự án này, không thể giao việc!");
                }

                TaskAssignee assignment = TaskAssignee.builder()
                        .task(savedTask)
                        .user(user)
                        .build();
                assigneeRepo.save(assignment);
            }
        }

        return savedTask;
    }

    // --- HÀM PHỤ: Validate Logic Gia Phả (Cha Con) ---
    private void validateTaskHierarchy(IssueType currentType, Task parentTask) {
        // LEVEL 0: EPIC -> Không được có cha
        if (currentType.getLevel() == IssueLevel.EPIC) {
            if (parentTask != null) throw new RuntimeException("Lỗi: Epic không được phép có cha!");
            return;
        }

        // LEVEL 2: SUBTASK -> Bắt buộc có cha là Standard
        if (currentType.getLevel() == IssueLevel.SUBTASK) {
            if (parentTask == null) throw new RuntimeException("Lỗi: Subtask bắt buộc phải có cha!");
            
            if (parentTask.getIssueType().getLevel() != IssueLevel.STANDARD) {
                throw new RuntimeException("Lỗi: Cha của Subtask phải là Story, Task hoặc Bug!");
            }
            return;
        }

        // LEVEL 1: STANDARD (Story/Task/Bug) -> Cha (nếu có) phải là Epic
        if (currentType.getLevel() == IssueLevel.STANDARD) {
            if (parentTask != null) {
                if (parentTask.getIssueType().getLevel() != IssueLevel.EPIC) {
                    throw new RuntimeException("Lỗi: Task thường chỉ được phép thuộc về Epic (hoặc không có cha)!");
                }
            }
        }
    }
}