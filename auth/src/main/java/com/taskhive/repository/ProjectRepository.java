package com.taskhive.repository;

import com.taskhive.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerIdAndDeletedAtIsNull(Long ownerId);
    Optional<Project> findByIdAndDeletedAtIsNull(Long id);
}
