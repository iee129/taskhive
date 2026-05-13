package com.taskhive.controller;

import com.taskhive.dto.BurndownPoint;
import com.taskhive.dto.CfdPoint;
import com.taskhive.dto.CycleTimeItem;
import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.Task;
import com.taskhive.model.TaskStatusHistory;
import com.taskhive.model.User;
import com.taskhive.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/projects/{projectId}/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final TaskRepository taskRepository;
    private final TaskStatusHistoryRepository historyRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;

    @GetMapping("/burndown")
    public ResponseEntity<List<BurndownPoint>> burndown(
            @PathVariable Long projectId,
            @RequestParam String from,
            @RequestParam String to,
            Authentication auth) {
        checkMember(projectId, auth.getName());
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);

        List<Task> tasks = taskRepository.findByProjectIdAndDeletedAtIsNull(projectId);
        Map<Long, List<TaskStatusHistory>> historyMap = loadHistory(tasks);

        List<BurndownPoint> result = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            final LocalDate d = day;
            long remaining = tasks.stream()
                    .filter(t -> !t.getCreatedAt().toLocalDate().isAfter(d))
                    .filter(t -> !"DONE".equals(statusOnDate(t, historyMap.get(t.getId()), d)))
                    .count();
            result.add(new BurndownPoint(day.toString(), (int) remaining));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cfd")
    public ResponseEntity<List<CfdPoint>> cfd(
            @PathVariable Long projectId,
            @RequestParam String from,
            @RequestParam String to,
            Authentication auth) {
        checkMember(projectId, auth.getName());
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);

        List<Task> tasks = taskRepository.findByProjectIdAndDeletedAtIsNull(projectId);
        Map<Long, List<TaskStatusHistory>> historyMap = loadHistory(tasks);

        List<CfdPoint> result = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            final LocalDate d = day;
            long todo = 0, inProgress = 0, done = 0;
            for (Task t : tasks) {
                if (t.getCreatedAt().toLocalDate().isAfter(d)) continue;
                String s = statusOnDate(t, historyMap.get(t.getId()), d);
                if ("TODO".equals(s)) todo++;
                else if ("IN_PROGRESS".equals(s)) inProgress++;
                else if ("DONE".equals(s)) done++;
            }
            result.add(new CfdPoint(day.toString(), todo, inProgress, done));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cycle-time")
    public ResponseEntity<List<CycleTimeItem>> cycleTime(
            @PathVariable Long projectId,
            Authentication auth) {
        checkMember(projectId, auth.getName());

        List<Task> doneTasks = taskRepository.findByProjectIdAndStatusAndDeletedAtIsNull(
                projectId, Task.Status.DONE);

        List<CycleTimeItem> result = new ArrayList<>();
        for (Task t : doneTasks) {
            List<TaskStatusHistory> history = historyRepository.findByTaskIdOrderByChangedAtAsc(t.getId());

            LocalDate startDate = t.getCreatedAt().toLocalDate();
            LocalDate endDate = t.getUpdatedAt().toLocalDate();

            for (TaskStatusHistory h : history) {
                if ("IN_PROGRESS".equals(h.getToStatus())) {
                    startDate = h.getChangedAt().toLocalDate();
                    break;
                }
            }
            for (TaskStatusHistory h : history) {
                if ("DONE".equals(h.getToStatus())) {
                    endDate = h.getChangedAt().toLocalDate();
                }
            }

            long days = ChronoUnit.DAYS.between(startDate, endDate);
            result.add(new CycleTimeItem(t.getId(), t.getTitle(), Math.max(0, days)));
        }
        return ResponseEntity.ok(result);
    }

    private void checkMember(Long projectId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!memberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_MEMBER);
        }
    }

    private Map<Long, List<TaskStatusHistory>> loadHistory(List<Task> tasks) {
        Map<Long, List<TaskStatusHistory>> map = new HashMap<>();
        for (Task t : tasks) {
            map.put(t.getId(), historyRepository.findByTaskIdOrderByChangedAtAsc(t.getId()));
        }
        return map;
    }

    private String statusOnDate(Task task, List<TaskStatusHistory> history, LocalDate date) {
        if (history == null || history.isEmpty()) return task.getStatus().name();
        String status = "TODO";
        for (TaskStatusHistory h : history) {
            if (!h.getChangedAt().toLocalDate().isAfter(date)) status = h.getToStatus();
            else break;
        }
        return status;
    }
}
