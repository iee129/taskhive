package com.taskhive.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.dto.AiTaskRequest;
import com.taskhive.dto.CommentRequest;
import com.taskhive.dto.CommentResponse;
import com.taskhive.dto.FilterParseResponse;
import com.taskhive.dto.TaskRequest;
import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.Task;
import com.taskhive.model.TaskActivity;
import com.taskhive.repository.CommentRepository;
import com.taskhive.repository.TaskActivityRepository;
import com.taskhive.repository.TaskRepository;
import com.taskhive.service.ai.AiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiProvider aiProvider;
    private final ObjectMapper objectMapper;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final CommentService commentService;

    public CommentResponse summarizeTask(Long taskId, String requesterEmail) {
        if (!aiProvider.isAvailable()) {
            throw new BusinessException(ErrorCode.AI_UNAVAILABLE);
        }

        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TASK_NOT_FOUND));

        List<String> commentLines = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(c -> "- [" + c.getAuthor().getEmail() + "] " + c.getContent())
                .collect(Collectors.toList());

        List<String> activityLines = taskActivityRepository.findByTaskIdOrderByOccurredAtDesc(taskId).stream()
                .limit(10)
                .map(a -> "- [" + a.getAction() + "] " + (a.getDetail() != null ? a.getDetail() : ""))
                .collect(Collectors.toList());

        String prompt = """
                You are a task management assistant. Summarize the following task in Korean in 2-3 sentences.
                Focus on the current state, key discussion points, and any important activities.

                Task title: %s
                Task description: %s
                Task status: %s
                Task priority: %s

                Comments:
                %s

                Recent activities:
                %s

                Respond with a concise summary only, no extra formatting.
                """.formatted(
                task.getTitle(),
                task.getDescription() != null ? task.getDescription() : "(없음)",
                task.getStatus(),
                task.getPriority(),
                commentLines.isEmpty() ? "(없음)" : String.join("\n", commentLines),
                activityLines.isEmpty() ? "(없음)" : String.join("\n", activityLines)
        );

        String summaryText;
        try {
            summaryText = aiProvider.generate(prompt);
            if (summaryText == null || summaryText.isBlank()) {
                summaryText = "AI 요약을 생성할 수 없습니다.";
            }
        } catch (Exception e) {
            log.warn("AI 요약 생성 실패: {}", e.getMessage());
            summaryText = "AI 요약을 생성할 수 없습니다.";
        }

        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setContent("[AI 요약] " + summaryText);
        return commentService.addComment(taskId, commentRequest, requesterEmail);
    }

    public TaskRequest generateTask(AiTaskRequest request) {
        if (!aiProvider.isAvailable()) {
            return fallback(request);
        }

        String today = LocalDate.now().toString();
        String prompt = """
                You are a task management assistant. Extract structured task information from the user's description.
                Today's date is %s.
                Respond ONLY with valid JSON in this exact format:
                {"title": "...", "description": "...", "priority": "LOW|MEDIUM|HIGH", "dueDate": "YYYY-MM-DD or null"}

                For dueDate: parse natural language date hints (e.g. "이번 주 금요일", "내일", "다음 주", "tomorrow", "next week", "this Friday") relative to today's date.
                If no date is mentioned, set dueDate to null.

                User description: %s
                """.formatted(today, request.getDescription());

        try {
            String responseText = aiProvider.generate(prompt);
            if (responseText == null || responseText.isBlank()) {
                return fallback(request);
            }

            JsonNode parsed = objectMapper.readTree(responseText);

            TaskRequest task = new TaskRequest();
            task.setTitle(parsed.path("title").asText("AI 생성 태스크"));
            task.setDescription(parsed.path("description").asText(request.getDescription()));
            task.setPriority(parsePriority(parsed.path("priority").asText("MEDIUM")));
            task.setProjectId(request.getProjectId());

            String dueDateStr = parsed.path("dueDate").asText(null);
            if (dueDateStr != null && !dueDateStr.isBlank() && !dueDateStr.equalsIgnoreCase("null")) {
                try {
                    task.setDueDate(LocalDate.parse(dueDateStr));
                } catch (Exception ex) {
                    log.warn("dueDate 파싱 실패, null 유지: {}", dueDateStr);
                }
            }

            return task;
        } catch (Exception e) {
            log.warn("AI 응답 파싱 실패, 기본값 반환: {}", e.getMessage());
            return fallback(request);
        }
    }

    public FilterParseResponse parseFilter(String query) {
        if (!aiProvider.isAvailable()) {
            throw new BusinessException(ErrorCode.AI_UNAVAILABLE);
        }

        String today = LocalDate.now().toString();
        String prompt = """
                You are a task filter assistant. Today is %s.
                Parse the user's natural language query into filter parameters.
                Respond ONLY with valid JSON:
                {"status": "TODO|IN_PROGRESS|DONE|null", "priority": "LOW|MEDIUM|HIGH|null", "dueDateBefore": "YYYY-MM-DD|null"}

                Query: %s
                """.formatted(today, query);

        try {
            String responseText = aiProvider.generate(prompt);
            if (responseText == null || responseText.isBlank()) {
                return new FilterParseResponse(null, null, null);
            }

            JsonNode parsed = objectMapper.readTree(responseText);

            String status = nullableText(parsed.path("status"));
            String priority = nullableText(parsed.path("priority"));
            String dueDateBefore = nullableText(parsed.path("dueDateBefore"));

            return new FilterParseResponse(status, priority, dueDateBefore);
        } catch (Exception e) {
            log.warn("AI 필터 파싱 실패, 기본값 반환: {}", e.getMessage());
            return new FilterParseResponse(null, null, null);
        }
    }

    private String nullableText(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return null;
        String text = node.asText(null);
        if (text == null || text.isBlank() || text.equalsIgnoreCase("null")) return null;
        return text;
    }

    private TaskRequest fallback(AiTaskRequest request) {
        TaskRequest task = new TaskRequest();
        task.setTitle(request.getDescription().length() > 50
                ? request.getDescription().substring(0, 50) + "..."
                : request.getDescription());
        task.setDescription(request.getDescription());
        task.setPriority(Task.Priority.MEDIUM);
        task.setProjectId(request.getProjectId());
        return task;
    }

    private Task.Priority parsePriority(String value) {
        try {
            return Task.Priority.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return Task.Priority.MEDIUM;
        }
    }
}
