package com.taskhive.service;

import com.taskhive.exception.InvalidTokenException;
import com.taskhive.model.RefreshToken;
import com.taskhive.model.User;
import com.taskhive.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${taskhive.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public RefreshToken issue(User user) {
        refreshTokenRepository.deleteByUser(user);
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();
        return refreshTokenRepository.save(token);
    }

    @Transactional
    public RefreshToken rotate(String tokenValue) {
        RefreshToken existing = refreshTokenRepository.findByTokenForUpdate(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 Refresh Token입니다"));

        if (existing.isExpired()) {
            refreshTokenRepository.delete(existing);
            throw new InvalidTokenException("만료된 Refresh Token입니다");
        }

        return issue(existing.getUser());
    }

    @Transactional
    public void invalidate(String tokenValue) {
        refreshTokenRepository.deleteByToken(tokenValue);
    }

    public User getUserByToken(String tokenValue) {
        return refreshTokenRepository.findByToken(tokenValue)
                .filter(t -> !t.isExpired())
                .map(RefreshToken::getUser)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 Refresh Token입니다"));
    }
}
