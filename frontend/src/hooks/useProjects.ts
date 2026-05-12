import { useQuery } from '@tanstack/react-query';
import client from '../api/client';
import type { ProjectResponse } from '../types/project';

const getProjects = () =>
  client.get<ProjectResponse[]>('/api/projects').then((r) => r.data);

export function useProjects() {
  return useQuery({
    queryKey: ['projects'],
    queryFn: getProjects,
    staleTime: 30_000,
    gcTime: 300_000,
  });
}
