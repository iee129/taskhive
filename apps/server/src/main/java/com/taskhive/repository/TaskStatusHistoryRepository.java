package com.taskhive.repository;

import com.taskhive.model.TaskStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistory, Long> {
    List<TaskStatusHistory> findByTaskIdOrderByChangedAtAsc(Long taskId);
}
