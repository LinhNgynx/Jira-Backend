package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.BoardResponse;
import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.enums.SprintStatus;
import com.taskmanager.backend.exception.ActionNotAllowedException;
import com.taskmanager.backend.exception.ResourceNotFoundException;
import com.taskmanager.backend.repository.ProjectMemberRepository;
import com.taskmanager.backend.repository.SprintRepository;
import com.taskmanager.backend.repository.TaskRepository;
import com.taskmanager.backend.repository.WorkflowStepRepository;
import com.taskmanager.backend.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final ProjectMemberRepository memberRepo;
    private final SprintRepository sprintRepo;
    private final TaskRepository taskRepo;
    private final WorkflowStepRepository workflowStepRepo;
    private final UserUtils userUtils;

    @Transactional(readOnly = true)
    public BoardResponse getBoardData(Integer projectId) {
        User currentUser = userUtils.getCurrentUser();

        // --- BƯỚC 1: SECURITY CHECK (Cực quan trọng) ---
        boolean isMember = memberRepo.existsByProjectIdAndUserId(projectId, currentUser.getId());
        if (!isMember) {
            throw new ActionNotAllowedException("Truy cập bị từ chối: Bạn không phải thành viên dự án.");
        }

        // --- BƯỚC 2: FIND ACTIVE SPRINT ---
        // Lưu ý: Dùng findFirst... để tránh lỗi nếu nhỡ tay có 2 sprint active (dù logic chặn rồi)
        Sprint activeSprint = sprintRepo.findFirstByProjectIdAndStatusOrderByIdAsc(projectId, SprintStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Dự án chưa có Sprint nào đang chạy (Active)."));

        // --- BƯỚC 3: GET COLUMNS (Khung xương) ---
        // Workflow nằm trong Project, không nằm trực tiếp trong Sprint
        Integer workflowId = activeSprint.getProject().getWorkflow().getId(); 
        List<WorkflowStep> steps = workflowStepRepo.findAllByWorkflowIdOrderByStepOrderAsc(workflowId);

        // --- BƯỚC 4: GET TASKS (Thịt) ---
        // Dùng hàm findAllBySprintIdWithDetails (nếu đã viết trong Repo) để fetch luôn avatar/icon
        List<Task> tasks = taskRepo.findAllBySprintIdWithDetails(activeSprint.getId());

        // --- BƯỚC 5: MAPPING & RETURN (Logic "Vibe" O(N)) ---
        
        // 5.1. Gom nhóm Task theo Status ID (Map<Integer, List<Task>>)
        Map<Integer, List<Task>> tasksByStatus = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().getId()));

        // 5.2. Biến đổi từng Step (Entity) thành Column (DTO) và nhét Task vào
        List<BoardResponse.ColumnResponse> columns = steps.stream().map(step -> {
            Integer statusId = step.getStatus().getId();
            
            // Lấy danh sách task của cột này từ Map (Rất nhanh)
            List<Task> taskEntities = tasksByStatus.getOrDefault(statusId, new ArrayList<>());

            // Convert List<Task> -> List<TaskPreviewDto>
            List<BoardResponse.TaskPreviewDto> taskDtos = taskEntities.stream()
                    .map(this::mapTaskToDto) // Gọi hàm helper bên dưới
                    .collect(Collectors.toList());

            return BoardResponse.ColumnResponse.builder()
                    .statusId(statusId)
                    .statusName(step.getStatus().getName())
                    .statusCategory(step.getStatus().getStatusCategory().name()) // TO_DO, DONE...
                    .order(step.getStepOrder())
                    .tasks(taskDtos) // ✅ Task nằm trong cột, không nằm ngoài
                    .build();
        }).collect(Collectors.toList());

        // 5.3. Trả về kết quả cuối cùng
        return BoardResponse.builder()
                .sprintId(activeSprint.getId())
                .sprintName(activeSprint.getName())
                .columns(columns) // ✅ Chỉ cần list cột là đủ
                .build();
    }

    // --- Helper để code gọn hơn ---
    private BoardResponse.TaskPreviewDto mapTaskToDto(Task t) {
        String avatar = null;
        if (t.getAssignees() != null && !t.getAssignees().isEmpty()) {
            avatar = t.getAssignees().get(0).getUser().getAvatarUrl();
        }
        
        // Lấy Project Code để tạo key (VD: "JIRA-123")
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