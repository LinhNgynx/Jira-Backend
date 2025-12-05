package com.taskmanager.backend.validator;

import com.taskmanager.backend.entity.ProjectMember;
import com.taskmanager.backend.entity.Sprint;
import com.taskmanager.backend.enums.RoleType;
import com.taskmanager.backend.enums.SprintDuration;
import com.taskmanager.backend.enums.SprintStatus;
import com.taskmanager.backend.exception.ActionNotAllowedException; // Custom Exception của bạn
import com.taskmanager.backend.exception.BusinessException;
import com.taskmanager.backend.exception.ResourceNotFoundException;
import com.taskmanager.backend.repository.ProjectMemberRepository;
import com.taskmanager.backend.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SprintValidator {

    private final ProjectMemberRepository memberRepo;
    private final SprintRepository sprintRepo;

    /**
     * 1. Check quyền: User có phải là Admin/Owner của Project không?
     * Sprint là tài nguyên cấp cao, Member thường không được đụng vào.
     */
    public void validateManagePermission(Integer projectId, Integer userId) {
        ProjectMember member = memberRepo.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ActionNotAllowedException("Bạn không phải thành viên dự án này"));

        // Giả sử dùng Enum RoleType (ADMIN, MEMBER...)
        if (member.getRole().getName() != RoleType.PRODUCT_OWNER && 
            member.getRole().getName() != RoleType.SCRUM_MASTER) {
            throw new ActionNotAllowedException("Bạn không có quyền quản lý Sprint (cần quyền PO hoặc Scrum Master)");
        }
    }

    public void validateStartSprint(Sprint sprint, LocalDate startDate, LocalDate endDate) {
        if (sprint.getStatus() != SprintStatus.UPCOMING) {
            throw new ActionNotAllowedException("Chỉ có thể bắt đầu Sprint đang ở trạng thái Upcoming.");
        }
        validateDates(startDate, endDate);
        
        if (sprintRepo.existsByProjectIdAndStatus(sprint.getProject().getId(), SprintStatus.ACTIVE)) {
            throw new ActionNotAllowedException("Dự án đang có một Sprint đang chạy.");
        }
    }

    // --- 3. Validate Edit Sprint (Mới) ---
    public void validateUpdateSprint(Sprint sprint) {
        if (sprint.getStatus() != SprintStatus.UPCOMING) {
            throw new ActionNotAllowedException("Chỉ có thể chỉnh sửa Sprint khi nó chưa bắt đầu (Upcoming).");
        }
    }

    // --- 4. Validate Logic Ngày tháng chung (Tách ra để dùng lại) ---
    public void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) throw new BusinessException("Ngày bắt đầu không được để trống.");
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu.");
        }
    }

    // --- 5. Tính toán EndDate dựa trên Duration (Mới) ---
    public LocalDate calculateAndValidateEndDate(SprintDuration duration, LocalDate startDate, LocalDate requestedEndDate) {
        if (duration == SprintDuration.CUSTOM) {
            if (requestedEndDate == null) throw new BusinessException("Với thời lượng Custom, bạn phải nhập ngày kết thúc.");
            validateDates(startDate, requestedEndDate);
            return requestedEndDate;
        } else {
            return startDate.plusWeeks(duration.getWeeks());
        }
    }

    // --- 6. Validate Complete Sprint (Cơ bản) ---
    public void validateCompleteSprint(Sprint sprint) {
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new ActionNotAllowedException("Chỉ có thể kết thúc Sprint đang chạy (Active).");
        }
    }

    // --- 7. 🔥 VALIDATE TARGET SPRINT KHI MOVE TASK (Cái bạn cần) ---
    // Hàm này trả về Target Sprint luôn để Service đỡ phải query lại
    public Sprint validateTargetSprint(Integer targetSprintId, Sprint currentSprint) {
        // a. Tồn tại?
        Sprint targetSprint = sprintRepo.findById(targetSprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Target Sprint ID không tồn tại"));

        // b. Cùng Project?
        if (!targetSprint.getProject().getId().equals(currentSprint.getProject().getId())) {
            throw new BusinessException("Không thể chuyển task sang Sprint của dự án khác");
        }

        // c. Không trùng chính nó?
        if (targetSprint.getId().equals(currentSprint.getId())) {
            throw new BusinessException("Không thể chuyển task vào chính Sprint đang đóng");
        }

        // d. Không phải đã đóng?
        if (targetSprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BusinessException("Không thể chuyển task vào Sprint đã hoàn thành");
        }

        return targetSprint;
    }
}