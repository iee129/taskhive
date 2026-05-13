import api from './client';

export interface SearchResult {
  type: 'task' | 'project';
  id: number;
  title: string;
  subtitle: string;
}

export async function search(q: string): Promise<SearchResult[]> {
  if (!q.trim()) return [];
  const res = await api.get<SearchResult[]>('/search', { params: { q } });
  return res.data;
}
