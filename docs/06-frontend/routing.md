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
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/tasks/:id" element={<TaskDetailPage />} />
        </Route>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
```

## 경로 목록

| 경로 | 컴포넌트 | 인증 필요 | 설명 |
|------|----------|----------|------|
| `/` | — | — | `/dashboard`로 리다이렉트 |
| `/login` | LoginPage | 불필요 | 로그인 폼 |
| `/register` | RegisterPage | 불필요 | 회원가입 폼 |
| `/dashboard` | DashboardPage | 필요 | 태스크 목록 |
| `/tasks/:id` | TaskDetailPage | 필요 | 태스크 상세·수정 |
| `*` | — | — | `/dashboard`로 리다이렉트 |

## PrivateRoute (인증 가드)

```typescript
function PrivateRoute() {
  const token = localStorage.getItem('token');
  return token ? <Outlet /> : <Navigate to="/login" replace />;
}
```

토큰이 없거나 만료된 경우 `/login`으로 자동 리다이렉트.  
401 응답 시 Axios 인터셉터가 토큰을 삭제하고 리다이렉트 처리.

## 네비게이션 예시

```typescript
import { useNavigate } from 'react-router-dom';

function LoginPage() {
  const navigate = useNavigate();

  const handleSuccess = () => {
    navigate('/dashboard');
  };
}
```
