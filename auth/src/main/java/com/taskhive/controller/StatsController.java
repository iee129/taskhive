package com.taskhive.controller;

import com.taskhive.dto.StatsResponse;
import com.taskhive.dto.TaskActivityResponse;
import com.taskhive.repository.TaskActivityRepository;
import com.taskhive.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final TaskActivityRepository taskActivityRepository;

    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(statsService.getStats());
    }

    @GetMapping("/activities")
    public ResponseEntity<List<TaskActivityResponse>> getActivities() {
        return ResponseEntity.ok(
                taskActivityRepository.findTop50ByOrderByOccurredAtDesc().stream()
                        .map(TaskActivityResponse::from)
                        .toList()
        );
    }

    @GetMapping("/activities/task/{taskId}")
    public ResponseEntity<List<TaskActivityResponse>> getTaskActivities(@PathVariable Long taskId) {
        return ResponseEntity.ok(
                taskActivityRepository.findByTaskIdOrderByOccurredAtDesc(taskId).stream()
                        .map(TaskActivityResponse::from)
                        .toList()
        );
    }
}
