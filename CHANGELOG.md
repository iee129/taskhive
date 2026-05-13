# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
