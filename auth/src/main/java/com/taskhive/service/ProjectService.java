package com.taskhive.service;

import com.taskhive.dto.ProjectRequest;
import com.taskhive.dto.ProjectResponse;
import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.Project;
import com.taskhive.model.User;
import com.taskhive.repository.ProjectRepository;
import com.taskhive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "projects", key = "#email")
    public List<ProjectResponse> getMyProjects(String email) {
        User owner = findUserByEmail(email);
        return projectRepository.findByOwnerIdAndDeletedAtIsNull(owner.getId()).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    public ProjectResponse getProject(Long id) {
        return projectRepository.findByIdAndDeletedAtIsNull(id)
                .map(ProjectResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    @CacheEvict(value = "projects", key = "#email")
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, String email) {
        User owner = findUserByEmail(email);
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();
        return ProjectResponse.from(projectRepository.save(project));
    }

    @CacheEvict(value = "projects", key = "#email")
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, String email) {
        Project project = findActiveProject(id);
        checkOwner(project, email);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @CacheEvict(value = "projects", key = "#email")
    @Transactional
    public void deleteProject(Long id, String email) {
        Project project = findActiveProject(id);
        checkOwner(project, email);
        project.setDeletedAt(LocalDateTime.now());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Project findActiveProject(Long id) {
        return projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private void checkOwner(Project project, String email) {
        if (!project.getOwner().getEmail().equals(email)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
