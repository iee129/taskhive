package com.taskhive.controller;

import com.taskhive.dto.*;
import com.taskhive.model.Task;
import com.taskhive.service.TaskService;
import com.taskhive.websocket.TaskEvent;
import com.taskhive.websocket.TaskEventPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TaskResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTask(id));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        taskEventPublisher.publish(new TaskEvent("TASK_CREATED", response.getId(), currentUser(), response));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        taskEventPublisher.publish(new TaskEvent("TASK_UPDATED", id, currentUser(),
                Map.of("status", response.getStatus(), "title", response.getTitle())));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        taskEventPublisher.publish(new TaskEvent("TASK_DELETED", id, currentUser(), Map.of()));
        return ResponseEntity.noContent().build();
    }

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
