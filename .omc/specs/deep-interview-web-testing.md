# Deep Interview Spec: TaskHive 웹 테스트 환경 구축

## Metadata
- Interview ID: di-taskhive-web-testing-2026
- Rounds: 6
- Final Ambiguity Score: 19%
- Type: brownfield
- Generated: 2026-05-12
- Threshold: 0.20
- Initial Context Summarized: no
- Status: PASSED

## Clarity Breakdown
| 차원 | 점수 | 가중치 | 가중합 |
|------|------|--------|--------|
| Goal Clarity | 0.83 | 35% | 0.291 |
| Constraint Clarity | 0.82 | 25% | 0.205 |
| Success Criteria | 0.75 | 25% | 0.188 |
| Context Clarity | 0.82 | 15% | 0.123 |
| **Total Clarity** | | | **0.807** |
| **Ambiguity** | | | **19%** |

---

## Goal

TaskHive를 **"웹에서 직접 기능을 테스트할 수 있는 완전한 테스트 환경"** 으로 만든다.

3개 축으로 구성:
- **축 1**: Swagger UI + JWT 인증 설정 → API 엔드포인트를 브라우저에서 직접 호출
- **축 2**: 시드 데이터(Seed Data) → 테스트용 사용자·프로젝트·태스크를 즉시 준비
- **축 3**: Playwright E2E → 4개 핵심 플로우를 GitHub Actions CI에서 Railway URL 대상으로 자동 실행

---

## Constraints

- **E2E 타겟**: Railway 배포 URL (이미 라이브) + Vercel 프론트엔드 URL — CI에서 백엔드를 별도 기동하지 않음
- **CI**: 기존 `.github/workflows/` 확장 (현재 `lighthouse.yml`만 존재)
- **기술 스택**: Spring Boot 3 + React 18 + TypeScript + Ant Design 유지
- **AI 백엔드**: Ollama 로컬 LLM (AI 플로우 E2E는 Ollama 미실행 환경 처리 필요)
- **1인 개발**: 복잡한 인프라 설정 최소화
- **springdoc-openapi 2.5.0** 이미 `pom.xml`에 존재 — 신규 의존성 불필요

---

## Non-Goals

- CI 안에서 Spring Boot를 직접 기동하는 풀스택 테스트 (Railway URL 타겟으로 대체)
- 성능 테스트 / 부하 테스트 (Lighthouse가 이미 커버)
- 프론트엔드 단위 테스트 (vitest 추가) — 별도 이슈
- 멀티 브라우저 테스트 (Chromium만으로 충분)

---

## Acceptance Criteria

### 축 1: Swagger UI + JWT

- [ ] `/swagger-ui.html` (또는 `/swagger-ui/index.html`)에서 전체 API 목록이 렌더링된다
- [ ] "Authorize" 버튼을 클릭해 `Bearer {token}` 형식으로 JWT를 입력하면 인증이 필요한 엔드포인트(`/api/tasks`, `/api/projects` 등)를 브라우저에서 직접 호출할 수 있다
- [ ] `SecurityConfig`에서 `/swagger-ui/**`, `/v3/api-docs/**` 경로가 인증 없이 접근 가능하다
- [ ] API 목록에 각 엔드포인트의 설명(description), 요청/응답 예시가 포함된다

### 축 2: 시드 데이터

- [ ] `POST /api/dev/seed` 엔드포인트(또는 `DataSeeder` 빈)가 존재하며 다음 데이터를 생성한다:
  - 테스트 사용자 2명: `test@example.com` / `pw: Test1234!` (OWNER), `member@example.com` / `pw: Test1234!` (MEMBER)
  - 프로젝트 1개 (이름: "Demo Project") — OWNER가 소유, MEMBER 초대됨
  - 태스크 5개 (상태 다양: TODO 2, IN_PROGRESS 2, DONE 1, 우선순위 혼합)
- [ ] 시드 실행 시 이미 존재하는 데이터는 중복 생성하지 않는다 (idempotent)
- [ ] `spring.profiles.active=dev` 환경에서만 시드 엔드포인트가 활성화된다 (production 보호)
- [ ] README 또는 `docs/SEED.md`에 시드 실행 방법이 명시된다

### 축 3: Playwright E2E

- [ ] `frontend/e2e/` 디렉토리에 Playwright 설정(`playwright.config.ts`)이 존재한다
- [ ] `BASE_URL` 환경변수로 테스트 대상 URL을 주입할 수 있다 (기본값: `http://localhost:5173`)
- [ ] 다음 4개 플로우가 각각 독립적인 테스트 파일로 커버된다:
  1. **auth.spec.ts** — 로그인(성공/실패) + 로그아웃
  2. **task.spec.ts** — 태스크 생성 → 상태 변경 → 수정 → 삭제 CRUD
  3. **project.spec.ts** — 프로젝트 생성 + 멤버 초대 플로우
  4. **ai-task.spec.ts** — 자연어 입력 → AI 제안 수신 → 태스크 저장 (Ollama 미실행 시 fallback 경로 테스트)
- [ ] `npx playwright test` 로컬 실행 시 전체 통과
- [ ] `.github/workflows/e2e.yml`이 추가되며 `push` 및 `pull_request` 이벤트에서 Railway URL(`E2E_BASE_URL` secret)을 대상으로 실행된다
- [ ] TypeScript 빌드 (`npx tsc --noEmit`) 오류 없음

---

## Assumptions Exposed & Resolved

| 가정 | 도전 | 결정 |
|------|------|------|
| E2E = 복잡한 CI 인프라 필요 | Railway URL로 대체 가능한가? | Railway 배포 URL 타겟으로 단순화 |
| 3가지 모두 일상적으로 필요 | E2E가 실제로 매일 쓸 것인가? | CI 필수 (매일 실행) 확인 |
| 풀스택 테스트 환경 | 백엔드를 CI에서 기동해야 하는가? | 기존 Railway 인스턴스 재사용 |
| 시드 데이터 없이 UI 테스트 가능 | DB 직접 조작이 현재 병목 | 시드 API 추가로 해결 |

---

## Technical Context

**기존 활용 가능 인프라:**
```
.github/workflows/
└── lighthouse.yml          # PR 시 Lighthouse 성능 체크 (확장 가능)

auth/pom.xml
└── springdoc-openapi 2.5.0 # Swagger UI 이미 의존성 존재

auth/src/main/java/com/taskhive/
└── config/SecurityConfig.java  # /swagger-ui/** permit 설정 추가 위치
```

**신규 생성 파일:**
```
auth/src/main/java/com/taskhive/
├── config/OpenApiConfig.java       # JWT SecurityScheme 정의
├── controller/DevController.java   # POST /api/dev/seed (dev 프로파일)
└── service/DataSeederService.java  # 시드 데이터 생성 로직

frontend/
├── e2e/
│   ├── playwright.config.ts
│   ├── auth.spec.ts
│   ├── task.spec.ts
│   ├── project.spec.ts
│   └── ai-task.spec.ts

.github/workflows/
└── e2e.yml                         # Playwright CI 워크플로우
```

---

## Ontology (Key Entities)

| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| SwaggerUI | feature | jwt_scheme, api_docs_path, authorize_button | extends SecurityConfig |
| SeedData | feature | test_users, project, tasks, idempotent, dev_profile | uses DataSeederService |
| PlaywrightE2E | feature | base_url, specs, browser=chromium | runs against RailwayURL |
| RailwayURL | external system | backend_url, secret=E2E_BASE_URL | target of E2E |
| GitHubActions | infrastructure | e2e.yml, triggers=push+PR | executes PlaywrightE2E |
| TaskHive | core domain | Spring Boot 3, React 18, Railway+Vercel | contains all features |

## Ontology Convergence

| 라운드 | 엔티티 수 | 신규 | 변경 | 안정 | Stability |
|--------|----------|------|------|------|-----------|
| 1 | 3 | 3 | - | - | N/A |
| 2 | 3 | 0 | 0 | 3 | 100% |
| 3 | 4 | 1 | 0 | 3 | 75% |
| 4 | 4 | 0 | 1 | 3 | **100%** |
| 5 | 6 | 2 | 1 | 3 | 67% |
| 6 | 6 | 0 | 1 | 5 | **100%** |

---

## Interview Transcript

<details>
<summary>Full Q&A (6 rounds)</summary>

### Round 1
**Q:** '웹에서 직접 기능 테스트'가 구체적으로 어떤 행동을 의미하나요?
**A:** 모두 포함 (API 호출 + UI 수동 + E2E 자동화)
**Ambiguity:** 77%

### Round 2
**Q:** 테스트 환경의 주요 대상자가 누구인가요?
**A:** 나 혼자 (1인 개발)
**Ambiguity:** 64%

### Round 3
**Q:** 지금 기능 하나를 테스트하려면 구체적으로 어떤 과정을 거치나요?
**A:** UI 클릭 + DB 직접 만지기 (옵션 3+4)
**Ambiguity:** 48%

### Round 4 [CONTRARIAN]
**Q:** E2E 자동화 테스트가 실제로 구체적으로 언제 필요한가요?
**A:** 매일 실행 (CI에 필수)
**Ambiguity:** 37.5%

### Round 5
**Q:** E2E 테스트가 반드시 커버해야 하는 핵심 플로우는?
**A:** 로그인/로그아웃 + 태스크 CRUD + 프로젝트 생성 및 멤버 초대 + AI 태스크 생성
**Ambiguity:** 26%

### Round 6 [SIMPLIFIER]
**Q:** Playwright E2E는 어떤 대상을 테스트해야 하나요?
**A:** Railway 배포 URL (already live)
**Ambiguity:** 19% ✅

</details>
