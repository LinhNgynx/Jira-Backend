package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import com.taskmanager.backend.enums.RoleType; // (Tùy chọn: xem giải thích bên dưới)

@Entity
@Table(name = "project_roles")
@Getter // ✅ 1. Vẫn nên bỏ @Data để đồng bộ với các Entity khác
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ✅ 2. Cân nhắc dùng Enum nếu role cố định
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING) 
    private RoleType name; 
    
    // HOẶC giữ nguyên String nếu muốn cho phép User tự tạo Role mới
    // private String name; 

    @Column(columnDefinition = "TEXT")
    private String description;
}