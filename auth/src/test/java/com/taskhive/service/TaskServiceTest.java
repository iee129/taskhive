package com.taskhive.service;

import com.taskhive.dto.TaskRequest;
import com.taskhive.dto.TaskResponse;
import com.taskhive.exception.BusinessException;
import com.taskhive.model.Project;
import com.taskhive.model.Task;
import com.taskhive.model.User;
import com.taskhive.repository.ProjectRepository;
import com.taskhive.repository.TaskRepository;
import com.taskhive.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;

    @InjectMocks TaskService taskService;

    private Task buildTask(Long id, String title, Task.Status status, Task.Priority priority) {
        Task task = Task.builder()
                .title(title)
                .status(status)
                .priority(priority)
                .build();
        task.setId(id);
        return task;
    }

    @Test
    void getAllTasks_빈목록_빈리스트반환() {
        when(taskRepository.findAllByDeletedAtIsNull()).thenReturn(List.of());
        assertThat(taskService.getAllTasks()).isEmpty();
    }

    @Test
    void getAllTasks_데이터있음_매핑된리스트반환() {
        Task task = buildTask(1L, "테스트 태스크", Task.Status.TODO, Task.Priority.MEDIUM);
        when(taskRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getAllTasks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 태스크");
        assertThat(result.get(0).getStatus()).isEqualTo(Task.Status.TODO);
    }

    @Test
    void getFilteredTasks_필터적용_결과반환() {
        Task task = buildTask(1L, "검색태스크", Task.Status.IN_PROGRESS, Task.Priority.HIGH);
        when(taskRepository.findFiltered(Task.Status.IN_PROGRESS, Task.Priority.HIGH, "검색"))
                .thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getFilteredTasks(Task.Status.IN_PROGRESS, Task.Priority.HIGH, "검색");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Task.Status.IN_PROGRESS);
        assertThat(result.get(0).getPriority()).isEqualTo(Task.Priority.HIGH);
    }

    @Test
    void getFilteredTasks_null필터_결과반환() {
        when(taskRepository.findFiltered(null, null, null)).thenReturn(List.of());
        assertThat(taskService.getFilteredTasks(null, null, null)).isEmpty();
    }

    @Test
    void getTask_존재하는ID_응답반환() {
        Task task = buildTask(1L, "태스크", Task.Status.TODO, Task.Priority.LOW);
        when(taskRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(task));

        TaskResponse result = taskService.getTask(1L);

        assertThat(result.getTitle()).isEqualTo("태스크");
    }

    @Test
    void getTask_존재하지않는ID_예외발생() {
        when(taskRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(99L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void createTask_기본값_TODO_MEDIUM() {
        TaskRequest req = new TaskRequest();
        req.setTitle("새 태스크");

        Task saved = buildTask(1L, "새 태스크", Task.Status.TODO, Task.Priority.MEDIUM);
        when(taskRepository.save(any())).thenReturn(saved);

        TaskResponse result = taskService.createTask(req);

        assertThat(result.getStatus()).isEqualTo(Task.Status.TODO);
        assertThat(result.getPriority()).isEqualTo(Task.Priority.MEDIUM);
        verify(taskRepository).save(any());
    }

    @Test
    void createTask_프로젝트지정_프로젝트설정됨() {
        TaskRequest req = new TaskRequest();
        req.setTitle("프로젝트 태스크");
        req.setProjectId(1L);

        Project project = new Project();
        project.setId(1L);
        project.setName("테스트 프로젝트");

        Task saved = buildTask(2L, "프로젝트 태스크", Task.Status.TODO, Task.Priority.MEDIUM);
        saved.setProject(project);

        when(projectRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(project));
        when(taskRepository.save(any())).thenReturn(saved);

        TaskResponse result = taskService.createTask(req);

        assertThat(result.getTitle()).isEqualTo("프로젝트 태스크");
        verify(projectRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void createTask_없는프로젝트_예외발생() {
        TaskRequest req = new TaskRequest();
        req.setTitle("태스크");
        req.setProjectId(999L);

        when(projectRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(req))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void createTask_담당자지정_담당자설정됨() {
        TaskRequest req = new TaskRequest();
        req.setTitle("담당 태스크");
        req.setAssigneeId(1L);

        User user = User.builder().email("user@test.com").name("테스터").password("pw").build();
        user.setId(1L);

        Task saved = buildTask(3L, "담당 태스크", Task.Status.TODO, Task.Priority.MEDIUM);
        saved.setAssignee(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any())).thenReturn(saved);

        taskService.createTask(req);

        verify(userRepository).findById(1L);
    }

    @Test
    void createTask_없는담당자_예외발생() {
        TaskRequest req = new TaskRequest();
        req.setTitle("태스크");
        req.setAssigneeId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(req))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void createTask_우선순위HIGH_설정됨() {
        TaskRequest req = new TaskRequest();
        req.setTitle("긴급 태스크");
        req.setPriority(Task.Priority.HIGH);

        Task saved = buildTask(4L, "긴급 태스크", Task.Status.TODO, Task.Priority.HIGH);
        when(taskRepository.save(any())).thenReturn(saved);

        TaskResponse result = taskService.createTask(req);

        assertThat(result.getPriority()).isEqualTo(Task.Priority.HIGH);
    }

    @Test
    void updateTask_정상수정_반환() {
        Task existing = buildTask(1L, "기존 태스크", Task.Status.TODO, Task.Priority.LOW);
        when(taskRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));

        Task updated = buildTask(1L, "수정된 태스크", Task.Status.IN_PROGRESS, Task.Priority.HIGH);
        when(taskRepository.save(any())).thenReturn(updated);

        TaskRequest req = new TaskRequest();
        req.setTitle("수정된 태스크");
        req.setStatus(Task.Status.IN_PROGRESS);
        req.setPriority(Task.Priority.HIGH);

        TaskResponse result = taskService.updateTask(1L, req);

        assertThat(result.getTitle()).isEqualTo("수정된 태스크");
        assertThat(result.getStatus()).isEqualTo(Task.Status.IN_PROGRESS);
    }

    @Test
    void updateTask_없는태스크_예외발생() {
        when(taskRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(99L, new TaskRequest()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updateTask_마감일설정_반영됨() {
        Task existing = buildTask(1L, "태스크", Task.Status.TODO, Task.Priority.MEDIUM);
        when(taskRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));

        LocalDate due = LocalDate.of(2026, 12, 31);
        Task updated = buildTask(1L, "태스크", Task.Status.TODO, Task.Priority.MEDIUM);
        updated.setDueDate(due);
        when(taskRepository.save(any())).thenReturn(updated);

        TaskRequest req = new TaskRequest();
        req.setTitle("태스크");
        req.setDueDate(due);

        TaskResponse result = taskService.updateTask(1L, req);

        assertThat(result.getDueDate()).isEqualTo(due);
    }

    @Test
    void deleteTask_정상삭제_deletedAt설정() {
        Task task = buildTask(1L, "삭제할 태스크", Task.Status.TODO, Task.Priority.MEDIUM);
        when(taskRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        assertThat(task.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteTask_없는태스크_예외발생() {
        when(taskRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(99L))
                .isInstanceOf(BusinessException.class);
    }
}
