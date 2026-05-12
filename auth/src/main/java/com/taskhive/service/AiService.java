package com.taskhive.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.dto.AiTaskRequest;
import com.taskhive.dto.TaskRequest;
import com.taskhive.model.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${taskhive.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${taskhive.ollama.model:llama3.2}")
    private String ollamaModel;

    public TaskRequest generateTask(AiTaskRequest request) {
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
            Map<String, Object> body = Map.of(
                    "model", ollamaModel,
                    "prompt", prompt,
                    "stream", false
            );
            String raw = restTemplate.postForObject(ollamaUrl + "/api/generate", body, String.class);
            JsonNode root = objectMapper.readTree(raw);
            String responseText = root.path("response").asText();

            int start = responseText.indexOf('{');
            int end = responseText.lastIndexOf('}') + 1;
            JsonNode parsed = objectMapper.readTree(responseText.substring(start, end));

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
            log.warn("Ollama 호출 실패, 기본값 반환: {}", e.getMessage());
            TaskRequest fallback = new TaskRequest();
            fallback.setTitle(request.getDescription().length() > 50
                    ? request.getDescription().substring(0, 50) + "..."
                    : request.getDescription());
            fallback.setDescription(request.getDescription());
            fallback.setPriority(Task.Priority.MEDIUM);
            fallback.setProjectId(request.getProjectId());
            return fallback;
        }
    }

    private Task.Priority parsePriority(String value) {
        try {
            return Task.Priority.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return Task.Priority.MEDIUM;
        }
    }
}
