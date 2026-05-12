package com.taskhive.integration;

import com.taskhive.config.TestcontainersConfig;
import com.taskhive.dto.*;
import com.taskhive.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TaskIntegrationTest extends TestcontainersConfig {

    @Autowired TestRestTemplate restTemplate;

    private String authToken;

    @BeforeEach
    void setUp() {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setName("태스크테스터");
        regReq.setEmail("task-it-" + System.currentTimeMillis() + "@test.com");
        regReq.setPassword("password123");
        ResponseEntity<AuthResponse> resp = restTemplate.postForEntity("/api/auth/register", regReq, AuthResponse.class);
        authToken = resp.getBody().getToken();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void createTask_정상생성_201반환() {
        TaskRequest req = new TaskRequest();
        req.setTitle("통합 테스트 태스크");
        req.setPriority(Task.Priority.HIGH);

        ResponseEntity<TaskResponse> response = restTemplate.exchange(
                "/api/tasks", HttpMethod.POST,
                new HttpEntity<>(req, authHeaders()), TaskResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTitle()).isEqualTo("통합 테스트 태스크");
        assertThat(response.getBody().getPriority()).isEqualTo(Task.Priority.HIGH);
        assertThat(response.getBody().getStatus()).isEqualTo(Task.Status.TODO);
    }

    @Test
    void getTasks_생성후조회_목록반환() {
        TaskRequest req = new TaskRequest();
        req.setTitle("조회용 태스크");
        restTemplate.exchange("/api/tasks", HttpMethod.POST,
                new HttpEntity<>(req, authHeaders()), TaskResponse.class);

        ResponseEntity<List<TaskResponse>> response = restTemplate.exchange(
                "/api/tasks", HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getTask_존재하는ID_단건반환() {
        TaskRequest req = new TaskRequest();
        req.setTitle("단건 조회 태스크");
        ResponseEntity<TaskResponse> created = restTemplate.exchange(
                "/api/tasks", HttpMethod.POST,
                new HttpEntity<>(req, authHeaders()), TaskResponse.class);
        Long id = created.getBody().getId();

        ResponseEntity<TaskResponse> response = restTemplate.exchange(
                "/api/tasks/" + id, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), TaskResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(id);
        assertThat(response.getBody().getTitle()).isEqualTo("단건 조회 태스크");
    }

    @Test
    void updateTask_정상수정_변경반영() {
        TaskRequest createReq = new TaskRequest();
        createReq.setTitle("수정 전 태스크");
        ResponseEntity<TaskResponse> created = restTemplate.exchange(
                "/api/tasks", HttpMethod.POST,
                new HttpEntity<>(createReq, authHeaders()), TaskResponse.class);
        Long id = created.getBody().getId();

        TaskRequest updateReq = new TaskRequest();
        updateReq.setTitle("수정 후 태스크");
        updateReq.setStatus(Task.Status.IN_PROGRESS);
        updateReq.setPriority(Task.Priority.HIGH);

        ResponseEntity<TaskResponse> response = restTemplate.exchange(
                "/api/tasks/" + id, HttpMethod.PUT,
                new HttpEntity<>(updateReq, authHeaders()), TaskResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTitle()).isEqualTo("수정 후 태스크");
        assertThat(response.getBody().getStatus()).isEqualTo(Task.Status.IN_PROGRESS);
    }

    @Test
    void deleteTask_정상삭제_이후조회불가() {
        TaskRequest req = new TaskRequest();
        req.setTitle("삭제할 태스크");
        ResponseEntity<TaskResponse> created = restTemplate.exchange(
                "/api/tasks", HttpMethod.POST,
                new HttpEntity<>(req, authHeaders()), TaskResponse.class);
        Long id = created.getBody().getId();

        restTemplate.exchange("/api/tasks/" + id, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()), Void.class);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/tasks/" + id, HttpMethod.GET,
                new HttpEntity<>(authHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getTasks_필터_상태별조회() {
        TaskRequest todoReq = new TaskRequest();
        todoReq.setTitle("TODO 필터 태스크");
        todoReq.setStatus(Task.Status.TODO);
        restTemplate.exchange("/api/tasks", HttpMethod.POST,
                new HttpEntity<>(todoReq, authHeaders()), TaskResponse.class);

        ResponseEntity<List<TaskResponse>> response = restTemplate.exchange(
                "/api/tasks?status=TODO", HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).allMatch(t -> t.getStatus() == Task.Status.TODO);
    }

    @Test
    void createTask_인증없음_401반환() {
        TaskRequest req = new TaskRequest();
        req.setTitle("미인증 태스크");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/tasks", req, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
