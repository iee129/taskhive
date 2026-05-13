import client from './client';
import type { LabelResponse } from '../types/task';

export type { LabelResponse };

export const getProjectLabels = (projectId: number) =>
  client.get<LabelResponse[]>(`/api/projects/${projectId}/labels`).then((r) => r.data);

export const createLabel = (projectId: number, name: string, color: string) =>
  client.post<LabelResponse>(`/api/projects/${projectId}/labels`, { name, color }).then((r) => r.data);

export const deleteLabel = (projectId: number, labelId: number) =>
  client.delete(`/api/projects/${projectId}/labels/${labelId}`);

export const addLabelToTask = (taskId: number, labelId: number) =>
  client.post<void>(`/api/tasks/${taskId}/labels/${labelId}`).then((r) => r.data);

export const removeLabelFromTask = (taskId: number, labelId: number) =>
  client.delete(`/api/tasks/${taskId}/labels/${labelId}`);
