# ADR-006: 로컬 LLM 런타임으로 Ollama 채택

**상태**: 채택  
**날짜**: 2026-05-12  
**결정자**: iee129

---

## 컨텍스트

Phase 6에서 두 가지 AI 기능을 추가한다:
1. 자연어 입력 → 태스크 구조체 파싱 (`POST /api/ai/parse-task`)
2. 현재 태스크 기반 일간 요약 다이제스트 (`GET /api/ai/digest`)

이를 위해 LLM 연동 방식을 결정해야 한다.

---

## 고려한 옵션

### Option A: OpenAI / Claude API (외부 API)
- **장점**: 최고 수준 모델 성능, 구현 단순 (API 키만 필요)
- **단점**: 매월 비용 발생, API 키 관리 필요, 외부 의존성으로 CI/CD에서 시크릿 주입 필요, 오프라인 개발 불가

### Option B: Ollama + Llama 3.2 3B (로컬 LLM) ← **채택**
- **장점**: 비용 0, API 키 없음, Docker Compose 서비스로 통합 → 자급자족 아키텍처, 오프라인 동작
- **단점**: 외부 API 대비 모델 성능 낮음, 초기 모델 다운로드 필요 (~2GB), GPU 없으면 응답 느림

### Option C: Hugging Face Inference API
- **장점**: 무료 티어 존재
- **단점**: Rate limit 엄격, 무료 모델 품질 불안정, 외부 의존성

---

## 결정

**Option B — Ollama + Llama 3.2 3B** 채택.

---

## 이유

| 드라이버 | 설명 |
|---------|------|
| **비용** | 학생 환경에서 지속적인 API 비용 없이 개발·시연 가능 |
| **자급자족** | `docker compose up -d` 한 명령으로 AI 포함 전체 스택 구동 — 포트폴리오 시연에 최적 |
| **포트폴리오 가치** | "외부 API 없이 로컬 LLM 통합" = 인프라 이해도 증명. Notion AI·Linear와 동일 기능을 자체 스택으로 구현했다는 차별점 |
| **아키텍처 일관성** | Phase 10 Docker Compose에 `ollama` 서비스 하나만 추가 — 기존 서비스 구조와 자연스럽게 통합 |

Llama 3.2 3B는 한국어 태스크 파싱·요약에 충분한 성능을 제공하며 RAM 4GB에서 동작한다.  
응답 품질이 GPT-4보다 낮더라도 포트폴리오 시연 목적에서는 허용 가능한 수준이다.

---

## 결과

- `AiService` — Spring `RestClient`로 `http://ollama:11434/api/generate` 호출
- 타임아웃 30초 설정 — 미응답 시 503 반환 (graceful fallback)
- `ollama.base-url`, `ollama.model` application.yml 외부화 → 나중에 OpenAI 교체 시 설정값만 변경
- Docker Compose `ollama_data` 볼륨으로 모델 영속화 — 재시작 시 재다운로드 불필요

---

## 후속 조치

- 최초 실행 시 모델 풀 명령 문서화: `docker exec taskhive-ollama-1 ollama pull llama3.2:3b`
- 성능 이슈 발생 시 OpenAI API로 교체 경로 확보 (`AiService` 인터페이스 추상화)
