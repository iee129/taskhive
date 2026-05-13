package com.taskhive.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "personal_access_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PersonalAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private String scopes;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (scopes == null) scopes = "read:write";
    }
}
