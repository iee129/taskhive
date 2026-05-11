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
