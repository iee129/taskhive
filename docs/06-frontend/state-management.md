# 상태 관리

## 전략 (Phase 9 기준)

서버 상태는 **TanStack Query**, UI 상태는 컴포넌트 로컬 `useState`로 분리.

| 상태 종류 | 솔루션 | 예시 |
|----------|--------|------|
| 서버 데이터 (태스크, 프로젝트) | TanStack Query (`useQuery`) | `useTasks`, `useProjects` |
| 뮤테이션 (생성·수정·삭제) | TanStack Query (`useMutation`) | `useCreateTask`, `useDeleteTask` |
| 낙관적 업데이트 | `onMutate` + rollback | `useOptimisticTaskStatus` |
| 인증 토큰 | `localStorage` + Axios 인터셉터 | `client.ts` |
| 테마 (다크/라이트) | React Context (`ThemeContext`) | `useThemeContext`, `ThemeToggle` |
| UI 상태 (모달, 폼) | 컴포넌트 `useState` | `modalOpen`, `editingTask` |

---

## QueryClient 설정

```typescript
// main.tsx
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,   // 30초간 fresh — 재방문 시 재요청 없음
      gcTime:    300_000,  // 5분 후 캐시 제거
      retry: 1,
    },
  },
});
```

`QueryClientProvider`는 `BrowserRouter` 바깥에 위치해 라우트 전환 시에도 캐시 유지.

---

## 서버 상태 훅

### useTasks

```typescript
// hooks/useTasks.ts
export function useTasks(filter?: TaskFilter) {
  return useQuery({
    queryKey: ['tasks', filter],
    queryFn: () => getTasks(filter),
    staleTime: 30_000,
  });
}
```

`queryKey`에 `filter`를 포함해 필터 변경 시 자동으로 새 쿼리 실행.

### useMutateTask

```typescript
// hooks/useMutateTask.ts
export function useCreateTask() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: TaskRequest) => createTask(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['tasks'] }),
  });
}

export function useOptimisticTaskStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => updateTask(id, data),
    onMutate: async ({ id, data }) => {
      await qc.cancelQueries({ queryKey: ['tasks'] });
      const previous = qc.getQueriesData({ queryKey: ['tasks'] });
      // 즉시 UI 반영
      qc.setQueriesData({ queryKey: ['tasks'] }, (old) =>
        old?.map((t) => t.id === id ? { ...t, status: data.status } : t)
      );
      return { previous };
    },
    onError: (_err, _vars, context) => {
      // 실패 시 롤백
      context?.previous.forEach(([key, data]) => qc.setQueryData(key, data));
    },
    onSettled: () => qc.invalidateQueries({ queryKey: ['tasks'] }),
  });
}
```

---

## 캐시 무효화 전략

| 이벤트 | 무효화 대상 |
|--------|------------|
| 태스크 생성·수정·삭제 | `['tasks']` 전체 |
| 칸반 드래그 완료 | `['tasks']` (onSettled) |
| 드래그 실패 | rollback → `['tasks']` 복원 |

---

## 컴포넌트별 상태 패턴

```
TasksPage
├── filter (useState)          — 검색·필터 조건
├── modalOpen (useState)       — 생성/수정 모달
├── editingTask (useState)     — 현재 수정 중인 태스크
├── drawerTask (useState)      — 상세 Drawer
└── useTasks(filter)           — TanStack Query 서버 상태

KanbanPage
├── useTasks()                 — TanStack Query
└── useOptimisticTaskStatus()  — 낙관적 업데이트
```

---

## staleTime 가이드

- `30_000` (30초) — 태스크 목록: 자주 변경되지만 즉각성 불필요
- `60_000` (60초) — 프로젝트 목록: 드물게 변경
- `0` — 실시간 필요 데이터 (현재 미사용)

---

## 테마 상태 (Phase 9)

```typescript
// contexts/ThemeContext.tsx
export function ThemeProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<'light' | 'dark'>(
    () => (localStorage.getItem('theme') as 'light' | 'dark') ?? 'light'
  );
  const toggle = () => {
    setMode((prev) => {
      const next = prev === 'light' ? 'dark' : 'light';
      localStorage.setItem('theme', next);
      return next;
    });
  };
  return (
    <ThemeContext.Provider value={{ isDark: mode === 'dark', algorithm, toggle }}>
      {children}
    </ThemeContext.Provider>
  );
}
```

`ThemeProvider`는 `main.tsx` 최상위에 위치해 모든 컴포넌트에서 `useThemeContext()`로 접근 가능.
`App.tsx`의 `ConfigProvider`가 `algorithm`을 구독해 Ant Design 전체 테마를 동기화.
