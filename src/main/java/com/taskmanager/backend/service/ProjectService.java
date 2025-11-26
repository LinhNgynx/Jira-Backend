package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.CreateProjectRequest;
import com.taskmanager.backend.dto.ProjectResponse;
import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.enums.ProjectStatus;
import com.taskmanager.backend.enums.RoleType;
import com.taskmanager.backend.repository.*;
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

    @Transactional // Quan trọng: Lỗi 1 bước là rollback hết
    public ProjectResponse createProject(CreateProjectRequest request) {
        
        // 1. Lấy User hiện tại (từ Token)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Validate Mã dự án
        if (projectRepo.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã dự án " + request.getCode() + " đã tồn tại!");
        }

        // 3. Lấy Workflow mặc định (Bắt buộc DB phải có trước)
        Workflow defaultWorkflow = workflowRepo.findByName("Basic Workflow")
                .orElseThrow(() -> new RuntimeException("Hệ thống chưa cấu hình Workflow (Basic Workflow)"));

        // 4. Tạo Project
        Project project = Project.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .owner(currentUser)
                .workflow(defaultWorkflow)
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
                .workflowName(defaultWorkflow.getName())
                .status(savedProject.getStatus().toString())
                .createdAt(savedProject.getCreatedAt())
                .build();
    }
}