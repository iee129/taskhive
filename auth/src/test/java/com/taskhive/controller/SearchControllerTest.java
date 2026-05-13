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

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.profiles.active=test", "taskhive.ai.provider=none"})
@AutoConfigureMockMvc
@Transactional
class SearchControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @MockBean EmailService emailService;

    @Test
    void search_키워드로_태스크_검색() throws Exception {
        String jwt = setupUserWithProjectAndTask("유니크검색태스크");

        MvcResult result = mockMvc.perform(get("/api/search")
                        .header("Authorization", "Bearer " + jwt)
                        .param("q", "유니크검색"))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("유니크검색태스크");
        assertThat(body).contains("task");
    }

    @Test
    void search_빈쿼리_빈배열_반환() throws Exception {
        String jwt = getJwt();

        mockMvc.perform(get("/api/search")
                        .header("Authorization", "Bearer " + jwt)
                        .param("q", ""))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void search_인증없이_401() throws Exception {
        mockMvc.perform(get("/api/search").param("q", "test"))
                .andExpect(status().isUnauthorized());
    }

    private String getJwt() throws Exception {
        String email = "search_" + System.nanoTime() + "@example.com";
        RegisterRequest reg = new RegisterRequest();
        reg.setName("검색테스터");
        reg.setEmail(email);
        reg.setPassword("password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));
        userRepository.findByEmail(email).ifPresent(u -> { u.setEmailVerified(true); userRepository.save(u); });

        AuthRequest login = new AuthRequest();
        login.setEmail(email);
        login.setPassword("password123");
        MvcResult r = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login))).andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("token").asText();
    }

    private String setupUserWithProjectAndTask(String taskTitle) throws Exception {
        String jwt = getJwt();

        MvcResult projRes = mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "검색테스트프로젝트"))))
                .andExpect(status().isOk()).andReturn();
        Long projectId = objectMapper.readTree(projRes.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("title", taskTitle, "projectId", projectId))))
                .andExpect(status().isOk());

        return jwt;
    }
}
