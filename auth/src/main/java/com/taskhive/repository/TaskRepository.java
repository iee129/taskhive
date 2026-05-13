package com.taskhive.repository;

import com.taskhive.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByDeletedAtIsNull();
    Optional<Task> findByIdAndDeletedAtIsNull(Long id);
    List<Task> findByProjectIdAndDeletedAtIsNull(Long projectId);
    List<Task> findByProjectIdAndStatusAndDeletedAtIsNull(Long projectId, Task.Status status);
    List<Task> findByAssigneeIdAndDeletedAtIsNull(Long assigneeId);
    long countByDeletedAtIsNull();
    long countByStatusAndDeletedAtIsNull(Task.Status status);
    long countByPriorityAndDeletedAtIsNull(Task.Priority priority);

    @Query("SELECT t FROM Task t WHERE t.deletedAt IS NULL " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Task> findFiltered(@Param("status") Task.Status status,
                            @Param("priority") Task.Priority priority,
                            @Param("search") String search);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = 'IN_PROGRESS' AND t.deletedAt IS NULL AND t.updatedAt < :cutoff")
    List<Task> findBlockers(@Param("projectId") Long projectId, @Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT t FROM Task t WHERE t.deletedAt IS NULL AND t.project.id IN :projectIds " +
           "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Task> searchByKeyword(@Param("q") String q, @Param("projectIds") List<Long> projectIds);
}
