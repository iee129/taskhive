import client from './client';
import type { MemberResponse } from './projects';

export interface UserSearchResult {
  userId: number;
  name: string;
  email: string;
}

export const getMembers = (projectId: number) =>
  client.get<MemberResponse[]>(`/api/projects/${projectId}/members`).then((r) => r.data);

export const addMember = (projectId: number, email: string) =>
  client.post<MemberResponse>(`/api/projects/${projectId}/members`, { email }).then((r) => r.data);

export const removeMember = (projectId: number, userId: number) =>
  client.delete(`/api/projects/${projectId}/members/${userId}`);

export const searchUsers = (email: string, projectId?: number) => {
  const params: Record<string, string> = { email };
  if (projectId !== undefined) params.projectId = String(projectId);
  return client.get<UserSearchResult[]>('/api/users/search', { params }).then((r) => r.data);
};
