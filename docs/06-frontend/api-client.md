# API 클라이언트

## Axios 인스턴스

```typescript
// api/client.ts
import axios from 'axios';

const client = axios.create({
  baseURL: '/api',           // Vite proxy → http://localhost:8080/api
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

// 요청 인터셉터 — JWT 자동 주입
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 응답 인터셉터 — 401 처리
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default client;
```

## Auth API

```typescript
// api/auth.ts
import client from './client';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';

export const authApi = {
  login: (data: LoginRequest) =>
    client.post<AuthResponse>('/auth/login', data).then(r => r.data),

  register: (data: RegisterRequest) =>
    client.post<AuthResponse>('/auth/register', data).then(r => r.data),
};
```

## Tasks API

```typescript
// api/tasks.ts
import client from './client';
import type { Task, CreateTaskRequest } from '../types/task';

export const taskApi = {
  getAll: () =>
    client.get<Task[]>('/tasks').then(r => r.data),

  create: (data: CreateTaskRequest) =>
    client.post<Task>('/tasks', data).then(r => r.data),

  update: (id: number, data: Partial<CreateTaskRequest>) =>
    client.put<Task>(`/tasks/${id}`, data).then(r => r.data),

  delete: (id: number) =>
    client.delete(`/tasks/${id}`),
};
```

## TypeScript 타입

```typescript
// types/auth.ts
export interface LoginRequest { email: string; password: string; }
export interface RegisterRequest extends LoginRequest { name: string; }
export interface AuthResponse { token: string; email: string; name: string; }

// types/task.ts
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface Task {
  id: number;
  title: string;
  description: string | null;
  status: TaskStatus;
  dueDate: string | null;  // YYYY-MM-DD
  createdAt: string;       // ISO 8601
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  dueDate?: string;
}
```

## 에러 처리 패턴

```typescript
try {
  await authApi.login(credentials);
  navigate('/dashboard');
} catch (error) {
  if (axios.isAxiosError(error)) {
    setError(error.response?.data?.message ?? '로그인 실패');
  }
}
```
