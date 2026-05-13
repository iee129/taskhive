package com.taskhive.controller;

import com.taskhive.dto.PrioritizeItem;
import com.taskhive.dto.ProjectRequest;
import com.taskhive.dto.ProjectResponse;
import com.taskhive.dto.StandupResponse;
import com.taskhive.dto.TaskResponse;
import com.taskhive.service.AiService;
import com.taskhive.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final AiService aiService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getMyProjects(Authentication authentication) {
        return ResponseEntity.ok(projectService.getMyProjects(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getOne(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(projectService.getProject(id, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request,
                                                  Authentication authentication) {
        return ResponseEntity.ok(projectService.createProject(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ProjectRequest request,
                                                  Authentication authentication) {
        return ResponseEntity.ok(projectService.updateProject(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        projectService.deleteProject(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/standup")
    public ResponseEntity<List<StandupResponse>> standup(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(aiService.generateStandup(id, authentication.getName()));
    }

    @PostMapping("/{id}/prioritize")
    public ResponseEntity<List<PrioritizeItem>> prioritize(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(aiService.prioritizeTasks(id, authentication.getName()));
    }

    @GetMapping("/{id}/blockers")
    public ResponseEntity<List<TaskResponse>> blockers(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(aiService.getBlockers(id, authentication.getName()));
    }
}
