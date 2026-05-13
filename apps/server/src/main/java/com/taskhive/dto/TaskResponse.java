package com.taskhive.dto;

import com.taskhive.model.Task;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Task.Status status;
    private Task.Priority priority;
    private Long projectId;
    private Long assigneeId;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private List<LabelResponse> labels;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .labels(task.getLabels().stream().map(LabelResponse::from).toList())
                .build();
    }
}
