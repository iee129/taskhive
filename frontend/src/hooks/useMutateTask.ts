import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createTask, updateTask, deleteTask } from '../api/tasks';
import type { TaskRequest, TaskResponse } from '../types/task';

export function useCreateTask() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: TaskRequest) => createTask(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['tasks'] }),
  });
}

export function useUpdateTask() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: TaskRequest }) => updateTask(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['tasks'] }),
  });
}

export function useDeleteTask() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => deleteTask(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['tasks'] }),
  });
}

export function useOptimisticTaskStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: TaskRequest }) => updateTask(id, data),
    onMutate: async ({ id, data }) => {
      await qc.cancelQueries({ queryKey: ['tasks'] });
      const previous = qc.getQueriesData<TaskResponse[]>({ queryKey: ['tasks'] });
      qc.setQueriesData<TaskResponse[]>({ queryKey: ['tasks'] }, (old) =>
        old?.map((t) => (t.id === id ? { ...t, status: data.status! } : t))
      );
      return { previous };
    },
    onError: (_err, _vars, context) => {
      context?.previous.forEach(([queryKey, data]) => {
        qc.setQueryData(queryKey, data);
      });
    },
    onSettled: () => qc.invalidateQueries({ queryKey: ['tasks'] }),
  });
}
