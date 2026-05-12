package com.taskhive.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.service.ai.AiProvider;
import com.taskhive.service.ai.GroqProvider;
import com.taskhive.service.ai.NoopProvider;
import com.taskhive.service.ai.OllamaProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiProviderConfig {

    @Value("${taskhive.ai.provider:ollama}")
    private String providerName;

    @Value("${taskhive.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${taskhive.ollama.model:llama3.2}")
    private String ollamaModel;

    @Value("${GROQ_API_KEY:}")
    private String groqApiKey;

    @Bean
    public AiProvider aiProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        return switch (providerName.toLowerCase()) {
            case "groq" -> new GroqProvider(restTemplate, objectMapper, groqApiKey);
            case "none", "noop" -> new NoopProvider();
            default -> new OllamaProvider(restTemplate, objectMapper, ollamaUrl, ollamaModel);
        };
    }
}
