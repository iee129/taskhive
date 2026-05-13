package com.taskhive.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.dto.AuthRequest;
import com.taskhive.dto.RegisterRequest;
import com.taskhive.model.Task;
import com.taskhive.model.TaskStatusHistory;
import com.taskhive.repository.TaskRepository;
import com.taskhive.repository.TaskStatusHistoryRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.profiles.active=test", "taskhive.ai.provider=none"})
@AutoConfigureMockMvc
@Transactional
class AnalyticsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired TaskRepository taskRepository;
    @Autowired TaskStatusHistoryRepository historyRepository;
    @MockBean EmailService emailService;

    @Test
    void burndown_날짜별_잔여태스크() throws Exception {
        Setup s = setup();
        String from = s.d.toString();
        String to = s.d.plusDays(2).toString();

        MvcResult res = mockMvc.perform(get("/api/projects/" + s.projectId + "/analytics/burndown")
                        .header("Authorization", "Bearer " + s.jwt)
                        .param("from", from).param("to", to))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(arr.size()).isEqualTo(3); // D, D+1, D+2
        // D+2: T1=DONE, T2=IN_PROGRESS, T3=TODO → remaining=2
        assertThat(arr.get(2).get("remaining").asInt()).isEqualTo(2);
    }

    @Test
    void cfd_날짜별_상태별_카운트() throws Exception {
        Setup s = setup();
        String from = s.d.toString();
        String to = s.d.plusDays(2).toString();

        MvcResult res = mockMvc.perform(get("/api/projects/" + s.projectId + "/analytics/cfd")
                        .header("Authorization", "Bearer " + s.jwt)
                        .param("from", from).param("to", to))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(arr.size()).isEqualTo(3);
        // D+2: T1=DONE(1), T2=IN_PROGRESS(1), T3=TODO(1)
        JsonNode day2 = arr.get(2);
        assertThat(day2.get("done").asInt()).isEqualTo(1);
        assertThat(day2.get("inProgress").asInt()).isEqualTo(1);
        assertThat(day2.get("todo").asInt()).isEqualTo(1);
    }

    @Test
    void cycleTime_완료태스크_소요일() throws Exception {
        Setup s = setup();

        MvcResult res = mockMvc.perform(get("/api/projects/" + s.projectId + "/analytics/cycle-time")
                        .header("Authorization", "Bearer " + s.jwt))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(res.getResponse().getContentAsString());
        assertThat(arr.size()).isEqualTo(1); // T1 only (DONE)
        assertThat(arr.get(0).get("cycleDays").asInt()).isEqualTo(1); // D+1 → D+2 = 1 day
    }

    private record Setup(String jwt, Long projectId, LocalDate d) {}

    private Setup setup() throws Exception {
        LocalDate d = LocalDate.now().minusDays(10);

        // 사용자 등록 및 로그인
        String email = "analytics_" + System.nanoTime() + "@example.com";
        RegisterRequest reg = new RegisterRequest();
        reg.setName("분석테스터"); reg.setEmail(email); reg.setPassword("password123");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));
        userRepository.findByEmail(email).ifPresent(u -> { u.setEmailVerified(true); userRepository.save(u); });

        AuthRequest login = new AuthRequest();
        login.setEmail(email); login.setPassword("password123");
        MvcResult lr = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login))).andReturn();
        String jwt = objectMapper.readTree(lr.getResponse().getContentAsString()).get("token").asText();

        // 프로젝트 생성
        MvcResult pr = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "분석테스트프로젝트"))))
                .andReturn();
        Long projectId = objectMapper.readTree(pr.getResponse().getContentAsString()).get("id").asLong();

        // T1: created D, status DONE, history: TODO→IN_PROGRESS(D+1), IN_PROGRESS→DONE(D+2)
        MvcResult t1r = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "T1", "projectId", projectId))))
                .andReturn();
        Long t1Id = objectMapper.readTree(t1r.getResponse().getContentAsString()).get("id").asLong();
        Task t1 = taskRepository.findById(t1Id).orElseThrow();
        t1.setStatus(Task.Status.DONE);
        taskRepository.save(t1);
        taskRepository.updateCreatedAtNative(t1Id, d.atStartOfDay());
        historyRepository.save(TaskStatusHistory.builder().task(t1)
                .fromStatus("TODO").toStatus("IN_PROGRESS").changedBy(email)
                .changedAt(d.plusDays(1).atStartOfDay()).build());
        historyRepository.save(TaskStatusHistory.builder().task(t1)
                .fromStatus("IN_PROGRESS").toStatus("DONE").changedBy(email)
                .changedAt(d.plusDays(2).atStartOfDay()).build());

        // T2: created D, status IN_PROGRESS, history: TODO→IN_PROGRESS(D+1)
        MvcResult t2r = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "T2", "projectId", projectId))))
                .andReturn();
        Long t2Id = objectMapper.readTree(t2r.getResponse().getContentAsString()).get("id").asLong();
        Task t2 = taskRepository.findById(t2Id).orElseThrow();
        t2.setStatus(Task.Status.IN_PROGRESS);
        taskRepository.save(t2);
        taskRepository.updateCreatedAtNative(t2Id, d.atStartOfDay());
        historyRepository.save(TaskStatusHistory.builder().task(t2)
                .fromStatus("TODO").toStatus("IN_PROGRESS").changedBy(email)
                .changedAt(d.plusDays(1).atStartOfDay()).build());

        // T3: created D+1, status TODO
        MvcResult t3r = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "T3", "projectId", projectId))))
                .andReturn();
        Long t3Id = objectMapper.readTree(t3r.getResponse().getContentAsString()).get("id").asLong();
        taskRepository.updateCreatedAtNative(t3Id, d.plusDays(1).atStartOfDay());

        return new Setup(jwt, projectId, d);
    }

}
