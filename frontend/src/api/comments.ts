import client from './client';
import type { CommentResponse } from '../types/task';

export const getComments = (taskId: number) =>
  client.get<CommentResponse[]>(`/api/tasks/${taskId}/comments`).then((r) => r.data);

export const addComment = (taskId: number, content: string) =>
  client.post<CommentResponse>(`/api/tasks/${taskId}/comments`, { content }).then((r) => r.data);

export const deleteComment = (taskId: number, commentId: number) =>
  client.delete(`/api/tasks/${taskId}/comments/${commentId}`);
