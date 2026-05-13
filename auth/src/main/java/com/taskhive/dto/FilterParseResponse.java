package com.taskhive.dto;

public record FilterParseResponse(
        String status,
        String priority,
        String dueDateBefore
) {}
