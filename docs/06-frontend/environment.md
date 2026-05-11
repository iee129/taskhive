# 환경 변수

## Vite 환경 변수 규칙

Vite는 `VITE_` 접두사가 붙은 변수만 클라이언트 코드에 노출:

```
frontend/
├── .env                  # 기본값 (모든 환경)
├── .env.local            # 로컬 오버라이드 (git 제외)
├── .env.development      # 개발 전용
└── .env.production       # 프로덕션 빌드 전용
```

## 변수 목록

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `VITE_API_BASE_URL` | `/api` | API Base URL (Nginx 프록시 경로) |
| `VITE_APP_TITLE` | `TaskHive` | 페이지 제목 |

## .env.development 예시

```bash
VITE_API_BASE_URL=/api
VITE_APP_TITLE=TaskHive (Dev)
```

## .env.production 예시

```bash
VITE_API_BASE_URL=/api
VITE_APP_TITLE=TaskHive
```

## 코드에서 접근

```typescript
const apiUrl = import.meta.env.VITE_API_BASE_URL;
const title  = import.meta.env.VITE_APP_TITLE;
```

TypeScript 타입 지원 (`src/vite-env.d.ts`):

```typescript
/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
  readonly VITE_APP_TITLE: string;
}
```

## 주의사항

- `VITE_` 접두사 없는 변수는 클라이언트 번들에 포함되지 않음
- `.env.local`은 반드시 `.gitignore`에 포함 (개인 설정, 시크릿)
- 프로덕션 빌드 시 `import.meta.env.PROD === true`
- API URL은 Nginx가 `/api/*` → `backend:8080`으로 프록시하므로 `/api` 고정 사용
