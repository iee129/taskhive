package com.taskhive.dto;

import com.taskhive.model.Project;
import com.taskhive.model.ProjectMember;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        Long ownerId,
        LocalDateTime createdAt,
        List<MemberResponse> members
) {
    public static ProjectResponse from(Project project, List<ProjectMember> members) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getId(),
                project.getCreatedAt(),
                members.stream().map(MemberResponse::from).toList()
        );
    }
}
