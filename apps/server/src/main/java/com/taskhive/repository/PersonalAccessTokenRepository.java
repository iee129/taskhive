package com.taskhive.repository;

import com.taskhive.model.PersonalAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalAccessTokenRepository extends JpaRepository<PersonalAccessToken, Long> {
    List<PersonalAccessToken> findByUserIdAndRevokedFalse(Long userId);
    Optional<PersonalAccessToken> findByTokenHashAndRevokedFalse(String tokenHash);
}
