import client from './client';
import type { AuthRequest, AuthResponse, RegisterRequest } from '../types/auth';

export const register = (data: RegisterRequest) =>
  client.post<AuthResponse>('/api/auth/register', data).then((r) => r.data);

export const login = (data: AuthRequest) =>
  client.post<AuthResponse>('/api/auth/login', data).then((r) => r.data);

export const me = () =>
  client.get<AuthResponse>('/api/auth/me').then((r) => r.data);

export const withdraw = () =>
  client.delete('/api/auth/users/me');

export const verifyEmail = (token: string) =>
  client.get('/api/auth/verify-email', { params: { token } }).then((r) => r.data);

export const forgotPassword = (email: string) =>
  client.post('/api/auth/forgot-password', { email }).then((r) => r.data);

export const resetPassword = (token: string, newPassword: string) =>
  client.post('/api/auth/reset-password', { token, newPassword }).then((r) => r.data);
