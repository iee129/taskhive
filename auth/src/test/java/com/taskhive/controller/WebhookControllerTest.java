package com.taskhive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.dto.AuthRequest;
import com.taskhive.dto.RegisterRequest;
import com.taskhive.model.ProjectWebhook;
import com.taskhive.repository.ProjectRepository;
import com.taskhive.repository.ProjectWebhookRepository;
import com.taskhive.repository.UserRepository;
import com.taskhive.service.EmailService;
import com.taskhive.service.WebhookDeliveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.profiles.active=test", "taskhive.ai.provider=none"})
@AutoConfigureMockMvc
@Transactional
class WebhookControllerTest {

    @TestConfiguration
    static class SyncAsyncConfig {
        @Bean @Primary
        public TaskExecutor taskExecutor() {
            return new SyncTaskExecutor();
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired ProjectWebhookRepository webhookRepository;
    @Autowired ProjectRepository projectRepository;
    @MockBean EmailService emailService;
    @MockBean RestTemplate restTemplate;

    @Test
    void SSRF_localhost_차단() throws Exception {
        String jwt = obtainTokenWithProject();
        Long projectId = projectRepository.findAll().stream()
                .filter(p -> !p.isDeleted())
                .findFirst().map(p -> p.getId()).orElseThrow();

        mockMvc.perform(post("/api/projects/" + projectId + "/webhooks")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("url", "http://localhost:8080/hook"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SSRF_BLOCKED"));
    }

    @Test
    void SSRF_private_IP_차단() throws Exception {
        String jwt = obtainTokenWithProject();
        Long projectId = projectRepository.findAll().stream()
                .filter(p -> !p.isDeleted())
                .findFirst().map(p -> p.getId()).orElseThrow();

        mockMvc.perform(post("/api/projects/" + projectId + "/webhooks")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("url", "http://192.168.1.1/hook"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SSRF_BLOCKED"));
    }

    @Test
    void hmacSha256_서명_검증() {
        String secret = "test-secret";
        String data = "{\"event\":\"task.created\"}";
        String sig1 = WebhookDeliveryService.hmacSha256(secret, data);
        String sig2 = WebhookDeliveryService.hmacSha256(secret, data);
        assertThat(sig1).isEqualTo(sig2);
        assertThat(sig1).hasSize(64); // SHA-256 hex = 64 chars
        assertThat(WebhookDeliveryService.hmacSha256("other", data)).isNotEqualTo(sig1);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void consecutive_failures_5회_초과시_비활성화(@Autowired WebhookDeliveryService deliveryService) throws Exception {
        String jwt = obtainTokenWithProject();
        Long projectId = projectRepository.findAll().stream()
                .filter(p -> !p.isDeleted())
                .findFirst().map(p -> p.getId()).orElseThrow();

        // 외부 도메인 웹훅 등록 (SSRF 아님, but RestTemplate이 mock → 실패)
        MvcResult res = mockMvc.perform(post("/api/projects/" + projectId + "/webhooks")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("url", "https://example.com/hook", "secret", "s3cr3t"))))
                .andExpect(status().isOk())
                .andReturn();
        Long webhookId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        doThrow(new RuntimeException("Connection refused"))
                .when(restTemplate).exchange(any(), any(), any(), (Class<?>) any());

        // 5회 배달 시도 — .get()으로 완료 대기 (트랜잭션 없이 각 deliver가 독립 커밋)
        for (int i = 0; i < 5; i++) {
            deliveryService.deliver(projectId, "task.created", Map.of("id", 1)).get(10, TimeUnit.SECONDS);
        }

        ProjectWebhook webhook = webhookRepository.findById(webhookId).orElseThrow();
        assertThat(webhook.isEnabled()).isFalse();
        assertThat(webhook.getConsecutiveFailures()).isGreaterThanOrEqualTo(5);
    }

    private String obtainTokenWithProject() throws Exception {
        // 회원가입
        String email = "webhook_test_" + System.nanoTime() + "@example.com";
        RegisterRequest reg = new RegisterRequest();
        reg.setName("웹훅테스터");
        reg.setEmail(email);
        reg.setPassword("password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());
        userRepository.findByEmail(email).ifPresent(u -> {
            u.setEmailVerified(true);
            userRepository.save(u);
        });

        AuthRequest login = new AuthRequest();
        login.setEmail(email);
        login.setPassword("password123");
        MvcResult loginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        String jwt = objectMapper.readTree(loginRes.getResponse().getContentAsString()).get("token").asText();

        // 프로젝트 생성
        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "웹훅테스트프로젝트"))))
                .andExpect(status().isOk());

        return jwt;
    }
}
