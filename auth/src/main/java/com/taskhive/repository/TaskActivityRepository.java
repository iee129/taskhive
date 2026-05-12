package com.taskhive.repository;

import com.taskhive.model.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {
    List<TaskActivity> findTop50ByOrderByOccurredAtDesc();
    List<TaskActivity> findByTaskIdOrderByOccurredAtDesc(Long taskId);
}
