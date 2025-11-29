package com.taskmanager.backend.validator;

import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.enums.IssueLevel;
import com.taskmanager.backend.enums.RoleType;
import com.taskmanager.backend.exception.BusinessException;
import com.taskmanager.backend.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskValidator {

    private final ProjectMemberRepository memberRepo;

    /**
     * Check 1: User có quyền ghi (CREATE) trong Project không?
     * Dùng cho API: Create Task
     */
    public void validateWritePermission(Integer projectId, Integer userId) {
        ProjectMember member = memberRepo.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException("Bạn không phải thành viên dự án này!"));

        if (member.getRole().getName() == RoleType.VIEWER) {
            throw new BusinessException("Bạn chỉ có quyền Xem (Viewer), không được phép tạo Task!");
        }
    }

    /**
     * Check 2: User có quyền sửa (UPDATE) Task này không?
     * Dùng cho API: Update Task
     * Logic: Giống check quyền ghi, nhưng truyền vào Task để tiện lấy ProjectId
     */
    public void validateUpdatePermission(Task task, Integer userId) {
        // Tái sử dụng logic của hàm trên
        validateWritePermission(task.getProject().getId(), userId);
    }

    /**
     * Check 3: Logic Assignee (Người được gán có thuộc dự án không?)
     */
    public void validateAssignee(Integer projectId, User assignee) {
        if (!memberRepo.existsByProjectIdAndUserId(projectId, assignee.getId())) {
            throw new BusinessException("Lỗi: User " + assignee.getEmail() + " không thuộc dự án này, không thể giao việc!");
        }
    }

    /**
     * Check 4: Logic Gia phả (Cha - Con)
     */
    public void validateHierarchy(IssueType currentType, Task parentTask) {
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
    
    /**
     * Check 5: Sprint có thuộc Project không?
     */
    public void validateSprint(Sprint sprint, Integer projectId) {
        if (sprint != null && !sprint.getProject().getId().equals(projectId)) {
            throw new BusinessException("Sprint được chọn không thuộc dự án này!");
        }
    }
}