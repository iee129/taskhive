package com.taskhive.service;

import com.taskhive.dto.StatsResponse;
import com.taskhive.model.Task;
import com.taskhive.repository.CommentRepository;
import com.taskhive.repository.ProjectRepository;
import com.taskhive.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock ProjectRepository projectRepository;
    @Mock CommentRepository commentRepository;

    @InjectMocks StatsService statsService;

    private Task buildTask(Task.Status status, Task.Priority priority, LocalDate dueDate) {
        Task task = Task.builder()
                .title("태스크")
                .status(status)
                .priority(priority)
                .build();
        task.setDueDate(dueDate);
        return task;
    }

    @Test
    void getStats_데이터없음_모두0() {
        when(taskRepository.findAllByDeletedAtIsNull()).thenReturn(List.of());
        when(taskRepository.countByDeletedAtIsNull()).thenReturn(0L);
        when(taskRepository.countByStatusAndDeletedAtIsNull(Task.Status.TODO)).thenReturn(0L);
        when(taskRepository.countByStatusAndDeletedAtIsNull(Task.Status.IN_PROGRESS)).thenReturn(0L);
        when(taskRepository.countByStatusAndDeletedAtIsNull(Task.Status.DONE)).thenReturn(0L);
        when(taskRepository.countByPriorityAndDeletedAtIsNull(Task.Priority.LOW)).thenReturn(0L);
        when(taskRepository.countByPriorityAndDeletedAtIsNull(Task.Priority.MEDIUM)).thenReturn(0L);
        when(taskRepository.countByPriorityAndDeletedAtIsNull(Task.Priority.HIGH)).thenReturn(0L);
        when(projectRepository.count()).thenReturn(0L);
        when(commentRepository.count()).thenReturn(0L);

        StatsResponse stats = statsService.getStats();

        assertThat(stats.getTotalTasks()).isZero();
        assertThat(stats.getOverdue()).isZero();
        assertThat(stats.getTotalProjects()).isZero();
        assertThat(stats.getTotalComments()).isZero();
    }

    @Test
    void getStats_기한초과태스크_overdue카운트됨() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        Task overdue = buildTask(Task.Status.TODO, Task.Priority.HIGH, yesterday);
        Task notOverdue = buildTask(Task.Status.TODO, Task.Priority.MEDIUM, tomorrow);
        Task doneOverdue = buildTask(Task.Status.DONE, Task.Priority.LOW, yesterday);

        when(taskRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(overdue, notOverdue, doneOverdue));
        when(taskRepository.countByDeletedAtIsNull()).thenReturn(3L);
        when(taskRepository.countByStatusAndDeletedAtIsNull(any())).thenReturn(0L);
        when(taskRepository.countByPriorityAndDeletedAtIsNull(any())).thenReturn(0L);
        when(projectRepository.count()).thenReturn(0L);
        when(commentRepository.count()).thenReturn(0L);

        StatsResponse stats = statsService.getStats();

        assertThat(stats.getOverdue()).isEqualTo(1L);
    }

    @Test
    void getStats_dueDate없는태스크_overdue제외() {
        Task noDue = buildTask(Task.Status.TODO, Task.Priority.MEDIUM, null);

        when(taskRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(noDue));
        when(taskRepository.countByDeletedAtIsNull()).thenReturn(1L);
        when(taskRepository.countByStatusAndDeletedAtIsNull(any())).thenReturn(0L);
        when(taskRepository.countByPriorityAndDeletedAtIsNull(any())).thenReturn(0L);
        when(projectRepository.count()).thenReturn(0L);
        when(commentRepository.count()).thenReturn(0L);

        StatsResponse stats = statsService.getStats();

        assertThat(stats.getOverdue()).isZero();
    }

    @Test
    void getStats_카운트집계_정상반환() {
        when(taskRepository.findAllByDeletedAtIsNull()).thenReturn(List.of());
        when(taskRepository.countByDeletedAtIsNull()).thenReturn(10L);
        when(taskRepository.countByStatusAndDeletedAtIsNull(Task.Status.TODO)).thenReturn(4L);
        when(taskRepository.countByStatusAndDeletedAtIsNull(Task.Status.IN_PROGRESS)).thenReturn(3L);
        when(taskRepository.countByStatusAndDeletedAtIsNull(Task.Status.DONE)).thenReturn(3L);
        when(taskRepository.countByPriorityAndDeletedAtIsNull(Task.Priority.LOW)).thenReturn(2L);
        when(taskRepository.countByPriorityAndDeletedAtIsNull(Task.Priority.MEDIUM)).thenReturn(5L);
        when(taskRepository.countByPriorityAndDeletedAtIsNull(Task.Priority.HIGH)).thenReturn(3L);
        when(projectRepository.count()).thenReturn(2L);
        when(commentRepository.count()).thenReturn(15L);

        StatsResponse stats = statsService.getStats();

        assertThat(stats.getTotalTasks()).isEqualTo(10L);
        assertThat(stats.getTodo()).isEqualTo(4L);
        assertThat(stats.getInProgress()).isEqualTo(3L);
        assertThat(stats.getDone()).isEqualTo(3L);
        assertThat(stats.getLowPriority()).isEqualTo(2L);
        assertThat(stats.getMediumPriority()).isEqualTo(5L);
        assertThat(stats.getHighPriority()).isEqualTo(3L);
        assertThat(stats.getTotalProjects()).isEqualTo(2L);
        assertThat(stats.getTotalComments()).isEqualTo(15L);
    }

    @Test
    void getStats_DONE태스크는overdue제외() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Task doneTask = buildTask(Task.Status.DONE, Task.Priority.HIGH, yesterday);

        when(taskRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(doneTask));
        when(taskRepository.countByDeletedAtIsNull()).thenReturn(1L);
        when(taskRepository.countByStatusAndDeletedAtIsNull(any())).thenReturn(0L);
        when(taskRepository.countByPriorityAndDeletedAtIsNull(any())).thenReturn(0L);
        when(projectRepository.count()).thenReturn(0L);
        when(commentRepository.count()).thenReturn(0L);

        StatsResponse stats = statsService.getStats();

        assertThat(stats.getOverdue()).isZero();
    }
}
