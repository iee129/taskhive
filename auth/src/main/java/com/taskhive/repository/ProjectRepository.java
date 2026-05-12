package com.taskhive.repository;

import com.taskhive.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerIdAndDeletedAtIsNull(Long ownerId);
    Optional<Project> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT p FROM Project p WHERE p.id IN :ids AND p.deletedAt IS NULL")
    List<Project> findByIdInAndDeletedAtIsNull(@Param("ids") List<Long> ids);
}
