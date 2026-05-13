# TaskHive 아키텍처 가이드

## 왜 만들었나

TaskHive는 "포트폴리오용 프로젝트 관리 도구"가 아니라, 실제 팀이 쓸 수 있는 **셀프호스팅 태스크 관리 + AI 코파일럿**을 목표로 설계했다.

### 만든 배경

오픈소스 프로젝트 관리 도구(Plane.so, Vikunja, Focalboard)는 대부분 두 가지 중 하나를 선택하라고 강요한다: **클라우드 전용 AI** (데이터가 외부로 나감) 또는 **AI 없음**. TaskHive는 `AI_PROVIDER=ollama|groq|none` 환경변수 하나로 이 선택을 런타임에 내릴 수 있게 설계했다. 민감한 데이터를 외부로 보내지 않고도 AI 기능을 쓸 수 있다.

### 핵심 설계 결정 3가지

1. **BYO-LLM 전략 패턴**: `AiProvider` 인터페이스 + OllamaProvider / GroqProvider / NoopProvider. 테스트 시에는 FakeProvider로 교체 — 실제 LLM 호출 없이 AI 엔드포인트 통합 테스트 가능.
2. **match_key 기반 GroupKFold 데이터 누수 차단**: ML 파이프라인에서 경기 단위로 train/val/test를 나눠 동일 경기가 두 셋에 걸치는 누수를 원천 차단. GroupKFold 없이는 AUC=0.99+처럼 보이지만 실제론 과적합이다.
3. **트랜잭션 경계를 서비스 레이어에**: Controller는 DTO 변환만, Service만 `@Transactional`. Repository의 JPQL 파생 쿼리는 `@EntityGraph`로 N+1을 컴파일 시점에 차단.

## 시스템 구성 개요

```
┌─────────────────────────────────────────────────────────┐
│                     Client (Browser)                     │
│            React 18 + TypeScript + Ant Design            │
└────────────────────────┬────────────────────────────────┘
                         │ HTTPS / WebSocket
                         ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Boot 3 REST API (Java 21)            │
│                                                         │
│  ┌──────────┐  ┌──────────┐  ┌───────────┐             │
│  │Controller│→│  Service │→│Repository │             │
│  └──────────┘  └──────────┘  └─────┬─────┘             │
│                                     │ JPA/Hibernate      │
└─────────────────────────────────────┼───────────────────┘
                                      │
                         ┌────────────▼────────────┐
                         │      PostgreSQL 16        │
                         └─────────────────────────┘
```

**외부 서비스:**
- **SMTP** — 이메일 인증 링크, 비밀번호 재설정 링크 발송
- **Sentry** — 런타임 에러 수집 및 알림
- **Ollama** (선택, 로컬) — AI 태스크 제안 (로컬 LLM, 기본값)
- **Groq** (선택, 클라우드) — 빠른 클라우드 LLM 추론 (`AI_PROVIDER=groq`)

---

## 백엔드 레이어 구조

```
com.taskhive/
├── controller/       # HTTP 요청 수신, DTO 변환, 응답 반환
│   ├── AuthController        — 인증 흐름 (register, login, logout, refresh, verify, reset)
│   ├── ProjectController     — 프로젝트 CRUD
│   ├── ProjectMemberController — 멤버 초대·제거·조회
│   ├── TaskController        — 태스크 CRUD + 상태 일괄 업데이트
│   ├── CommentController     — 댓글 조회·추가·삭제
│   ├── StatsController       — 통계 요약·활동 피드
│   ├── AnalyticsController   — 번다운·CFD·사이클 타임 분석
│   ├── LabelController       — 라벨 CRUD + 태스크 라벨 연결
│   ├── PersonalAccessTokenController — PAT 생성·조회·폐기
│   ├── WebhookController     — 아웃고잉 웹훅 등록·목록·삭제
│   ├── SearchController      — 전역 태스크 퍼지 검색 (GET /api/search)
│   ├── UserController        — 사용자 검색 (이메일 자동완성)
│   └── AiController          — AI 요약·자연어 필터·브레인덤프·스탠드업·우선순위·공수 추정
│
├── service/          # 비즈니스 로직, 트랜잭션 경계
│   ├── AuthService           — JWT 발급, 이메일 인증, 비밀번호 재설정
│   ├── ProjectService        — 프로젝트 권한 검사(멤버여부·OWNER), CRUD
│   ├── ProjectMemberService  — 멤버 초대/제거, LAST_OWNER 방어
│   ├── TaskService           — 태스크 CRUD, 프로젝트 멤버십 검사, 라벨 연결
│   ├── CommentService        — 댓글 CRUD, 프로젝트 멤버십 검사
│   ├── StatsService          — 집계 쿼리
│   ├── AnalyticsService      — 번다운·CFD·사이클 타임 집계
│   ├── WebhookDeliveryService — HMAC-SHA256 서명 @Async 전달, 5회 실패 자동 비활성
│   ├── PersonalAccessTokenService — PAT SHA-256 해시 발급·인증
│   └── AiService             — AiProvider 전략 패턴으로 LLM 호출 위임
│
├── service/ai/       # AiProvider 전략 구현체
│   ├── AiProvider    — 인터페이스
│   ├── OllamaProvider — 로컬 Ollama REST 호출
│   ├── GroqProvider  — Groq 클라우드 API 호출
│   └── NoopProvider  — AI 비활성화 (항상 빈 응답)
│
├── repository/       # Spring Data JPA 인터페이스
│   ├── UserRepository
│   ├── ProjectRepository
│   ├── ProjectMemberRepository
│   ├── TaskRepository           — @EntityGraph N+1 차단, labelId 필터
│   ├── CommentRepository
│   ├── TaskActivityRepository
│   ├── TaskStatusHistoryRepository
│   ├── LabelRepository
│   ├── PersonalAccessTokenRepository
│   ├── ProjectWebhookRepository
│   ├── RefreshTokenRepository
│   └── PasswordResetTokenRepository
│
├── model/            # JPA 엔티티
│   ├── BaseEntity    — createdAt, updatedAt (자동 감사)
│   ├── User
│   ├── Project
│   ├── ProjectMember — OWNER / MEMBER 역할
│   ├── Task          — ManyToMany labels
│   ├── Comment
│   ├── TaskActivity  — 활동 이력
│   ├── TaskStatusHistory — 상태 변경 이력 (사이클 타임 계산용)
│   ├── Label         — 프로젝트별 색상 태그
│   ├── PersonalAccessToken — SHA-256 해시, 스코프, 폐기 여부
│   ├── ProjectWebhook — URL, HMAC 시크릿, 이벤트 목록, 연속 실패 카운트
│   ├── RefreshToken
│   ├── PasswordResetToken
│   └── enums/        — ProjectMemberRole, TaskStatus, TaskPriority
│
├── dto/              # 요청(Request) · 응답(Response) record 클래스
├── security/         # JwtUtil, JwtAuthFilter, SecurityConfig, PATAuthFilter
├── filter/           # RequestIdFilter (X-Request-Id 전파)
└── exception/        # ErrorCode enum, GlobalExceptionHandler
```

---

## 인증 흐름

### 회원가입 → 이메일 인증 → 로그인

```
Client                    Server                      DB
  │                          │                         │
  │── POST /api/auth/register ──▶                       │
  │                          │── INSERT user ──────────▶│
  │                          │   (emailVerified=false)  │
  │                          │── SMTP 발송 (토큰 포함) ─▶│ (email)
  │◀── 200 OK (안내 메시지) ──│                         │
  │                          │                         │
  │── GET /api/auth/verify-email?token={t} ──▶          │
  │                          │── emailVerified=true ───▶│
  │◀── 200 OK ───────────────│                         │
  │                          │                         │
  │── POST /api/auth/login ──▶                          │
  │                          │── SELECT user ──────────▶│
  │                          │   emailVerified 검사     │
  │◀── 200 OK ───────────────│                         │
  │   { accessToken, name }  │── INSERT refresh_token ─▶│
  │   Set-Cookie: refreshToken (HttpOnly)               │
```

### 토큰 갱신

```
Client                    Server
  │                          │
  │── POST /api/auth/refresh ─▶ (Cookie: refreshToken 자동 전송)
  │                          │── DB에서 refresh_token 검증
  │◀── { accessToken } ──────│
```

### 보호된 엔드포인트 접근

```
Client                    JwtAuthFilter              Controller
  │                          │                          │
  │── Authorization: Bearer {token} ──▶                 │
  │                          │── JWT 검증·파싱           │
  │                          │── SecurityContext 설정    │
  │                          │──────────────────────────▶│
  │                          │              비즈니스 처리 │
  │◀─────────────────────────────────────── 응답 ────────│
```

---

## 프로젝트 멤버 권한 모델

TaskHive의 프로젝트는 **OWNER**와 **MEMBER** 두 역할을 사용합니다.

| 작업 | OWNER | MEMBER |
|------|:-----:|:------:|
| 프로젝트 조회 | ✅ | ✅ |
| 프로젝트 수정 | ✅ | ✅ |
| 프로젝트 삭제 | ✅ | ❌ |
| 멤버 초대 | ✅ | ✅ |
| 멤버 제거 | ✅ | ✅ |
| 태스크 CRUD | ✅ | ✅ |
| 댓글 CRUD | ✅ | ✅ |

**LAST_OWNER 방어:** 프로젝트에 OWNER가 1명뿐일 때 제거 요청 시 `400 LAST_OWNER` 에러를 반환합니다.

프로젝트 생성 시 생성자는 자동으로 OWNER 멤버로 등록됩니다.

---

## 핵심 DB 엔티티 및 관계

```
users
  id (PK), email, password, name
  email_verified, deleted_at
  created_at, updated_at

projects
  id (PK), name, description, owner_id (FK→users)
  deleted_at, created_at, updated_at

project_members                         ← 다대다 조인 테이블 (확장됨)
  id (PK)
  project_id (FK→projects)
  user_id (FK→users)
  role  (OWNER | MEMBER)
  created_at
  UNIQUE (project_id, user_id)

tasks
  id (PK), title, description
  status (TODO | IN_PROGRESS | DONE)
  priority (LOW | MEDIUM | HIGH)
  due_date, assignee_id (FK→users)
  project_id (FK→projects, nullable)
  creator_id (FK→users)
  deleted_at, created_at, updated_at

comments
  id (PK), content
  task_id (FK→tasks), author_id (FK→users)
  deleted_at, created_at, updated_at

task_activities
  id (PK), action, detail
  task_id (FK→tasks), user_id (FK→users)
  created_at

refresh_tokens
  id (PK), token (UNIQUE)
  user_id (FK→users)
  expires_at, revoked

password_reset_tokens
  id (PK), token (UNIQUE)
  user_id (FK→users)
  expires_at, used
```

---

## 에러 응답 형식

모든 에러는 `GlobalExceptionHandler`에서 통일된 형식으로 반환됩니다:

```json
{
  "code": "ERROR_CODE_NAME",
  "message": "에러 설명",
  "status": 400
}
```

에러 코드는 `ErrorCode` enum에 정의되어 있으며, HTTP 상태와 메시지를 함께 보유합니다. 전체 목록은 [API.md](API.md#에러-코드-목록)를 참조하세요.

---

## 웹 테스트 환경

### Swagger UI + JWT

`springdoc-openapi 2.5.0`으로 Swagger UI를 제공합니다. `OpenApiConfig.java`에서 전역 `bearerAuth` SecurityScheme을 정의하여, UI의 **Authorize** 버튼으로 JWT를 입력하면 이후 모든 API 호출에 자동 첨부됩니다.

접속: `{백엔드 URL}/swagger-ui.html`

### Security 멀티체인 (dev 프로파일)

`/api/dev/**` 경로는 `@Profile("dev")` + `@Order(1)` 으로 분리된 `DevSecurityConfig`의 전용 체인에서 인증 없이 허용됩니다. 기존 `SecurityConfig`의 전역 체인은 변경하지 않으므로 프로덕션 보안에 영향이 없습니다.

```
요청: /api/dev/seed
        │
        ▼
DevSecurityConfig (Order=1, Profile=dev)
  securityMatcher("/api/dev/**") → permitAll
        │  매칭 시 여기서 처리 완료
        ▼  매칭 실패 시
SecurityConfig (Order=2, 전역)
  JWT 검증 필요
```

### Playwright E2E + GitHub Actions

`frontend/e2e/` 아래 4개 spec 파일로 인증·태스크·프로젝트·AI 흐름을 자동 검증합니다. CI(`.github/workflows/e2e.yml`)는 push·PR 트리거로 실행되며, `E2E_BACKEND_URL` secret이 설정된 경우 테스트 전 seed API를 호출하여 데이터를 초기화합니다.

자세한 사용법은 [TESTING.md](TESTING.md)를 참조하세요.

---

## 주요 기술 결정

### 스키마 관리: Flyway 마이그레이션

`ddl-auto=validate`를 사용하며, 스키마는 `auth/src/main/resources/db/migration/`의 Flyway 파일로만 변경합니다.

| 파일 | 내용 |
|------|------|
| `V1__baseline.sql` | 초기 스키마 (users, projects, tasks, comments 등) |
| `V2__task_status_history.sql` | task_status_history 테이블 |
| `V3__indexes.sql` | 성능 인덱스 3개 (tasks.project_id+status, task_activities.task_id, task_activities.occurred_at) |
| `V4__personal_access_tokens.sql` | personal_access_tokens 테이블 (SHA-256 해시, 스코프, 폐기 플래그) |
| `V5__project_webhooks.sql` | project_webhooks 테이블 (URL, 시크릿, 이벤트, 연속 실패 카운트) |
| `V6__labels.sql` | labels + task_labels 테이블 (ManyToMany) |

### Soft Delete
`Task`와 `Comment`는 `deleted_at` 컬럼으로 소프트 삭제를 구현합니다. 모든 조회 쿼리는 `WHERE deleted_at IS NULL` 조건을 포함합니다.

### JWT Stateless + Refresh Token Stateful
액세스 토큰(15분)은 stateless JWT이고, 리프레시 토큰(7일)은 DB에 저장하여 명시적 로그아웃(토큰 무효화)을 지원합니다.

### Spring Security 보안 헤더

`SecurityConfig`의 `.headers()` DSL로 5개 헤더를 전역 적용합니다:

| 헤더 | 값 |
|------|----|
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` |
| `Content-Security-Policy` | `default-src 'self'` |
| `X-Content-Type-Options` | `nosniff` |
| `Referrer-Policy` | `strict-origin-when-cross-origin` |
| `X-Frame-Options` | `DENY` |

### Rate Limiting: bucket4j
로그인 엔드포인트는 분당 10회로 제한됩니다. 메모리 내 버킷을 사용하며, 분산 환경에서는 Redis 백엔드로 전환이 필요합니다.

### 프로젝트 멤버십 검사
프로젝트 리소스에 대한 모든 접근은 `ProjectMemberRepository.existsByProjectIdAndUserId()`로 멤버십을 확인합니다. 비멤버 접근 시 `403 NOT_PROJECT_MEMBER`를 반환합니다.
