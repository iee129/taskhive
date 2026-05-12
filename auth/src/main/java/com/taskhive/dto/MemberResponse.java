package com.taskhive.dto;

import com.taskhive.model.ProjectMember;
import com.taskhive.model.enums.ProjectMemberRole;

import java.time.LocalDateTime;

public record MemberResponse(
        Long userId,
        String name,
        String email,
        ProjectMemberRole role,
        LocalDateTime createdAt
) {
    public static MemberResponse from(ProjectMember pm) {
        return new MemberResponse(
                pm.getUser().getId(),
                pm.getUser().getName(),
                pm.getUser().getEmail(),
                pm.getRole(),
                pm.getCreatedAt()
        );
    }
}
