package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.ProjectRole;
import com.taskmanager.backend.enums.RoleType; // Nhớ import Enum này
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Integer> {
    
    // Spring Data JPA "thần thánh" sẽ tự dịch tên hàm này thành SQL:
    // SELECT * FROM project_roles WHERE name = ?
    Optional<ProjectRole> findByName(RoleType name);
}

