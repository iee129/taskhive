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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.profiles.active=test", "taskhive.ai.provider=none"})
@AutoConfigureMockMvc
@Transactional
class PersonalAccessTokenTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @MockBean EmailService emailService;

    @Test
    void PAT_생성_조회_폐기_인증_검증() throws Exception {
        // 1. 회원가입 + 인증
        String email = "pat_test@example.com";
        RegisterRequest reg = new RegisterRequest();
        reg.setName("PAT테스터");
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
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        String jwt = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // 2. PAT 생성
        MvcResult createResult = mockMvc.perform(post("/api/settings/tokens")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "CI Token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(startsWith("th_")))
                .andExpect(jsonPath("$.name").value("CI Token"))
                .andReturn();
        String rawToken = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("token").asText();
        Long tokenId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 3. PAT 목록 조회
        mockMvc.perform(get("/api/settings/tokens")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name").value("CI Token"));

        // 4. PAT로 보호된 엔드포인트 인증 성공
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + rawToken))
                .andExpect(status().isOk());

        // 5. PAT 폐기
        mockMvc.perform(delete("/api/settings/tokens/" + tokenId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNoContent());

        // 6. 폐기된 PAT로 인증 실패 → 401
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + rawToken))
                .andExpect(status().isUnauthorized());
    }
}
