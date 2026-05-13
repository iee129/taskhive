package com.taskhive.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
public class GroqProvider implements AiProvider {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "llama3-8b-8192";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GroqProvider(RestTemplate restTemplate, ObjectMapper objectMapper, String apiKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    @Override
    public String generate(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", DEFAULT_MODEL,
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "temperature", 0.3
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(GROQ_API_URL, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                log.warn("Groq 응답에 choices 없음: {}", response.getBody());
                return null;
            }
            return choices.get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.warn("Groq 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String getProviderName() {
        return "groq";
    }

    @Override
    public boolean isCloudProvider() {
        return true;
    }
}
