package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.CreateTaskRequest;
import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.enums.IssueLevel;
import com.taskmanager.backend.enums.RoleType;
import com.taskmanager.backend.enums.TaskPriority;
// ✅ Import custom exceptions
import com.taskmanager.backend.exception.BusinessException;
import com.taskmanager.backend.exception.ResourceNotFoundException;
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
    private final ProjectMemberRepository memberRepo;
    private final UserRepository userRepo;
    private final UserUtils userUtils;

    @Transactional
    public Task createTask(CreateTaskRequest request) {
        User currentUser = userUtils.getCurrentUser();

        // 1. Tìm Project (Nếu không thấy -> 404 Not Found)
        Project project = projectRepo.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Dự án không tồn tại (ID: " + request.getProjectId() + ")"));

        // 2. CHECK QUYỀN (Nếu vi phạm -> 400 Bad Request)
        ProjectMember currentMember = project.getProjectMembers().stream()
                .filter(m -> m.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Bạn không phải thành viên dự án này!"));

        if (currentMember.getRole().getName() == RoleType.VIEWER) {
            throw new BusinessException("Bạn chỉ có quyền Xem (Viewer), không được phép tạo Task!");
        }

        // 3. Tìm Issue Type (Nếu không thấy -> 404)
        IssueType issueType = issueTypeRepo.findById(request.getIssueTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Loại Task (Issue Type) không tồn tại"));

        // 4. Xử lý Cha Con & Validate
        Task parentTask = null;
        if (request.getParentTaskId() != null) {
            parentTask = taskRepo.findById(request.getParentTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task cha (Parent Task) không tồn tại"));
        }
        
        // Gọi hàm validate (Ném BusinessException nếu sai luật)
        validateTaskHierarchy(issueType, parentTask);

        // 5. Tìm Sprint (Nếu có)
        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepo.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint không tồn tại"));
            
            // Check logic: Sprint phải thuộc Project (Lỗi logic -> 400)
            if (!sprint.getProject().getId().equals(project.getId())) {
                throw new BusinessException("Sprint được chọn không thuộc dự án này!");
            }
        }

        // 6. Tự động tìm Status khởi đầu
        WorkflowStep startStep = stepRepo.findByWorkflowIdAndStepOrder(project.getWorkflow().getId(), 1)
                .orElseThrow(() -> new BusinessException("Lỗi cấu hình Workflow: Dự án chưa có bước khởi đầu (Step 1/To Do)"));

        // 7. Tự động tính Task Index
        Integer nextIndex = taskRepo.getMaxTaskIndex(project.getId()) + 1;

        // 8. Build & Save Task
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

        // 9. Lưu Assignees
        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            List<User> assignees = userRepo.findAllById(request.getAssigneeIds());
            
            for (User user : assignees) {
                boolean isProjectMember = memberRepo.existsByProjectIdAndUserId(project.getId(), user.getId());
                if (!isProjectMember) {
                    throw new BusinessException("Lỗi: User " + user.getEmail() + " không thuộc dự án này, không thể giao việc!");
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

    // --- HÀM PHỤ: Validate Logic Gia Phả ---
    private void validateTaskHierarchy(IssueType currentType, Task parentTask) {
        // LEVEL 0: EPIC
        if (currentType.getLevel() == IssueLevel.EPIC) {
            if (parentTask != null) throw new BusinessException("Lỗi: Epic không được phép có cha!");
            return;
        }

        // LEVEL 2: SUBTASK
        if (currentType.getLevel() == IssueLevel.SUBTASK) {
            if (parentTask == null) throw new BusinessException("Lỗi: Subtask bắt buộc phải có cha!");
            
            if (parentTask.getIssueType().getLevel() != IssueLevel.STANDARD) {
                throw new BusinessException("Lỗi: Cha của Subtask phải là Story, Task hoặc Bug!");
            }
            return;
        }

        // LEVEL 1: STANDARD
        if (currentType.getLevel() == IssueLevel.STANDARD) {
            if (parentTask != null) {
                if (parentTask.getIssueType().getLevel() != IssueLevel.EPIC) {
                    throw new BusinessException("Lỗi: Task thường chỉ được phép thuộc về Epic (hoặc không có cha)!");
                }
            }
        }
    }
}