package com.taskhive.dto;

import com.taskhive.model.TaskActivity;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @Builder
public class TaskActivityResponse {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private String actorEmail;
    private String action;
    private String detail;
    private LocalDateTime occurredAt;

    public static TaskActivityResponse from(TaskActivity activity) {
        return TaskActivityResponse.builder()
                .id(activity.getId())
                .taskId(activity.getTaskId())
                .taskTitle(activity.getTaskTitle())
                .actorEmail(activity.getActorEmail())
                .action(activity.getAction())
                .detail(activity.getDetail())
                .occurredAt(activity.getOccurredAt())
                .build();
    }
}
