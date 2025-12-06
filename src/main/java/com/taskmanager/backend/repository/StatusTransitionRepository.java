package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.StatusTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StatusTransitionRepository extends JpaRepository<StatusTransition, Integer> {

    // Đếm xem trong Workflow này có bao nhiêu luật chuyển đổi
    long countByWorkflowId(Integer workflowId);

    // Kiểm tra xem có luật nào cho phép đi từ Status A -> Status B không
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM StatusTransition t " +
           "WHERE t.workflow.id = :workflowId " +
           "AND t.fromStatus.id = :fromId " +
           "AND t.toStatus.id = :toId")
    boolean isTransitionAllowed(@Param("workflowId") Integer workflowId, 
                                @Param("fromId") Integer fromId, 
                                @Param("toId") Integer toId);
}