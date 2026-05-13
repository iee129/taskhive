package com.taskhive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.dto.AuthRequest;
import com.taskhive.dto.PasswordChangeRequest;
import com.taskhive.dto.RegisterRequest;
import com.taskhive.repository.UserRepository;
import com.taskhive.service.EmailService;
import jakarta.servlet.http.Cookie;
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
class AuthRefreshControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @MockBean EmailService emailService;

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL    = "/api/auth/login";
    private static final String REFRESH_URL  = "/api/auth/refresh";
    private static final String LOGOUT_URL   = "/api/auth/logout";
    private static final String PASSWORD_URL = "/api/auth/password";
    private static final String ME_URL       = "/api/auth/me";

    private RegisterRequest registerReq(String email) {
        RegisterRequest req = new RegisterRequest();
        req.setName("테스터");
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

    private MvcResult login(String email) throws Exception {
        AuthRequest loginReq = new AuthRequest();
        loginReq.setEmail(email);
        loginReq.setPassword("password123");
        return mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String registerAndGetToken(String email) throws Exception {
        registerAndVerify(email);
        return objectMapper.readTree(login(email).getResponse().getContentAsString())
                .get("token").asText();
    }

    private Cookie registerAndGetRefreshCookie(String email) throws Exception {
        registerAndVerify(email);
        return login(email).getResponse().getCookie("refreshToken");
    }

    @Test
    void register_이메일인증필요_토큰없음() throws Exception {
        MvcResult result = mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq("cookie1@example.com"))))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        // register 이후에는 token=null (@JsonInclude(NON_NULL) 로 필드가 없거나 null)
        var tokenNode = objectMapper.readTree(body).get("token");
        assertThat(tokenNode == null || tokenNode.isNull()).isTrue();
        Cookie cookie = result.getResponse().getCookie("refreshToken");
        assertThat(cookie).isNull();
    }

    @Test
    void login_쿠키에refreshToken설정() throws Exception {
        String email = "cookie2@example.com";
        registerAndVerify(email);

        AuthRequest req = new AuthRequest();
        req.setEmail(email);
        req.setPassword("password123");

        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("refreshToken");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
    }

    @Test
    void refresh_유효한쿠키_새accessToken반환() throws Exception {
        Cookie refreshCookie = registerAndGetRefreshCookie("refresh1@example.com");

        mockMvc.perform(post(REFRESH_URL).cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void refresh_쿠키없음_401() throws Exception {
        mockMvc.perform(post(REFRESH_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_정상_204() throws Exception {
        Cookie refreshCookie = registerAndGetRefreshCookie("logout1@example.com");

        mockMvc.perform(post(LOGOUT_URL).cookie(refreshCookie))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_후_refresh시도_401() throws Exception {
        Cookie refreshCookie = registerAndGetRefreshCookie("logout2@example.com");

        mockMvc.perform(post(LOGOUT_URL).cookie(refreshCookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(post(REFRESH_URL).cookie(refreshCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void password_변경_정상_200() throws Exception {
        String email = "pwd1@example.com";
        String accessToken = registerAndGetToken(email);

        PasswordChangeRequest req = new PasswordChangeRequest();
        req.setCurrentPassword("password123");
        req.setNewPassword("newpassword456");

        mockMvc.perform(put(PASSWORD_URL)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void password_현재비밀번호틀림_401() throws Exception {
        String email = "pwd2@example.com";
        String accessToken = registerAndGetToken(email);

        PasswordChangeRequest req = new PasswordChangeRequest();
        req.setCurrentPassword("wrongpassword");
        req.setNewPassword("newpassword456");

        mockMvc.perform(put(PASSWORD_URL)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void password_미인증_401() throws Exception {
        PasswordChangeRequest req = new PasswordChangeRequest();
        req.setCurrentPassword("password123");
        req.setNewPassword("newpassword456");

        mockMvc.perform(put(PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
