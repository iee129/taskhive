# API 클라이언트

## Axios 인스턴스

```typescript
// src/api/client.ts
import axios from 'axios';

const client = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,  // HttpOnly 쿠키 전송 필수
});

// 요청 인터셉터 — Access Token 자동 주입
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 응답 인터셉터 — 401 수신 시 Refresh Token으로 재발급 후 원래 요청 재시도
let isRefreshing = false;
let pendingQueue: Array<{ resolve: (token: string) => void; reject: (err: unknown) => void }> = [];

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return client(originalRequest);
        });
      }
      originalRequest._retry = true;
      isRefreshing = true;
      try {
        const { data } = await axios.post(
          'http://localhost:8080/api/auth/refresh',
          {},
          { withCredentials: true }
        );
        localStorage.setItem('token', data.accessToken);
        pendingQueue.forEach(({ resolve }) => resolve(data.accessToken));
        pendingQueue = [];
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return client(originalRequest);
      } catch {
        pendingQueue.forEach(({ reject }) => reject(error));
        pendingQueue = [];
        localStorage.removeItem('token');
        window.location.href = '/login';
        return Promise.reject(error);
      } finally {
        isRefreshing = false;
      }
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

export const logout = () =>
  client.post('/api/auth/logout');

export const changePassword = (data: { currentPassword: string; newPassword: string }) =>
  client.put('/api/auth/password', data);
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
