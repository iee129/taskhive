package com.taskhive.service;

import com.taskhive.dto.TokenCreateResponse;
import com.taskhive.dto.TokenListResponse;
import com.taskhive.exception.BusinessException;
import com.taskhive.exception.ErrorCode;
import com.taskhive.model.PersonalAccessToken;
import com.taskhive.model.User;
import com.taskhive.repository.PersonalAccessTokenRepository;
import com.taskhive.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalAccessTokenService {

    private final PersonalAccessTokenRepository patRepository;
    private final UserRepository userRepository;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public TokenCreateResponse createToken(String email, String name) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        String rawToken = "th_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String tokenHash = sha256(rawToken);

        PersonalAccessToken pat = PersonalAccessToken.builder()
                .user(user)
                .name(name)
                .tokenHash(tokenHash)
                .scopes("read:write")
                .revoked(false)
                .build();
        patRepository.save(pat);

        return new TokenCreateResponse(pat.getId(), pat.getName(), rawToken, pat.getCreatedAt());
    }

    public List<TokenListResponse> listTokens(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return patRepository.findByUserIdAndRevokedFalse(user.getId()).stream()
                .map(TokenListResponse::from)
                .toList();
    }

    @Transactional
    public void revokeToken(String email, Long tokenId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        PersonalAccessToken pat = patRepository.findById(tokenId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_NOT_FOUND));
        if (!pat.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        pat.setRevoked(true);
        patRepository.save(pat);
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 해시 생성 실패", e);
        }
    }
}
