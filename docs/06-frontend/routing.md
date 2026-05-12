# 라우팅

## React Router v6 구조

```typescript
// App.tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route element={<PrivateRoute />}>
          <Route path="/tasks" element={<TasksPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Route>
        <Route path="/" element={<Navigate to="/tasks" replace />} />
        <Route path="*" element={<Navigate to="/tasks" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
```

## 경로 목록

| 경로 | 컴포넌트 | 인증 필요 | 설명 |
|------|----------|----------|------|
| `/` | — | — | `/tasks`로 리다이렉트 |
| `/login` | LoginPage | 불필요 | 로그인 폼 |
| `/register` | RegisterPage | 불필요 | 회원가입 폼 |
| `/tasks` | TasksPage | 필요 | 태스크 목록·생성·수정·삭제 |
| `/profile` | ProfilePage | 필요 | 내 정보 |
| `*` | — | — | `/tasks`로 리다이렉트 |

## PrivateRoute (인증 가드)

```typescript
function PrivateRoute() {
  const token = localStorage.getItem('token');
  return token ? <Outlet /> : <Navigate to="/login" replace />;
}
```

토큰이 없으면 `/login`으로 자동 리다이렉트.  
401 응답 시 Axios 인터셉터가 `/api/auth/refresh`를 시도하고, 실패하면 `/login`으로 리다이렉트합니다.

## 네비게이션 예시

```typescript
import { useNavigate } from 'react-router-dom';

function LoginPage() {
  const navigate = useNavigate();

  const handleSuccess = () => {
    navigate('/tasks');
  };
}
```
