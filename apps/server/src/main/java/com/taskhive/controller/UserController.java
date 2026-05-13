package com.taskhive.controller;

import com.taskhive.dto.UserSearchResponse;
import com.taskhive.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ProjectMemberService memberService;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> search(
            @RequestParam String email,
            @RequestParam(required = false) Long projectId,
            Authentication auth) {
        return ResponseEntity.ok(memberService.searchUsers(email, projectId, auth.getName()));
    }
}
