# 도메인 모델

## Entity 관계도

```mermaid
erDiagram
    USER {
        Long id PK
        String email UK
        String name
        String password
        LocalDateTime createdAt
    }
    PROJECT {
        Long id PK
        String name
        String description
        Long ownerId FK
        LocalDateTime createdAt
    }
    TASK {
        Long id PK
        String title
        String description
        Status status
        Long projectId FK
        Long assigneeId FK
        LocalDate dueDate
        LocalDateTime createdAt
    }

    USER ||--o{ PROJECT : "소유"
    USER ||--o{ TASK : "담당"
    PROJECT ||--o{ TASK : "포함"
```

## Entity 상세

### User

```java
@Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;           // BCrypt 해시

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "owner")
    private List<Project> projects;
}
```

### Task

```java
@Entity @Table(name = "tasks")
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.TODO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    private LocalDate dueDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum Status { TODO, IN_PROGRESS, DONE }
}
```

## Task.Status 상태 전이

```mermaid
stateDiagram-v2
    [*] --> TODO : 생성 (기본값)
    TODO --> IN_PROGRESS : 작업 시작
    IN_PROGRESS --> DONE : 완료 처리
    IN_PROGRESS --> TODO : 재오픈
    DONE --> IN_PROGRESS : 재작업
```

## 비즈니스 규칙

| 규칙 | 설명 |
|------|------|
| 이메일 유일성 | `users.email`은 UNIQUE 제약 — 중복 가입 불가 |
| 비밀번호 저장 | 평문 저장 금지 — BCrypt 해시만 저장 |
| Task 기본 상태 | 생성 시 `TODO`로 초기화 |
| Soft Delete | 현재 미구현 — `DELETE` 시 실제 행 삭제 |
| 소유권 검증 | Task 수정·삭제 전 `assignee.email == jwtEmail` 검증 (예정) |
