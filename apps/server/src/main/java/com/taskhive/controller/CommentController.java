package com.taskhive.controller;

import com.taskhive.dto.CommentRequest;
import com.taskhive.dto.CommentResponse;
import com.taskhive.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long taskId,
                                                              Authentication auth) {
        return ResponseEntity.ok(commentService.getComments(taskId, auth.getName()));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long taskId,
                                                       @Valid @RequestBody CommentRequest request,
                                                       Authentication auth) {
        return ResponseEntity.ok(commentService.addComment(taskId, request, auth.getName()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long taskId,
                                               @PathVariable Long commentId,
                                               Authentication auth) {
        commentService.deleteComment(commentId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
