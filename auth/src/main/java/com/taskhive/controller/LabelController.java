package com.taskhive.controller;

import com.taskhive.dto.LabelResponse;
import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.Label;
import com.taskhive.model.Project;
import com.taskhive.repository.LabelRepository;
import com.taskhive.repository.ProjectRepository;
import com.taskhive.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LabelController {

    private final LabelRepository labelRepository;
    private final ProjectRepository projectRepository;
    private final TaskService taskService;

    @PostMapping("/api/projects/{projectId}/labels")
    public ResponseEntity<LabelResponse> createLabel(@PathVariable Long projectId,
                                                     @RequestBody Map<String, String> body) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        String name = body.get("name");
        String color = body.getOrDefault("color", "#6366f1");
        Label label = labelRepository.save(Label.builder()
                .project(project)
                .name(name)
                .color(color)
                .build());
        return ResponseEntity.ok(LabelResponse.from(label));
    }

    @GetMapping("/api/projects/{projectId}/labels")
    public ResponseEntity<List<LabelResponse>> getLabels(@PathVariable Long projectId) {
        List<LabelResponse> labels = labelRepository.findByProjectId(projectId).stream()
                .map(LabelResponse::from)
                .toList();
        return ResponseEntity.ok(labels);
    }

    @DeleteMapping("/api/projects/{projectId}/labels/{labelId}")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long projectId,
                                            @PathVariable Long labelId) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LABEL_NOT_FOUND));
        labelRepository.delete(label);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/tasks/{taskId}/labels/{labelId}")
    public ResponseEntity<Void> addLabelToTask(@PathVariable Long taskId,
                                               @PathVariable Long labelId,
                                               Authentication auth) {
        taskService.addLabelToTask(taskId, labelId, auth.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/tasks/{taskId}/labels/{labelId}")
    public ResponseEntity<Void> removeLabelFromTask(@PathVariable Long taskId,
                                                    @PathVariable Long labelId,
                                                    Authentication auth) {
        taskService.removeLabelFromTask(taskId, labelId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
