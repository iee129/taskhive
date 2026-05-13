# AI API

> Phase 6에서 구현 완료. Ollama 로컬 LLM 연동. 모든 엔드포인트는 JWT 인증 필요.

## 사전 조건

Ollama가 로컬에서 실행 중이어야 합니다:

```bash
ollama serve
ollama pull llama3.2
```

Ollama가 없어도 서비스는 정상 동작합니다. AI 호출 실패 시 입력값 기반 fallback 태스크를 반환합니다.

## 엔드포인트 목록

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/ai/suggest-task` | 자연어 → 태스크 구조 제안 (저장 안 함) |
| POST | `/api/ai/create-task` | 자연어 → 태스크 생성 (DB 저장) |

## POST /api/ai/suggest-task

자연어 설명을 AI가 분석해 태스크 구조를 제안합니다. 저장하지 않으므로 미리보기 용도입니다.

**요청**

```json
{
  "description": "다음 주까지 로그인 화면 버그 수정해야 함, 급해",
  "projectId": 2
}
```

**응답 `200 OK`** — `TaskRequest` 형식

```json
{
  "title": "로그인 화면 버그 수정",
  "description": "로그인 화면의 버그를 다음 주까지 수정",
  "priority": "HIGH",
  "projectId": 2
}
```

## POST /api/ai/create-task

AI 제안을 즉시 DB에 저장하고 생성된 태스크를 반환합니다.

**요청** — `/api/ai/suggest-task`와 동일

**응답 `200 OK`** — `TaskResponse` (생성된 태스크)

## 설정

```yaml
# application.yml
taskhive:
  ollama:
    url: ${OLLAMA_URL:http://localhost:11434}
    model: ${OLLAMA_MODEL:llama3.2}
```

## Fallback 동작

Ollama 호출 실패 또는 JSON 파싱 오류 시 서버는 예외를 던지지 않고 fallback을 반환합니다:
- `title` — 입력 description 앞 50자 (이상이면 `...` 생략)
- `description` — 입력 description 전체
- `priority` — `MEDIUM`
