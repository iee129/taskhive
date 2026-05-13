package com.taskhive.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_webhooks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectWebhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 128)
    private String secret;

    @Column(nullable = false)
    private String events;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "consecutive_failures", nullable = false)
    private int consecutiveFailures;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (events == null) events = "task.created,task.updated,task.deleted";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
