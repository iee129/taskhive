package com.taskhive.controller;

import com.taskhive.dto.AiCapabilitiesResponse;
import com.taskhive.dto.AiTaskRequest;
import com.taskhive.dto.TaskRequest;
import com.taskhive.dto.TaskResponse;
import com.taskhive.service.AiService;
import com.taskhive.service.TaskService;
import com.taskhive.service.ai.AiProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final TaskService taskService;
    private final AiProvider aiProvider;

    @GetMapping("/capabilities")
    public ResponseEntity<AiCapabilitiesResponse> capabilities() {
        return ResponseEntity.ok(new AiCapabilitiesResponse(
                aiProvider.isAvailable(),
                aiProvider.getProviderName(),
                aiProvider.isCloudProvider()
        ));
    }

    @PostMapping("/suggest-task")
    public ResponseEntity<TaskRequest> suggestTask(@Valid @RequestBody AiTaskRequest request) {
        return ResponseEntity.ok(aiService.generateTask(request));
    }

    @PostMapping("/create-task")
    public ResponseEntity<TaskResponse> createTaskFromAi(@Valid @RequestBody AiTaskRequest request,
                                                          Authentication auth) {
        TaskRequest taskRequest = aiService.generateTask(request);
        return ResponseEntity.ok(taskService.createTask(taskRequest, auth.getName()));
    }
}
