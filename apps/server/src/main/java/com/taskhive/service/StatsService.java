package com.taskhive.service;

import com.taskhive.dto.StatsResponse;
import com.taskhive.model.Task;
import com.taskhive.repository.CommentRepository;
import com.taskhive.repository.ProjectRepository;
import com.taskhive.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final CommentRepository commentRepository;

    public StatsResponse getStats() {
        long overdue = taskRepository.findAllByDeletedAtIsNull().stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(LocalDate.now())
                        && t.getStatus() != Task.Status.DONE)
                .count();

        return StatsResponse.builder()
                .totalTasks(taskRepository.countByDeletedAtIsNull())
                .todo(taskRepository.countByStatusAndDeletedAtIsNull(Task.Status.TODO))
                .inProgress(taskRepository.countByStatusAndDeletedAtIsNull(Task.Status.IN_PROGRESS))
                .done(taskRepository.countByStatusAndDeletedAtIsNull(Task.Status.DONE))
                .lowPriority(taskRepository.countByPriorityAndDeletedAtIsNull(Task.Priority.LOW))
                .mediumPriority(taskRepository.countByPriorityAndDeletedAtIsNull(Task.Priority.MEDIUM))
                .highPriority(taskRepository.countByPriorityAndDeletedAtIsNull(Task.Priority.HIGH))
                .overdue(overdue)
                .totalProjects(projectRepository.count())
                .totalComments(commentRepository.count())
                .build();
    }
}
