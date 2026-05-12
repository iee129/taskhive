package com.taskhive.service;

import com.taskhive.dto.AddMemberRequest;
import com.taskhive.dto.MemberResponse;
import com.taskhive.dto.UserSearchResponse;
import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.Project;
import com.taskhive.model.ProjectMember;
import com.taskhive.model.User;
import com.taskhive.model.enums.ProjectMemberRole;
import com.taskhive.repository.ProjectMemberRepository;
import com.taskhive.repository.ProjectRepository;
import com.taskhive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<MemberResponse> getMembers(Long projectId, String requesterEmail) {
        checkMembership(projectId, requesterEmail);
        return memberRepository.findByProjectId(projectId).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse addMember(Long projectId, AddMemberRequest request, String requesterEmail) {
        checkMembership(projectId, requesterEmail);
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        User target = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (memberRepository.existsByProjectIdAndUserId(projectId, target.getId())) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }
        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(target)
                .role(ProjectMemberRole.MEMBER)
                .build();
        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public void removeMember(Long projectId, Long targetUserId, String requesterEmail) {
        checkMembership(projectId, requesterEmail);
        ProjectMember target = memberRepository.findByProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (target.getRole() == ProjectMemberRole.OWNER) {
            long ownerCount = memberRepository.countByProjectIdAndRole(projectId, ProjectMemberRole.OWNER);
            if (ownerCount <= 1) {
                throw new BusinessException(ErrorCode.LAST_OWNER);
            }
        }
        memberRepository.delete(target);
    }

    public List<UserSearchResponse> searchUsers(String email, Long projectId, String requesterEmail) {
        List<User> candidates = userRepository.searchByEmailExcluding(email, requesterEmail);
        if (projectId != null) {
            List<Long> memberUserIds = memberRepository.findByProjectId(projectId).stream()
                    .map(pm -> pm.getUser().getId())
                    .toList();
            candidates = candidates.stream()
                    .filter(u -> !memberUserIds.contains(u.getId()))
                    .toList();
        }
        return candidates.stream().map(UserSearchResponse::from).toList();
    }

    private void checkMembership(Long projectId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!memberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
    }
}
