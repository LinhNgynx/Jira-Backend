package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.CreateTaskRequest;
import com.taskmanager.backend.entity.*;
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
    private final UserRepository userRepo;
    private final TaskAssigneeRepository assigneeRepo;
    private final UserUtils userUtils;

    @Transactional
    public Task createTask(CreateTaskRequest request) {
        User currentUser = userUtils.getCurrentUser();

        // 1. Tìm Project
        Project project = projectRepo.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // 2. CHECK QUYỀN: User phải là thành viên dự án
        boolean isMember = project.getProjectMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(currentUser.getId()));
        if (!isMember) {
            throw new RuntimeException("Bạn không phải thành viên dự án này!");
        }

        // 3. Tìm Issue Type
        IssueType issueType = issueTypeRepo.findById(request.getIssueTypeId())
                .orElseThrow(() -> new RuntimeException("Issue Type not found"));

        // 4. LOGIC TỰ ĐỘNG STATUS: Luôn lấy bước 1 (To Do)
        WorkflowStep startStep = stepRepo.findByWorkflowIdAndStepOrder(project.getWorkflow().getId(), 1)
                .orElseThrow(() -> new RuntimeException("Workflow lỗi: Chưa cấu hình bước 1"));
        WorkflowStatus initialStatus = startStep.getStatus();

        // 5. LOGIC TỰ ĐỘNG TASK INDEX:
        // Lấy max hiện tại + 1. Ví dụ max là 10 thì cái này là 11.
        Integer nextIndex = taskRepo.getMaxTaskIndex(project.getId()) + 1;

        // 6. Tạo Task Entity
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .issueType(issueType)
                .status(initialStatus) // Tự gán To Do
                .taskIndex(nextIndex)  // Tự gán Index (VD: 1, 2, 3)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .build();

        Task savedTask = taskRepo.save(task);

        // 7. Xử lý Assignees (Giao việc)
        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            List<User> assignees = userRepo.findAllById(request.getAssigneeIds());
            
            // Validate: Tất cả người được giao phải thuộc dự án (Optional - Làm kỹ thì thêm vào)
            
            for (User user : assignees) {
                TaskAssignee assignment = TaskAssignee.builder()
                        .task(savedTask)
                        .user(user)
                        .build();
                assigneeRepo.save(assignment);
            }
        }

        return savedTask;
    }
}