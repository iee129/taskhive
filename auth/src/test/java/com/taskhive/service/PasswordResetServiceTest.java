package com.taskhive.service;

import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.PasswordResetToken;
import com.taskhive.model.User;
import com.taskhive.repository.PasswordResetTokenRepository;
import com.taskhive.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordResetTokenRepository tokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;
    @InjectMocks PasswordResetService passwordResetService;

    @Test
    void sendResetEmail_existingUser_savesTokenAndSendsEmail() {
        User user = User.builder().email("user@test.com").name("홍길동").build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        passwordResetService.sendResetEmail("user@test.com");

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("user@test.com"), eq("홍길동"), anyString());
    }

    @Test
    void sendResetEmail_unknownEmail_doesNothing() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        passwordResetService.sendResetEmail("nobody@test.com");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
    }

    @Test
    void resetPassword_validToken_changesPassword() {
        User user = User.builder().email("user@test.com").name("홍길동").password("old").build();
        PasswordResetToken prt = PasswordResetToken.builder()
                .user(user).token("validtoken")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(tokenRepository.findByToken("validtoken")).thenReturn(Optional.of(prt));
        when(passwordEncoder.encode("newpass123")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        passwordResetService.resetPassword("validtoken", "newpass123");

        assertThat(prt.getUsedAt()).isNotNull();
        assertThat(user.getPassword()).isEqualTo("encoded");
    }

    @Test
    void resetPassword_alreadyUsedToken_throwsTokenAlreadyUsed() {
        PasswordResetToken prt = PasswordResetToken.builder()
                .user(User.builder().build()).token("usedtoken")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .usedAt(LocalDateTime.now().minusMinutes(10))
                .build();
        when(tokenRepository.findByToken("usedtoken")).thenReturn(Optional.of(prt));

        assertThatThrownBy(() -> passwordResetService.resetPassword("usedtoken", "newpass123"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.TOKEN_ALREADY_USED));
    }

    @Test
    void resetPassword_expiredToken_throwsTokenExpired() {
        PasswordResetToken prt = PasswordResetToken.builder()
                .user(User.builder().build()).token("expiredtoken")
                .expiresAt(LocalDateTime.now().minusHours(2))
                .build();
        when(tokenRepository.findByToken("expiredtoken")).thenReturn(Optional.of(prt));

        assertThatThrownBy(() -> passwordResetService.resetPassword("expiredtoken", "newpass123"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.TOKEN_EXPIRED));
    }
}
