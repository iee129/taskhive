package com.taskhive.service;

import com.taskhive.dto.ProjectRequest;
import com.taskhive.dto.ProjectResponse;
import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.Project;
import com.taskhive.model.User;
import com.taskhive.repository.ProjectRepository;
import com.taskhive.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;

    @InjectMocks ProjectService projectService;

    private User buildUser(Long id, String email) {
        User user = User.builder().email(email).name("사용자").password("pw").build();
        user.setId(id);
        return user;
    }

    private Project buildProject(Long id, String name, User owner) {
        Project project = Project.builder().name(name).description("설명").owner(owner).build();
        project.setId(id);
        return project;
    }

    @Test
    void getMyProjects_사용자없음_예외발생() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getMyProjects("none@test.com"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void getMyProjects_정상_목록반환() {
        User owner = buildUser(1L, "owner@test.com");
        Project project = buildProject(1L, "테스트 프로젝트", owner);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(projectRepository.findByOwnerIdAndDeletedAtIsNull(1L)).thenReturn(List.of(project));

        List<ProjectResponse> result = projectService.getMyProjects("owner@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("테스트 프로젝트");
    }

    @Test
    void getMyProjects_프로젝트없음_빈목록() {
        User owner = buildUser(1L, "owner@test.com");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(projectRepository.findByOwnerIdAndDeletedAtIsNull(1L)).thenReturn(List.of());

        assertThat(projectService.getMyProjects("owner@test.com")).isEmpty();
    }

    @Test
    void getProject_존재함_반환() {
        User owner = buildUser(1L, "owner@test.com");
        Project project = buildProject(1L, "프로젝트", owner);
        when(projectRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(project));

        ProjectResponse result = projectService.getProject(1L);

        assertThat(result.name()).isEqualTo("프로젝트");
        assertThat(result.ownerId()).isEqualTo(1L);
    }

    @Test
    void getProject_없음_예외발생() {
        when(projectRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.PROJECT_NOT_FOUND));
    }

    @Test
    void createProject_정상_저장후반환() {
        User owner = buildUser(1L, "owner@test.com");
        ProjectRequest req = new ProjectRequest();
        req.setName("새 프로젝트");
        req.setDescription("설명");

        Project saved = buildProject(1L, "새 프로젝트", owner);
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(projectRepository.save(any())).thenReturn(saved);

        ProjectResponse result = projectService.createProject(req, "owner@test.com");

        assertThat(result.name()).isEqualTo("새 프로젝트");
        verify(projectRepository).save(any());
    }

    @Test
    void createProject_사용자없음_예외발생() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(new ProjectRequest(), "none@test.com"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updateProject_없는프로젝트_예외발생() {
        when(projectRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(99L, new ProjectRequest(), "owner@test.com"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.PROJECT_NOT_FOUND));
    }

    @Test
    void updateProject_소유자아님_FORBIDDEN() {
        User owner = buildUser(1L, "owner@test.com");
        Project project = buildProject(1L, "프로젝트", owner);
        when(projectRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateProject(1L, new ProjectRequest(), "other@test.com"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void updateProject_정상수정_반환() {
        User owner = buildUser(1L, "owner@test.com");
        Project project = buildProject(1L, "기존 이름", owner);
        ProjectRequest req = new ProjectRequest();
        req.setName("수정된 이름");
        req.setDescription("새 설명");

        Project updated = buildProject(1L, "수정된 이름", owner);
        when(projectRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenReturn(updated);

        ProjectResponse result = projectService.updateProject(1L, req, "owner@test.com");

        assertThat(result.name()).isEqualTo("수정된 이름");
    }

    @Test
    void deleteProject_없는프로젝트_예외발생() {
        when(projectRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(99L, "owner@test.com"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deleteProject_소유자아님_FORBIDDEN() {
        User owner = buildUser(1L, "owner@test.com");
        Project project = buildProject(1L, "프로젝트", owner);
        when(projectRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.deleteProject(1L, "other@test.com"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void deleteProject_정상삭제_deletedAt설정() {
        User owner = buildUser(1L, "owner@test.com");
        Project project = buildProject(1L, "삭제할 프로젝트", owner);
        when(projectRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(project));

        projectService.deleteProject(1L, "owner@test.com");

        assertThat(project.getDeletedAt()).isNotNull();
    }
}
