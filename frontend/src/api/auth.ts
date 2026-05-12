import client from './client';
import type { AuthRequest, AuthResponse, RegisterRequest } from '../types/auth';

export const register = (data: RegisterRequest) =>
  client.post<AuthResponse>('/api/auth/register', data).then((r) => r.data);

export const login = (data: AuthRequest) =>
  client.post<AuthResponse>('/api/auth/login', data).then((r) => r.data);

export const me = () =>
  client.get<AuthResponse>('/api/auth/me').then((r) => r.data);

export const checkEmail = (email: string) =>
  client.get<{ available: boolean }>('/api/auth/check-email', { params: { email } }).then((r) => r.data);
