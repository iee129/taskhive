import { useQuery } from '@tanstack/react-query';
import { getTasks } from '../api/tasks';
import type { TaskStatus, TaskPriority } from '../types/task';

export interface TaskFilter {
  status?: TaskStatus;
  priority?: TaskPriority;
  search?: string;
}

export function useTasks(filter?: TaskFilter) {
  return useQuery({
    queryKey: ['tasks', filter],
    queryFn: () => getTasks(filter),
    staleTime: 30_000,
    gcTime: 300_000,
  });
}
