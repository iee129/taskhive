package com.taskhive.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenCreateRequest(@NotBlank String name) {}
