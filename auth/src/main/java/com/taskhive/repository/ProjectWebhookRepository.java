package com.taskhive.repository;

import com.taskhive.model.ProjectWebhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectWebhookRepository extends JpaRepository<ProjectWebhook, Long> {
    List<ProjectWebhook> findByProjectId(Long projectId);
    List<ProjectWebhook> findByProjectIdAndEnabledTrue(Long projectId);
}
