package com.taskhive.integration;

import com.taskhive.config.TestcontainersConfig;
import com.taskhive.dto.AuthRequest;
import com.taskhive.dto.AuthResponse;
import com.taskhive.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.*;

class AuthIntegrationTest extends TestcontainersConfig {

    @Autowired TestRestTemplate restTemplate;

    @Test
    void register_정상등록_토큰반환() {
        RegisterRequest req = new RegisterRequest();
        req.setName("통합테스트사용자");
        req.setEmail("integration@test.com");
        req.setPassword("password123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register", req, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getEmail()).isEqualTo("integration@test.com");
    }

    @Test
    void register_중복이메일_에러반환() {
        RegisterRequest req = new RegisterRequest();
        req.setName("사용자1");
        req.setEmail("dup-integration@test.com");
        req.setPassword("password123");

        restTemplate.postForEntity("/api/auth/register", req, AuthResponse.class);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register", req, String.class);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void login_정상로그인_토큰반환() {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setName("로그인테스터");
        regReq.setEmail("login-integration@test.com");
        regReq.setPassword("password123");
        restTemplate.postForEntity("/api/auth/register", regReq, AuthResponse.class);

        AuthRequest loginReq = new AuthRequest();
        loginReq.setEmail("login-integration@test.com");
        loginReq.setPassword("password123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login", loginReq, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
    }

    @Test
    void login_잘못된비밀번호_401반환() {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setName("테스터");
        regReq.setEmail("badpw-integration@test.com");
        regReq.setPassword("correctpassword");
        restTemplate.postForEntity("/api/auth/register", regReq, AuthResponse.class);

        AuthRequest loginReq = new AuthRequest();
        loginReq.setEmail("badpw-integration@test.com");
        loginReq.setPassword("wrongpassword");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", loginReq, String.class);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void getMe_유효한토큰_사용자정보반환() {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setName("나자신");
        regReq.setEmail("me-integration@test.com");
        regReq.setPassword("password123");
        ResponseEntity<AuthResponse> regResp = restTemplate.postForEntity(
                "/api/auth/register", regReq, AuthResponse.class);

        String token = regResp.getBody().getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<AuthResponse> response = restTemplate.exchange(
                "/api/auth/me", HttpMethod.GET, entity, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo("me-integration@test.com");
        assertThat(response.getBody().getName()).isEqualTo("나자신");
    }

    @Test
    void getMe_토큰없음_401반환() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/auth/me", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
