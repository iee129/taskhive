package com.taskhive.controller;

import com.taskhive.dto.*;
import com.taskhive.model.Task;
import com.taskhive.service.TaskService;
import com.taskhive.websocket.TaskEvent;
import com.taskhive.websocket.TaskEventPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskEventPublisher taskEventPublisher;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAll(
            @RequestParam(required = false) Task.Status status,
            @RequestParam(required = false) Task.Priority priority,
            @RequestParam(required = false) String search) {
        if (status == null && priority == null && (search == null || search.isBlank())) {
            return ResponseEntity.ok(taskService.getAllTasks());
        }
        return ResponseEntity.ok(taskService.getFilteredTasks(status, priority, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getOne(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(taskService.getTask(id, auth.getName()));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request,
                                               Authentication auth) {
        TaskResponse response = taskService.createTask(request, auth.getName());
        taskEventPublisher.publish(new TaskEvent("TASK_CREATED", response.getId(), auth.getName(), response));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody TaskRequest request,
                                               Authentication auth) {
        TaskResponse response = taskService.updateTask(id, request, auth.getName());
        taskEventPublisher.publish(new TaskEvent("TASK_UPDATED", id, auth.getName(),
                Map.of("status", response.getStatus(), "title", response.getTitle())));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        taskService.deleteTask(id, auth.getName());
        taskEventPublisher.publish(new TaskEvent("TASK_DELETED", id, auth.getName(), Map.of()));
        return ResponseEntity.noContent().build();
    }
}
