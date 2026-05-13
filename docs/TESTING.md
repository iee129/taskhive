# TaskHive 테스트 환경 가이드

개발자가 배포된 환경에서 기능을 직접 검증하기 위한 3가지 도구를 설명합니다.

---

## 1. Swagger UI (수동 API 테스트)

배포된 백엔드에서 Swagger UI를 통해 모든 REST 엔드포인트를 브라우저에서 직접 호출할 수 있습니다.

### 접속 방법

```
https://<backend>/swagger-ui.html
```

로컬 실행 시: `http://localhost:8080/swagger-ui.html`

### JWT 인증 설정

1. `POST /api/auth/login` 호출 → `accessToken` 복사
2. 우측 상단 **Authorize** 버튼 클릭
3. `bearerAuth` 필드에 복사한 토큰 입력 → **Authorize**
4. 이후 모든 엔드포인트 호출에 `Authorization: Bearer <token>` 헤더가 자동 첨부됨

### 인증 없이 접근 가능한 엔드포인트

| 엔드포인트 | 설명 |
|-----------|------|
| `POST /api/auth/register` | 회원가입 |
| `POST /api/auth/login` | 로그인 (토큰 발급) |
| `GET /api/auth/verify-email` | 이메일 인증 |
| `POST /api/auth/refresh` | 토큰 갱신 |
| `POST /api/auth/forgot-password` | 비밀번호 재설정 요청 |

---

## 2. Seed Data API (테스트 데이터 초기화)

`dev` 프로파일에서만 활성화되는 엔드포인트입니다. 호출 한 번으로 테스트에 필요한 계정·프로젝트·태스크를 생성합니다.

### 엔드포인트

```
POST /api/dev/seed
```

> **주의:** `spring.profiles.active=dev` 환경에서만 동작합니다. 프로덕션 배포에서는 이 엔드포인트가 존재하지 않습니다.

### 생성 데이터

| 리소스 | 내용 |
|--------|------|
| 계정 1 | `test@example.com` / `Test1234!` (프로젝트 OWNER) |
| 계정 2 | `member@example.com` / `Test1234!` (프로젝트 MEMBER) |
| 프로젝트 | `Demo Project` |
| 태스크 | 5개 (TODO×2, IN_PROGRESS×2, DONE×1) |

### 응답

| 응답 본문 | 설명 |
|-----------|------|
| `{"result": "seeded"}` | 데이터 생성 완료 |
| `{"result": "already_seeded"}` | 이미 시드됨 (idempotent — 중복 호출 안전) |

### 실행 예시

```bash
# 로컬
curl -X POST http://localhost:8080/api/dev/seed

# 개발 서버 (E2E_BACKEND_URL을 dev 프로파일 배포 URL로 설정)
curl -X POST $E2E_BACKEND_URL/api/dev/seed
```

---

## 3. Playwright E2E 테스트

브라우저 자동화로 실제 UI 흐름을 검증합니다. 배포된 Vercel URL 또는 로컬 개발 서버를 타겟으로 실행합니다.

### 사전 조건

- Node.js 20+
- 타겟 서버가 실행 중이며 `POST /api/dev/seed`로 테스트 데이터가 초기화된 상태

### 설치

```bash
cd frontend
npm install
npx playwright install chromium
```

### 로컬 실행

```bash
# 기본 (http://localhost:5173 타겟)
cd frontend && npx playwright test

# 배포 URL 타겟
BASE_URL=https://<vercel-url> npx playwright test

# UI 모드 (브라우저에서 테스트 진행 과정 시각화)
npx playwright test --ui

# 특정 파일만 실행
npx playwright test e2e/auth.spec.ts
```

### 테스트 커버리지

| 파일 | 커버 범위 |
|------|----------|
| `e2e/auth.spec.ts` | 로그인 성공/실패, 로그아웃 |
| `e2e/task.spec.ts` | 태스크 생성, 상태 변경, 삭제 (CRUD) |
| `e2e/project.spec.ts` | 프로젝트 생성, 멤버 초대 |
| `e2e/ai-task.spec.ts` | AI 자연어 입력 → 제안 수신, Ollama 오류 fallback (mock) |

### 테스트 리포트 확인

테스트 실행 후 HTML 리포트가 `frontend/playwright-report/`에 생성됩니다.

```bash
npx playwright show-report
```

---

## 4. GitHub Actions CI

두 개의 워크플로우가 자동 실행됩니다.

### 4-1. 메인 CI (`ci.yml`)

백엔드 빌드·테스트·보안 감사를 수행합니다. `push` 및 `pull_request` 이벤트에서 트리거됩니다.

```
1. Checkout
2. Set up Java 21
3. mvn verify -q          — 단위·통합 테스트 포함 전체 빌드
4. Set up Node 20
5. npm ci                 — 프론트엔드 의존성 설치
6. npx tsc --noEmit       — TypeScript 타입 검사
7. npm audit              — 고위험 취약점 감사 (continue-on-error: true)
```

> npm audit는 경고로만 동작하며 CI를 블로킹하지 않습니다 (`--audit-level=high`, `continue-on-error: true`).

### 4-2. E2E CI (`e2e.yml`)

`push` 또는 `pull_request` 이벤트에서 E2E 테스트가 자동 실행됩니다.

### 트리거 조건

| 이벤트 | 브랜치 | 경로 조건 |
|--------|--------|----------|
| `push` | `master` | `frontend/**` 또는 `auth/**` 변경 시 |
| `pull_request` | `master` | 항상 |

### 필요한 Repository Secrets

| Secret | 설명 |
|--------|------|
| `E2E_BASE_URL` | Playwright 타겟 URL (Vercel 프론트엔드 URL) |
| `E2E_BACKEND_URL` | Seed 호출 대상 URL (dev 프로파일 백엔드 URL) |

GitHub 저장소 → **Settings → Secrets and variables → Actions** 에서 설정합니다.

### CI 실행 흐름

```
1. Validate target environment  — E2E_BACKEND_URL 설정 여부 확인
2. Seed test data               — POST $E2E_BACKEND_URL/api/dev/seed (미설정 시 스킵)
3. Setup Node.js 20             — npm cache 활성화
4. Install dependencies         — npm ci
5. Install Playwright Chromium  — --with-deps 포함
6. Run E2E tests                — BASE_URL=$E2E_BASE_URL npx playwright test
7. Upload test report           — playwright-report/ 아티팩트 (7일 보존)
```

### 리포트 아티팩트 다운로드

Actions 탭 → 해당 워크플로우 실행 → **Artifacts → playwright-report** 다운로드 후 로컬에서 `npx playwright show-report` 실행.

---

## 전체 흐름 요약

```
[배포된 백엔드 (dev profile)]
        │
        ▼
POST /api/dev/seed          ← 테스트 데이터 초기화
        │
        ▼
Swagger UI 수동 검증        ← /swagger-ui.html → Authorize → API 호출
        │
        ▼
Playwright E2E 자동화       ← npx playwright test (로컬 또는 CI)
        │
        ▼
GitHub Actions 리포트       ← playwright-report/ 아티팩트
```
