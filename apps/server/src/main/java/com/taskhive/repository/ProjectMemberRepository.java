package com.taskhive.repository;

import com.taskhive.model.ProjectMember;
import com.taskhive.model.enums.ProjectMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectId(Long projectId);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    long countByProjectIdAndRole(Long projectId, ProjectMemberRole role);

    @Query("SELECT pm.project.id FROM ProjectMember pm WHERE pm.user.id = :userId")
    List<Long> findProjectIdsByUserId(@Param("userId") Long userId);
}
