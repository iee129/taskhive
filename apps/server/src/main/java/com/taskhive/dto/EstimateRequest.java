package com.taskhive.dto;

import jakarta.validation.constraints.NotBlank;

public record EstimateRequest(@NotBlank String title, String description) {}
