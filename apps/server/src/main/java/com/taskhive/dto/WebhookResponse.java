package com.taskhive.dto;

import com.taskhive.model.ProjectWebhook;
import java.time.LocalDateTime;

public record WebhookResponse(
        Long id,
        String url,
        String events,
        boolean enabled,
        int consecutiveFailures,
        LocalDateTime createdAt
) {
    public static WebhookResponse from(ProjectWebhook w) {
        return new WebhookResponse(w.getId(), w.getUrl(), w.getEvents(), w.isEnabled(),
                w.getConsecutiveFailures(), w.getCreatedAt());
    }
}
