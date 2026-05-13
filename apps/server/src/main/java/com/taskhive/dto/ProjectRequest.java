package com.taskhive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequest {

    @NotBlank(message = "프로젝트 이름은 필수입니다")
    private String name;

    private String description;
}
