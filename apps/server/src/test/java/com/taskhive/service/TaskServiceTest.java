package com.taskhive.service;

import com.taskhive.dto.TaskRequest;
import com.taskhive.dto.TaskResponse;
import com.taskhive.exception.BusinessException;
import com.taskhive.model.Task;
import com.taskhive.model.TaskStatusHistory;
import com.taskhive.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;
    @Mock ProjectMemberRepository memberRepository;
    @Mock TaskStatusHistoryRepository statusHistoryRepository;

    @InjectMocks TaskService taskService;

    @Test
    void updateTask_상태변경시_히스토리행_생성() {
        Task task = Task.builder()
                .title("기존 제목")
                .status(Task.Status.TODO)
                .priority(Task.Priority.MEDIUM)
                .build();

        TaskRequest request = new TaskRequest();
        request.setTitle("기존 제목");
        request.setStatus(Task.Status.IN_PROGRESS);

        when(taskRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(task));
        // no project → membership check skipped
        when(taskRepository.save(any())).thenReturn(task);
        when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse response = taskService.updateTask(1L, request, "user@example.com");

        ArgumentCaptor<TaskStatusHistory> captor = ArgumentCaptor.forClass(TaskStatusHistory.class);
        verify(statusHistoryRepository).save(captor.capture());
        TaskStatusHistory saved = captor.getValue();
        assertThat(saved.getFromStatus()).isEqualTo("TODO");
        assertThat(saved.getToStatus()).isEqualTo("IN_PROGRESS");
        assertThat(saved.getChangedBy()).isEqualTo("user@example.com");
        assertThat(saved.getChangedAt()).isNotNull();
    }

    @Test
    void updateTask_상태무변경시_히스토리행_미생성() {
        Task task = Task.builder()
                .title("기존 제목")
                .status(Task.Status.TODO)
                .priority(Task.Priority.MEDIUM)
                .build();

        TaskRequest request = new TaskRequest();
        request.setTitle("기존 제목");
        request.setStatus(Task.Status.TODO); // same status

        when(taskRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        taskService.updateTask(2L, request, "user@example.com");

        verify(statusHistoryRepository, never()).save(any());
    }
}
