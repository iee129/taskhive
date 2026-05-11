package com.taskhive.dto;

import com.taskhive.model.Task;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class TaskRequest {
    @NotBlank
    private String title;
    private String description;
    private Task.Status status;
    private Long projectId;
    private Long assigneeId;
    private LocalDate dueDate;
}
