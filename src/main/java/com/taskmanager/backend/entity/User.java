package com.taskmanager.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction; // ✅ IMPORT MỚI
import com.taskmanager.backend.enums.UserStatus;
import java.util.List;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false") // ✅ THAY THẾ @Where
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Builder.Default
    @Column(name = "is_superuser")
    private Boolean isSuperuser = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ProjectMember> projectMembers;
}