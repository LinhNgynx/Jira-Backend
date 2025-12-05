package com.taskmanager.backend.repository;
import com.taskmanager.backend.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Integer> {
    // Tìm bước đầu tiên (stepOrder = 1) của workflow
    Optional<WorkflowStep> findByWorkflowIdAndStepOrder(Integer workflowId, Integer stepOrder);
    List<WorkflowStep> findAllByWorkflowIdOrderByStepOrderAsc(Integer workflowId);
}
