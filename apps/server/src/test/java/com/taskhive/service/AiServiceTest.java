package com.taskhive.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.dto.AiTaskRequest;
import com.taskhive.dto.TaskRequest;
import com.taskhive.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock RestTemplate restTemplate;
    @Spy ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks AiService aiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiService, "ollamaUrl", "http://localhost:11434");
        ReflectionTestUtils.setField(aiService, "ollamaModel", "llama3.2");
    }

    @Test
    void generateTask_RestTemplate실패_폴백반환() {
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        AiTaskRequest req = new AiTaskRequest();
        req.setDescription("버그 수정 작업이 필요합니다");

        TaskRequest result = aiService.generateTask(req);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("버그 수정 작업이 필요합니다");
        assertThat(result.getPriority()).isEqualTo(Task.Priority.MEDIUM);
    }

    @Test
    void generateTask_긴설명_50자로잘림() {
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RestClientException("timeout"));

        AiTaskRequest req = new AiTaskRequest();
        req.setDescription("A".repeat(100));

        TaskRequest result = aiService.generateTask(req);

        assertThat(result.getTitle()).endsWith("...");
        assertThat(result.getTitle()).hasSize(53);
    }

    @Test
    void generateTask_Ollama성공_JSON파싱() throws Exception {
        String ollamaResponse = "{\"response\": \"{\\\"title\\\": \\\"테스트 태스크\\\", \\\"description\\\": \\\"설명\\\", \\\"priority\\\": \\\"HIGH\\\"}\"}";
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(ollamaResponse);

        AiTaskRequest req = new AiTaskRequest();
        req.setDescription("긴급 버그 수정");

        TaskRequest result = aiService.generateTask(req);

        assertThat(result.getTitle()).isEqualTo("테스트 태스크");
        assertThat(result.getPriority()).isEqualTo(Task.Priority.HIGH);
    }

    @Test
    void generateTask_projectId전달_폴백에서도유지() {
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RestClientException("error"));

        AiTaskRequest req = new AiTaskRequest();
        req.setDescription("프로젝트 태스크");
        req.setProjectId(42L);

        TaskRequest result = aiService.generateTask(req);

        assertThat(result.getProjectId()).isEqualTo(42L);
    }

    @Test
    void generateTask_알수없는우선순위_MEDIUM폴백() throws Exception {
        String ollamaResponse = "{\"response\": \"{\\\"title\\\": \\\"태스크\\\", \\\"description\\\": \\\"설명\\\", \\\"priority\\\": \\\"UNKNOWN\\\"}\"}";
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(ollamaResponse);

        AiTaskRequest req = new AiTaskRequest();
        req.setDescription("작업");

        TaskRequest result = aiService.generateTask(req);

        assertThat(result.getPriority()).isEqualTo(Task.Priority.MEDIUM);
    }
}
