import client from './client';

export interface MemberResponse {
  userId: number;
  name: string;
  email: string;
  role: 'OWNER' | 'MEMBER';
  createdAt: string;
}

export interface ProjectResponse {
  id: number;
  name: string;
  description?: string;
  ownerId: number;
  createdAt: string;
  members: MemberResponse[];
}

export interface ProjectRequest {
  name: string;
  description?: string;
}

export const getProjects = () =>
  client.get<ProjectResponse[]>('/api/projects').then((r) => r.data);

export const getProject = (id: number) =>
  client.get<ProjectResponse>(`/api/projects/${id}`).then((r) => r.data);

export const createProject = (data: ProjectRequest) =>
  client.post<ProjectResponse>('/api/projects', data).then((r) => r.data);

export const updateProject = (id: number, data: ProjectRequest) =>
  client.put<ProjectResponse>(`/api/projects/${id}`, data).then((r) => r.data);

export const deleteProject = (id: number) =>
  client.delete(`/api/projects/${id}`);
