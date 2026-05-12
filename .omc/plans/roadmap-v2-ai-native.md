# TaskHive Roadmap v2 — "Local-First AI Project Platform"

> 작성: 2026-05-12 · 개정: 2026-05-12 (consensus 리뷰 v2 — Architect + Critic 반영) · 대상: `/Users/iee12/taskhive` · 척추 방향: **AI 네이티브(메인) + Dev 친화 + 분석** · 호스팅: **완전 무료 스택** · 야심: **오픈엔드 (4대 품질 기준 충족까지)**
>
> 이 문서는 기존 README의 Phase 1–15(완료) 다음 챕터다. 새 작업은 **M0–M10 마일스톤**으로 표기한다. 각 마일스톤은 독립 출시 가능하며, M1(배포)·M2(AI 코어) 완료 시점에 이미 포트폴리오 가치가 성립한다 — 이후는 메뉴이지 강제 행군이 아니다.
>
> **권장 종료 지점 (ship-and-stop):** `M0 → M1 → M2 → M2.5 → M10-lite`. 여기까지가 "라이브 AI 태스크 매니저 + 무료 셀프호스팅 + 하드닝 플로어 + OSS 릴리스" — P2(OSS 배포)·P3(차별성)·P4(실사용)는 합격선, P1(포트폴리오)은 (b)의 6역량 체크리스트 중 LLM·실시간·테스트(+CI/CD·관측성·보안 보너스)로 3–4/6 + 보너스 충족. 나머지 P1 역량(OAuth·웹훅·CLI)은 M3/M4/M7이 채운다 — 즉 *충분히 강한 포트폴리오*이되 *최강*은 메뉴를 더 소화해야 함. M3–M8·M9 풀버전은 에너지가 남을 때만. (§1.5 실행 규칙 참조)

---

## RALPLAN-DR 요약

### 원칙 (Principles)
1. **Ship-first, then differentiate** — 동작하는 공개 배포가 기능 목록보다 먼저. M1(배포)이 M2(AI 코어)에 선행한다. 라이브 데모 > 기능 나열.
2. **모든 마일스톤 독립 출시 가능 · 빅뱅 금지** — 어떤 마일스톤도 프로젝트를 깨진 상태로 남기지 않는다. M0→M1→M2 만으로 포트폴리오 가치 성립; M3–M8은 메뉴이지 강제 순서가 아니다. 마일스톤 내부에도 빅뱅 통합 단계를 두지 않는다 (M2의 유일한 하드 필수 = 2a; 나머지 2b–2g는 개별 독립).
3. **$0 하드 제약, 포장보다 정직** — 무료 티어 서비스만. 트레이드오프(콜드스타트·레이트리밋·750hr 캡)는 숨기지 말고 문서화한다. "30초 기다리세요" 무료 데모 > "준비 중" 가짜.
4. **Provider 추상화 over Provider 락인** — AI 기능은 `AiProvider` 인터페이스(Ollama/Groq/Gemini/Noop)를 거친다. 로컬-퍼스트도, 클라우드 데모도, provider down도 안 깨진다. **구조화 출력 파싱·재시도·서킷브레이커는 추상화 안에**. CI에서 실 LLM 절대 호출 안 함.
5. **도그푸딩이 증거** — M2부터 TaskHive 백로그는 TaskHive에 산다. "실사용 가능"은 주장이 아니라 사용으로 충족한다. **도그푸딩 인스턴스는 Ollama(셀프호스팅)로 돌린다** — 그래야 "데이터가 내 서버를 안 떠난다"가 그 인스턴스에서 참이다. 도그푸딩 프로젝트는 공개 조회 가능한 TaskHive 프로젝트 URL로 둔다.

### 결정 동인 (Decision Drivers, top 3)
1. **노력 단위당 포트폴리오 임팩트** — 가장 뚜렷하고 수요 높은 역량(LLM 통합·OAuth·웹훅·실시간·CLI·데이터 시각화·테스트·관측성)을 드러내는 작업 우선.
2. **솔로 개발 지속가능성** — 독립 출시 가능 마일스톤, 메뉴식 순서, 빅뱅 통합 없음, "한 번에 기능 마일스톤 하나" 규칙(§1.5). 중단·에너지 저하를 견디는 계획이어야 함.
3. **"무료" 제약** — 모든 인프라 결정은 "진짜 무료 티어가 있는가"로 게이팅. Railway-as-is 배제, Render/Neon/Vercel/Groq/GHCR/R2 스택을 강제, 이메일 옵셔널화, 750hr 캡 대응 명시.

### 검토한 옵션 (차별화 축 — 척추 결정)
- **옵션 A: AI-Native Local-First (채택)** — 장점: 가장 차별화됨(OSS 태스크 매니저에서 BYO-LLM/프라이버시 각도는 드묾), 가장 핫한 포트폴리오 스킬, $0 친화(Ollama 로컬 + Groq 무료 데모), 모멘텀 존재(AI 코파일럿 spec + Phase 1 완료). 단점: AI 기능 fiddly, LLM E2E 플래키(mock으로 완화), 무료 LLM 레이트리밋, **"로컬-퍼스트" 포지셔닝 ↔ 클라우드 데모 모순**(→ M2a/M2h에서 UI·README 라벨링으로 *교육 포인트*로 전환).
- **옵션 B: 실시간 멀티플레이어 워크스페이스** — 장점: 시각적 임팩트, 분산 시스템 쇼케이스. 단점: Figma식 멀티플레이어 솔로 = 거대한 작업, 무료 dyno 슬립이 WS 끊김, 솔로 데모 어색. **기각**: 솔로+무료엔 노력/리스크 과다, 데모 스토리 약함. (강한 부분 = 기존 WebSocket 알림·활동피드는 이미 있음 — 추가 멀티플레이어는 비용 대비 효용 낮음.)
- **옵션 C: Dev-First 깃-연동 트래커** — 장점: 균형잡힌 풀스택 쇼케이스, GitHub API 무료. 단점: Plane.so/Huly가 이미 이 OSS 니치 강함, 차별화 더 어려움. **척추로는 기각** 하되 포트폴리오 강점 부분(GitHub 통합·공개 API·CLI·Cmd-K)을 채택안의 M3–M5/M7에 흡수 — 단, 그만큼 스코프가 커지므로 §1.5 실행 규칙으로 통제.
- **채택: A(척추) + C의 포트폴리오 강점 + 분석 대시보드(D)** — 최대주의 조합, 사용자의 "오픈엔드, 품질 기준이 요구하는 만큼" 지시에 근거. 통제 장치: §1.5(한 번에 하나, 버려지면 revert), 권장 종료 지점(M0→M1→M2→M2.5→M10-lite).

---

## 0. 현황 사실 (코드 기준)

- **브랜치**: `phase13` — 미커밋 파일 ~50개 (`?? auth/.../ProjectMemberController.java`, `?? .github/`, `?? docs/`, `?? .omc/`, `M README.md` 등). git 히스토리는 `cca8c61 Vercel·Railway 배포 설정 추가`에서 멈춤. **즉, 프로젝트 멤버·AI 코파일럿·웹 테스트·배포 설정·문서가 전부 커밋 안 됨.**
- **스택**: Spring Boot 3 + Java 21 (`auth/pom.xml`), Postgres, JWT(jjwt) + Refresh Rotation, WebSocket/STOMP (`websocket/`), bucket4j Rate Limit (`filter/RateLimitFilter.java`), Sentry (`sentry-spring-boot-starter-jakarta`), Spring Mail + Thymeleaf (`templates/`), springdoc-openapi, AOP 감사로그 (`aspect/TaskActivityAspect.java` → `model/TaskActivity.java`), Ollama AI (`service/AiService.java`). 프론트: React 18 + TS 5 + Vite + Ant Design + `@hello-pangea/dnd` + `@stomp/stompjs` + `@sentry/react`.
- **배포 설정 (오늘 생성, 미커밋)**: `auth/railway.toml`, `auth/Dockerfile`, `frontend/Dockerfile` + `frontend/nginx.conf`(**존재 확인됨**), `frontend/vercel.json`. **실제 배포 URL은 없음** — README의 "Railway 배포 URL"은 플레이스홀더.
- **알려진 결함**:
  - `application-prod.yml` 이 `spring.data.redis` 를 참조하나 `pom.xml` 에 Redis 의존성 없음 → 미완성/오작동.
  - `auth/Dockerfile` 의 `ENTRYPOINT ["java","-jar","app.jar"]` 가 `$PORT` 를 처리하지 않음 → Render 등 PaaS 호환 안 됨.
  - `application.yml` 에 `spring:` 키가 2번 등장 (YAML last-wins으로 동작은 하나 정리 필요).
  - **DB 마이그레이션 도구 없음** — 현재 사실상 `ddl-auto=update`. 새 테이블(`task_status_history`, `project_webhooks`, `cycles`, `labels`, PAT 등)을 배포된 Postgres에 안전히 반영하려면 Flyway/Liquibase 필요 → M0/M1에서 도입 (P2 "clean checkout `docker compose up`"의 전제).
  - `DevController` 는 현재 `@Profile("dev")` 이고 `/api/dev/seed` 에 **인증 게이트 없음** → 배포 시 위험 (M1.4에서 처리).
  - `TaskActivity` 는 `action`(문자열) + `detail`(자유 텍스트) 만 보유 → 상태 전이(`from→to`)·사이클타임·CFD 산출 불가 → 별도 `task_status_history` 필요 (M1.5로 앞당김).
- **무료 제약 충돌**: Railway 무료 티어 폐지(2023). 현 스택으론 무료 공개 배포 불가 → M1에서 무료 스택으로 이전.

---

## 1. 4대 품질 기준 — 측정 가능한 정의

| # | 기준 | 합격 조건 (측정 가능) |
|---|------|------------------------|
| **P1** | **포트폴리오** | (a) 라이브 데모 URL + 60–90초 데모 GIF/영상이 README에 있음. (b) **역량 체크리스트** — 레포에 다음 6개가 각각 ≥1개 명명된 파일/디렉토리로 시연됨: ① LLM 통합/프롬프트 엔지니어링 (`service/ai/`) ② OAuth (`integrations/github/`) ③ 웹훅 송수신 (`webhook/`) ④ 실시간 (`websocket/`) ⑤ CLI (`cli/`) ⑥ 테스트 피라미드 (`*/src/test/`(단위·통합 Testcontainers) + `frontend/e2e/`). + CI/CD(`.github/workflows/`)·관측성(Sentry 와이어링)·보안(JWT rotation·rate limit·security headers)도 보너스. (c) 아키텍처 다이어그램 + "왜 만들었나·핵심 결정" 글이 `docs/` 에 있음. (d) master 선형 히스토리(merge 노드 없음 — M0에서 rebase/cherry-pick). |
| **P2** | **오픈소스 배포** | (a) `git clone && docker compose up` 으로 클린 체크아웃에서 10분 내 풀스택 기동(마이그레이션 포함 — `ddl-auto` 의존 안 함). (b) OSS 위생 체크리스트 100%: `LICENSE`(MIT) · `CONTRIBUTING.md` · `CODE_OF_CONDUCT.md` · `SECURITY.md` · 이슈/PR 템플릿 · `CHANGELOG.md`(Keep a Changelog) · GitHub Releases(시맨틱 버전) · GHCR 퍼블릭 Docker 이미지 · CI green 배지. (c) README에 셀프호스팅 가이드 + 설정 레퍼런스. |
| **P3** | **창의성·차별성** | (a) 경쟁자 3개 이상(Plane.so, Vikunja, Focalboard, Linear) 대비 기능 비교표가 README에 있고, TaskHive 고유 기능 3개 이상(AI 스탠드업 생성·브레인덤프→태스크 분해·AI 우선순위+근거·AI 블로커 탐지 등)이 표시됨. (b) "로컬-퍼스트 AI / BYO-LLM(Ollama 기본값) — 프로젝트 데이터가 내 서버를 안 떠난다"는 포지셔닝이 README + `docs/AI.md` 에 명문화. (c) **공개 데모는 클라우드 LLM(Groq) 사용을 UI 배너 + README에서 명시** — 데모 시드 데이터는 합성이라 실 데이터 유출 없음을 함께 명시. 셀프호스트 = 데이터 로컬. |
| **P4** | **실사용 가능** | (a) 개발자(=사용자) 본인이 **Ollama 셀프호스팅 인스턴스**로 TaskHive 개발 백로그를 추적(도그푸딩) — 최소 2주 연속, 그 기간 P0 버그 0건, 도그푸딩 프로젝트는 공개 조회 가능한 TaskHive 프로젝트 URL로 제공(누구나 검증 가능). (b) 신규 사용자가 문서 없이 3분 내 가입→프로젝트 생성→태스크 추가→멤버 초대 완료. (c) p95 API 지연 <300ms(웜, `k6`/`hey` 100 요청 `/api/tasks` 기준), 페이지 로드 <2s, 프로젝트당 100+ 태스크 부드럽게. (d) AI/이메일 미가용 시 명확한 graceful degradation (프론트가 `GET /api/ai/capabilities` 로 AI 비활성 인지 → 버튼 숨김). (e) Lighthouse perf/a11y/best-practices ≥90 (`lighthouse-ci`, 라이브 Vercel URL의 메인 3페이지: 로그인·태스크·칸반), 모바일 반응형. |

---

## 1.5 실행 규칙 (스코프 통제 — 솔로 지속가능성)

이 규칙들은 "11개 마일스톤·오픈엔드" 가 *반쯤 만든 sprawl* 로 끝나 P1을 *해치는* 실패 모드를 막는다.

1. **권장 종료 지점**: `M0 → M1 → M2 → M2.5 → M10-lite` 완료 = "출시하고 멈춰도 됨" 지점. 4대 기준 합격선을 이미 넘는다. 그 이후(M3–M8, M9 풀버전)는 *에너지가 남을 때만*.
2. **한 번에 기능 마일스톤 하나**: M2.5 이후, M3–M8 중 *동시에 진행 중*인 것은 최대 1개. 다음 것을 시작하려면 현재 것이 "done" 이어야 한다.
3. **"done" 의 정의**: 마일스톤은 (a) 자체 수용 기준 체크리스트 통과 + (b) 자체 통합/E2E 테스트 통과 + (c) 커밋·머지 — 이 셋이 모두 충족돼야 done. 부분 done 없음.
4. **버려지면 revert, 매달려 두지 않음**: 시작했다가 중단하기로 한 마일스톤은 브랜치를 머지하지 않고 **revert/폐기**한다. master에 반쯤 만든 기능을 남기지 않는다. (P1: 리뷰어가 보는 건 master.)
5. **하드닝은 천장이 아니라 바닥**: M2.5가 최소 하드닝 플로어. 이후 *각* 기능 마일스톤(M3–M8)은 *자체* 통합 테스트를 수용 기준에 포함한다 — "나중에 M9에서 한꺼번에 테스트"는 금지. M9는 "최종 폴리시"이지 "처음 테스트하는 곳"이 아니다.

---

## 2. 마일스톤

각 마일스톤은 끝에 **(a) 빌드 green** (`cd auth && mvn package` / `cd frontend && npx tsc --noEmit && npm run lint`) **+ (b) 해당 수용 기준 체크 + (c) 자체 통합/E2E 테스트 통과 + (d) 커밋·머지(rebase, merge 노드 없이)** 를 거친다.

### M0 — 정리 & 위생 (블로커, 최우선) · 기여: P1, P2

`phase13` 의 미커밋 더미를 정리·커밋·머지하지 않으면 배포는 stale 코드 배포이고 히스토리는 엉망이다.

| 작업 | 상세 | 수용 기준 |
|------|------|-----------|
| 0.1 안전망 | `git tag pre-consolidation` 으로 현 상태 태깅. **master에 검증 푸시될 때까지 이 태그·`phase13` 브랜치 삭제 금지** | 태그 존재 |
| 0.2 빌드/테스트 확인 | `cd auth && mvn package` , `cd frontend && npx tsc --noEmit && npm run lint` , `mvn test` 전부 실행. **주의: 미커밋 `EmailServiceTest`·`PasswordResetServiceTest` 가 mail 설정을 요구할 수 있음 — 테스트용 mail mock/프로퍼티 정비 예산 잡기** | 모두 exit 0; 실패 시 수정 후 진행 |
| 0.3 결함 수정 | (i) `application-prod.yml` 의 `spring.data.redis` 제거(인메모리 bucket4j로 충분 — 단일 인스턴스 무료 티어). (ii) `application.yml` 의 중복 `spring:` 키 병합. (iii) `auth/Dockerfile` — ENTRYPOINT는 **exec-form 유지**하고 `$PORT`는 `SERVER_PORT` 환경변수로 Spring이 네이티브하게 읽게 함(Render가 `PORT` 주입 → `SERVER_PORT=$PORT` 매핑은 `render.yaml` 에서). shell wrapper 쓰지 않음 → 시그널 전파/graceful shutdown 보존. (iv) `frontend/nginx.conf` SPA fallback 확인 + API origin 하드코딩 없는지 확인(프론트=Vercel, 백=Render = 다른 origin → CORS이지 nginx 프록시 아님; nginx 프록시 경로는 `docker compose` 로컬 케이스에만 의미) | 위 4건 처리; `mvn package` 재실행 green |
| 0.4 마이그레이션 도구 도입 | **Flyway** 추가(`pom.xml` + `application.yml` `spring.flyway.enabled=true`). 현재 스키마를 베이스라인 `V1__baseline.sql` 로 캡처(`ddl-auto` 로 한 번 생성 후 덤프 → V1으로 고정). **주의: Hibernate 자동 생성 DDL은 네이밍·타입 관용구가 비표준일 수 있음 — V1 베이스라인을 사람이 한 번 손검토(컬럼명·제약·인덱스 명시)**. `ddl-auto` 를 `validate` 로 전환 | `mvn package` green; 빈 DB에 부팅 시 V1이 전체 스키마 생성; `ddl-auto=validate` 통과; V1 손검토 완료 |
| 0.5 논리적 커밋 분할 | 미커밋 파일을 기능 영역별로 그룹핑해 4–6개 커밋: ① project-members ② ai-copilot Phase 1 ③ web-testing(Swagger·DevController·Playwright·e2e.yml) ④ deploy-config(Dockerfile·nginx.conf·vercel.json·railway.toml) ⑤ docs(API/ARCHITECTURE/TESTING.md, README) ⑥ migration(Flyway·baseline)+misc. 커밋 메시지 = 한글 명사형 어미, 영문 prefix 없음 (메모리 규칙) | `git log` 에 의미 단위 커밋들; `git status` clean |
| 0.6 머지 | `phase13` → `master` 를 **rebase 또는 cherry-pick** 으로(merge 노드 없이 — P1.d "선형 히스토리"). master 빌드 green 확인 후 푸시. 푸시 검증 완료 후 `phase13` 삭제(`pre-consolidation` 태그는 유지). 이후 브랜치 규칙: 기능=소문자 한 단어(예: `deploy`, `aicore`, `webhook`), 문서=`docs` 브랜치 (메모리 규칙) | `master` 가 모든 현 작업 포함, 선형 히스토리, 빌드 green, 원격 푸시됨 |

---

### M1 — 무료 스택 배포 ("웹에서 직접 열어 테스트" — 사용자 1순위) · 기여: P1, P2, P4

목표: TaskHive가 100% 무료 스택에 라이브, 공개 URL로 접속, README에 실제 URL.

**스택 결정** (모두 무료 티어):
- **Postgres**: **Neon** (0.5GB, autosuspend~1초 웨이크, 비파괴적). 대안 Supabase(500MB, 1주 미사용 시 수동 unpause — 그래서 Neon 우선). **주의: 0.5GB 한도 → 시드 데이터는 소량 캡(태스크 ~20개, 첨부 없음)**.
- **백엔드**: **Render** Web Service (Docker, 512MB RAM, 15분 유휴 후 슬립, **750hr/mo는 계정당 — 서비스 1개만 운영**). JVM 콜드스타트 ~30–60s → 완화: (i) `cron-job.org`(무료)가 `/actuator/health` 를 **24/7 10분마다** 핑, (ii) Spring `lazy-initialization: true` + JVM 플래그 `-XX:+UseSerialGC -Xss512k -XX:MaxRAMPercentage=75`, (iii) **프론트엔드에 "백엔드 깨우는 중… ~30초" 스플래시 + 자동 재시도** — 콜드 히트가 "고장난 것처럼" 안 보이게, (iv) README에 명시. 대안 Koyeb 무료(유사). **트레이드오프 인지: 24/7 핑 + 활성 데모는 월 750hr를 넘길 수 있음 → 월말 일부 슬립 수용 또는 Koyeb 폴백 (§4 리스크).**
- **프론트**: **Vercel** 무료 (`vercel.json` 존재; 정적 SPA → SSR 이슈 없음).
- **이메일**: SMTP 옵셔널화 (`MAIL_ENABLED` 플래그). 데모는 **Resend**(무료 100/일, 3000/월) 또는 **Brevo**(300/일); 미설정 시 인증 링크를 콘솔 로그로 출력 + 문서화.
- **AI 클라우드 폴백**: 데모 배포에선 **Groq**(무료, Llama 3.x 고속)를 LLM 제공자로; **셀프호스트 기본값(및 도그푸딩 인스턴스)은 Ollama 유지**. `AI_PROVIDER` 설정 (`ollama` | `groq` | `gemini` | `none`).
- **컨테이너 레지스트리**: GHCR (퍼블릭 레포 무료) — 백/프론트 이미지 publish.
- **도메인**: `*.onrender.com` + `*.vercel.app` 서브도메인으로 충분.

| 작업 | 상세 | 수용 기준 |
|------|------|-----------|
| 1.1 docker-compose | 레포 루트 `docker-compose.yml`: postgres + backend(Flyway 마이그레이션 자동 적용) + frontend, 원커맨드 로컬 풀스택 | `docker compose up` → 3 컨테이너 healthy, `localhost` 에서 로그인 동작, Flyway가 스키마 생성 |
| 1.2 Dockerfile 마무리 | `auth/Dockerfile` 멀티스테이지 정리(빌드 캐시 레이어, non-root, exec-form ENTRYPOINT). `frontend/Dockerfile` + `nginx.conf`(SPA fallback, gzip) | 두 이미지 로컬 빌드 성공, 컨테이너 기동, 시그널 처리 정상 |
| 1.3 Render 블루프린트 | `render.yaml` 추가 (web service from Dockerfile, env vars 포함 `SERVER_PORT=$PORT`, health check `/actuator/health`). `railway.toml` 은 보조로 유지하되 README는 Render를 primary로 | Render에 배포 성공, `curl https://<url>/actuator/health` → 200 |
| 1.4 prod/demo 프로파일 + 시드 락다운 | `prod` 프로파일: Neon DSN(`?sslmode=require`), Vercel 도메인 CORS(`SecurityConfig` 가 `CORS_ORIGINS` env 읽음), refresh 토큰 쿠키 `SameSite=None; Secure`(크로스사이트 필수), Sentry DSN. **`demo` 프로파일** = `spring.profiles.include: prod` + 시드 동작만 추가. **시드 락다운(택 1):** (A) `DevController`/`DataSeederService` 를 `@Profile("dev","demo")` 로, `/api/dev/seed` 는 idempotent + bucket4j rate-limit + `X-Seed-Token` 비밀 헤더 게이트, 또는 (B) HTTP 엔드포인트 없이 `@Profile("demo")` `CommandLineRunner` 가 배포 시 1회 시드(권장). | `demo` 프로파일 배포 시 시드 동작; 시드 엔드포인트가 있다면 비밀 헤더 없이는 403; 무한 호출해도 데이터 1회분만 |
| 1.5 `task_status_history` 테이블 (앞당김 — 원래 M6) | `task_status_history(id, task_id, from_status, to_status, changed_by, changed_at)` 마이그레이션 추가. `TaskService.updateTask` 에서 (old status ≠ new status일 때) 직접 행 emit — AOP `detail` 문자열 파싱 안 함. *이걸 지금 넣어야 도그푸딩 기간 동안 분석용 히스토리가 쌓인다.* | 마이그레이션 적용; 태스크 상태 변경 시 history 행 생성; 단위 테스트(`TaskServiceTest`)로 검증 |
| 1.6 프론트 배포 | `vercel.json` rewrites/headers, `VITE_API_URL` → Render 백엔드 URL, 빌드 명령/출력 디렉토리 설정, "백엔드 깨우는 중" 스플래시 컴포넌트 | Vercel URL 접속 시 앱 로드, API 호출 성공(네트워크 탭 200), 콜드 백엔드일 때 스플래시 표시 |
| 1.7 데모 시드 실행 | 배포 후 시드 트리거(헤더 비밀로 또는 CommandLineRunner 자동) → 테스트 계정/프로젝트/태스크(~20개 캡) 생성 | `test@example.com / Test1234!` 로그인 → Demo Project + 태스크 보임 |
| 1.8 keep-warm | cron-job.org가 `/actuator/health` 를 24/7 10분 핑 | 핑 설정됨, 콜드스타트 빈도 감소 |
| 1.9 CI 배포 + E2E 분리 | `.github/workflows/deploy.yml` (master push → Render deploy hook; Vercel 자체 자동배포). **CI E2E 재구성**: ① *블로킹* E2E = `docker compose up` 띄운 뒤 Playwright(결정적, AI 엔드포인트 mock — 기존 `ai-task.spec.ts` 의 `page.route` mock 사용) ② *논블로킹* 라이브 스모크 = 배포된 Vercel URL 대상 별도 job, **AI 플로우는 실행 안 함**(콜드/레이트리밋/`GROQ_API_KEY` 의존 회피). 기존 `e2e.yml` 을 이 2-job 구조로 | master push → 자동 배포; 블로킹 E2E green(docker compose); 라이브 스모크는 실패해도 머지 안 막음 |
| 1.10 README | 라이브 데모 URL, "Try it" 섹션, 데모 자격증명, "무료 티어 — 30초 대기" 안내, `docker compose up` 퀵스타트, "공개 데모는 Groq 사용 / 셀프호스트는 Ollama 로컬" 명시 | README에 실 URL; 외부인이 URL만으로 데모 가능, `docker compose up` 으로 10분 내 셀프호스트 |

**수용 (M1 전체)**: 브라우저에서 Vercel URL 열기 → (콜드면 스플래시 후) 시드 자격증명 로그인 → Demo Project 보임 → 태스크 생성/이동(→ history 행 생김) → 새로고침 후 영속. 로컬 셋업 0. `git clone && docker compose up` 으로도 10분 내 풀스택. README에 URL.

---

### M2 — AI 코파일럿: Phase 2·3 + 확장 (차별화 코어) · 기여: P1, P3, P4

`.omc/specs/deep-interview-ai-copilot.md` 의 Phase 2(AI 요약 코멘트)·Phase 3(지능형 NL 필터) + 선택 방향의 추가 기능들. **2a·2h 는 하드 필수; 2b–2g 는 개별 독립 — 그리고 2b–2g 도 한 번에 하나씩 진행**(마일스톤 내부에도 빅뱅 금지 — 원칙 #2; §1.5 rule 2의 정신을 M2 내부에도 적용). Ollama 로컬 ↔ Groq 데모 양쪽 동작, 어디서나 graceful degradation, E2E는 AI 엔드포인트 mock.

| 서브 | 내용 | 핵심 파일 | 수용 기준 |
|------|------|-----------|-----------|
| **2a (필수) AI 인프라 리팩터** | `AiProvider` 인터페이스 + `OllamaProvider`/`GroqProvider`/`GeminiProvider`/`NoopProvider`, `AI_PROVIDER` 설정 기반 선택. **구조화 출력 파싱을 추상화 안으로** — `indexOf('{')`/`lastIndexOf('}')` 핵 제거, 공유 JSON 추출 + 스키마 검증, provider-native JSON 모드 사용(Ollama `format:json`, Groq `response_format`). 토큰/지연 로깅, 재시도 + 서킷브레이커(추상화 안). **`GET /api/ai/capabilities`** — 프론트가 AI on/off·provider·클라우드 여부를 알게 함. | 신규 `service/ai/` 패키지, `AiService` 리팩터, `AiController` | `AI_PROVIDER=none` → `capabilities` 가 `enabled:false` → 프론트가 모든 AI 버튼 숨김; provider 교체 = 설정만; **단위/통합 테스트는 fake provider 사용 — 실 LLM 호출 0**; Ollama·Groq 양쪽에서 JSON 출력 안정 |
| 2b AI 태스크 요약 (spec P2) | 태스크 상세 "AI 요약 생성" → 태스크+코멘트+활동 → 상태 요약 코멘트. `POST /api/tasks/{taskId}/ai-summary` | `AiController`, `TaskController` | 요약 버튼 → 코멘트 생성; provider down 시 에러 토스트; 통합 테스트(fake provider) |
| 2c AI NL 필터 (spec P3) | `FilterBar.tsx` 에 자연어 입력 → AI가 status·priority·dueDate·assignee 필터로 번역 → 기존 `GET /api/tasks?...` 적용. 개별 필터 하위호환 | `components/FilterBar.tsx`, `AiController` | "이번 주 마감 HIGH 내 태스크" → 정확한 필터; AI 없이도 개별 필터 동작; 통합 테스트(fake provider, 고정 응답) |
| 2d 브레인덤프 → 태스크 분해 | "브레인 덤프" 모달 — 텍스트/회의록 → AI가 태스크 리스트(제목/설명/우선순위/담당자 추정) 제안 → 검토 후 일괄 생성 | 신규 `components/BrainDumpModal.tsx`, `POST /api/ai/breakdown` | 회의록 → 3+ 태스크 제안 → 선택 일괄 생성 → DB 반영; 통합 테스트(fake provider) |
| 2e AI 스탠드업 생성기 | 프로젝트별 "스탠드업 생성" → 활동로그+`task_status_history` 에서 "마지막/어제 이후 변경" 사람별 요약. 선택적 이메일 | `POST /api/projects/{id}/standup`, `StatsService` | 활동 있는 프로젝트 → 사람별 요약; 빈 활동 → "변경 없음"; 통합 테스트 |
| 2f AI 우선순위 + 블로커 탐지 | "백로그 우선순위화" → TODO 태스크를 한 줄 근거와 함께 순위. 패시브 "블로커 레이더" → 14일+ 정체 IN_PROGRESS 플래그 | `POST /api/projects/{id}/prioritize`, 대시보드 위젯 | 우선순위 결과에 각 태스크 근거 1줄; 14일+ 정체 플래그; 통합 테스트 |
| 2g AI 공수·마감 추정 | 태스크 생성/편집 시 "추정" → 유사 과거 태스크 기반 공수(S/M/L 또는 시간) + 마감 제안 | `POST /api/ai/estimate`, `AiTaskInput.tsx` | "추정" → 합리적 제안, 편집 가능; 통합 테스트 |
| **2h (필수) 포지셔닝 + 데모 정직성** | README + `docs/AI.md`: "로컬-퍼스트 AI / BYO-LLM(Ollama 기본)" 논지, 경쟁자 비교표. **UI: 클라우드 provider 활성 시 AI 모달/버튼 근처에 배너 "⚠ 이 호스팅 데모는 AI 요청을 Groq로 보냅니다 — 셀프호스트하면 로컬 유지"**. 데모 시드는 합성 데이터임을 명시. | `docs/AI.md`, README, AI UI 컴포넌트 | 비교표 + 논지 + UI 배너 + 합성데이터 고지 모두 존재 (P3 합격 조건) |

**수용 (M2)**: 2a + 2h 완료 필수; 2b–2g 는 done 된 것만 카운트. 각 AI 기능이 Ollama 로컬 + Groq 데모 양쪽 동작; provider down 시 명확한 비활성(`capabilities` 기반); 모든 AI 통합 테스트가 fake provider 사용(실 LLM 0); `docs/AI.md` 에 포지셔닝+비교표, UI에 클라우드 배너.

---

### M2.5 — 하드닝 플로어 (필수, 권장 종료 지점 직전) · 기여: P1, P2, P4

"M9에서 한꺼번에"가 아니라 *지금* 깔아야 하는 최소 하드닝. 작고 mandatory.

| 작업 | 상세 | 수용 기준 |
|------|------|-----------|
| 2.5.1 CI 그린 게이트 | `.github/workflows/ci.yml`: `mvn verify`(단위+통합 Testcontainers) + `npx tsc --noEmit` + `npm run lint` + 블로킹 E2E(docker compose) — 모두 통과해야 머지 | PR이 CI green 없이는 머지 불가; 배지 README에 |
| 2.5.2 Sentry 와이어링 | 백엔드 `SENTRY_DSN`(`application-prod.yml` 에 이미 슬롯) + 프론트 `@sentry/react` DSN 환경변수. 의도적 테스트 에러로 검증 | FE/BE 에러가 Sentry에 잡힘 |
| 2.5.3 보안 헤더 | Spring Security 응답 헤더: CSP(기본 정책), HSTS, `X-Content-Type-Options`, `Referrer-Policy`. 프론트 nginx/vercel 헤더도 | `securityheaders.com` 또는 `curl -I` 로 헤더 확인; CSP가 앱을 안 깨뜨림 |
| 2.5.4 의존성 스캔 | Dependabot 활성화(`.github/dependabot.yml` — npm + maven). CI에 `npm audit --audit-level=high` (비차단 경고) | Dependabot PR 생성됨; `npm audit` 가 CI에 출력 |
| 2.5.5 기본 DB 인덱스 | 마이그레이션: FK 인덱스, `tasks(project_id, status)`, `task_status_history(task_id, changed_at)`, `task_activities(task_id, occurred_at)` | 마이그레이션 적용; `EXPLAIN` 으로 인덱스 사용 확인(주요 쿼리 1–2개) |

**수용 (M2.5)**: CI가 단위+통합+블로킹 E2E 실행하고 green; Sentry FE/BE 동작; 보안 헤더 검증; Dependabot 활성; 핵심 인덱스 존재. — **이 시점이 권장 종료 지점**: M10-lite(아래)만 더하면 4대 기준 합격선.

---

### M3 — Dev 친화 ①: 공개 API · API 토큰 · 웹훅 · 기여: P1, P2, P3

TaskHive를 스크립트/통합 가능하게. **M3.1(PAT)을 첫 서브로 — M4·M7이 이걸 분기점으로 씀.**

| 작업 | 상세 | 수용 기준 |
|------|------|-----------|
| 3.1 개인 API 토큰 (PAT) — 먼저 | `Settings → API tokens` 페이지; 토큰은 DB에 해시(SHA-256) 저장; 새 인증 필터가 `Authorization: Bearer <pat>` 수용(`SecurityConfig`·필터 체인 수정); 스코프(read/write); 폐기; last-used 추적; bucket4j 별도 정책 | PAT 생성 → `curl` 로 API 호출 → 태스크 생성 성공; 폐기 후 403; 통합 테스트(PAT 발급·인증·폐기) |
| 3.2 공개 REST API 정형화 | 현 경로를 v1로 문서화(또는 `/api/v1/...` alias), 일관 페이지네이션/필터/에러, springdoc OpenAPI를 안정 URL에 publish, `docs/API.md` 최신화 | OpenAPI UI가 전 엔드포인트+bearer auth 표시; `docs/API.md` 와 실제 일치(자동 비교 스크립트 또는 수동 체크) |
| 3.3 웹훅 (송신) | `project_webhooks(id, project_id, url, secret, events, enabled, consecutive_failures)`; task created/updated/deleted/commented 시 **HMAC-SHA256(타임스탬프 포함 — 리플레이 방지) 서명 JSON** POST; `@Async` + bounded executor 로 비동기(메시지 큐 안 씀); **최대 5회 재시도(지수 백오프) 후 dead-letter 테이블**; **per-webhook 서킷브레이커**(N회 연속 실패 시 `enabled=false`, UI에 표시); **SSRF 가드**(사용자 URL을 private IP/localhost/link-local 대역 검증·차단); 전송 로그; "테스트 웹훅" 버튼 | webhook.site URL 등록 → 태스크 변경 시 서명+타임스탬프 페이로드 도착; 실패 시 5회 재시도 후 dead-letter; 연속 실패 → 자동 비활성+UI 표시; `127.0.0.1` 등록 시도 → 거부; 통합 테스트(서명 검증·재시도·SSRF 거부) |

**수용 (M3)**: PAT 생성→curl로 태스크 생성; 웹훅 등록→변경 시 서명+타임스탬프 전송, 실패 시 재시도/dead-letter/서킷브레이커 동작, SSRF 거부; OpenAPI UI 완비; M3 통합 테스트 통과.

---

### M4 — Dev 친화 ②: GitHub 통합 · 기여: P1, P3, P4 — (M3.1 선행 권장)

태스크 ↔ git. **OAuth App**(GitHub App 아님 — 솔로/$0엔 더 단순, 설치 플로우 없음).

| 작업 | 상세 | 수용 기준 |
|------|------|-----------|
| 4.1 GitHub OAuth | OAuth App(무료); 콜백 처리 → 토큰을 **AES-GCM**(키는 `GITHUB_TOKEN_ENC_KEY` env)로 암호화해 사용자별 저장. **스코프 최소화**: 가능하면 `public_repo` + read-only | OAuth 플로우 완료, 토큰 암호화 저장(평문 미저장 — 통합 테스트로 확인) |
| 4.2 레포 연결 | 프로젝트에 GitHub 레포 링크 | 프로젝트-레포 매핑 저장 |
| 4.3 PR/이슈 링크 | 태스크에 PR/이슈 링크·해제; PR 상태(open/merged/closed)를 태스크 카드에 표시 | 태스크에 PR 링크 → 카드에 상태 뱃지 |
| 4.4 GitHub 웹훅 수신 | `/api/integrations/github/webhook`: **`X-Hub-Signature-256` HMAC 검증 필수**(시크릿 불일치 시 401). PR `merged` → 링크된 태스크 자동 DONE; 라벨 붙은 issue `opened` → 선택적 태스크 생성; `push` 이벤트의 커밋 메시지 `taskhive #123` 파싱 → 태스크 코멘트 (**push는 고볼륨 → 디바운스/필터: 메시지에 `taskhive #` 없으면 즉시 무시**) | 서명 위조 webhook → 401; PR 머지 → 태스크 자동 DONE; `taskhive #123` 커밋 → 코멘트; 통합 테스트(서명 검증·PR-merged 핸들러) |
| 4.5 이슈 임포트 | 레포 오픈 이슈 일괄 → 태스크로 임포트 | 임포트 → 이슈들이 태스크로; 중복 임포트 방지(github_issue_id 유니크) |

**수용 (M4)**: GitHub 연결(토큰 암호화) → 태스크에 PR 링크 → PR 머지 → 태스크 자동 DONE; 서명 검증 동작; 레포 이슈 임포트; M4 통합 테스트 통과.

---

### M5 — Cmd-K 커맨드 팔레트 & 키보드 우선 UX · 기여: P1, P4

| 작업 | 상세 | 수용 기준 |
|------|------|-----------|
| 5.1 Cmd-K 팔레트 | `cmdk` 라이브러리(무료) 또는 자체 구현: 내비게이션(프로젝트/태스크/페이지 이동), 액션(태스크/프로젝트 생성, 테마 토글), 검색(태스크/프로젝트/코멘트 퍼지) | Cmd-K가 어디서나 열림, 퍼지 검색 결과 정확 |
| 5.2 단축키 | `c` 태스크 생성, `/` 검색 포커스, `g p` 프로젝트 이동, `j/k` 리스트 내비, `?` 단축키 시트 | 각 단축키 동작, `?` 가 치트시트 표시 |
| 5.3 글로벌 검색 백엔드 | `GET /api/search?q=` — Postgres full-text 또는 pg_trgm — 태스크 제목/설명/코멘트 | 관련 결과 반환; 인덱스 사용; 통합 테스트(검색 정확도) |
| 5.4 접근성 폴리시 | 포커스 관리, Escape 처리, ARIA | 키보드만으로 팔레트 전 기능 사용 가능; E2E(키보드 내비) |

---

### M6 — 분석 대시보드 · 기여: P1, P3, P4 — (`task_status_history` 는 M1.5에서 이미 도입됨)

감사로그(`TaskActivity`) + `task_status_history` + `StatsService` → 프로젝트 인텔리전스. 차트 라이브러리 Recharts/visx(무료). **주의: AOP 감사 aspect 는 실패 시 catch-and-log → 분석 데이터에 미세 갭 가능(README/docs 각주).**

먼저 **`docs/analytics-fixtures.md`** 작성 — 명명된 시드 데이터셋(태스크 목록·상태 전이·날짜) + 각 차트의 기대값을 손계산해 명시. 모든 M6 수용 기준은 이 픽스처 대비 검증.

| 작업 | 상세 | 수용 기준 |
|------|------|-----------|
| 6.1 분석 픽스처 | `docs/analytics-fixtures.md`: 시드 데이터셋 X, 기간 2026-01-01..01-14, 각 차트 기대값 표 | 파일 존재, 손계산 검증됨 |
| 6.2 번다운/번업 | 프로젝트별(스프린트 도입 시 스프린트별 — M8). 스코프는 **프로젝트 레벨**로 고정(M8 전까지) | 픽스처 X에서 번다운이 `analytics-fixtures.md` 의 값과 일치 |
| 6.3 누적흐름도(CFD) | 상태별 태스크 수 추이 | 픽스처와 일치; 희소 데이터 graceful |
| 6.4 사이클/리드타임 | `task_status_history` 타임스탬프 기반 | 픽스처의 태스크 #N 사이클타임 = 명시된 M일 |
| 6.5 처리량·WIP | 주당 완료 태스크, 시간별 WIP | 픽스처와 일치 |
| 6.6 개인 대시보드 | 내 태스크 상태별, 내 사이클타임, 최근 활동 | 렌더; 통합 테스트(집계 쿼리) |
| 6.7 병목 하이라이트 | "IN_PROGRESS가 2주째 증가 중" 인사이트 카드 | 조건 충족 시 카드 표시; 픽스처로 트리거 검증 |

---

### M7 — CLI 툴 (`taskhive`) · 기여: P1, P3 — (M3.1 PAT 선행)

터미널 우선 워크플로우. TS 권장(프론트 API 타입 재사용) — npm 배포(무료); 또는 Go 단일 바이너리.

| 작업 | 상세 | 수용 기준 |
|------|------|-----------|
| 7.1 CLI 스캐폴드 | `cli/` 워크스페이스, `commander`/`yargs`, 설정 `~/.taskhive/config` | `taskhive --help` 동작 |
| 7.2 인증 | `taskhive login` (PAT 붙여넣기) | 로그인 후 인증된 요청 |
| 7.3 핵심 명령 | `task list/create/update/done`, `project list`, `standup`(AI 스탠드업 호출) | `taskhive task create "fix bug" --priority high` → 웹 UI에 보임; `taskhive standup` → AI 스탠드업 출력; E2E(CLI→API→DB) |
| 7.4 배포 | npm publish 또는 GitHub Releases 바이너리 | `npx @taskhive/cli` 또는 바이너리로 실행 |

---

### M8 — 제품 깊이: 스프린트/사이클 · 라벨 · 첨부 · 마크다운 · 모바일 · 다크모드 · 기여: P4

"실사용"에 필요하나 아직 없는 것들. **각 신규 테이블은 Flyway 마이그레이션으로.**

| 작업 | 상세 | 수용 기준 |
|------|------|-----------|
| 8.1 스프린트/사이클 | `cycles` 테이블; 태스크에 사이클 배정; 사이클 뷰; 벨로시티 → 번다운 입력 | 마이그레이션; 사이클 생성→배정→뷰→번다운(스프린트 스코프) 반영; 통합 테스트 |
| 8.2 라벨/태그 | 태스크에 M:N(`labels`, `task_labels`); 라벨 필터; 색칩 | 마이그레이션; 라벨 생성·부여·필터; 통합 테스트 |
| 8.3 첨부 | 파일 업로드 — $0 저장: Cloudflare R2 무료(10GB) 권장(서명 URL); 또는 DB bytea ≤1MB. **Neon 0.5GB 보호 위해 DB 저장 시 엄격 캡** | 파일 업로드·다운로드, 크기 제한 적용; 통합 테스트 |
| 8.4 마크다운 | 설명·코멘트 마크다운 렌더 + sanitizer(DOMPurify 등 — XSS 방지) | 마크다운 렌더, `<script>`·`onerror` 무력화; 단위 테스트(sanitizer) |
| 8.5 모바일 반응형 | 칸반·테이블 폰 대응, 하단 내비, 터치 DnD | 폰 뷰포트에서 핵심 플로우 동작; E2E(모바일 뷰포트) |
| 8.6 다크모드 | Ant Design 다크 토큰 + 토글 영속(localStorage) | 토글 동작, 새로고침 후 유지 |
| 8.7 UX 폴리시 | 빈 상태, 로딩 스켈레톤, 낙관적 업데이트 전면 | 주요 화면에 적용 |

---

### M9 — 최종 폴리시: 커버리지·성능·관측성 심화 · 기여: P1, P2, P4 — (M2.5 플로어 위에 *추가*)

M2.5가 플로어, M9가 천장. *처음 테스트하는 곳이 아님* — 각 M3–M8이 이미 자체 테스트를 가져왔다.

| 작업 | 상세 | 수용 기준 |
|------|------|-----------|
| 9.1 커버리지 | 백엔드 JaCoCo 라인 ≥75% **+ 서비스 레이어 브랜치 ≥60%**(getter 인플레이션 방지); CI 커버리지 게이트 | 리포트 ≥75% 라인 & ≥60% 서비스 브랜치 |
| 9.2 성능 | N+1 감사(Hibernate statistics); 페이지네이션 전수 점검; 프론트 코드 스플리팅·lazy route; 번들 분석(`vite-bundle-visualizer`), 기준선 문서화 | p95 API <300ms(웜, `k6`/`hey` 100 req `/api/tasks`); 페이지 로드 <2s; 번들 크기 기준선 `docs/` 에 기록 |
| 9.3 관측성 심화 | 구조화 JSON 로그 + 요청 ID(`RequestIdFilter` 활용); `/actuator/metrics` 노출; 선택 `/status` 페이지 | 로그에 request ID; metrics 엔드포인트 동작 |
| 9.4 보안 심화 | JWT/refresh rotation 재검토; CORS 락다운 재확인; `mvn dependency-check`(또는 OWASP) CI 추가; `SECURITY.md` 보강 | 보안 스캔 CI 통과; 리뷰 노트 `docs/` 에 |
| 9.5 신뢰성 | graceful shutdown 확인(exec-form Dockerfile 덕에 가능); HikariCP 풀 튜닝(무료 티어 커넥션 수); AI provider 재시도/서킷브레이커 재확인; 웹훅 수신 idempotency 키 | 부하 중 정상 종료; AI down 시 앱 정상; 중복 webhook 수신 무해 |

**수용 (M9)**: 커버리지 ≥75% 라인 & ≥60% 서비스 브랜치; Lighthouse perf/a11y/best-practices ≥90; p95 API <300ms 웜; CI가 unit+integration+E2E(docker compose)+의존성 스캔 green; 1주 도그푸딩 중 critical Sentry 이슈 0.

---

### M10 — OSS 릴리스 폴리시 & 런치 · 기여: P1, P2, P3, P4

**M10-lite (권장 종료 지점에 필요한 최소)** = 10.1 + 10.2 + 10.3. 나머지(10.4–10.7)는 풀버전.

| 작업 | 상세 | 수용 기준 |
|------|------|-----------|
| 10.1 OSS 위생 파일 | `LICENSE`(MIT), `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, `.github/ISSUE_TEMPLATE/*`, PR 템플릿 | 체크리스트 100% |
| 10.2 README 대개편 | 히어로 스크린샷/GIF; 배지(CI·라이선스·릴리스·Docker pulls); 기능 목록+스크린샷; "로컬-퍼스트 AI" 피치 + 비교표(P3); 퀵스타트(`docker compose up`); 셀프호스팅 가이드; 설정 레퍼런스; API 문서 링크; 로드맵; 기여 링크 | 외부인이 README만으로 셀프호스팅 <10분 |
| 10.3 릴리스 파이프라인 | `CHANGELOG.md`(Keep a Changelog); 시맨틱 버전; GitHub Releases(노트); 태그 시 GHCR에 백/프론트 이미지 publish (GitHub Actions) | v1.0.0 릴리스 + 노트 + GHCR 이미지 pull 가능 |
| 10.4 문서 사이트 | GitHub Pages(무료) + MkDocs Material 또는 Docusaurus — 또는 `docs/` README들로 충분 | (선택) 사이트 빌드·배포 |
| 10.5 포트폴리오 자산 | "왜 만들었나·핵심 결정" 글(`docs/`); 아키텍처 다이어그램; 60–90초 데모 영상/GIF | README에 임베드 |
| 10.6 도그푸딩 증빙 | M2 이후 **Ollama 셀프호스팅 인스턴스**로 TaskHive 백로그 추적해온 것 — 2주+, 공개 조회 가능한 TaskHive 프로젝트 URL | "실사용" 증빙 (P4 합격 조건); URL 공개 |
| 10.7 런치 | awesome-selfhosted PR; 선택: Show HN, r/selfhosted | (선택) 게시 |

---

## 3. 의존성 & 권장 순서

```
M0 (블로커: 정리·Flyway·결함수정·머지)
  └─ M1 (배포; 1순위 — "웹에서 열어 테스트" 충족 + task_status_history 도입 + E2E 분리)
       └─ M2 (AI 코어; 2a 필수 + 2h 필수, 2b–2g 개별 독립 — 차별화)
            └─ M2.5 (하드닝 플로어: CI green·Sentry·보안헤더·Dependabot·인덱스) ◀── 권장 종료 지점
                 ├─ (선택, 한 번에 하나 §1.5) ────────────────────────┐
                 │   M3 (공개 API/PAT/웹훅) — M3.1(PAT) 먼저            │
                 │   M4 (GitHub) ← M3.1 선행                            │  M3·M4·M5·M6·M7·M8
                 │   M5 (Cmd-K) — task_status_history 무관              │  = 메뉴, 임의 순서,
                 │   M6 (분석) — M1.5 task_status_history 위에서        │  한 번에 1개만 in-progress
                 │   M7 (CLI) ← M3.1 선행                               │
                 │   M8 (제품 깊이: 스프린트·라벨·첨부·마크다운·모바일) │
                 │   └─ M8.1(스프린트) → M6.2 번다운 스코프 풍부화 ─────┘
                 └─ M9 (최종 폴리시: 커버리지·성능·관측성 심화 — 메뉴 끝나면)
                      └─ M10 (OSS 릴리스 & 런치; M10-lite = 10.1–10.3)
```

- **최소 포트폴리오 경로 (= 권장 종료 지점)**: M0 → M1 → M2 → M2.5 → M10-lite. 여기까지면 "라이브 AI 태스크 매니저 + 무료 셀프호스팅 + 하드닝 플로어 + OSS 릴리스" — 4대 기준 합격선.
- **풀 비전 경로**: 권장 종료 지점 후 → (M3·M4·M5·M6·M7·M8 임의 순서, 한 번에 1개) → M9 → M10 풀버전.
- 숨은 의존성: M1.5의 `task_status_history` 가 M2e(스탠드업)·M6(분석)의 데이터 소스; M3.1(PAT)가 M4·M7의 전제; M8.1(스프린트)가 M6.2(번다운 스프린트 스코프)를 풍부화하나 필수는 아님; M1.9의 블로킹 E2E는 라이브 배포가 아닌 `docker compose` 대상(라이브는 논블로킹 스모크, AI 미실행).

---

## 4. 리스크 & 완화

| 리스크 | 영향 | 완화 |
|--------|------|------|
| JVM 콜드스타트(Render 무료) ~30–60s | 데모 첫인상 | 24/7 keep-warm 핑, lazy-init + JVM 튜닝, **프론트 "깨우는 중 ~30초" 스플래시 + 자동 재시도**, README 명시. 최악: Koyeb 전환 |
| **Render 750hr/mo 캡 (계정당)** | 월말 서비스 일시정지 | 24/7 핑 + 활성 데모는 캡을 넘길 수 있음 → 서비스 1개만 운영(웹훅 워커 분리 금지 — `@Async` 인-프로세스), 월말 일부 슬립 수용, 또는 Koyeb/Fly 폴백. README에 "무료 티어 — 가끔 슬립" 명시 |
| Neon/Supabase 자동 일시정지 + 0.5GB 한도 | 첫 쿼리 지연; 용량 초과 | Neon 우선(웨이크 ~1초). 시드 데이터 소량 캡(태스크 ~20, 첨부 없음). 첨부(M8.3)는 R2로 외부화 권장. Supabase 쓰면 keep-warm 쿼리 |
| **데모 시드 엔드포인트 남용 / 데이터 오염** | Neon 용량 소진·스팸 | M1.4: `/api/dev/seed` 를 idempotent + rate-limit + `X-Seed-Token` 비밀 게이트, 또는 HTTP 엔드포인트 없이 `@Profile("demo")` CommandLineRunner 1회 시드(권장) |
| LLM이 CI/테스트에서 플래키 | CI 불안정 | 블로킹 E2E·통합 테스트는 fake `AiProvider`/`page.route` mock; 라이브 스모크는 AI 플로우 미실행; 실 LLM은 CI에서 절대 호출 안 함 |
| Groq/Gemini 무료 티어 레이트리밋 낮음 | 데모 AI 가끔 실패 | "AI가 바빠요, 잠시 후" 상태(`capabilities` 기반), 최근 AI 결과 캐시, 친절한 폴백 |
| **반쯤 만든 sprawl이 P1을 해침** | 포트폴리오 역효과 | §1.5 실행 규칙: 권장 종료 지점, 한 번에 기능 마일스톤 1개, "done" 정의(수용+통합테스트+머지), **버려지면 master에 안 남기고 revert** |
| 솔로 번아웃 / 스코프 크리프 | 미완성 | 각 마일스톤 독립 출시; M0→M1→M2→M2.5 만으로 포트폴리오 가치; M3–M8 메뉴; M2부터 도그푸딩으로 도구가 값어치함 |
| GitHub OAuth/웹훅 복잡도·SSRF·리플레이 | M3/M4 취약점 | 공식 SDK; 송신 웹훅 HMAC+타임스탬프+SSRF 가드+서킷브레이커+dead-letter; 수신 GitHub 웹훅 `X-Hub-Signature-256` 검증; 토큰 AES-GCM(env 키); 스코프 최소화; 일회용 레포로 테스트 |
| `ddl-auto` 로는 배포 DB 스키마 진화 불가 | P2 막힘 | M0에서 Flyway 도입, `ddl-auto=validate`, 모든 신규 테이블은 마이그레이션으로 |
| 무료 티어 이메일 도달률 | 인증 메일 실패 | `MAIL_ENABLED` 옵셔널, 콘솔-로그 폴백, Resend/Brevo 무료, 문서화 |
| `phase13` → master 머지 botch / mvn test가 mail 설정 요구 | 작업 손실·빌드 깨짐 | M0: `pre-consolidation` 태그 선행·master 푸시 검증까지 유지; rebase/cherry-pick; mail mock/프로퍼티 정비 예산 |
| "로컬-퍼스트" 포지셔닝 ↔ 클라우드 데모 모순 | P3 신뢰성 훼손 | Ollama가 *기본값*·도그푸딩 인스턴스; 데모 클라우드 모드는 UI 배너 + README 명시 + 합성 시드 데이터 고지 → 모순을 *교육 포인트*로 |
| 분석 데이터 갭(AOP aspect catch-and-log) | 차트 미세 부정확 | `docs/` 각주; 상태 전이는 aspect가 아닌 `TaskService.updateTask` 가 직접 emit(M1.5) → 핵심 분석은 영향 적음 |
| "창의성" 기준 주관성 | P3 미충족 논란 | P3의 측정 정의(비교표 + 고유기능 3+ + 포지셔닝 문서 + UI 배너) |

---

## 5. 전체 검증 (요약)

- **M0 후**: `cd auth && mvn package` green; `cd frontend && npx tsc --noEmit && npm run lint` green; `mvn test` green; Flyway 베이스라인이 빈 DB에 스키마 생성; `ddl-auto=validate` 통과; `git log` master 선형(merge 노드 없음)·의미 단위 커밋; `git status` clean; 원격 푸시됨.
- **M1 후**: `curl https://<render-url>/actuator/health` → 200; Vercel URL 열기 → (콜드면 스플래시) 시드 자격증명 로그인 → 태스크 생성 → `task_status_history` 행 생성 확인 → 새로고침 후 영속; `git clone && docker compose up` → 10분 내 풀스택; README에 URL; 블로킹 E2E(docker compose) green, 라이브 스모크(AI 미실행) 동작.
- **M2 후**: 2a + 2h 완료; `OLLAMA_URL` 로컬 동작 + `AI_PROVIDER=groq`+`GROQ_API_KEY` 데모 동작 + `AI_PROVIDER=none` → `GET /api/ai/capabilities` `enabled:false` → 프론트 AI 버튼 숨김; 모든 AI 통합 테스트 fake provider(실 LLM 0); UI 클라우드 배너 + `docs/AI.md` 비교표.
- **M2.5 후**: CI(`mvn verify` + tsc + lint + docker-compose E2E) green 게이트; Sentry FE/BE 의도적 에러 캐치; 보안 헤더(`curl -I`/securityheaders.com); Dependabot PR 생성; 핵심 인덱스 `EXPLAIN` 확인.
- **M6 후**: `docs/analytics-fixtures.md` 존재; 픽스처 데이터셋에서 번다운·CFD·사이클타임·처리량이 명시값과 일치.
- **각 기능 마일스톤(M3–M8) 후**: 해당 수용 기준 + 자체 통합/E2E 테스트 green + 커밋·머지 (또는 중단 시 revert).
- **M9 후**: JaCoCo 라인 ≥75% & 서비스 브랜치 ≥60%; `lighthouse-ci`(라이브 Vercel, 로그인·태스크·칸반 3페이지) ≥90; `k6`/`hey` 100 req `/api/tasks` p95 <300ms 웜; CI 전체 green; 1주 도그푸딩 critical Sentry 0.
- **최종 (M10)**: OSS 위생 체크리스트 100%; `git clone && docker compose up` 클린 체크아웃 10분 내; v1.0.0 릴리스(노트 + GHCR 이미지 pull); README에 비교표·라이브 URL·데모 GIF; 공개 도그푸딩 프로젝트 URL.

---

## 6. ADR — Architecture Decision Record

**Decision**: TaskHive의 다음 챕터를 "Local-First AI Project Platform" 으로 진행한다 — AI-Native(척추) + Dev 친화(공개 API·PAT·웹훅·GitHub·Cmd-K·CLI) + 분석 대시보드를, 완전 무료 스택(Render/Neon/Vercel/Groq/GHCR/R2)에, M0–M10 마일스톤으로. 권장 종료 지점 = M0→M1→M2→M2.5→M10-lite.

**Drivers**: ① 노력 단위당 포트폴리오 임팩트 ② 솔로 개발 지속가능성 ③ $0 하드 제약.

**Alternatives considered**:
- *옵션 B (실시간 멀티플레이어)* — 기각: 솔로+무료엔 노력/리스크 과다, dyno 슬립이 WS 끊김, 솔로 데모 어색.
- *옵션 C (Dev-First 깃 트래커 단독)* — 척추로는 기각: Plane.so/Huly가 OSS 니치 선점. 단, 포트폴리오 강점 부분은 채택안에 흡수.
- *기본 태스크 매니저 폴리시만* (새 차별화 없이) — 기각: P3(창의성·차별성) 미충족.
- *Railway 유지(유료)* — 기각: "무료" 하드 제약 위반.

**Why chosen**: A는 가장 차별화됨(OSS 태스크 매니저에서 BYO-LLM/프라이버시 각도 희소) + 가장 핫한 포트폴리오 스킬 + $0 친화(Ollama 로컬 + Groq 무료 데모) + 모멘텀(AI 코파일럿 spec·Phase 1 완료). C의 강점을 흡수해 풀스택 전 영역 커버. 사용자의 "오픈엔드, 품질 기준이 요구하는 만큼" 지시.

**Consequences**:
- (+) 라이브 데모 + AI 차별화 + 풀스택 폭 + OSS 위생 → 4대 기준 달성 경로 명확.
- (−) 스코프가 큼 → §1.5 통제 장치(한 번에 하나, 권장 종료 지점, revert-if-abandoned) 없으면 sprawl 위험.
- (−) "로컬-퍼스트" ↔ 클라우드 데모 모순 → UI 배너·README 라벨링으로 *교육 포인트* 전환 필요(M2a/M2h).
- (−) 무료 티어 트레이드오프(콜드스타트·750hr·레이트리밋)를 *숨기지 않고 문서화* 한다 — 포장보다 정직(원칙 #3).
- (+) Flyway 도입으로 배포 DB 스키마 진화 가능 — P2 전제 충족.

**Follow-ups**:
- M1.5에서 `task_status_history` 를 일찍 도입(분석 데이터 축적).
- M2a에서 `GET /api/ai/capabilities` 추가(프론트 graceful degradation).
- 매 마일스톤 자체 통합 테스트(M9 몰아치기 금지).
- 도그푸딩 인스턴스는 Ollama로 — 포지셔닝 일관성.
- 첨부(M8.3)는 R2로 외부화 검토(Neon 용량 보호).

---

## 7. 다음 행동 (이 계획 승인 시)

1. **M0부터** 시작 — `git tag pre-consolidation` → 빌드/`mvn test` 확인 → 결함 수정 → Flyway 도입 → 영역별 커밋 → master rebase/머지·푸시. (`ralph` 또는 `team` 으로 실행 가능)
2. 또는 사용자가 마일스톤 순서·범위·권장 종료 지점을 조정 후 시작.

> 이 계획은 `/Users/iee12/taskhive/.omc/plans/roadmap-v2-ai-native.md` 에 저장됨. 비대화형 `--consensus --review` 모드이므로 자동 실행하지 않음 — 시작하려면 "M0 시작해" 또는 `/oh-my-claudecode:ralph` 로 지시.

---

## 8. 변경 로그 (consensus 리뷰 v2 — 반영 내역)

**Architect (SOUND-WITH-CHANGES) 반영:**
1. "로컬-퍼스트 ↔ 클라우드 데모" 모순 → Ollama 기본값 명시, M2h UI 배너 + README 라벨링 + 합성 시드 고지를 수용 기준에 추가.
2. 하드닝을 천장→바닥: 신규 **M2.5 하드닝 플로어**(CI green·Sentry·보안헤더·Dependabot·인덱스) 삽입; M3–M8 각자 통합 테스트를 수용 기준에 포함; M9는 "최종 폴리시"로 재정의.
3. `task_status_history` 를 M6→**M1.5로 앞당김**; `TaskService.updateTask` 가 직접 emit.
4. `demo` 프로파일/`/api/dev/seed` 락다운(idempotent+rate-limit+`X-Seed-Token`, 또는 `@Profile("demo")` CommandLineRunner) — M1.4.
5. 24/7 keep-warm + 프론트 "깨우는 중" 스플래시 — M1.6/1.8, §4.
6. 웹훅: 최대 5회+dead-letter, per-webhook 서킷브레이커, SSRF 가드, 타임스탬프 HMAC, `@Async` 인-프로세스 — M3.3.
7. GitHub 인바운드 `X-Hub-Signature-256` 검증 명시, AES-GCM 토큰 암호화(env 키), 스코프 최소화, push 디바운스 — M4.
8. CI E2E 분리: 블로킹=`docker compose`, 논블로킹 라이브 스모크(AI 미실행) — M1.9.
9. M0: rebase/cherry-pick(merge 노드 없음), `pre-consolidation` 태그 유지, `mvn test` mail 설정 예산 — M0.
+ Dockerfile `SERVER_PORT` env(shell wrapper 대신, 시그널 전파), `nginx.conf` 존재 확인됨, CORS env + `SameSite=None;Secure` 쿠키, Render 750hr 계정당, Neon 0.5GB 캡, `AiProvider` 안에 구조화 출력 파싱 + provider-native JSON 모드, `GET /api/ai/capabilities`, `demo`=`profiles.include: prod`, 분석 aspect catch-and-log 각주.

**Critic (REJECT → 반영) 처리:**
- 9개 Architect 변경을 계획 *본문*에 반영(위).
- 신규 **§1.5 실행 규칙**: 권장 종료 지점, 한 번에 기능 마일스톤 1개, "done" 정의, 버려지면 revert, 하드닝 플로어.
- §4에 리스크 추가: Render 750hr 캡, 데모 시드 남용, 반쯤 만든 sprawl(완화=§1.5).
- M6.2/M6.4 모호 수용 기준 → `docs/analytics-fixtures.md` 명명 픽스처 + 기대값으로 교체.
- P1(b) "시니어가 6역량 식별" → **명명 파일/디렉토리 체크리스트**로 교체(검증 가능).
- M9.1: 서비스 레이어 브랜치 커버리지 ≥60% 추가(getter 인플레이션 방지).
- P4(a) 도그푸딩: **Ollama 셀프호스팅 인스턴스** + 공개 조회 가능 프로젝트 URL — 원칙 #5 모순 해소.
- §5: Lighthouse(`lighthouse-ci`, 3페이지)·p95(`k6`/`hey`) 도구 명시.
- **DB 마이그레이션 도구(Flyway)** — M0.4 신규, `ddl-auto=validate`, P2 전제로 §0·§4에 명시.
- M1.9 라이브 스모크는 AI 엔드포인트 미실행 확인 — 명시됨.
- 로드맵 전체 "done 정의" = 권장 종료 지점(M0→M1→M2→M2.5→M10-lite) — §1.5·§3·헤더에 명시.
- M2: 2a 만 하드 필수, 2b–2g 개별 독립 — 빅뱅 금지(원칙 #2).
- ADR 섹션(§6) 추가 — consensus 최종 출력 요구.
