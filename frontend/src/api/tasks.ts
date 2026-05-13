import client from './client';
import type { TaskRequest, TaskResponse, TaskStatus, TaskPriority } from '../types/task';

interface TaskFilter {
  status?: TaskStatus;
  priority?: TaskPriority;
  search?: string;
  labelId?: number;
}

export const getTasks = (filter?: TaskFilter) => {
  const params = new URLSearchParams();
  if (filter?.status) params.set('status', filter.status);
  if (filter?.priority) params.set('priority', filter.priority);
  if (filter?.search) params.set('search', filter.search);
  if (filter?.labelId !== undefined) params.set('labelId', String(filter.labelId));
  return client.get<TaskResponse[]>('/api/tasks', { params }).then((r) => r.data);
};

export const getTask = (id: number) =>
  client.get<TaskResponse>(`/api/tasks/${id}`).then((r) => r.data);

export const createTask = (data: TaskRequest) =>
  client.post<TaskResponse>('/api/tasks', data).then((r) => r.data);

export const updateTask = (id: number, data: TaskRequest) =>
  client.put<TaskResponse>(`/api/tasks/${id}`, data).then((r) => r.data);

export const deleteTask = (id: number) =>
  client.delete(`/api/tasks/${id}`);

export const patchTaskStatus = (id: number, status: TaskStatus) =>
  client.put<TaskResponse>(`/api/tasks/${id}`, { title: '', status } as any).then((r) => r.data);
