# 데이터베이스 스키마

> Hibernate `ddl-auto=update`로 Entity에서 자동 생성됨 (개발/테스트 환경).  
> 프로덕션에서는 Flyway 마이그레이션 스크립트 사용 예정 (Phase 10).

## 테이블 목록

### users

```sql
CREATE TABLE users (
    id          BIGSERIAL       PRIMARY KEY,
    email       VARCHAR(255)    NOT NULL UNIQUE,
    name        VARCHAR(100)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,   -- BCrypt 해시
    role        VARCHAR(20)     NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL
);
```

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | BIGSERIAL | PK | 자동 증가 식별자 |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | 로그인 ID, JWT subject |
| `name` | VARCHAR(100) | NOT NULL | 표시 이름 |
| `password` | VARCHAR(255) | NOT NULL | BCrypt 해시 (60자) |
| `role` | VARCHAR(20) | NOT NULL | USER \| ADMIN (기본값 USER) |
| `created_at` | TIMESTAMP | NOT NULL | 가입 시각 (JPA Auditing 자동 설정) |
| `updated_at` | TIMESTAMP | NOT NULL | 마지막 수정 시각 (JPA Auditing 자동 설정) |

### refresh_tokens

```sql
CREATE TABLE refresh_tokens (
    id          BIGSERIAL       PRIMARY KEY,
    token       VARCHAR(512)    NOT NULL UNIQUE,
    user_id     BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at  TIMESTAMP       NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
```

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | BIGSERIAL | PK | 자동 증가 식별자 |
| `token` | VARCHAR(512) | NOT NULL, UNIQUE | UUID 토큰 값 |
| `user_id` | BIGINT | FK → users | 토큰 소유자 |
| `expires_at` | TIMESTAMP | NOT NULL | 만료 시각 (7일) |
| `created_at` | TIMESTAMP | NOT NULL | 발급 시각 |

### projects

```sql
CREATE TABLE projects (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(200)    NOT NULL,
    description TEXT,
    owner_id    BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,
    deleted_at  TIMESTAMP                  -- NULL이면 활성 프로젝트
);
```

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | BIGSERIAL | PK | 자동 증가 식별자 |
| `name` | VARCHAR(200) | NOT NULL | 프로젝트 이름 |
| `description` | TEXT | - | 프로젝트 설명 |
| `owner_id` | BIGINT | FK → users | 생성자 |
| `created_at` | TIMESTAMP | NOT NULL | 생성 시각 (JPA Auditing) |
| `updated_at` | TIMESTAMP | NOT NULL | 수정 시각 (JPA Auditing) |
| `deleted_at` | TIMESTAMP | - | 소프트 삭제 시각 (NULL = 활성) |

### tasks

```sql
CREATE TABLE tasks (
    id          BIGSERIAL       PRIMARY KEY,
    title       VARCHAR(500)    NOT NULL,
    description TEXT,
    status      VARCHAR(20)     NOT NULL DEFAULT 'TODO'
                CHECK (status IN ('TODO','IN_PROGRESS','DONE')),
    project_id  BIGINT          REFERENCES projects(id) ON DELETE SET NULL,
    assignee_id BIGINT          REFERENCES users(id) ON DELETE SET NULL,
    due_date    DATE,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,
    deleted_at  TIMESTAMP                  -- NULL이면 활성 태스크
);
```

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | BIGSERIAL | PK | 자동 증가 식별자 |
| `title` | VARCHAR(500) | NOT NULL | 태스크 제목 |
| `description` | TEXT | - | 상세 설명 |
| `status` | VARCHAR(20) | CHECK | TODO / IN_PROGRESS / DONE |
| `project_id` | BIGINT | FK → projects | 소속 프로젝트 (nullable) |
| `assignee_id` | BIGINT | FK → users | 담당자 (nullable) |
| `due_date` | DATE | - | 마감일 |
| `created_at` | TIMESTAMP | NOT NULL | 생성 시각 (JPA Auditing) |
| `updated_at` | TIMESTAMP | NOT NULL | 수정 시각 (JPA Auditing) |
| `deleted_at` | TIMESTAMP | - | 소프트 삭제 시각 (NULL = 활성) |

## FK 삭제 정책

| 참조 | 정책 | 이유 |
|------|------|------|
| refresh_tokens.user_id → users | CASCADE | 회원 탈퇴 시 토큰 자동 삭제 |
| tasks.project_id → projects | SET NULL | 프로젝트 삭제 시 태스크 보존 |
| tasks.assignee_id → users | SET NULL | 회원 탈퇴 시 태스크 보존 |
| projects.owner_id → users | CASCADE | 소유자 삭제 시 프로젝트도 삭제 |

## 소프트 삭제 쿼리 패턴

```java
// TaskRepository
List<Task> findAllByDeletedAtIsNull();
Optional<Task> findByIdAndDeletedAtIsNull(Long id);

// ProjectRepository
List<Project> findByOwnerIdAndDeletedAtIsNull(Long ownerId);
Optional<Project> findByIdAndDeletedAtIsNull(Long id);
```

삭제 시 `taskRepository.deleteById()` 대신:

```java
task.setDeletedAt(LocalDateTime.now());  // 소프트 삭제 — DB 행 유지
```
