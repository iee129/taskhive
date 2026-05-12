package com.taskhive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AiTaskRequest {
    @NotBlank
    private String description;
    private Long projectId;
}
