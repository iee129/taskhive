# API 클라이언트

## Axios 인스턴스

```typescript
// src/api/client.ts
import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080',
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
// src/api/auth.ts
import client from './client';
import type { AuthRequest, AuthResponse, RegisterRequest } from '../types/auth';

export const register = (data: RegisterRequest) =>
  client.post<AuthResponse>('/api/auth/register', data).then((r) => r.data);

export const login = (data: AuthRequest) =>
  client.post<AuthResponse>('/api/auth/login', data).then((r) => r.data);

export const me = () =>
  client.get<AuthResponse>('/api/auth/me').then((r) => r.data);
```

## Tasks API

```typescript
// src/api/tasks.ts
import client from './client';
import type { TaskRequest, TaskResponse } from '../types/task';

export const getTasks = () =>
  client.get<TaskResponse[]>('/api/tasks').then((r) => r.data);

export const createTask = (data: TaskRequest) =>
  client.post<TaskResponse>('/api/tasks', data).then((r) => r.data);

export const updateTask = (id: number, data: TaskRequest) =>
  client.put<TaskResponse>(`/api/tasks/${id}`, data).then((r) => r.data);

export const deleteTask = (id: number) =>
  client.delete(`/api/tasks/${id}`);
```

## TypeScript 타입

```typescript
// src/types/auth.ts
export interface AuthRequest { email: string; password: string; }
export interface RegisterRequest { name: string; email: string; password: string; }
export interface AuthResponse { token?: string; email: string; name: string; }

// src/types/task.ts
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface TaskRequest {
  title: string;
  description?: string;
  status?: TaskStatus;
  dueDate?: string;  // YYYY-MM-DD
}

export interface TaskResponse {
  id: number;
  title: string;
  description?: string;
  status: TaskStatus;
  dueDate?: string;
  createdAt: string;  // ISO 8601
}
```

## 에러 처리 패턴

```typescript
try {
  const res = await login(credentials);
  localStorage.setItem('token', res.token!);
  navigate('/tasks');
} catch {
  messageApi.error('이메일 또는 비밀번호가 올바르지 않습니다');
}
```
