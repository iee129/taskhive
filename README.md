# TaskHive 🐝

> 벌집처럼 유기적으로 연결되는 팀 작업·프로젝트 통합 관리 플랫폼

## 🚀 데모 (무료 호스팅)

| 링크 | 설명 |
|------|------|
| **[taskhive.vercel.app](https://taskhive.vercel.app)** *(배포 후 업데이트 예정)* | 프론트엔드 (Vercel) |
| **백엔드** | Render Web Service |

**데모 계정**: `test@example.com` / `Test1234!`

> ⚠️ **무료 티어 안내**: 백엔드가 슬립 상태일 경우 첫 접속 시 30–60초 대기가 발생할 수 있습니다. 화면의 "서버를 깨우는 중..." 안내를 기다려 주세요.
>
> ⚠️ **공개 데모는 Groq(클라우드 LLM)를 사용합니다.** 셀프호스팅 시 기본값은 Ollama(로컬) — 데이터가 외부로 나가지 않습니다.

## ⚡ 빠른 시작 (로컬 셀프호스팅)

```bash
git clone https://github.com/iee129/taskhive.git
cd taskhive
docker compose up
```

- 프론트엔드: http://localhost
- 백엔드 API: http://localhost:8080
- 데모 계정이 자동 생성됩니다 (`test@example.com` / `Test1234!`)

> **사전 요구 사항**: Docker Desktop 설치 필요. 첫 빌드는 약 5–10분 소요됩니다.

## 기술 스택

| 레이어 | 기술 |
|--------|------|
| 백엔드 | Java 21 + Spring Boot 3 + Spring Security (JWT) |
| 프론트엔드 | React 18 + TypeScript 5 + Vite + Ant Design |
| 데이터베이스 | PostgreSQL 16 |
| 인증 | JWT (액세스 15분) + Refresh Token (HttpOnly Cookie, 7일) |
| 이메일 | SMTP (이메일 인증 · 비밀번호 재설정) |
| Rate Limiting | bucket4j |
| 에러 모니터링 | Sentry |
| 배포 | Render (백엔드) + Vercel (프론트엔드) + Neon (Postgres) |
| CI | GitHub Actions |

## 구현된 기능

- **인증** — 회원가입 (이메일 인증 필수), 로그인/로그아웃, JWT 토큰 갱신, 비밀번호 재설정
- **프로젝트** — 생성·수정·삭제, 멤버 초대 (OWNER / MEMBER 역할), 멤버 제거
- **태스크** — CRUD, 상태(TODO/IN_PROGRESS/DONE), 우선순위(LOW/MEDIUM/HIGH), 담당자 배정, Soft Delete
- **칸반 보드** — 드래그&드롭 상태 변경
- **댓글** — 태스크별 댓글 추가·삭제 (작성자 본인만 삭제)
- **통계** — 완료율, 상태·우선순위별 집계, 전체 활동 피드
- **사용자 검색** — 이메일 자동완성 (멤버 초대 시)
- **AI 태스크 제안** — Ollama 기반 태스크 제목·설명·우선순위 자동 생성
- **실시간 알림** — WebSocket (활동 이벤트)
- **에러 모니터링** — Sentry 통합

## 프로젝트 구조

```
taskhive/
├── auth/               # Spring Boot 3 REST API (Java 21)
│   └── src/main/java/com/taskhive/
│       ├── controller/ # REST 컨트롤러
│       ├── service/    # 비즈니스 로직
│       ├── repository/ # Spring Data JPA
│       ├── model/      # JPA 엔티티
│       ├── dto/        # 요청·응답 DTO
│       ├── security/   # JWT 필터, 설정
│       └── exception/  # 에러 코드, 핸들러
├── frontend/           # React 18 + TypeScript 5 (Vite)
│   └── src/
│       ├── api/        # Axios 클라이언트 모듈
│       ├── components/ # 공통 UI 컴포넌트
│       └── pages/      # 페이지 컴포넌트
├── docs/               # API · 아키텍처 문서
└── scripts/            # 유틸리티 스크립트
```

## 개발자 테스트 환경

배포된 환경에서 기능을 직접 검증하기 위한 3가지 도구가 준비되어 있습니다.

### Swagger UI

브라우저에서 모든 REST 엔드포인트를 직접 호출할 수 있습니다.

```
{백엔드 URL}/swagger-ui.html
```

**Authorize** 버튼 → `POST /api/auth/login`으로 받은 `accessToken` 입력 → 이후 모든 호출에 JWT 자동 첨부.

### Seed Data (dev 프로파일)

테스트 계정·프로젝트·태스크를 한 번에 생성합니다.

```bash
curl -X POST http://localhost:8080/api/dev/seed
# 응답: {"result": "seeded"} 또는 {"result": "already_seeded"}
```

생성 계정: `test@example.com` / `member@example.com` (비밀번호: `Test1234!`)

> `dev` 프로파일 전용 — 프로덕션 배포에서는 이 엔드포인트가 존재하지 않습니다.

### Playwright E2E

배포 URL 또는 로컬 서버를 타겟으로 브라우저 자동화 테스트를 실행합니다.

```bash
cd frontend
npx playwright install chromium   # 최초 1회
npx playwright test               # localhost:5173 기준

# 배포 URL 타겟
BASE_URL=https://<vercel-url> npx playwright test
```

커버리지: 로그인/로그아웃, 태스크 CRUD, 프로젝트 생성·멤버 초대, AI 태스크 생성.

자세한 사항은 [테스트 환경 가이드](docs/TESTING.md)를 참조하세요.

---

## 로컬 실행

### 환경 변수 설정

**백엔드** (`auth/src/main/resources/application.properties` 또는 환경 변수):

| 변수 | 설명 | 예시 |
|------|------|------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/taskhive` |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자명 | `taskhive` |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 | `secret` |
| `JWT_SECRET` | JWT 서명 키 (Base64, 256비트 이상) | — |
| `MAIL_HOST` | SMTP 호스트 | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP 포트 | `587` |
| `MAIL_USERNAME` | SMTP 계정 | `noreply@example.com` |
| `MAIL_PASSWORD` | SMTP 비밀번호 | — |
| `APP_BASE_URL` | 프론트엔드 베이스 URL (이메일 링크용) | `http://localhost:5173` |
| `SENTRY_DSN` | Sentry DSN (선택) | — |
| `OLLAMA_URL` | Ollama API URL (AI 기능, 선택) | `http://localhost:11434` |

**프론트엔드** (`.env`):

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `VITE_API_URL` | 백엔드 API 베이스 URL | `http://localhost:8080` |

### 개별 실행

```bash
# 백엔드 (포트 8080)
cd auth && mvn spring-boot:run

# 프론트엔드 (포트 5173)
cd frontend && npm install && npm run dev
```

PostgreSQL이 로컬에서 실행 중이어야 합니다. 스키마는 `spring.jpa.hibernate.ddl-auto=update`로 자동 생성됩니다.

## 배포

| 서비스 | 플랫폼 | 설정 |
|--------|--------|------|
| 백엔드 | Railway | `auth/` 디렉토리, 환경 변수 주입 |
| 프론트엔드 | Vercel | `frontend/` 디렉토리, `VITE_API_URL` 설정 |
| DB | Railway PostgreSQL | 자동 프로비저닝 |

## 기존 DB 마이그레이션 노트

프로젝트 멤버 기능 도입 전에 생성된 프로젝트는 멤버 레코드가 없습니다.
기존 데이터를 보유한 환경에서는 다음 SQL을 한 번 실행하세요:

```sql
INSERT INTO project_members (project_id, user_id, role, created_at)
SELECT id, owner_id, 'OWNER', now()
FROM projects
WHERE deleted_at IS NULL
  AND id NOT IN (SELECT project_id FROM project_members);
```

## 문서

- [API 레퍼런스](docs/API.md) — 모든 REST 엔드포인트, 요청/응답 예시, 에러 코드
- [아키텍처 가이드](docs/ARCHITECTURE.md) — 시스템 구성, 인증 흐름, DB 스키마
- [테스트 환경 가이드](docs/TESTING.md) — Swagger UI, Seed Data API, Playwright E2E, GitHub Actions CI

## 개발 로드맵

- [x] Phase 1 — 레포 및 골격 생성
- [x] Phase 2 — Spring Boot REST API + JWT 인증
- [x] Phase 3 — React UI + Axios 클라이언트
- [x] Phase 4 — 이메일 인증 + 비밀번호 재설정
- [x] Phase 5 — 태스크 CRUD (상태·우선순위·담당자)
- [x] Phase 6 — 댓글 API
- [x] Phase 7 — 테스트 인프라 (JUnit5 + Testcontainers)
- [x] Phase 8 — 통계 API + 활동 피드
- [x] Phase 9 — Rate Limiting (bucket4j)
- [x] Phase 10 — Sentry 에러 모니터링
- [x] Phase 11 — AI 태스크 제안 (Ollama)
- [x] Phase 12 — WebSocket 실시간 알림
- [x] Phase 13 — 칸반 보드 (드래그&드롭)
- [x] Phase 14 — 프로젝트 멤버 초대 & 공유 (OWNER/MEMBER 역할)
- [x] Phase 15 — 웹 테스트 환경 (Swagger UI + JWT, Seed Data API, Playwright E2E + GitHub Actions)
