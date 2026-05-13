package com.taskhive.service;

import com.taskhive.exception.InvalidTokenException;
import com.taskhive.model.RefreshToken;
import com.taskhive.model.User;
import com.taskhive.model.enums.Role;
import com.taskhive.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock RefreshTokenRepository refreshTokenRepository;

    @InjectMocks RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 604800000L);
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("홍길동")
                .password("encoded")
                .role(Role.USER)
                .build();
    }

    @Test
    void issue_정상_RefreshToken발급() {
        RefreshToken saved = RefreshToken.builder()
                .id(1L)
                .token("uuid-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        when(refreshTokenRepository.save(any())).thenReturn(saved);

        RefreshToken result = refreshTokenService.issue(user);

        assertThat(result.getToken()).isEqualTo("uuid-token");
        assertThat(result.getUser()).isEqualTo(user);
        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void issue_기존토큰삭제후재발급() {
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        refreshTokenService.issue(user);
        refreshTokenService.issue(user);

        verify(refreshTokenRepository, times(2)).deleteByUser(user);
    }

    @Test
    void rotate_유효한토큰_새토큰반환() {
        RefreshToken existing = RefreshToken.builder()
                .token("old-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        RefreshToken rotated = RefreshToken.builder()
                .token("new-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByTokenForUpdate("old-token")).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any())).thenReturn(rotated);

        RefreshToken result = refreshTokenService.rotate("old-token");

        assertThat(result.getToken()).isEqualTo("new-token");
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void rotate_존재하지않는토큰_예외발생() {
        when(refreshTokenRepository.findByTokenForUpdate("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotate("invalid"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("유효하지 않은");
    }

    @Test
    void rotate_만료된토큰_예외발생() {
        RefreshToken expired = RefreshToken.builder()
                .token("expired-token")
                .user(user)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        when(refreshTokenRepository.findByTokenForUpdate("expired-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.rotate("expired-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("만료된");
        verify(refreshTokenRepository).delete(expired);
    }

    @Test
    void invalidate_토큰삭제() {
        refreshTokenService.invalidate("some-token");
        verify(refreshTokenRepository).deleteByToken("some-token");
    }

    @Test
    void getUserByToken_유효한토큰_사용자반환() {
        RefreshToken token = RefreshToken.builder()
                .token("valid-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        User result = refreshTokenService.getUserByToken("valid-token");

        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getUserByToken_만료된토큰_예외발생() {
        RefreshToken expired = RefreshToken.builder()
                .token("expired")
                .user(user)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.getUserByToken("expired"))
                .isInstanceOf(InvalidTokenException.class);
    }
}
