# 데이터베이스 인덱스 전략

## 현재 인덱스

### 자동 생성 (제약 조건)

| 인덱스 | 테이블 | 컬럼 | 생성 원인 |
|--------|--------|------|----------|
| `users_pkey` | users | id | PK |
| `users_email_key` | users | email | UNIQUE |
| `projects_pkey` | projects | id | PK |
| `tasks_pkey` | tasks | id | PK |

### 추가 권장 인덱스

```sql
-- 태스크 목록 조회 최적화 (assignee_id로 필터링)
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);

-- 프로젝트별 태스크 조회 최적화
CREATE INDEX idx_tasks_project_id ON tasks(project_id);

-- 상태 필터링 (status + assignee_id 복합)
CREATE INDEX idx_tasks_assignee_status ON tasks(assignee_id, status);

-- 프로젝트 소유자 조회
CREATE INDEX idx_projects_owner_id ON projects(owner_id);
```

## 인덱스 선택 근거

| 쿼리 패턴 | 인덱스 | 예상 효과 |
|-----------|--------|----------|
| `WHERE assignee_id = ?` | `idx_tasks_assignee_id` | Seq Scan → Index Scan |
| `WHERE assignee_id = ? AND status = ?` | `idx_tasks_assignee_status` | 복합 조건 커버 |
| `WHERE project_id = ?` | `idx_tasks_project_id` | 프로젝트 태스크 조회 |
| `WHERE email = ?` (로그인) | `users_email_key` (UNIQUE) | 이미 존재 |

## 인덱스 관리 원칙

- 인덱스는 읽기 성능을 높이지만 쓰기 비용(INSERT/UPDATE)을 증가시킴
- 현재 단계(MVP)에서는 FK 컬럼 인덱스 위주로 최소화
- 데이터가 10만 행 이상 누적되면 `EXPLAIN ANALYZE`로 실제 쿼리 계획 검증
- 중복 인덱스 생성 금지 — `pg_stat_user_indexes`로 사용률 주기적 확인
