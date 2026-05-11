# 상태 관리

## 전략

MVP 단계에서는 **React Context API + useReducer**로 전역 상태 관리.  
복잡도 증가 시 **Zustand** 도입 검토 (경량, boilerplate 최소).

## 인증 상태 (authStore)

```typescript
// store/authStore.ts
interface AuthState {
  token: string | null;
  email: string | null;
  name: string | null;
  isAuthenticated: boolean;
}

const AuthContext = createContext<{
  state: AuthState;
  login: (response: AuthResponse) => void;
  logout: () => void;
} | null>(null);

function authReducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case 'LOGIN':
      localStorage.setItem('token', action.payload.token);
      return { ...action.payload, isAuthenticated: true };
    case 'LOGOUT':
      localStorage.removeItem('token');
      return { token: null, email: null, name: null, isAuthenticated: false };
    default:
      return state;
  }
}
```

## 태스크 상태 (taskStore)

서버 상태는 커스텀 훅으로 관리 — 전역 스토어 불필요:

```typescript
// hooks/useTasks.ts
function useTasks() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchTasks = async () => {
    setLoading(true);
    try {
      const data = await taskApi.getAll();
      setTasks(data);
    } catch (e) {
      setError('태스크를 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchTasks(); }, []);

  return { tasks, loading, error, refetch: fetchTasks };
}
```

## 상태 범위 정리

| 상태 | 범위 | 솔루션 |
|------|------|--------|
| 인증 토큰·사용자 정보 | 전역 | AuthContext |
| 태스크 목록 | 페이지 | `useTasks` 커스텀 훅 |
| 폼 입력값 | 컴포넌트 | `useState` |
| 모달 열림 여부 | 컴포넌트 | `useState` |
| API 로딩·에러 | 훅 | `useTasks` 내부 |

## Zustand 전환 기준

Context API로 처리하기 어려운 시점:
- 3개 이상의 독립 전역 상태가 서로 구독
- 상태 업데이트로 인한 불필요한 리렌더링이 성능에 영향
- 개발자 도구(devtools)가 필요한 복잡한 상태 디버깅
