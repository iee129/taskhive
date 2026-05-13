package com.taskhive.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
public class OllamaProvider implements AiProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String ollamaUrl;
    private final String ollamaModel;

    public OllamaProvider(RestTemplate restTemplate, ObjectMapper objectMapper,
                          String ollamaUrl, String ollamaModel) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.ollamaUrl = ollamaUrl;
        this.ollamaModel = ollamaModel;
    }

    @Override
    public String generate(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", ollamaModel,
                    "prompt", prompt,
                    "stream", false,
                    "format", "json"
            );
            String raw = restTemplate.postForObject(ollamaUrl + "/api/generate", body, String.class);
            JsonNode root = objectMapper.readTree(raw);
            return root.path("response").asText();
        } catch (Exception e) {
            log.warn("Ollama 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            restTemplate.getForObject(ollamaUrl + "/api/tags", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "ollama";
    }

    @Override
    public boolean isCloudProvider() {
        return false;
    }
}
