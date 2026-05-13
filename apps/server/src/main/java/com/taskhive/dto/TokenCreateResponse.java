package com.taskhive.dto;

import java.time.LocalDateTime;

public record TokenCreateResponse(Long id, String name, String token, LocalDateTime createdAt) {}
