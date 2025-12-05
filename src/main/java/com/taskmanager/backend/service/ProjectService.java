package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.BacklogResponse;
import com.taskmanager.backend.dto.CreateProjectRequest;
import com.taskmanager.backend.dto.ProjectDetailResponse;
import com.taskmanager.backend.dto.ProjectResponse;
import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.enums.ProjectStatus;
import com.taskmanager.backend.enums.RoleType;
import com.taskmanager.backend.enums.SprintStatus;
import com.taskmanager.backend.repository.*;
import com.taskmanager.backend.utils.UserUtils;
import com.taskmanager.backend.dto.ProjectListResponse;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

        private final ProjectRepository projectRepo;
        private final ProjectRoleRepository roleRepo;
        private final WorkflowRepository workflowRepo;
        private final ProjectMemberRepository memberRepo;
        private final SprintRepository sprintRepo; // ✅ MỚI: Để lấy danh sách Sprint
        private final TaskRepository taskRepo; // ✅ MỚI: Để lấy danh sách Task
        private final UserRepository userRepo;
        private final UserUtils userUtils;

        @Transactional // Quan trọng: Lỗi 1 bước là rollback hết
        public ProjectResponse createProject(CreateProjectRequest request) {

                User currentUser = userUtils.getCurrentUser();

                // 2. Validate Mã dự án
                if (projectRepo.existsByCode(request.getCode())) {
                        throw new IllegalArgumentException("Mã dự án " + request.getCode() + " đã tồn tại!");
                }

                // 3. Lấy Workflow mặc định (Bắt buộc DB phải có trước)
                Workflow workflow;

                if (request.getWorkflowId() != null) {
                        // CASE A: User có chọn Workflow (Gửi ID lên)
                        workflow = workflowRepo.findById(request.getWorkflowId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Workflow ID " + request.getWorkflowId() + " không tồn tại!"));
                } else {
                        // CASE B: User lười không chọn -> Hệ thống lấy mặc định
                        workflow = workflowRepo.findByName("Basic Workflow")
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Lỗi hệ thống: Chưa cấu hình Workflow mặc định"));
                }

                // 4. Tạo Project
                Project project = Project.builder()
                                .name(request.getName())
                                .code(request.getCode())
                                .description(request.getDescription())
                                .owner(currentUser)
                                .workflow(workflow)
                                .status(ProjectStatus.ACTIVE) // Nhớ tạo Enum ProjectStatus nhé
                                .build();

                Project savedProject = projectRepo.save(project);

                // 5. Tìm Role PRODUCT_OWNER trong DB (Dựa vào Enum)
                ProjectRole ownerRole = roleRepo.findByName(RoleType.PRODUCT_OWNER)
                                .orElseThrow(() -> new RuntimeException("Hệ thống chưa cấu hình Role (PRODUCT_OWNER)"));

                // 6. Add User vào bảng Member với Role đó
                ProjectMember membership = ProjectMember.builder()
                                .project(savedProject)
                                .user(currentUser)
                                .role(ownerRole) // Lưu Entity Role vào
                                .build();

                memberRepo.save(membership);

                // 7. Trả về kết quả
                return ProjectResponse.builder()
                                .id(savedProject.getId())
                                .name(savedProject.getName())
                                .code(savedProject.getCode())
                                .description(savedProject.getDescription())
                                .ownerName(currentUser.getFullName())
                                .workflowName(workflow.getName())
                                .status(savedProject.getStatus().toString())
                                .createdAt(savedProject.getCreatedAt())
                                .build();
        }

        @Transactional(readOnly = true) // Tối ưu hiệu năng vì chỉ đọc
        public List<ProjectListResponse> getMyProjects() {
                // 1. Lấy User hiện tại
                User currentUser = userUtils.getCurrentUser();

                // 2. Query DB lấy danh sách Project
                List<Project> projects = projectRepo.findProjectsByUserEmail(currentUser.getEmail());

                // 3. Map sang DTO
                return projects.stream().map(project -> {

                        // Logic tìm Role của mình trong dự án này
                        String myRole = project.getProjectMembers().stream()
                                        .filter(m -> m.getUser().getId().equals(currentUser.getId()))
                                        .findFirst()
                                        .map(m -> m.getRole().getName().toString()) // Lấy tên Enum
                                        .orElse("MEMBER");

                        return ProjectListResponse.builder()
                                        .id(project.getId())
                                        .name(project.getName())
                                        .code(project.getCode())
                                        .ownerName(project.getOwner().getFullName())
                                        .myRole(myRole)
                                        .build();
                }).collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public ProjectDetailResponse getProjectDetail(Integer projectId) {
                // 1. Lấy User đang đăng nhập
                User currentUser = userUtils.getCurrentUser();

                // 2. Tìm dự án
                Project project = projectRepo.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Dự án không tồn tại"));

                // 3. 🛡️ BẢO MẬT: Kiểm tra xem User có phải thành viên không?
                // Logic: Lọc trong list member xem có ai trùng ID với mình không
                boolean isMember = project.getProjectMembers().stream()
                                .anyMatch(pm -> pm.getUser().getId().equals(currentUser.getId()));

                if (!isMember) {
                        throw new RuntimeException("Truy cập bị từ chối! Bạn không phải thành viên dự án này.");
                        // Thực tế nên ném custom exception trả về 403 Forbidden
                }

                // 4. Map danh sách thành viên sang DTO
                List<ProjectDetailResponse.MemberDto> memberDtos = project.getProjectMembers().stream()
                                .map(pm -> ProjectDetailResponse.MemberDto.builder()
                                                .userId(pm.getUser().getId())
                                                .fullName(pm.getUser().getFullName())
                                                .email(pm.getUser().getEmail())
                                                .avatarUrl(pm.getUser().getAvatarUrl())
                                                .role(pm.getRole().getName().toString())
                                                .build())
                                .collect(Collectors.toList());

                // 5. Map Project sang DTO
                return ProjectDetailResponse.builder()
                                .id(project.getId())
                                .name(project.getName())
                                .code(project.getCode())
                                .description(project.getDescription())
                                .status(project.getStatus().toString())
                                .workflowName(project.getWorkflow().getName())
                                .owner(ProjectDetailResponse.UserSummaryDto.builder()
                                                .id(project.getOwner().getId())
                                                .fullName(project.getOwner().getFullName())
                                                .email(project.getOwner().getEmail())
                                                .avatarUrl(project.getOwner().getAvatarUrl())
                                                .build())
                                .members(memberDtos)
                                .createdAt(project.getCreatedAt())
                                .build();
        }

        /**
         * API: Lấy dữ liệu màn hình Backlog (Gồm Sprint Active, Planned và Backlog)
         * Đã tối ưu code: Tách logic map DTO ra hàm riêng.
         */
        @Transactional(readOnly = true)
        public BacklogResponse getBacklogData(Integer projectId) {
                // 1. Lấy dữ liệu thô từ DB
                Project project = projectRepo.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Dự án không tồn tại"));

                List<Sprint> sprints = sprintRepo.findActiveAndUpcomingSprints(projectId);

                // Lưu ý: taskRepo phải dùng câu @Query JOIN FETCH để tối ưu hiệu năng (tránh
                // lỗi N+1)
                List<Task> allTasks = taskRepo.findTasksForBacklog(projectId, SprintStatus.COMPLETED);

                // 2. NHÓM 1: Xử lý các Sprint (Active/Planned)
                List<BacklogResponse.SprintDto> sprintDtos = sprints.stream().map(sprint -> {
                        // Lọc task thuộc sprint này
                        List<Task> tasksInSprint = allTasks.stream()
                                        .filter(t -> t.getSprint() != null
                                                        && t.getSprint().getId().equals(sprint.getId()))
                                        .collect(Collectors.toList());

                        return BacklogResponse.SprintDto.builder()
                                        .id(sprint.getId())
                                        .name(sprint.getName())
                                        .status(sprint.getStatus().toString())
                                        .startDate(sprint.getStartDate() != null ? sprint.getStartDate().toString()
                                                        : "")
                                        .endDate(sprint.getEndDate() != null ? sprint.getEndDate().toString() : "")
                                        .totalIssues(tasksInSprint.size())
                                        .tasks(mapTasksToDtos(tasksInSprint, project.getCode())) // ✅ Gọi hàm con để map
                                        .build();
                }).collect(Collectors.toList());

                // 3. NHÓM 2: Xử lý Backlog (Task chưa vào Sprint)
                List<Task> backlogTasksRaw = allTasks.stream()
                                .filter(t -> t.getSprint() == null) // Quan trọng: Sprint ID là null
                                .collect(Collectors.toList());

                // 4. Trả về kết quả tổng hợp
                return BacklogResponse.builder()
                                .projectId(project.getId())
                                .projectName(project.getName())
                                .sprints(sprintDtos)
                                .backlogTasks(mapTasksToDtos(backlogTasksRaw, project.getCode())) // ✅ Gọi hàm con để
                                                                                                  // map
                                .build();
        }

        /**
         * HÀM PHỤ (HELPER METHOD)
         * Nhiệm vụ: Chuyển đổi List<Task> Entity -> List<TaskDto>
         * Giúp code chính không bị rối mắt.
         */
        private List<BacklogResponse.TaskDto> mapTasksToDtos(List<Task> tasks, String projectCode) {
                return tasks.stream()
                                .map(task -> {
                                        // Logic lấy Avatar Assignee (An toàn với null)
                                        String avatar = null;
                                        if (task.getAssignees() != null && !task.getAssignees().isEmpty()) {
                                                avatar = task.getAssignees().get(0).getUser().getAvatarUrl();
                                        }

                                        // Logic tạo Key hiển thị (VD: "SCRUM-10")
                                        String taskKey = projectCode + "-" + task.getTaskIndex();

                                        return BacklogResponse.TaskDto.builder()
                                                        .id(task.getId())
                                                        .key(taskKey)
                                                        .title(task.getTitle())
                                                        .priority(task.getPriority().name())
                                                        .storyPoints(task.getStoryPoints())
                                                        .issueTypeIcon(task.getIssueType().getIconUrl())
                                                        .statusName(task.getStatus().getName())
                                                        .statusColor(task.getStatus().getColorCode())
                                                        .assigneeAvatar(avatar)
                                                        .build();
                                })
                                // Sắp xếp: Task mới nhất (ID lớn nhất) lên đầu
                                .sorted(Comparator.comparing(BacklogResponse.TaskDto::getId).reversed())
                                .collect(Collectors.toList());
        }
}