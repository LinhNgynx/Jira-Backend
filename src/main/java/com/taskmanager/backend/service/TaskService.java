package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.CreateTaskRequest;
import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.enums.ActivityAction;
import com.taskmanager.backend.enums.IssueLevel;
import com.taskmanager.backend.enums.NotificationType;
import com.taskmanager.backend.enums.RoleType;
import com.taskmanager.backend.enums.TaskPriority;
import com.taskmanager.backend.event.SystemEvent; // Import Event
import com.taskmanager.backend.exception.BusinessException;
import com.taskmanager.backend.exception.ResourceNotFoundException;
import com.taskmanager.backend.repository.*;
import com.taskmanager.backend.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher; // Import Publisher
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    // 🔥 Thay ActivityLogService bằng EventPublisher
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Task createTask(CreateTaskRequest request) {
        User currentUser = userUtils.getCurrentUser();

        // 1. Tìm Project
        Project project = projectRepo.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Dự án không tồn tại (ID: " + request.getProjectId() + ")"));

        // 2. CHECK QUYỀN
        ProjectMember currentMember = memberRepo.findByProjectIdAndUserId(project.getId(), currentUser.getId())
                .orElseThrow(() -> new BusinessException("Bạn không phải thành viên dự án này!"));
        
        if (currentMember.getRole().getName() == RoleType.VIEWER) {
            throw new BusinessException("Bạn chỉ có quyền Xem (Viewer), không được phép tạo Task!");
        }

        // 3. Tìm Issue Type
        IssueType issueType = issueTypeRepo.findById(request.getIssueTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Loại Task (Issue Type) không tồn tại"));

        // 4. Xử lý Cha Con & Validate
        Task parentTask = null;
        if (request.getParentTaskId() != null) {
            parentTask = taskRepo.findById(request.getParentTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task cha (Parent Task) không tồn tại"));
        }
        
        validateTaskHierarchy(issueType, parentTask);

        // 5. Tìm Sprint
        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepo.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint không tồn tại"));
            
            if (!sprint.getProject().getId().equals(project.getId())) {
                throw new BusinessException("Sprint được chọn không thuộc dự án này!");
            }
        }

        // 6. Tự động tìm Status khởi đầu
        WorkflowStep startStep = stepRepo.findByWorkflowIdAndStepOrder(project.getWorkflow().getId(), 1)
                .orElseThrow(() -> new BusinessException("Lỗi cấu hình Workflow: Dự án chưa có bước khởi đầu (Step 1/To Do)"));

        // 7. Tự động tính Task Index
        Integer maxIndex = taskRepo.getMaxTaskIndex(project.getId());
        Integer nextIndex = (maxIndex == null ? 0 : maxIndex) + 1;

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

        // 9. Lưu Assignees và xác định người nhận thông báo
        List<User> notificationRecipients = new ArrayList<>(); // Danh sách người sẽ nhận Noti

        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            List<User> assignees = userRepo.findAllById(request.getAssigneeIds());
            
            for (User user : assignees) {
                // Check member
                if (!memberRepo.existsByProjectIdAndUserId(project.getId(), user.getId())) {
                    throw new BusinessException("Lỗi: User " + user.getEmail() + " không thuộc dự án này!");
                }

                TaskAssignee assignment = TaskAssignee.builder()
                        .task(savedTask)
                        .user(user)
                        .build();
                assigneeRepo.save(assignment);
                
                // Thêm vào list nhận thông báo (trừ chính mình ra)
                if (!user.getId().equals(currentUser.getId())) {
                    notificationRecipients.add(user);
                }
            }
        }

        // 🔥 10. BẮN SỰ KIỆN (EVENT)
        // Logic: Mỗi người được assign sẽ nhận 1 thông báo riêng
        String taskKey = project.getCode() + "-" + nextIndex;
        
        if (notificationRecipients.isEmpty()) {
            // Trường hợp 1: Không assign cho ai (hoặc assign cho chính mình)
            // -> Chỉ bắn Event để ghi Log, không bắn Noti (recipient = null)
            eventPublisher.publishEvent(new SystemEvent(
                    this,
                    currentUser,
                    savedTask,
                    ActivityAction.CREATED,
                    "Created task " + taskKey,
                    null, null, null // Không gửi Noti
            ));
        } else {
            // Trường hợp 2: Có assign cho người khác
            // -> Bắn Event cho từng người (để mỗi người nhận được 1 noti riêng)
            // Lưu ý: Log chỉ cần ghi 1 lần là đủ, nên ta chỉ set Log Action cho người đầu tiên
            boolean isLogRecorded = false;

            for (User recipient : notificationRecipients) {
                eventPublisher.publishEvent(new SystemEvent(
                        this,
                        currentUser,
                        savedTask,
                        isLogRecorded ? null : ActivityAction.CREATED, // Chỉ ghi log lần đầu
                        isLogRecorded ? null : "Created task " + taskKey,
                        recipient,
                        NotificationType.TASK_ASSIGNED,
                        currentUser.getFullName() + " đã gán bạn vào task: " + savedTask.getTitle()
                ));
                isLogRecorded = true;
            }
        }

        return savedTask;
    }

    // --- HÀM PHỤ giữ nguyên ---
    private void validateTaskHierarchy(IssueType currentType, Task parentTask) {
        if (currentType.getLevel() == IssueLevel.EPIC) {
            if (parentTask != null) throw new BusinessException("Lỗi: Epic không được phép có cha!");
            return;
        }
        if (currentType.getLevel() == IssueLevel.SUBTASK) {
            if (parentTask == null) throw new BusinessException("Lỗi: Subtask bắt buộc phải có cha!");
            if (parentTask.getIssueType().getLevel() != IssueLevel.STANDARD) {
                throw new BusinessException("Lỗi: Cha của Subtask phải là Story, Task hoặc Bug!");
            }
            return;
        }
        if (currentType.getLevel() == IssueLevel.STANDARD) {
            if (parentTask != null) {
                if (parentTask.getIssueType().getLevel() != IssueLevel.EPIC) {
                    throw new BusinessException("Lỗi: Task thường chỉ được phép thuộc về Epic (hoặc không có cha)!");
                }
            }
        }
    }
}