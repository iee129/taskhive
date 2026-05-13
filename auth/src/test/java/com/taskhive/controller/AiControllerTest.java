package com.taskhive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.dto.AuthRequest;
import com.taskhive.dto.RegisterRequest;
import com.taskhive.repository.UserRepository;
import com.taskhive.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.profiles.active=test", "taskhive.ai.provider=none"})
@AutoConfigureMockMvc
@Transactional
class AiControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @MockBean EmailService emailService;

    @Test
    void capabilities_인증없이_호출가능하고_NoopProvider_반환() throws Exception {
        mockMvc.perform(get("/api/ai/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.provider").value("none"))
                .andExpect(jsonPath("$.cloudProvider").value(false));
    }

    @Test
    void summarizeTask_unavailable_returns503() throws Exception {
        // 1. 회원가입
        RegisterRequest reg = new RegisterRequest();
        reg.setName("테스터");
        reg.setEmail("aisummary_test@example.com");
        reg.setPassword("password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        // 2. 이메일 인증 처리
        userRepository.findByEmail("aisummary_test@example.com").ifPresent(u -> {
            u.setEmailVerified(true);
            userRepository.save(u);
        });

        // 3. 로그인으로 JWT 획득
        AuthRequest login = new AuthRequest();
        login.setEmail("aisummary_test@example.com");
        login.setPassword("password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // 4. AI provider=none → isAvailable()=false → AI_UNAVAILABLE(503) (task 조회 전에 체크)
        mockMvc.perform(post("/api/ai/tasks/999/ai-summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
    }

    @Test
    void parseFilter_unavailable_returns503() throws Exception {
        mockMvc.perform(post("/api/ai/parse-filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("query", "이번 주 마감 HIGH 내 태스크"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
    }
}
