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
    │   ├── tasks.ts              # getTasks(filter?), getTask, createTask, updateTask, deleteTask
    │   ├── comments.ts           # getComments, addComment, deleteComment
    │   ├── stats.ts              # getStats, getActivities
    │   └── ai.ts                 # suggestTask, createTaskFromAi
    ├── components/
    │   ├── PrivateRoute.tsx      # 비인증 접근 시 /login 리다이렉트
    │   ├── Layout.tsx            # Ant Design 사이드바 레이아웃 (태스크·칸반·통계·프로필)
    │   ├── FilterBar.tsx         # 상태·우선순위·키워드 필터 바
    │   ├── CommentList.tsx       # 댓글 목록 + 입력 폼 (Drawer 내 사용)
    │   ├── ActivityFeed.tsx      # 활동 이력 Timeline 위젯
    │   └── AiTaskInput.tsx       # 자연어 입력 → AI 제안 → 태스크 생성 모달
    ├── pages/
    │   ├── LoginPage.tsx         # /login
    │   ├── RegisterPage.tsx      # /register
    │   ├── TasksPage.tsx         # /tasks — 목록·FilterBar·우선순위·Drawer 댓글·AI 버튼
    │   ├── KanbanPage.tsx        # /kanban — @hello-pangea/dnd 드래그앤드롭 3열 보드
    │   ├── StatsPage.tsx         # /stats — 통계 카드 + 완료율 + ActivityFeed
    │   └── ProfilePage.tsx       # /profile — 내 정보
    └── types/
        ├── auth.ts               # AuthRequest, RegisterRequest, AuthResponse
        └── task.ts               # TaskStatus, TaskPriority, TaskRequest, TaskResponse,
                                  # CommentResponse, TaskActivityResponse, StatsResponse
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
| `/kanban` | KanbanPage | ✅ |
| `/stats` | StatsPage | ✅ |
| `/profile` | ProfilePage | ✅ |
| `*` | Navigate → `/tasks` | — |

보호된 경로는 `PrivateRoute`가 감싸며, `localStorage['token']` 없으면 `/login`으로 리다이렉트.

## 네이밍 규칙

| 유형 | 규칙 | 예시 |
|------|------|------|
| 컴포넌트 | PascalCase | `TasksPage.tsx` |
| 타입/인터페이스 | PascalCase | `TaskResponse` |
| API 함수 | camelCase | `createTask()` |
