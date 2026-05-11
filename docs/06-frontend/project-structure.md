# 프론트엔드 프로젝트 구조

```
frontend/
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── src/
    ├── main.tsx                  # React 앱 진입점 (BrowserRouter 포함)
    ├── App.tsx                   # 라우팅 설정
    ├── index.css                 # 전역 스타일
    ├── api/
    │   ├── client.ts             # Axios 인스턴스 + JWT 인터셉터
    │   ├── auth.ts               # register, login, me API 함수
    │   └── tasks.ts              # getTasks, createTask, updateTask, deleteTask
    ├── components/
    │   ├── PrivateRoute.tsx      # 비인증 접근 시 /login 리다이렉트
    │   └── Layout.tsx            # Ant Design 사이드바 레이아웃 + 로그아웃
    ├── pages/
    │   ├── LoginPage.tsx         # /login
    │   ├── RegisterPage.tsx      # /register
    │   ├── TasksPage.tsx         # /tasks — 목록 + 생성/수정/삭제 모달
    │   └── ProfilePage.tsx       # /profile — 내 정보
    └── types/
        ├── auth.ts               # AuthRequest, RegisterRequest, AuthResponse
        └── task.ts               # TaskStatus, TaskRequest, TaskResponse
```

## vite.config.ts 핵심 설정

```typescript
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

## 라우팅 구조

| 경로 | 컴포넌트 | 인증 필요 |
|------|----------|----------|
| `/login` | LoginPage | 불필요 |
| `/register` | RegisterPage | 불필요 |
| `/tasks` | TasksPage | ✅ |
| `/profile` | ProfilePage | ✅ |
| `*` | Navigate → `/tasks` | — |

보호된 경로는 `PrivateRoute`가 감싸며, `localStorage['token']` 없으면 `/login`으로 리다이렉트.

## 네이밍 규칙

| 유형 | 규칙 | 예시 |
|------|------|------|
| 컴포넌트 | PascalCase | `TasksPage.tsx` |
| 타입/인터페이스 | PascalCase | `TaskResponse` |
| API 함수 | camelCase | `createTask()` |
