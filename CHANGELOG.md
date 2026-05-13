# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-05-13

### Added (M6 · M8 · M9)
- **M6 분석 대시보드** — GET /api/projects/{id}/analytics/burndown|cfd|cycle-time; 프론트엔드 Recharts LineChart·AreaChart·BarChart
- **M8-2 라벨/태그** — labels + task_labels Flyway 마이그레이션; CRUD 5개 엔드포인트; GET /api/tasks?labelId= 필터; 태스크 카드 색상 칩
- **M8-4 마크다운 렌더링** — MarkdownContent (DOMPurify + react-markdown); 태스크 설명·코멘트 XSS 무력화; vitest 4개 유닛 테스트
- **M8-6 다크모드** — Ant Design ConfigProvider dark/default 알고리즘 토글; localStorage 영속; 사이드바 스위치
- **M9 최종 폴리시** — JaCoCo 라인 커버리지 리포트 (jacoco-maven-plugin 0.8.11); TaskRepository @EntityGraph N+1 차단; RequestIdFilter X-Request-Id 전파 (기존 완비)

### Changed
- `TaskRepository.findFiltered` — labelId 파라미터 추가 (DISTINCT + LEFT JOIN t.labels)
- `WebhookDeliveryService.deliver()` — @Transactional 추가; doThrow mock anyString() 오버로드 수정
- `TaskRepository.updateCreatedAtNative` — @Modifying(clearAutomatically=true, flushAutomatically=true)로 Hibernate L1 캐시 동기화

## [1.1.0] - 2026-05-13

### Added (M2–M5)
- **M2 AI 코파일럿** (M2b–M2g): AI 태스크 요약·자연어 필터·브레인덤프·스탠드업·우선순위·공수 추정
- **M3-1 PAT** — SHA-256 해시 개인 API 토큰; 스코프 기반 인증
- **M3-3 웹훅** — SSRF 가드, HMAC-SHA256 서명, 5회 연속 실패 자동 비활성
- **M5 커맨드 팔레트** — Cmd+K 팔레트; 퍼지 검색 /api/search; 키보드 단축키 c·/·?

## [1.0.0] - 2026-05-13

### Added
- **AI 코파일럿 인프라** — `AiProvider` 인터페이스 + OllamaProvider / GroqProvider / NoopProvider 전략 패턴
- **BYO-LLM** — `AI_PROVIDER` 환경변수로 런타임 LLM 프로바이더 교체 (ollama / groq / none)
- `GET /api/ai/capabilities` — 프론트엔드 AI 활성화 상태 감지 엔드포인트 (인증 불필요)
- `AiProviderBanner` — 클라우드 LLM 사용 시 UI 경고 배너
- **Spring Security 보안 헤더** — HSTS, CSP, X-Content-Type-Options, Referrer-Policy, X-Frame-Options
- **Dependabot** — npm + Maven 주간 자동 의존성 업데이트
- `V3__indexes.sql` — tasks(project_id, status), task_activities(task_id, occurred_at) 복합 인덱스
- **CI npm audit** — `npm audit --audit-level=high` 비차단 의존성 감사
- OSS 위생 파일 — LICENSE(MIT), CONTRIBUTING.md, CODE_OF_CONDUCT.md, SECURITY.md
- GitHub 이슈/PR 템플릿
- GHCR 릴리스 파이프라인 (태그 push → Docker 이미지 자동 publish)

### Changed
- `AiService` — `AiProvider` 인터페이스 기반으로 리팩터, indexOf 핵 제거
- `AppConfig` — `RestTemplate` 타임아웃 설정 (connectTimeout 5s, readTimeout 30s)
- CI 배지 README 추가

### Fixed
- `application-test.yml` 위치 오류 수정 (`src/test/resources/`로 이동)
- `GroqProvider` — choices 배열 빈 체크 추가 (NPE 방지)

## [0.x] — Phase 1–15 (기존 개발)

- Phase 1–15: 인증, 프로젝트, 태스크, 칸반, 댓글, 통계, Rate Limiting, Sentry, AI, WebSocket, E2E 테스트 등 기반 구축
