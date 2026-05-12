package com.taskhive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.dto.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.profiles.active=test"})
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @MockBean EmailService emailService;

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL = "/api/auth/login";
    private static final String ME_URL = "/api/auth/me";

    private RegisterRequest registerReq(String email) {
        RegisterRequest req = new RegisterRequest();
        req.setName("홍길동");
        req.setEmail(email);
        req.setPassword("password123");
        return req;
    }

    private void registerAndVerify(String email) throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq(email))))
                .andExpect(status().isOk());
        userRepository.findByEmail(email).ifPresent(u -> {
            u.setEmailVerified(true);
            userRepository.save(u);
        });
    }

    private String getToken(String email) throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail(email);
        req.setPassword("password123");
        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test
    void register_정상_이메일인증안내반환() throws Exception {
        MvcResult result = mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq("reg1@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("reg1@example.com"))
                .andReturn();

        var tokenNode = objectMapper.readTree(result.getResponse().getContentAsString()).get("token");
        assertThat(tokenNode == null || tokenNode.isNull()).isTrue();
    }

    @Test
    void register_중복이메일_400() throws Exception {
        String email = "dup@example.com";
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq(email))))
                .andExpect(status().isOk());

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq(email))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void register_빈이름_400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("");
        req.setEmail("blank@example.com");
        req.setPassword("password123");

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").isArray());
    }

    @Test
    void login_정상_200_token반환() throws Exception {
        String email = "login1@example.com";
        registerAndVerify(email);

        AuthRequest req = new AuthRequest();
        req.setEmail(email);
        req.setPassword("password123");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_잘못된비밀번호_401() throws Exception {
        String email = "login2@example.com";
        registerAndVerify(email);

        AuthRequest req = new AuthRequest();
        req.setEmail(email);
        req.setPassword("wrongpassword");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_JWT없음_401() throws Exception {
        mockMvc.perform(get(ME_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_변조JWT_401() throws Exception {
        mockMvc.perform(get(ME_URL)
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_유효한JWT_200_사용자정보반환() throws Exception {
        String email = "me@example.com";
        registerAndVerify(email);
        String token = getToken(email);

        mockMvc.perform(get(ME_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value("홍길동"));
    }
}
