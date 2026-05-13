package com.taskhive.dto;

import com.taskhive.model.PersonalAccessToken;
import java.time.LocalDateTime;

public record TokenListResponse(Long id, String name, String scopes, LocalDateTime lastUsedAt, LocalDateTime createdAt) {
    public static TokenListResponse from(PersonalAccessToken pat) {
        return new TokenListResponse(pat.getId(), pat.getName(), pat.getScopes(), pat.getLastUsedAt(), pat.getCreatedAt());
    }
}
