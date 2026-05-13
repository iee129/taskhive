package com.taskhive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.dto.AuthRequest;
import com.taskhive.dto.RegisterRequest;
import com.taskhive.model.Project;
import com.taskhive.model.ProjectMember;
import com.taskhive.model.User;
import com.taskhive.model.enums.ProjectMemberRole;
import com.taskhive.repository.ProjectMemberRepository;
import com.taskhive.repository.ProjectRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.profiles.active=test", "taskhive.ai.provider=none"})
@AutoConfigureMockMvc
@Transactional
class StandupControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProjectMemberRepository projectMemberRepository;
    @MockBean EmailService emailService;

    @Test
    void standup_empty_activities_returns_empty_array() throws Exception {
        String token = obtainToken("standup_empty@example.com");
        User user = userRepository.findByEmail("standup_empty@example.com").orElseThrow();

        Project project = projectRepository.save(Project.builder()
                .name("스탠드업 테스트 프로젝트")
                .owner(user)
                .build());
        projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .user(user)
                .role(ProjectMemberRole.OWNER)
                .build());

        // AI provider=none → isAvailable()=false → AI_UNAVAILABLE(503) before activity check
        mockMvc.perform(post("/api/projects/" + project.getId() + "/standup")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
    }

    @Test
    void standup_unavailable_returns503() throws Exception {
        String token = obtainToken("standup_503@example.com");
        User user = userRepository.findByEmail("standup_503@example.com").orElseThrow();

        Project project = projectRepository.save(Project.builder()
                .name("스탠드업 503 테스트")
                .owner(user)
                .build());
        projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .user(user)
                .role(ProjectMemberRole.OWNER)
                .build());

        mockMvc.perform(post("/api/projects/" + project.getId() + "/standup")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
    }

    private String obtainToken(String email) throws Exception {
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

        AuthRequest login = new AuthRequest();
        login.setEmail(email);
        login.setPassword("password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();
    }
}
