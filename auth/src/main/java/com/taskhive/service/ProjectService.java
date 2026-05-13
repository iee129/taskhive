package com.taskhive.service;

import com.taskhive.dto.ProjectRequest;
import com.taskhive.dto.ProjectResponse;
import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.Project;
import com.taskhive.model.ProjectMember;
import com.taskhive.model.User;
import com.taskhive.model.enums.ProjectMemberRole;
import com.taskhive.repository.ProjectMemberRepository;
import com.taskhive.repository.ProjectRepository;
import com.taskhive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository memberRepository;

    @Cacheable(value = "projects", key = "#email")
    public List<ProjectResponse> getMyProjects(String email) {
        User user = findUserByEmail(email);
        List<Long> memberProjectIds = memberRepository.findProjectIdsByUserId(user.getId());
        List<Project> projects = projectRepository.findByIdInAndDeletedAtIsNull(memberProjectIds);
        return projects.stream()
                .map(p -> ProjectResponse.from(p, memberRepository.findByProjectId(p.getId())))
                .toList();
    }

    public ProjectResponse getProject(Long id, String email) {
        User user = findUserByEmail(email);
        Project project = findActiveProject(id);
        if (!memberRepository.existsByProjectIdAndUserId(id, user.getId())) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
        return ProjectResponse.from(project, memberRepository.findByProjectId(id));
    }

    @CacheEvict(value = "projects", key = "#email")
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, String email) {
        User owner = findUserByEmail(email);
        Project project = projectRepository.save(Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build());
        ProjectMember ownerMember = memberRepository.save(ProjectMember.builder()
                .project(project)
                .user(owner)
                .role(ProjectMemberRole.OWNER)
                .build());
        return ProjectResponse.from(project, List.of(ownerMember));
    }

    @CacheEvict(value = "projects", key = "#email")
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, String email) {
        User user = findUserByEmail(email);
        Project project = findActiveProject(id);
        if (!memberRepository.existsByProjectIdAndUserId(id, user.getId())) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        return ProjectResponse.from(projectRepository.save(project), memberRepository.findByProjectId(id));
    }

    @CacheEvict(value = "projects", key = "#email")
    @Transactional
    public void deleteProject(Long id, String email) {
        User user = findUserByEmail(email);
        Project project = findActiveProject(id);
        memberRepository.findByProjectIdAndUserId(id, user.getId())
                .filter(m -> m.getRole() == ProjectMemberRole.OWNER)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
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
}
