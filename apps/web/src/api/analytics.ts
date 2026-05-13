import client from './client';

export interface BurndownPoint { date: string; remaining: number; }
export interface CfdPoint { date: string; todo: number; inProgress: number; done: number; }
export interface CycleTimeItem { taskId: number; title: string; cycleDays: number; }

export async function getBurndown(projectId: number, from: string, to: string): Promise<BurndownPoint[]> {
  const res = await client.get(`/projects/${projectId}/analytics/burndown`, { params: { from, to } });
  return res.data;
}

export async function getCfd(projectId: number, from: string, to: string): Promise<CfdPoint[]> {
  const res = await client.get(`/projects/${projectId}/analytics/cfd`, { params: { from, to } });
  return res.data;
}

export async function getCycleTime(projectId: number): Promise<CycleTimeItem[]> {
  const res = await client.get(`/projects/${projectId}/analytics/cycle-time`);
  return res.data;
}
