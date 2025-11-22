package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<Loại Entity, Kiểu dữ liệu của ID>
public interface UserRepository extends JpaRepository<User, Integer> {

    // 1. Tìm user theo email
    // Hibernate tự dịch thành: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // 2. Kiểm tra email đã tồn tại chưa (để chặn đăng ký trùng)
    // Hibernate tự dịch thành: SELECT COUNT(*) > 0 FROM users WHERE email = ?
    Boolean existsByEmail(String email);
}