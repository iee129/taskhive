package com.taskhive.dto;

import jakarta.validation.constraints.NotBlank;

public record WebhookRequest(
        @NotBlank String url,
        String secret,
        String events
) {}
