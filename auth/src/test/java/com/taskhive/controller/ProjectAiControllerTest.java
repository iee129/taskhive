package com.taskhive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.dto.AuthRequest;
import com.taskhive.dto.ProjectRequest;
import com.taskhive.dto.RegisterRequest;
import com.taskhive.dto.TaskRequest;
import com.taskhive.repository.TaskRepository;
import com.taskhive.repository.UserRepository;
import com.taskhive.service.EmailService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.profiles.active=test", "taskhive.ai.provider=none"})
@AutoConfigureMockMvc
@Transactional
class ProjectAiControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired TaskRepository taskRepository;
    @Autowired EntityManager entityManager;
    @MockBean EmailService emailService;

    private void registerAndVerify(String email) throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("테스터");
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
    }

    private String obtainToken(String email) throws Exception {
        AuthRequest login = new AuthRequest();
        login.setEmail(email);
        login.setPassword("password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    private long createProject(String token, String name) throws Exception {
        ProjectRequest req = new ProjectRequest();
        req.setName(name);
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private long createTask(String token, long projectId, String title) throws Exception {
        TaskRequest req = new TaskRequest();
        req.setTitle(title);
        req.setProjectId(projectId);
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    void blockers_returns_stale_tasks() throws Exception {
        String email = "blockers_test@example.com";
        registerAndVerify(email);
        String token = obtainToken(email);

        long projectId = createProject(token, "블로커 테스트 프로젝트");
        long taskId = createTask(token, projectId, "오래된 진행 중 태스크");

        // 태스크를 IN_PROGRESS로 변경하고 updatedAt을 15일 전으로 직접 업데이트 (Auditing 우회)
        entityManager.createNativeQuery(
                "UPDATE tasks SET status = 'IN_PROGRESS', updated_at = :cutoff WHERE id = :id")
                .setParameter("cutoff", LocalDateTime.now().minusDays(15))
                .setParameter("id", taskId)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/projects/" + projectId + "/blockers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(taskId));
    }

    @Test
    void prioritize_unavailable_returns503() throws Exception {
        String email = "prioritize_test@example.com";
        registerAndVerify(email);
        String token = obtainToken(email);

        long projectId = createProject(token, "우선순위화 테스트 프로젝트");

        // AI provider=none → isAvailable()=false → AI_UNAVAILABLE(503)
        mockMvc.perform(post("/api/projects/" + projectId + "/prioritize")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
    }
}
