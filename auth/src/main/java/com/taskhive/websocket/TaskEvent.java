package com.taskhive.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaskEvent {
    private String type;       // TASK_CREATED, TASK_UPDATED, TASK_DELETED
    private Long taskId;
    private String updatedBy;
    private Object payload;    // TaskResponse 또는 Map<String, Object>
}
