package com.taskhive.controller;

import com.taskhive.dto.AiTaskRequest;
import com.taskhive.dto.TaskRequest;
import com.taskhive.dto.TaskResponse;
import com.taskhive.service.AiService;
import com.taskhive.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final TaskService taskService;

    @PostMapping("/suggest-task")
    public ResponseEntity<TaskRequest> suggestTask(@Valid @RequestBody AiTaskRequest request) {
        return ResponseEntity.ok(aiService.generateTask(request));
    }

    @PostMapping("/create-task")
    public ResponseEntity<TaskResponse> createTaskFromAi(@Valid @RequestBody AiTaskRequest request) {
        TaskRequest taskRequest = aiService.generateTask(request);
        return ResponseEntity.ok(taskService.createTask(taskRequest));
    }
}
