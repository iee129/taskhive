package com.taskhive.dto;

import com.taskhive.model.User;

public record UserSearchResponse(
        Long userId,
        String name,
        String email
) {
    public static UserSearchResponse from(User user) {
        return new UserSearchResponse(user.getId(), user.getName(), user.getEmail());
    }
}
