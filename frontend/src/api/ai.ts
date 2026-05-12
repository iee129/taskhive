import client from './client';
import type { TaskRequest, TaskResponse } from '../types/task';

export const suggestTask = (description: string, projectId?: number) =>
  client.post<TaskRequest>('/api/ai/suggest-task', { description, projectId }).then((r) => r.data);

export const createTaskFromAi = (description: string, projectId?: number) =>
  client.post<TaskResponse>('/api/ai/create-task', { description, projectId }).then((r) => r.data);
