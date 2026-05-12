# 개발 로드맵

> **포지션 목표**: 풀스택 (Java/Spring + React/TypeScript)  
> **강조 역량**: 클린 아키텍처 · 보안/인증 · AI·LLM 연동 · 테스트/TDD · 성능 최적화 · UI/UX

---

## Phase 1 — 프로젝트 골격 ✅ 완료

GitHub 레포지토리 생성, 디렉토리 구조, `.gitignore`, `README.md`

---

## Phase 2 — 백엔드 REST API + JWT 인증 기초 ✅ 완료

JPA 3계층 · JWT 발급·검증 · Task CRUD · GlobalExceptionHandler · MockMvc 14개 테스트

---

## Phase 3 — 프론트엔드 React UI 기초 ✅ 완료

Vite + React 18 + TypeScript 5 + Ant Design 5 · PrivateRoute · 4개 페이지 · `npm run build` 에러 0

---

## Phase 4 — 인증 고도화 ✅ 완료

> Access/Refresh Token 이중 구조 + HttpOnly Cookie + Role 권한

### 작업 순서

1. `Role` enum (`USER`, `ADMIN`) 정의 → `User` 엔티티에 `role` 컬럼 추가
2. `RefreshToken` 엔티티·Repository 구현
3. `RefreshTokenService` — 발급·검증·삭제·Rotation 로직
4. `AuthController` — `/refresh`, `/logout`, `/password` 엔드포인트 추가
5. `SecurityConfig` — `@EnableMethodSecurity` 활성화, httpOnly Cookie CORS 허용
6. 프론트엔드 Axios 인터셉터 — 401 수신 시 `/refresh` 호출 후 원래 요청 재시도
7. 단위·통합 테스트 추가

### 구현 명세

**신규 파일**
```
auth/src/main/java/com/taskhive/
  model/
    RefreshToken.java          # id, token(unique), userId(FK), expiresAt, createdAt
    enums/Role.java            # USER, ADMIN
  repository/
    RefreshTokenRepository.java
  service/
    RefreshTokenService.java   # issue, rotate, invalidate
```

**DB 스키마**
```sql
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

CREATE TABLE refresh_tokens (
  id         BIGSERIAL PRIMARY KEY,
  token      VARCHAR(512) NOT NULL UNIQUE,
  user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
```

**API 계약**

| Method | Path | 요청 | 응답 |
|--------|------|------|------|
| POST | `/api/auth/refresh` | Cookie: `refreshToken` | `{accessToken, expiresIn: 900}` |
| POST | `/api/auth/logout` | Cookie: `refreshToken` | 204 No Content |
| PUT | `/api/auth/password` | `{currentPassword, newPassword}` | 200 OK |

- Access Token 만료 시간: **15분**
- Refresh Token 만료 시간: **7일**, `HttpOnly; Secure; SameSite=Lax`
- Rotation: `/refresh` 호출 시 기존 토큰 삭제 + 신규 발급

### 완료 기준 (AC)

- [x] Access Token 만료 후 프론트엔드가 자동으로 `/refresh` 호출하여 세션 유지
- [x] 응답 Set-Cookie 헤더에 `HttpOnly`, `SameSite=Lax` 확인
- [x] 로그아웃 후 동일 Refresh Token으로 재발급 시도 → 401 반환
- [x] `@PreAuthorize("hasRole('ADMIN')")` 엔드포인트에 USER 접근 → 403 반환
- [x] `RefreshTokenService` 단위 테스트 커버리지 ≥ 90%

---

## Phase 5 — 아키텍처 고도화 ✅ 완료

> ErrorCode enum · Soft Delete · Project 리소스 · OpenAPI · MDC 요청 추적

### 작업 순서

1. `ErrorCode` enum + `BusinessException` 정의
2. `GlobalExceptionHandler` 리팩터 — `BusinessException` 단일 핸들러로 통합
3. `BaseEntity` (`@MappedSuperclass`, `createdAt`, `updatedAt`)
4. `User`, `Task` 엔티티 → `BaseEntity` 상속 + Soft Delete 컬럼 추가
5. `Project` 엔티티·Repository·Service·Controller 구현
6. `Task`에 `project_id` 외래키 추가 (nullable — 기존 데이터 호환)
7. SpringDoc OpenAPI 의존성 추가 + 컨트롤러 어노테이션
8. MDC `RequestIdFilter` 구현 (X-Request-Id 헤더 → 로그 상관)

### 구현 명세

**신규 파일**
```
auth/src/main/java/com/taskhive/
  exception/
    ErrorCode.java             # TASK_NOT_FOUND(404), USER_ALREADY_EXISTS(409), ...
    BusinessException.java     # RuntimeException + ErrorCode
  entity/
    BaseEntity.java            # @MappedSuperclass, createdAt, updatedAt
  model/
    Project.java               # id, name, description, owner(FK), BaseEntity
  repository/ProjectRepository.java
  service/ProjectService.java
  controller/ProjectController.java
  filter/RequestIdFilter.java  # MDC put("requestId", ...)
```

**ErrorCode 응답 형식**
```json
{
  "code": "TASK_NOT_FOUND",
  "message": "태스크를 찾을 수 없습니다.",
  "status": 404,
  "requestId": "a1b2c3d4"
}
```

**DB 스키마**
```sql
CREATE TABLE projects (
  id          BIGSERIAL PRIMARY KEY,
  name        VARCHAR(100) NOT NULL,
  description TEXT,
  owner_id    BIGINT REFERENCES users(id),
  created_at  TIMESTAMPTZ DEFAULT NOW(),
  updated_at  TIMESTAMPTZ,
  deleted_at  TIMESTAMPTZ
);

ALTER TABLE tasks
  ADD COLUMN deleted_at  TIMESTAMPTZ,
  ADD COLUMN project_id  BIGINT REFERENCES projects(id);
```

**Project API**

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/projects` | 내 프로젝트 목록 |
| POST | `/api/projects` | 프로젝트 생성 (`{name, description}`) |
| PUT | `/api/projects/:id` | 수정 |
| DELETE | `/api/projects/:id` | Soft Delete |

### 완료 기준 (AC)

- [x] 존재하지 않는 Task 조회 시 `{"code":"TASK_NOT_FOUND","status":404}` 반환
- [x] Task 삭제 후 목록 조회 시 해당 행 미노출, DB에는 `deleted_at` 기록됨
- [x] `GET /swagger-ui.html` 접속 시 전체 엔드포인트 명세 확인 및 직접 호출 가능
- [x] 모든 API 로그에 `requestId` 필드 포함 (로그 grep 가능)
- [x] 잘못된 필드(`email` 형식 오류 등) 요청 시 `{"code":"INVALID_INPUT","fields":[...]}` 반환

### 검증

- `mvn test` — 33개 테스트 전체 통과

---

## Phase 6 — 기능 확장 ✅ 완료

> 검색/필터 · 우선순위 · 댓글 · 칸반 보드 · **Audit Log (AOP)** · **통계 대시보드** · **AI 연동 (Ollama)**

**브랜치**: `board` | **선행 조건**: Phase 5 완료

### 작업 순서

1. `Task` 엔티티에 `priority` 필드 추가
2. `TaskController` — 페이지네이션·필터·검색 파라미터 지원
3. `TaskRepository` — JPA Specification 또는 QueryDSL 동적 쿼리
4. `Comment` 엔티티·Repository·Controller 구현
5. **`TaskActivity` 엔티티 + `ActivityRepository` 구현**
6. **`ActivityAspect` — `@AfterReturning` AOP로 Task 변경 이력 자동 기록**
7. **`StatsService` + `GET /api/stats/summary` 엔드포인트 구현**
8. 프론트엔드 — 검색창 + 상태/우선순위 필터 드롭다운
9. 프론트엔드 — 칸반 보드 컴포넌트 (`@hello-pangea/dnd`)
10. 프론트엔드 — 댓글 섹션 (태스크 상세 사이드 패널)
11. **프론트엔드 — 통계 카드 위젯 (`StatsWidget.tsx`) + 활동 이력 패널 (`ActivityFeed.tsx`)**
12. Ollama 로컬 AI 서비스 연동 — `application.yml`에 `ollama.base-url` 설정
13. `AiService` — Spring `RestClient`로 Ollama API 호출, JSON 구조화 응답 파싱
14. `AiController` — `POST /api/ai/parse-task`, `GET /api/ai/digest` 엔드포인트
15. 프론트엔드 — AI 입력 모달 (`AiTaskInput.tsx`) + 대시보드 AI 위젯 (`AiDigestWidget.tsx`)

### 구현 명세

**Task 추가 필드**
```sql
ALTER TABLE tasks
  ADD COLUMN priority    VARCHAR(10) DEFAULT 'MEDIUM',  -- LOW/MEDIUM/HIGH
  ADD COLUMN assignee_id BIGINT REFERENCES users(id);
```

**페이지네이션 API**
```
GET /api/tasks?page=0&size=10&sort=dueDate,asc&status=TODO&priority=HIGH&search=회의
```
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5
}
```

**Comment API**

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/tasks/:id/comments` | 댓글 목록 |
| POST | `/api/tasks/:id/comments` | 댓글 작성 (`{content}`) |
| DELETE | `/api/tasks/:id/comments/:cid` | 내 댓글 삭제 |

**프론트엔드 신규 컴포넌트**
```
frontend/src/
  pages/
    BoardPage.tsx              # /board — 칸반 보드 (3열: TODO/IN_PROGRESS/DONE)
  components/
    TaskFilterBar.tsx          # 상태·우선순위·키워드 필터
    CommentSection.tsx         # 댓글 목록 + 입력 폼
    TaskDetailPanel.tsx        # 사이드 슬라이드 패널
    AiTaskInput.tsx            # 자연어 입력 → 태스크 폼 자동 채움 모달
    AiDigestWidget.tsx         # 대시보드 상단 AI 요약 카드
```

**AI 연동 (Ollama)**
```
auth/src/main/java/com/taskhive/
  ai/
    AiService.java         # RestClient → Ollama, 프롬프트 구성 + JSON 파싱
    AiController.java      # POST /api/ai/parse-task, GET /api/ai/digest
  dto/
    AiParseRequest.java    # { text: String }
    AiParseResponse.java   # { title, description, dueDate, priority }
    AiDigestResponse.java  # { summary: String }
```

**Ollama 설정**
```yaml
# application.yml
ollama:
  base-url: http://ollama:11434   # Docker Compose 서비스명
  model: llama3.2:3b
  timeout: 30s
```

**AI API**

| Method | Path | 요청 | 응답 |
|--------|------|------|------|
| POST | `/api/ai/parse-task` | `{"text": "..."}` | `{title, description, dueDate, priority}` |
| GET | `/api/ai/digest` | — (JWT 인증) | `{"summary": "..."}` |

**Audit Log (활동 이력)**
```sql
CREATE TABLE task_activities (
  id           BIGSERIAL PRIMARY KEY,
  task_id      BIGINT NOT NULL REFERENCES tasks(id),
  user_id      BIGINT NOT NULL REFERENCES users(id),
  action       VARCHAR(20) NOT NULL,  -- CREATED / UPDATED / DELETED / STATUS_CHANGED
  field_name   VARCHAR(50),           -- 변경된 필드명 (status, title 등)
  before_value TEXT,
  after_value  TEXT,
  created_at   TIMESTAMP NOT NULL
);
```

```java
// AOP 적용 예시
@AfterReturning(pointcut = "execution(* com.taskhive.service.TaskService.updateTask(..))",
                returning = "result")
public void logTaskUpdate(JoinPoint jp, TaskResponse result) {
    // SecurityContextHolder에서 현재 사용자 추출 후 task_activities 기록
}
```

**통계 API**
```
GET /api/stats/summary
```
```json
{
  "totalTasks": 42,
  "completedTasks": 18,
  "overdueTasks": 5,
  "completionRate": 42.8,
  "createdThisWeek": 7
}
```

**신규 프론트엔드 컴포넌트 (추가)**
```
frontend/src/components/
  StatsWidget.tsx      # 완료율·기한초과 수치 카드 (대시보드 상단)
  ActivityFeed.tsx     # 태스크 변경 이력 타임라인 (사이드 패널)
```

### 완료 기준 (AC)

- [x] `search=키워드` 파라미터로 title 포함 태스크만 필터됨
- [x] `status`, `priority` 파라미터로 복합 필터 동작
- [x] 칸반 보드에서 카드 드래그 → 상태 즉시 변경 + `PUT /api/tasks/:id` 호출 확인
- [x] 댓글 작성 후 새로고침 없이 목록에 즉시 반영
- [x] 다른 사용자의 댓글 삭제 시도 → 403 반환
- [x] `POST /api/ai/suggest-task` — 자연어 설명 입력 시 `{title, description, priority}` JSON 반환
- [x] Ollama 미응답 시 fallback으로 기본값 반환 (graceful degradation)
- [x] Task CRUD 시 `task_activities`에 CREATED/UPDATED/DELETED 자동 기록 (AOP)
- [x] `ActivityFeed`에서 변경 이력 시간순 표시
- [x] `GET /api/stats` — `totalTasks`, `overdue`, 우선순위별·상태별 집계 정확

---

## Phase 6.5 — 실시간 동기화 (WebSocket) 🚧 예정

> 칸반 보드 + WebSocket STOMP — 다른 사용자의 변경이 즉시 반영

**브랜치**: `realtime` | **선행 조건**: Phase 6 완료 (칸반 보드 구현 후 적용)

### 작업 순서

1. `pom.xml` — `spring-boot-starter-websocket` 의존성 추가
2. `WebSocketConfig` — STOMP 엔드포인트(`/ws`) + 메시지 브로커 설정
3. `TaskEventPublisher` — Task 변경 시 `/topic/projects/{projectId}` 채널로 이벤트 발행
4. `TaskController` — 기존 REST 응답 후 WebSocket 이벤트도 함께 발행
5. 프론트엔드 — `@stomp/stompjs` 클라이언트 설정
6. `useBoardSync.ts` — 프로젝트 구독 훅, 수신 이벤트 → 카드 상태 즉시 업데이트
7. 칸반 보드 — WebSocket 수신 시 드래그 없이 카드 위치 자동 이동

### 구현 명세

**WebSocket 메시지 형식**
```json
{
  "type": "TASK_UPDATED",
  "taskId": 5,
  "projectId": 1,
  "updatedBy": "user@example.com",
  "payload": { "status": "IN_PROGRESS" }
}
```

**구독 채널**
```
/topic/projects/{projectId}   # 프로젝트 단위 구독
```

**신규 파일**
```
auth/src/main/java/com/taskhive/
  websocket/
    WebSocketConfig.java        # @EnableWebSocketMessageBroker
    TaskEventPublisher.java     # SimpMessagingTemplate 래퍼

frontend/src/
  hooks/
    useBoardSync.ts             # STOMP 구독 + 상태 동기화
```

### 완료 기준 (AC)

- [ ] 브라우저 탭 2개로 같은 프로젝트 접속 후 한쪽에서 카드 이동 → 다른 탭 자동 반영 (새로고침 없음)
- [ ] WebSocket 연결 끊김 시 자동 재연결 (STOMP `reconnectDelay`)
- [ ] JWT 인증 사용자만 `/ws` 핸드셰이크 가능 (`ChannelInterceptor` 토큰 검증)
- [ ] 10개 동시 접속 시 메시지 유실 없음 (로컬 부하 테스트)

---

## Phase 7 — 테스트 고도화 (TDD) ✅ 완료

> Testcontainers · JaCoCo 80% · React Testing Library · MSW · Playwright E2E

**브랜치**: `testing` | **완료**: 2026-05-12

### 작업 순서

1. Testcontainers `pom.xml` 의존성 추가 + `@TestcontainersConfig` 베이스 클래스
2. `TaskRepositoryTest` — `@DataJpaTest` + 실제 PostgreSQL 슬라이스 테스트
3. `AuthIntegrationTest` — 전체 Spring Context + Testcontainers 시나리오 테스트
4. JaCoCo 플러그인 설정 + 임계값 규칙 (라인 80% 미만 시 빌드 실패)
5. Vitest + React Testing Library 설정
6. MSW `handlers.ts` — API 목업 핸들러 작성
7. 핵심 컴포넌트 테스트 (`LoginPage`, `TasksPage`, `TaskFilterBar`)
8. Playwright 설치 + E2E 시나리오 작성

### 구현 명세

**신규 테스트 파일**
```
auth/src/test/java/com/taskhive/
  config/TestcontainersConfig.java      # @Container PostgreSQL
  repository/TaskRepositoryTest.java    # @DataJpaTest, Soft Delete 검증
  repository/ProjectRepositoryTest.java
  integration/AuthIntegrationTest.java  # register→login→refresh→logout 전 시나리오
  integration/TaskIntegrationTest.java  # CRUD + 페이지네이션 검증

frontend/
  src/
    mocks/
      handlers.ts                        # MSW API 핸들러
      server.ts                          # setupServer
    tests/
      LoginPage.test.tsx
      TasksPage.test.tsx
      TaskFilterBar.test.tsx
  e2e/
    auth.spec.ts                         # 회원가입 → 로그인 → 로그아웃
    task-crud.spec.ts                    # 태스크 생성 → 수정 → 삭제
```

**JaCoCo 임계값 설정**
```xml
<limit>
  <counter>LINE</counter>
  <value>COVEREDRATIO</value>
  <minimum>0.80</minimum>
</limit>
<limit>
  <counter>BRANCH</counter>
  <value>COVEREDRATIO</value>
  <minimum>0.70</minimum>
</limit>
```

**Playwright E2E 시나리오 (auth.spec.ts)**
```
1. /register 접속 → 폼 작성 → 제출 → /tasks 리다이렉트 확인
2. /tasks 에서 로그아웃 → /login 리다이렉트 확인
3. /tasks 직접 접근(비로그인) → /login 리다이렉트 확인
4. 잘못된 비밀번호 로그인 → 에러 메시지 노출 확인
```

### 완료 기준 (AC)

- [x] `mvn verify` — JaCoCo 라인 커버리지 ≥ 80%, 브랜치 커버리지 ≥ 70% (105개 테스트 통과)
- [x] Testcontainers 통합 테스트 — Docker 환경에서 실행 (로컬 Docker 미실행 시 skip)
- [x] `vitest run` — FilterBar(6), LoginPage(5) 컴포넌트 테스트 통과
- [x] Playwright E2E — `playwright.config.ts` 설정 완료, auth/task-crud 시나리오 작성
- [x] MSW 목업으로 네트워크 없이 컴포넌트 테스트 통과

---

## Phase 8 — 성능 최적화 ✅ 완료

> TanStack Query · Lazy Loading · N+1 제거 · Redis 캐싱 · Lighthouse CI

**브랜치**: `performance` | **완료**: 2026-05-12

### 작업 순서

1. TanStack Query (`@tanstack/react-query`) 도입 — `QueryClientProvider` 설정
2. `getTasks`, `getProjects` 훅을 `useQuery`로 전환
3. Optimistic Update — 태스크 상태 변경 즉시 UI 반영
4. React Router `lazy()` + `Suspense` — 페이지 단위 코드 스플리팅
5. `React.memo` + `useCallback` — 리렌더링 병목 제거 (`React DevTools Profiler` 측정)
6. 백엔드 — `@EntityGraph` 또는 Fetch Join으로 N+1 제거 (`show_sql` 확인)
7. DB 인덱스 추가 + `EXPLAIN ANALYZE` 전후 비교 기록
8. Redis 의존성 + `@Cacheable` 설정 (자주 조회되는 프로젝트 목록)
9. Lighthouse CI GitHub Action 설정

### 구현 명세

**TanStack Query 훅 구조**
```
frontend/src/hooks/
  useTasks.ts        # useQuery + useInfiniteQuery (페이지네이션)
  useMutateTask.ts   # useMutation (create/update/delete + invalidateQueries)
  useProjects.ts
```

**캐싱 전략**
```
staleTime: 30_000ms   # 30초간 fresh 유지 (재요청 없음)
gcTime:    300_000ms  # 5분 후 캐시 제거
```

**N+1 수정 대상 쿼리 예시**
```java
// Before: Task 목록 조회 시 User N번 추가 조회 발생
// After:
@Query("SELECT t FROM Task t JOIN FETCH t.assignee WHERE t.deletedAt IS NULL")
Page<Task> findAllWithAssignee(Pageable pageable);
```

**Lighthouse CI 임계값**
```yaml
assert:
  assertions:
    categories:performance:   [warn, {minScore: 0.90}]
    categories:accessibility: [error, {minScore: 0.90}]
```

**성능 목표 수치**

| 지표 | 목표 |
|------|------|
| Lighthouse Performance | ≥ 90 |
| First Contentful Paint | ≤ 1.5s (로컬) |
| 초기 JS 번들 (gzip) | ≤ 300KB |
| 태스크 목록 쿼리 (DB) | Seq Scan 0개 |
| 페이지 재방문 네트워크 요청 | 0 (캐시 히트) |

### 완료 기준 (AC)

- [ ] `React DevTools Profiler` — 태스크 상태 변경 시 관련 컴포넌트만 리렌더링
- [ ] 태스크 목록 페이지 재방문 시 Network 탭에서 API 요청 미발생 (캐시 히트)
- [ ] `EXPLAIN ANALYZE` 결과 — 태스크 목록 쿼리 Seq Scan → Index Scan 전환
- [ ] Lighthouse Performance 점수 ≥ 90, Accessibility ≥ 90
- [ ] 초기 번들에 `TasksPage`, `BoardPage` 코드 미포함 (Network 탭 chunk 확인)

---

## Phase 9 — UI/UX 완성도 ✅ 완료

> 반응형 · 다크모드 · 스켈레톤 · Error Boundary · 접근성

**브랜치**: `polish` | **완료**: 2026-05-12

### 작업 순서

1. Ant Design `ConfigProvider` theme token 기반 다크모드 구현 + `localStorage` 저장
2. 반응형 레이아웃 — 모바일(375px)에서 Sider → Drawer 메뉴 전환
3. `Skeleton` 컴포넌트 — 태스크 목록 로딩 중 스켈레톤 UI 표시
4. React `ErrorBoundary` 클래스 컴포넌트 — 페이지 단위 에러 격리 + fallback UI
5. 전역 알림 시스템 — `AntdNotificationProvider` (성공·실패 토스트 일원화)
6. 폼 비동기 검증 — 이메일 중복 여부 실시간 서버 검증
7. ARIA 속성 추가 + Tab 키 네비게이션 전체 경로 검증

### 구현 명세

**신규/수정 파일**
```
frontend/src/
  components/
    ErrorBoundary.tsx          # componentDidCatch + fallback prop
    ThemeToggle.tsx            # 다크/라이트 토글 버튼
    SkeletonTable.tsx          # Ant Design Skeleton rows
    NotificationProvider.tsx   # Context + antd notification API
  hooks/
    useTheme.ts                # localStorage 'theme' ↔ antd algorithm
    useCheckEmail.ts           # 이메일 중복 비동기 검증 훅
```

**다크모드 토큰 적용**
```typescript
theme={{
  algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,
  token: { colorPrimary: '#6366f1' }
}}
```

**Error Boundary 사용 예시**
```tsx
<ErrorBoundary fallback={<ErrorPage />}>
  <TasksPage />
</ErrorBoundary>
```

### 완료 기준 (AC)

- [x] 다크모드 토글 후 새로고침해도 다크모드 유지 (`localStorage` 확인)
- [x] 모바일 — `Grid.useBreakpoint` 감지, Sider → Drawer 전환 (햄버거 버튼)
- [x] 태스크 목록 로딩 중 스켈레톤 UI 노출, 데이터 수신 후 즉시 교체
- [x] API 에러 발생 시 ErrorBoundary fallback UI 노출, 앱 전체 크래시 없음
- [x] 회원가입 이메일 blur 시 서버 중복 검증 (`GET /api/auth/check-email`)
- [x] `aria-label` (메뉴, 토글 버튼, 스켈레톤 role=status) ARIA 속성 추가

---

## Phase 10 — PostgreSQL 전환 + Docker Compose 통합 ✅ 완료

> H2 → 실제 RDB · Flyway 마이그레이션 · 4-컨테이너 스택

**브랜치**: `docker` | **완료**: 2026-05-12

### 작업 순서

1. `pom.xml` — PostgreSQL JDBC 드라이버 + Flyway 의존성 추가
2. `application-prod.yml` — PostgreSQL DataSource 설정
3. `src/main/resources/db/migration/` — Flyway 스크립트 작성 (`V1__init.sql` …)
4. H2 테스트 환경 유지 (`application-test.yml`), 프로덕션만 PostgreSQL 전환
5. `auth/Dockerfile` — Multi-stage (Maven → JRE 21-slim)
6. `frontend/Dockerfile` — Multi-stage (Node build → Nginx)
7. `nginx.conf` — `/api/*` → `backend:8080` 리버스 프록시
8. `docker-compose.yml` — PostgreSQL + Redis + Backend + Frontend + 환경변수
9. `docker compose up -d` 통합 실행 검증

### 구현 명세

**Flyway 스크립트 목록**
```
db/migration/
  V1__init_users.sql
  V2__add_projects.sql
  V3__add_tasks.sql
  V4__add_refresh_tokens.sql
  V5__add_comments.sql
  V6__add_task_activities_and_indexes.sql
```

**docker-compose.yml 서비스 구성**
```yaml
services:
  postgres:   image: postgres:16-alpine, port 5432
  redis:      image: redis:7-alpine, port 6379
  ollama:     image: ollama/ollama, port 11434, volumes: ollama_data:/root/.ollama
  backend:    build: ./auth, port 8080, depends_on: postgres, redis, ollama
  frontend:   build: ./frontend, port 80, depends_on: backend
volumes:
  postgres_data:
  ollama_data:
```

**Nginx 프록시 규칙**
```nginx
location /api/ {
    proxy_pass http://backend:8080;
}
location / {
    root /usr/share/nginx/html;
    try_files $uri /index.html;
}
```

### 완료 기준 (AC)

- [x] `docker compose up -d` 한 명령으로 전체 스택 구동 (postgres→redis→backend→frontend)
- [x] `localhost` 접속 → 프론트엔드 80포트, Nginx `/api/` 프록시로 백엔드 연결
- [x] Flyway V1~V6 스크립트 실행 — 재실행 시 적용된 스크립트 건너뜀 (멱등성)
- [x] `postgres_data` + `ollama_data` Volume으로 재시작 후 데이터 유지
- [x] `auth/Dockerfile` Multi-stage (eclipse-temurin:21-jdk-alpine → jre-alpine)

---

## Phase 11 — CI/CD 🚧 예정

> GitHub Actions · 커버리지 게이트 · Docker 이미지 GHCR 푸시

**브랜치**: `cicd` | **선행 조건**: Phase 10 완료

### 작업 순서

1. Branch protection rule — `main` PR 병합 전 CI 통과 필수
2. `ci.yml` — PR 트리거: Java 빌드 + Testcontainers 테스트 + JaCoCo 커버리지 코멘트
3. `ci.yml` — PR 트리거: React 빌드 + Vitest + Playwright E2E
4. `ci.yml` — `main` 병합 트리거: Docker 이미지 빌드 + GHCR 푸시
5. Dependabot 설정 (`dependabot.yml`)

### 구현 명세

**워크플로우 파일**
```
.github/
  workflows/
    ci-backend.yml    # PR: mvn verify + JaCoCo 커버리지 PR 코멘트
    ci-frontend.yml   # PR: npm build + vitest + playwright
    cd-docker.yml     # main merge: docker build + push to ghcr.io
  dependabot.yml      # maven + npm 자동 업데이트
```

**JaCoCo PR 코멘트 예시**
```
## Coverage Report
| 패키지 | 라인 | 브랜치 |
|--------|------|--------|
| service | 87% | 82% |
| controller | 91% | 78% |
| **전체** | **85%** | **76%** |
```

**GHCR 이미지 태그 전략**
```
ghcr.io/iee129/taskhive-backend:latest
ghcr.io/iee129/taskhive-backend:sha-{commit_sha}
ghcr.io/iee129/taskhive-frontend:latest
```

### 완료 기준 (AC)

- [ ] PR 오픈 시 백엔드·프론트엔드 CI 자동 실행, 커버리지 미달 시 빌드 실패
- [ ] PR에 JaCoCo 커버리지 수치 자동 코멘트
- [ ] `main` 병합 시 GHCR에 이미지 자동 푸시 (Actions 로그 확인)
- [ ] Dependabot PR 자동 생성 확인 (최소 1개)
- [ ] 테스트 실패하는 코드 PR 시 GitHub Checks 빨간 표시

---

## Phase 12 — Kubernetes 배포 🚧 예정 (선택)

> 컨테이너 오케스트레이션 이해도를 코드로 증명

**브랜치**: `kubernetes` | **선행 조건**: Phase 11 완료

매니페스트 목록, HPA, Ingress 설정, minikube 검증 — 취업 준비 상황에 따라 진행 여부 결정

---

## 향후 계획 (미정)

- 실시간 알림 (WebSocket + STOMP)
- OAuth2 소셜 로그인 (Google, GitHub)
- 파일 첨부 (MinIO Presigned URL)
- 팀 초대 이메일 (JavaMailSender)
