import client from './client';
import type { TaskRequest, TaskResponse, CommentResponse } from '../types/task';

export const suggestTask = (description: string, projectId?: number) =>
  client.post<TaskRequest>('/api/ai/suggest-task', { description, projectId }).then((r) => r.data);

export const createTaskFromAi = (description: string, projectId?: number) =>
  client.post<TaskResponse>('/api/ai/create-task', { description, projectId }).then((r) => r.data);

export const summarizeTask = (taskId: number) =>
  client.post<CommentResponse>(`/api/ai/tasks/${taskId}/ai-summary`).then((r) => r.data);

export async function parseFilter(query: string): Promise<{ status?: string; priority?: string; dueDateBefore?: string }> {
  const res = await client.post('/api/ai/parse-filter', { query });
  return res.data;
}

export interface BrainDumpItem {
  title: string;
  description: string;
  priority: string;
}

export async function breakdownText(text: string, projectId?: number): Promise<BrainDumpItem[]> {
  const res = await client.post<BrainDumpItem[]>('/api/ai/breakdown', { text, projectId });
  return res.data;
}

export async function createTasksFromBreakdown(items: BrainDumpItem[], projectId?: number): Promise<TaskResponse[]> {
  const res = await client.post<TaskResponse[]>('/api/ai/breakdown/create', { items, projectId });
  return res.data;
}
