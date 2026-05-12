package com.taskhive.repository;

import com.taskhive.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByDeletedAtIsNull();
    Optional<Task> findByIdAndDeletedAtIsNull(Long id);
    List<Task> findByProjectIdAndDeletedAtIsNull(Long projectId);
    List<Task> findByAssigneeIdAndDeletedAtIsNull(Long assigneeId);
}
