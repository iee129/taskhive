package com.taskhive.service;

import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.PasswordResetToken;
import com.taskhive.repository.PasswordResetTokenRepository;
import com.taskhive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void sendResetEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString().replace("-", "");
            PasswordResetToken prt = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            tokenRepository.save(prt);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        if (prt.getUsedAt() != null) {
            throw new BusinessException(ErrorCode.TOKEN_ALREADY_USED);
        }
        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        prt.setUsedAt(LocalDateTime.now());
        tokenRepository.save(prt);
        prt.getUser().setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(prt.getUser());
    }
}
