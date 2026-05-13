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

export interface PrioritizeItem {
  taskId: number;
  reason: string;
}

export async function prioritizeTasks(projectId: number): Promise<PrioritizeItem[]> {
  const res = await client.post<PrioritizeItem[]>(`/api/projects/${projectId}/prioritize`);
  return res.data;
}

export async function getBlockers(projectId: number): Promise<TaskResponse[]> {
  const res = await client.get<TaskResponse[]>(`/api/projects/${projectId}/blockers`);
  return res.data;
}

export interface StandupItem {
  userId: number;
  name: string;
  summary: string;
}

export async function generateStandup(projectId: number): Promise<StandupItem[]> {
  const res = await client.post<StandupItem[]>(`/api/projects/${projectId}/standup`);
  return res.data;
}

export interface EstimateResponse {
  effort: 'S' | 'M' | 'L';
  estimatedDays: number;
  suggestedDueDate: string;
}

export async function estimateTask(title: string, description?: string): Promise<EstimateResponse> {
  const res = await client.post<EstimateResponse>('/api/ai/estimate', { title, description });
  return res.data;
}
