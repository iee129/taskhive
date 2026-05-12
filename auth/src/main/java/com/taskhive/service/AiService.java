package com.taskhive.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.dto.AiTaskRequest;
import com.taskhive.dto.TaskRequest;
import com.taskhive.model.Task;
import com.taskhive.service.ai.AiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiProvider aiProvider;
    private final ObjectMapper objectMapper;

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
