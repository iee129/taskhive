package com.taskhive.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_activities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "task_title")
    private String taskTitle;

    @Column(name = "actor_email", nullable = false)
    private String actorEmail;

    @Column(nullable = false, length = 50)
    private String action;

    private String detail;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
