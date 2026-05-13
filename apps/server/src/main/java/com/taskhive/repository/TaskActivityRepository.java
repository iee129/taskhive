package com.taskhive.repository;

import com.taskhive.model.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {
    List<TaskActivity> findTop50ByOrderByOccurredAtDesc();
    List<TaskActivity> findByTaskIdOrderByOccurredAtDesc(Long taskId);

    @Query("SELECT a FROM TaskActivity a WHERE a.taskId IN " +
           "(SELECT t.id FROM Task t WHERE t.project.id = :projectId AND t.deletedAt IS NULL) " +
           "AND a.occurredAt >= :since ORDER BY a.occurredAt DESC")
    List<TaskActivity> findByProjectIdAndOccurredAtAfter(
            @Param("projectId") Long projectId,
            @Param("since") LocalDateTime since);
}
