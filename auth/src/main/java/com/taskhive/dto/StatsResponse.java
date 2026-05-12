package com.taskhive.dto;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class StatsResponse {
    private long totalTasks;
    private long todo;
    private long inProgress;
    private long done;
    private long lowPriority;
    private long mediumPriority;
    private long highPriority;
    private long overdue;
    private long totalProjects;
    private long totalComments;
}
