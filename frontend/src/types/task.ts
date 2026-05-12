export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface TaskRequest {
  title: string;
  description?: string;
  status?: TaskStatus;
  dueDate?: string;
}

export interface TaskResponse {
  id: number;
  title: string;
  description?: string;
  status: TaskStatus;
  dueDate?: string;
  createdAt: string;
}
