# AI 기능 가이드

TaskHive의 AI 태스크 제안은 **로컬 퍼스트(Local-First)** 설계를 따릅니다.  
기본 설정에서는 외부 클라우드로 데이터가 전송되지 않습니다.

## 지원 AI 프로바이더

| 프로바이더 | 환경변수 값 | 클라우드 여부 | 설명 |
|------------|------------|--------------|------|
| Ollama (기본) | `ollama` | 로컬 | 로컬 LLM, 데이터 외부 전송 없음 |
| Groq | `groq` | ☁️ 클라우드 | 빠른 무료 클라우드 추론 |
| None | `none` | — | AI 기능 비활성화 |

## 설정 방법

`AI_PROVIDER` 환경변수로 프로바이더를 선택합니다.

```bash
# Ollama (로컬, 기본값)
AI_PROVIDER=ollama

# Groq (클라우드 데모용)
AI_PROVIDER=groq
GROQ_API_KEY=gsk_...

# AI 비활성화
AI_PROVIDER=none
```

### Ollama 설정

1. [Ollama 설치](https://ollama.com/download)
2. 모델 다운로드:
   ```bash
   ollama pull llama3.2
   ```
3. 서버 실행 (포트 11434, 기본)

`OLLAMA_URL`과 `OLLAMA_MODEL` 환경변수로 커스터마이징 가능:
```bash
OLLAMA_URL=http://localhost:11434
OLLAMA_MODEL=llama3.2
```

### Groq 설정

1. [console.groq.com](https://console.groq.com)에서 무료 API 키 발급
2. `GROQ_API_KEY` 환경변수 설정

> ⚠️ **클라우드 경고**: `AI_PROVIDER=groq` 사용 시 태스크 설명이 Groq 서버로 전송됩니다.  
> 민감한 데이터를 포함하지 마세요.

## 경쟁 제품 비교

| 기능 | TaskHive | Plane.so | Vikunja | Focalboard |
|------|----------|----------|---------|------------|
| AI 태스크 제안 | ✅ BYO-LLM | ✅ 클라우드 전용 | ❌ | ❌ |
| 로컬 LLM 지원 | ✅ Ollama | ❌ | ❌ | ❌ |
| 데이터 클라우드 미전송 | ✅ (Ollama 모드) | ❌ | ✅ | ✅ |
| 클라우드 LLM 선택 | ✅ Groq | ✅ OpenAI 고정 | ❌ | ❌ |
| 셀프호스팅 | ✅ Docker Compose | ✅ 복잡 | ✅ | ✅ |
| 오픈소스 | ✅ | ✅ (일부) | ✅ | ✅ |

**TaskHive 고유 포지셔닝**:
- **BYO-LLM(Bring Your Own LLM)**: Ollama/Groq/None 중 런타임에 선택 가능
- **로컬 퍼스트**: 기본 설정에서 외부 네트워크 없이 AI 동작
- **공개 데모 = Groq**: 별도 서버 없이 즉시 AI 체험 가능

## API

```
GET /api/ai/capabilities
```
인증 불필요. 현재 AI 설정 상태를 반환합니다.

**응답 예시 (Ollama)**:
```json
{
  "enabled": true,
  "provider": "ollama",
  "cloudProvider": false
}
```

**응답 예시 (none)**:
```json
{
  "enabled": false,
  "provider": "none",
  "cloudProvider": false
}
```
