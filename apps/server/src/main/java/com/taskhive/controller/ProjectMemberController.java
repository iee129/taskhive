package com.taskhive.controller;

import com.taskhive.dto.AddMemberRequest;
import com.taskhive.dto.MemberResponse;
import com.taskhive.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService memberService;

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getMembers(@PathVariable Long projectId,
                                                            Authentication auth) {
        return ResponseEntity.ok(memberService.getMembers(projectId, auth.getName()));
    }

    @PostMapping
    public ResponseEntity<MemberResponse> addMember(@PathVariable Long projectId,
                                                     @Valid @RequestBody AddMemberRequest request,
                                                     Authentication auth) {
        return ResponseEntity.ok(memberService.addMember(projectId, request, auth.getName()));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long projectId,
                                              @PathVariable Long userId,
                                              Authentication auth) {
        memberService.removeMember(projectId, userId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
