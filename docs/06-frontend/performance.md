# 프론트엔드 성능 최적화

## 개요 (Phase 8)

| 기법 | 적용 위치 | 효과 |
|------|----------|------|
| TanStack Query 캐싱 | 모든 서버 상태 훅 | 재방문 시 API 재요청 0 |
| 낙관적 업데이트 | KanbanPage 드래그 | 드래그 즉시 반영 (지연 없음) |
| `lazy()` + `Suspense` | App.tsx 라우트 | 초기 번들에 4개 페이지 미포함 |
| `manualChunks` | vite.config.ts | 벤더 라이브러리 별도 캐시 |
| `React.memo` | FilterBar | props 미변경 시 리렌더 차단 |

---

## 코드 스플리팅

### 번들 크기 비교

| 청크 | Before | After (gzip) |
|------|--------|-------------|
| TasksPage | 477KB | **8KB / 3KB** |
| KanbanPage | 100KB | **2.75KB / 1.45KB** |
| StatsPage | — | **3.68KB / 1.42KB** |
| ProfilePage | — | **0.64KB / 0.48KB** |

### App.tsx 설정

```typescript
const TasksPage   = lazy(() => import('./pages/TasksPage'));
const KanbanPage  = lazy(() => import('./pages/KanbanPage'));
const StatsPage   = lazy(() => import('./pages/StatsPage'));
const ProfilePage = lazy(() => import('./pages/ProfilePage'));

// 라우트별 Suspense
<Route path="/tasks" element={
  <Suspense fallback={<Spin />}><TasksPage /></Suspense>
} />
```

`LoginPage`, `RegisterPage`는 eager load — 인증 전 즉시 필요.

### vite.config.ts manualChunks

```typescript
manualChunks: {
  'vendor-react':  ['react', 'react-dom', 'react-router-dom'],
  'vendor-antd':   ['antd', '@ant-design/icons'],
  'vendor-query':  ['@tanstack/react-query'],
  'vendor-dnd':    ['@hello-pangea/dnd'],
}
```

벤더 청크는 코드 변경 없이 캐시 유지 → 재방문 시 다운로드 없음.

---

## TanStack Query 캐싱 전략

```
staleTime: 30_000ms  — fresh 유지 (재방문 30초 내 재요청 없음)
gcTime:    300_000ms — 5분 후 캐시 제거
retry:     1         — 실패 시 1회 재시도
```

### 캐시 히트 시나리오

```
1. /tasks 페이지 방문 → API 요청
2. /kanban 이동
3. /tasks 재방문 (30초 이내) → Network 탭에서 요청 없음 (캐시 히트)
```

---

## 낙관적 업데이트 (KanbanPage)

```typescript
onMutate: async ({ id, data }) => {
  // 진행 중인 쿼리 취소 (race condition 방지)
  await qc.cancelQueries({ queryKey: ['tasks'] });

  // 현재 캐시 스냅샷 저장 (롤백용)
  const previous = qc.getQueriesData({ queryKey: ['tasks'] });

  // 즉시 UI 반영
  qc.setQueriesData({ queryKey: ['tasks'] }, (old) =>
    old?.map((t) => t.id === id ? { ...t, status: data.status } : t)
  );

  return { previous };
},
onError: (_err, _vars, context) => {
  // API 실패 시 스냅샷으로 롤백
  context?.previous.forEach(([key, data]) => qc.setQueryData(key, data));
},
```

---

## Lighthouse CI

```yaml
# .github/workflows/lighthouse.yml
on:
  pull_request:
    paths: ['frontend/**']
```

```json
// frontend/.lighthouserc.json
{
  "ci": {
    "assert": {
      "assertions": {
        "categories:performance":   ["warn",  { "minScore": 0.90 }],
        "categories:accessibility": ["error", { "minScore": 0.90 }]
      }
    }
  }
}
```

> **주의**: 워크플로우 파일 푸시는 PAT에 `workflow` scope가 필요합니다.

---

## 성능 측정 방법

```bash
# 번들 분석
cd frontend && npm run build

# Lighthouse 로컬 실행 (Chrome 필요)
npx lighthouse http://localhost:5173 --output html --view

# React DevTools Profiler
# 브라우저 DevTools → Profiler 탭 → Record → 태스크 상태 변경
```
