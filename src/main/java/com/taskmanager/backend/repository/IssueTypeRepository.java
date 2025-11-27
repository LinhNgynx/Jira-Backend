package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.IssueType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueTypeRepository extends JpaRepository<IssueType, Integer> {
    // Hiện tại chỉ cần các hàm có sẵn: findById, findAll...
}