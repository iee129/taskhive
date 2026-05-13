package com.taskhive.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "test-secret-key-for-testing-only-must-be-at-least-32chars";
    private static final long EXPIRATION = 3_600_000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_정상생성() {
        String token = jwtUtil.generateToken("user@test.com");
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractEmail_정상추출() {
        String token = jwtUtil.generateToken("user@test.com");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@test.com");
    }

    @Test
    void isValid_유효토큰_true() {
        String token = jwtUtil.generateToken("user@test.com");
        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void isValid_잘못된토큰_false() {
        assertThat(jwtUtil.isValid("invalid.token.here")).isFalse();
    }

    @Test
    void isValid_빈문자열_false() {
        assertThat(jwtUtil.isValid("")).isFalse();
    }

    @Test
    void isValid_만료토큰_false() {
        JwtUtil shortLived = new JwtUtil(SECRET, 1L);
        String token = shortLived.generateToken("user@test.com");
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        assertThat(shortLived.isValid(token)).isFalse();
    }

    @Test
    void getExpirationMs_설정값반환() {
        assertThat(jwtUtil.getExpirationMs()).isEqualTo(EXPIRATION);
    }

    @Test
    void generateToken_이메일다름_추출값다름() {
        String token1 = jwtUtil.generateToken("user1@test.com");
        String token2 = jwtUtil.generateToken("user2@test.com");
        assertThat(jwtUtil.extractEmail(token1)).isNotEqualTo(jwtUtil.extractEmail(token2));
    }
}
