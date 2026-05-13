# 컴포넌트 구조

## 컴포넌트 계층

```
App
├── Navbar (공통)
├── LoginPage
├── RegisterPage
└── DashboardPage
    ├── TaskList
    │   └── TaskCard (×N)
    └── TaskForm (모달 또는 사이드패널)
```

## 주요 컴포넌트

### TaskCard

```typescript
interface TaskCardProps {
  task: Task;
  onStatusChange: (id: number, status: TaskStatus) => void;
  onDelete: (id: number) => void;
}

function TaskCard({ task, onStatusChange, onDelete }: TaskCardProps) {
  return (
    <div className="task-card">
      <h3>{task.title}</h3>
      <StatusBadge status={task.status} />
      {task.dueDate && <span>{formatDate(task.dueDate)}</span>}
      <button onClick={() => onDelete(task.id)}>삭제</button>
    </div>
  );
}
```

### TaskForm

```typescript
interface TaskFormProps {
  initialValues?: Partial<CreateTaskRequest>;
  onSubmit: (data: CreateTaskRequest) => Promise<void>;
  onCancel: () => void;
}
```

## 컴포넌트 설계 원칙

| 원칙 | 적용 |
|------|------|
| 단일 책임 | 각 컴포넌트는 하나의 UI 역할만 담당 |
| Props 인터페이스 | 모든 Props는 TypeScript 인터페이스로 명시 |
| 순수 컴포넌트 | 프레젠테이션 컴포넌트는 부작용 없음 |
| 커스텀 훅 분리 | API 호출·상태 관리는 커스텀 훅으로 분리 |

## 공통 컴포넌트 (components/)

| 컴포넌트 | 용도 |
|---------|------|
| `Layout` | 반응형 레이아웃 — 모바일 Drawer, 데스크탑 Sider, ThemeToggle 내장 |
| `ThemeToggle` | 다크/라이트 모드 전환 버튼 (`SunOutlined`/`MoonOutlined`, aria-label 포함) |
| `ErrorBoundary` | React 클래스 컴포넌트, `getDerivedStateFromError`, Ant Design Result 폴백, `reset()` |
| `SkeletonTable` | 로딩 중 행 단위 스켈레톤 (`rows` prop, `role="status"`, `aria-label="로딩 중"`) |
| `NotificationProvider` | `notification.useNotification()` 기반 전역 토스트 Context (`notifySuccess/Error/Warning`) |
| `FilterBar` | React.memo 최적화, 상태·우선순위·검색 필터 |
| `PrivateRoute` | JWT 미보유 시 `/login` 리다이렉트 |

### ErrorBoundary 사용 예시

```tsx
<ErrorBoundary>
  <TasksPage />
</ErrorBoundary>

// 커스텀 폴백
<ErrorBoundary fallback={<div>페이지 오류</div>}>
  <KanbanPage />
</ErrorBoundary>
```

### NotificationProvider 사용 예시

```tsx
// main.tsx — 루트에 주입
<NotificationProvider>
  <App />
</NotificationProvider>

// 컴포넌트 내부
const { notifySuccess, notifyError } = useNotification();
notifySuccess('저장 완료', '태스크가 생성되었습니다');
```

## 상태 분류

| 상태 유형 | 위치 | 예시 |
|-----------|------|------|
| 서버 상태 | 커스텀 훅 (API 호출) | 태스크 목록, 사용자 정보 |
| 전역 클라이언트 상태 | authStore | 로그인 여부, 토큰 |
| 로컬 UI 상태 | useState | 폼 입력값, 모달 열림 여부 |
