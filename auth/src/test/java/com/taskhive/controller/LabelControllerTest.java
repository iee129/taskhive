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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.profiles.active=test", "taskhive.ai.provider=none"})
@AutoConfigureMockMvc
@Transactional
class LabelControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @MockBean EmailService emailService;

    @Test
    void 라벨_생성_조회_태스크추가_필터_제거_플로우() throws Exception {
        String jwt = obtainTokenWithProject();

        // 프로젝트 ID 조회
        MvcResult projectsRes = mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn();
        Long projectId = objectMapper.readTree(projectsRes.getResponse().getContentAsString())
                .get(0).get("id").asLong();

        // 라벨 생성
        MvcResult labelRes = mockMvc.perform(post("/api/projects/" + projectId + "/labels")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "버그", "color", "#ef4444"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("버그"))
                .andExpect(jsonPath("$.color").value("#ef4444"))
                .andReturn();
        Long labelId = objectMapper.readTree(labelRes.getResponse().getContentAsString()).get("id").asLong();

        // 라벨 목록 조회
        mockMvc.perform(get("/api/projects/" + projectId + "/labels")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("버그"));

        // 태스크 생성
        MvcResult taskRes = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("title", "버그 수정", "projectId", projectId))))
                .andExpect(status().isOk())
                .andReturn();
        Long taskId = objectMapper.readTree(taskRes.getResponse().getContentAsString()).get("id").asLong();

        // 태스크에 라벨 추가
        mockMvc.perform(post("/api/tasks/" + taskId + "/labels/" + labelId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());

        // labelId로 태스크 필터링
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + jwt)
                        .param("labelId", labelId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(taskId))
                .andExpect(jsonPath("$[0].labels[0].name").value("버그"));

        // 태스크에서 라벨 제거
        mockMvc.perform(delete("/api/tasks/" + taskId + "/labels/" + labelId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNoContent());

        // 라벨 필터 결과 비어있음
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + jwt)
                        .param("labelId", labelId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        // 라벨 삭제
        mockMvc.perform(delete("/api/projects/" + projectId + "/labels/" + labelId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNoContent());
    }

    private String obtainTokenWithProject() throws Exception {
        String email = "label_test_" + System.nanoTime() + "@example.com";
        RegisterRequest reg = new RegisterRequest();
        reg.setName("라벨테스터");
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

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "라벨테스트프로젝트"))))
                .andExpect(status().isOk());

        return jwt;
    }
}
