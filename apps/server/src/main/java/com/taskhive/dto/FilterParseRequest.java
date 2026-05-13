package com.taskhive.dto;

import jakarta.validation.constraints.NotBlank;

public record FilterParseRequest(@NotBlank String query) {}
