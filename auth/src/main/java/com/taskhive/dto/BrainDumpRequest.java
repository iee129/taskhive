package com.taskhive.dto;

import jakarta.validation.constraints.NotBlank;

public record BrainDumpRequest(@NotBlank String text, Long projectId) {}
