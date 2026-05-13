package com.taskhive.controller;

import com.taskhive.dto.AiCapabilitiesResponse;
import com.taskhive.dto.AiTaskRequest;
import com.taskhive.dto.BrainDumpItem;
import com.taskhive.dto.BrainDumpRequest;
import com.taskhive.dto.CommentResponse;
import com.taskhive.dto.CreateFromBreakdownRequest;
import com.taskhive.dto.FilterParseRequest;
import com.taskhive.dto.FilterParseResponse;
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

import java.util.List;

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

    @PostMapping("/tasks/{taskId}/ai-summary")
    public ResponseEntity<CommentResponse> summarizeTask(@PathVariable Long taskId,
                                                          Authentication auth) {
        return ResponseEntity.ok(aiService.summarizeTask(taskId, auth.getName()));
    }

    @PostMapping("/parse-filter")
    public ResponseEntity<FilterParseResponse> parseFilter(
            @Valid @RequestBody FilterParseRequest request) {
        return ResponseEntity.ok(aiService.parseFilter(request.query()));
    }

    @PostMapping("/breakdown")
    public ResponseEntity<List<BrainDumpItem>> breakdown(@Valid @RequestBody BrainDumpRequest req) {
        return ResponseEntity.ok(aiService.breakdown(req.text(), req.projectId()));
    }

    @PostMapping("/breakdown/create")
    public ResponseEntity<List<TaskResponse>> createFromBreakdown(
            @RequestBody CreateFromBreakdownRequest req, Authentication auth) {
        return ResponseEntity.ok(aiService.createTasksFromBreakdown(req.items(), req.projectId(), auth.getName()));
    }
}
