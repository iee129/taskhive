export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface TaskRequest {
  title: string;
  description?: string;
  status?: TaskStatus;
  priority?: TaskPriority;
  projectId?: number;
  assigneeId?: number;
  dueDate?: string;
}

export interface TaskResponse {
  id: number;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  projectId?: number;
  assigneeId?: number;
  dueDate?: string;
  createdAt: string;
}

export interface CommentResponse {
  id: number;
  content: string;
  taskId: number;
  authorId: number;
  authorEmail: string;
  createdAt: string;
}

export interface TaskActivityResponse {
  id: number;
  taskId: number;
  taskTitle?: string;
  actorEmail: string;
  action: string;
  detail?: string;
  occurredAt: string;
}

export interface StatsResponse {
  totalTasks: number;
  todo: number;
  inProgress: number;
  done: number;
  lowPriority: number;
  mediumPriority: number;
  highPriority: number;
  overdue: number;
  totalProjects: number;
  totalComments: number;
}
