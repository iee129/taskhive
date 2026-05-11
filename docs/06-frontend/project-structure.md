# 프론트엔드 프로젝트 구조

```
frontend/
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── src/
    ├── main.tsx                  # React 앱 진입점
    ├── App.tsx                   # 루트 컴포넌트 + 라우터 설정
    ├── api/
    │   ├── client.ts             # Axios 인스턴스 + 인터셉터
    │   ├── auth.ts               # 회원가입·로그인 API 함수
    │   └── tasks.ts              # 태스크 CRUD API 함수
    ├── components/
    │   ├── common/
    │   │   ├── Navbar.tsx        # 상단 네비게이션
    │   │   ├── Button.tsx        # 공용 버튼
    │   │   └── LoadingSpinner.tsx
    │   └── tasks/
    │       ├── TaskList.tsx      # 태스크 목록
    │       ├── TaskCard.tsx      # 태스크 카드
    │       └── TaskForm.tsx      # 태스크 생성·수정 폼
    ├── pages/
    │   ├── LoginPage.tsx
    │   ├── RegisterPage.tsx
    │   └── DashboardPage.tsx
    ├── store/                    # 상태 관리 (Context 또는 Zustand)
    │   ├── authStore.ts
    │   └── taskStore.ts
    ├── types/
    │   ├── auth.ts               # LoginRequest, AuthResponse 타입
    │   └── task.ts               # Task, CreateTaskRequest 타입
    └── utils/
        ├── tokenStorage.ts       # localStorage 래퍼
        └── dateFormatter.ts      # ISO 8601 → 표시용 날짜 변환
```

## vite.config.ts 핵심 설정

```typescript
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

## tsconfig.json strict 옵션

```json
{
  "compilerOptions": {
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noImplicitReturns": true
  }
}
```

## 네이밍 규칙

| 유형 | 규칙 | 예시 |
|------|------|------|
| 컴포넌트 | PascalCase | `TaskCard.tsx` |
| 훅 | camelCase, `use` 접두사 | `useAuth.ts` |
| 타입/인터페이스 | PascalCase | `TaskResponse` |
| API 함수 | camelCase | `createTask()` |
| 유틸 함수 | camelCase | `formatDate()` |
