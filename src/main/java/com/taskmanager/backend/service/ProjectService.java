package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.CreateProjectRequest;
import com.taskmanager.backend.dto.ProjectResponse;
import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.enums.ProjectStatus;
import com.taskmanager.backend.enums.RoleType;
import com.taskmanager.backend.repository.*;
import com.taskmanager.backend.utils.UserUtils;
import com.taskmanager.backend.dto.ProjectListResponse;
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
}