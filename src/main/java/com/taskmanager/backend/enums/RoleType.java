package com.taskmanager.backend.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    
    // 1. Ông chủ sản phẩm: Người quyết định làm cái gì
    PRODUCT_OWNER("Product Owner", "Người định hướng sản phẩm, quản lý Backlog"),

    // 2. Quản trò: Người đảm bảo quy trình, gỡ rối cho team
    SCRUM_MASTER("Scrum Master", "Người điều phối quy trình, hỗ trợ team"),

    // 3. Đội ngũ thực thi: Code chính
    DEVELOPER("Developer", "Lập trình viên, người thực hiện task"),

    // 4. Đội ngũ kiểm thử: Bắt bug
    TESTER("Tester", "Người kiểm thử chất lượng phần mềm"),
    
    // 5. Khách mời: Chỉ xem, không sửa
    VIEWER("Viewer", "Thành viên chỉ có quyền xem");

    // --- Phần mở rộng của Enum (Java Enum rất mạnh) ---
    
    private final String displayName;
    private final String description;

    RoleType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}