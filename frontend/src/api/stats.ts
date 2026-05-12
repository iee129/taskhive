import client from './client';
import type { StatsResponse, TaskActivityResponse } from '../types/task';

export const getStats = () =>
  client.get<StatsResponse>('/api/stats').then((r) => r.data);

export const getActivities = () =>
  client.get<TaskActivityResponse[]>('/api/stats/activities').then((r) => r.data);
